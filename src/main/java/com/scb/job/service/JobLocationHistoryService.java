package com.scb.job.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.scb.job.constants.JobConstants;
import com.scb.job.kafka.NotificationPublisher;
import com.scb.job.model.kafka.BroadcastNotification;
import com.scb.job.model.kafka.PinChangeNotificationBody;
import com.scb.job.service.proxy.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scb.job.constants.JobLocationUpdatedBy;
import com.scb.job.entity.DistanceResponseEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.entity.JobLocationHistory;
import com.scb.job.entity.RiderLocationEntity;
import com.scb.job.exception.DataNotFoundException;
import com.scb.job.exception.InvalidJobStateException;
import com.scb.job.exception.ResourceNotFoundException;
import com.scb.job.model.request.JobLocationUpdateDto;
import com.scb.job.model.request.Location;
import com.scb.job.model.response.AddressResponse;
import com.scb.job.repository.JobLocationHistoryRepository;
import com.scb.job.repository.JobRepository;
import com.scb.job.service.proxy.EstimatePriceProxy;
import com.scb.job.service.proxy.EstimatePriceProxyResponse;
import com.scb.job.service.proxy.EstimatePriceRequest;
import com.scb.job.service.proxy.LocationProxy;
import com.scb.job.util.CommonUtils;
import com.scb.job.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobLocationHistoryService {

  private static final String CONFIRM = "CONFIRM";

  @Autowired
  JobRepository repository;

  @Autowired
  JobLocationHistoryRepository jobLocationHistoryRepository;

  @Autowired
  EstimatePriceProxy estimatePriceProxy;

  @Autowired
  LocationProxy locationProxy;

  @Autowired
  NotificationPublisher notificationPublisher;

  @Autowired
  RiderProfileProxy riderProfileProxy;

  @Autowired
  ObjectMapper objectMapper;

  public JobEntity getJobById(String jobId) {
    JobEntity jobEntity = repository.getjobById(jobId);
    if (!ObjectUtils.isEmpty(jobEntity)) {
      return jobEntity;
    }
    throw new ResourceNotFoundException(String.format("Rider jobId %s not found", jobId));
  }

  public JobLocationHistory updateLocation(String jobId,
      JobLocationUpdateDto jobLocationUpdateDto) {
    log.info("Updating Location for JobId:{}, RequestBody:{}", jobId, jobLocationUpdateDto);
    JobLocationHistory jobLocationHistory =
        getUpdatedJobLocation(jobId, jobLocationUpdateDto.getAddressType());
    JobEntity jobEntity = getJobById(jobId);
    if (jobLocationUpdateDto.getAction().equals(CONFIRM)) {
      jobLocationHistory.setStatus("Accepted");
      int seq = jobLocationUpdateDto.getAddressType().equals(JobLocationUpdatedBy.MERCHANT) ? 1 : 2;
      validateJobState(jobEntity);
      List<JobLocation> jobLocationsDB = jobEntity.getLocationList();
      updateLatLongJobLocationsDB(jobLocationsDB, jobLocationHistory.getNewLat(),
          jobLocationHistory.getNewLong(), seq);
      updateJobPriceJobEntity(jobLocationHistory, jobEntity, seq);
      updateEvBikeDistance(jobEntity, jobLocationHistory, seq);


      jobEntity.setIsJobLocationUpdateHistory(true);
    } else {
      jobLocationHistory.setStatus("Rejected");
    }

    jobEntity.setUpdatedBy(jobLocationUpdateDto.getUpdatedBy());
    log.info("saving job:{} to db", jobEntity.getJobId());
    jobEntity = repository.save(jobEntity);
    log.info("job entity is saved");

    log.info("saving jobLocationHistory:{} to db", jobLocationHistory.getJobId());
    jobLocationHistory.setUpdatedBy(jobLocationUpdateDto.getUpdatedBy());
    jobLocationHistoryRepository.save(jobLocationHistory);
    log.info("saved jobLocationHistory:{} to db", jobLocationHistory.getJobId());
    pushNotification(jobLocationHistory, jobEntity);
    return jobLocationHistory;
  }

  private void updateEvBikeDistance(JobEntity jobEntity, JobLocationHistory jobLocationHistory,
      int seq) {
    log.info("Update Rider Ev Bike Distance riderId: {}, MerchantToCustomerRepin:{}, seq:{}",
        jobEntity.getRiderId(), jobLocationHistory.getMerchantToCustomerRepin(), seq);
    jobEntity.setMerchantToCustomerRepin(jobLocationHistory.getMerchantToCustomerRepin());
    if (seq == 1) {
      CompletableFuture<DistanceResponseEntity> responseEntity =
          locationProxy.getDistanceFromRidersCurrentLocation(jobEntity.getDriverId(),
              jobEntity.getRiderInitialLatLong().get("lng"),
              jobEntity.getRiderInitialLatLong().get("lat"));
      try {
        jobEntity.setDistanceToMerchantRepin(responseEntity.get().getDistanceInKms());
        log.info("Update DistanceToMerchantRepin riderId: {}, Distance in Kms",
            jobEntity.getRiderId(), responseEntity.get().getDistanceInKms());
      } catch (InterruptedException | ExecutionException e) {
        Thread.currentThread().interrupt();
        log.error(String.format("Error occurred while getting rider distanceToMerchantRepin %s", e.getMessage()));
      }

    } else if (seq == 2 && !jobEntity.getIsJobLocationUpdateHistory()) {
      jobEntity.setDistanceToMerchantRepin(jobEntity.getDistanceToMerchant());
    }
  }

  private void updateJobPriceJobEntity(JobLocationHistory jobLocationHistory, JobEntity jobEntity,
      int seq) {
    if (seq == 1 && jobLocationHistory.getDifferenceAmount() > 0) {
      jobEntity.setNetPrice(jobLocationHistory.getRePinNetPrice());
      jobEntity.setNetPaymentPrice(jobLocationHistory.getNetPaymentPrice());
      jobEntity.setTaxAmount(jobLocationHistory.getTaxAmount());
      jobEntity.setNormalPrice(jobLocationHistory.getNormalPrice());
      jobEntity.setNetPriceSearch(Double.toString(jobLocationHistory.getRePinNetPrice()));
      jobEntity.setRePinDifferenceAmountMerchant(jobLocationHistory.getDifferenceAmount());
      log.info(
          "Updating Job Price for Merchant Re-Pint, JobId:{}, JobNetPaymentPrice:{}, JobTaxPrice:{}, JobNormalPrice:{}",
          jobEntity.getJobId(), jobEntity.getNetPaymentPrice(), jobEntity.getTaxAmount(),
          jobEntity.getNormalPrice());
    } else if (seq == 2) {
      jobEntity.setRePinDifferenceAmountCustomer(jobLocationHistory.getDifferenceAmount());
    } else if (seq == 1) {
      jobEntity.setRePinDifferenceAmountMerchant(jobLocationHistory.getDifferenceAmount());
    }
    log.info("Update JobPrice Job Seq:{}, Difference Payment Price: {}, JobId:{}", seq,
        jobLocationHistory.getDifferenceAmount(), jobLocationHistory.getJobId());
  }

  public void updateLatLongJobLocationsDB(List<JobLocation> jobLocationsDB, String lat, String lng,
      int seq) {
    // IMPLICIT UPDATE OF LAT, LONG
    jobLocationsDB.stream().filter(job -> job.getSeq() == seq).forEach(job -> {
      job.setLat(lat);
      job.setLng(lng);
    });
  }

  public JobLocationHistory getUpdatedJobLocation(String jobId, JobLocationUpdatedBy addressType) {
    int seq = addressType.equals(JobLocationUpdatedBy.MERCHANT) ? 1 : 2;
    log.info("GET Location Update for JobId:{}, RequestedFor:{}, Confirm:{}", jobId, addressType);
    String riderLong = "0.0";
    String riderLat = "0.0";
    JobEntity jobEntity = getJobById(jobId);
    validateJobState(jobEntity);
    List<JobLocation> jobLocationsDB = jobEntity.getLocationList();
    List<Location> jobLocations = new ArrayList<>();

    Optional<JobLocation> oldJobLocation =
        jobLocationsDB.stream().filter(job -> job.getSeq() == seq).findAny();

    String oldLat = oldJobLocation.get().getLat();
    String oldLong = oldJobLocation.get().getLng();
    String oldAddress = oldJobLocation.get().getAddress();

    String riderId = jobEntity.getDriverId();

    CompletableFuture<RiderLocationEntity> riderLocation =
        locationProxy.getRiderCurrentLocation(riderId);

    if (ObjectUtils.isEmpty(riderLocation)) {
      log.error("Current Location not Found for Rider:{} for GET Update Location Request", riderId);
      throw new DataNotFoundException("No Location Found for Rider");
    }

    try {
      riderLong = riderLocation.get().getGeom().getCoordinates().get(0);
      riderLat = riderLocation.get().getGeom().getCoordinates().get(1);
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      log.error(String.format("Error occurred while getting rider lat long %s", e.getMessage()));
    }

    log.info("RiderId:{} current Lat:{}, Long:{}", riderId, riderLat, riderLong);

    updateLatLongJobLocationsDB(jobLocationsDB, riderLat, riderLong, seq);

    jobLocationsDB.forEach(list -> {
      Location location = new Location();
      location.setSeq(list.getSeq());
      location.setLat(list.getLat());
      location.setLng(list.getLng());
      jobLocations.add(location);
    });

    log.info("getting pricing information for Update Location jobId:{}", jobId);

    EstimatePriceRequest estimatePriceRequest =
        EstimatePriceRequest.builder().userName("Update EstimatePriceRequest")
            .apiKey("Update EstimatePriceRequest").channel("Food")
            .jobType("Update EstimatePriceRequest").option("Update EstimatePriceRequest")
            .promoCode("Update EstimatePriceRequest").locationList(jobLocations).build();
        
    EstimatePriceProxyResponse estimatePriceProxyResponse =
        estimatePriceProxy.getEstimatedPrice(estimatePriceRequest);

    log.info("JobEntity JobId-{}, Old Lat-{}, Long-{}, Net Price-{}, Distance-{}", jobId, oldLat,
        oldLong, jobEntity.getNetPrice(), jobEntity.getTotalDistance());
    log.info("JobEntity JobId-{}, New Lat-{}, Long-{}, Net Price-{}, Distance-{}", jobId, riderLat,
        riderLong, estimatePriceProxyResponse.getNetPrice(),
        estimatePriceProxyResponse.getDistance());

    Optional<JobLocation> jobLocationsUpdate =
        jobLocationsDB.stream().filter(job -> job.getSeq() == seq).findAny();
    JobLocationHistory jobLocationHistory = new JobLocationHistory();
    if (jobLocationsUpdate.isPresent()) {
      double priceDifference = estimatePriceProxyResponse.getNetPrice() - jobEntity.getNetPrice();
      priceDifference = priceDifference <= 0.0 ? 0 : priceDifference;

      jobLocationHistory = JobLocationHistory.builder().updateLocationType(addressType.name())
          .oldLat(oldLat).oldLong(oldLong).previousAddress(oldAddress)
          .jobNetPrice(jobEntity.getNetPrice()).newLong(riderLong).newLat(riderLat)
          .rePinNetPrice(priceDifference > 0.0 ? estimatePriceProxyResponse.getNetPrice()
              : jobEntity.getNetPrice())
          .jobId(jobEntity.getJobId()).riderId(jobEntity.getDriverId())
          .updatedTime(LocalDateTime.now())
          .updatedTimeTh(
              DateUtils.zonedDateTimeToString(ZonedDateTime.now(ZoneId.of("Asia/Bangkok"))))
          .netPaymentPrice(priceDifference > 0.0
              ? CommonUtils.round(estimatePriceProxyResponse.getNetPaymentPrice())
              : jobEntity.getNetPaymentPrice())
          .normalPrice(priceDifference > 0.0 ? estimatePriceProxyResponse.getNormalPrice()
              : jobEntity.getNormalPrice())
          .taxAmount(
              priceDifference > 0.0 ? CommonUtils.round(estimatePriceProxyResponse.getTaxAmount())
                  : jobEntity.getTaxAmount())
          .differenceAmount(priceDifference > 0.0 ? priceDifference : 0)
          .merchantToCustomerRepin(estimatePriceProxyResponse.getDistance()).build();
    }
    return jobLocationHistory;
  }

  public List<JobLocationHistory> getJobLocationHistoryById(String jobId) {
    return jobLocationHistoryRepository.getJobLocationHistoryByJobId(jobId);
  }

  private void validateJobState(JobEntity jobEntity) {
    String jobStatus = jobEntity.getJobStatusKey();
    String jobId = jobEntity.getJobId();
    if (jobStatus.equals(JobConstants.FOO_DEL)) {
      log.error("Job Status should not be FOOD_DELIVERED for Location Update Job Id:{}", jobId);
      throw new InvalidJobStateException(
          "Job Status should not be FOOD_DELIVERED for Location Update");
    }
    if (jobStatus.equals(JobConstants.CANCELLED)) {
      log.error(
          "Job Status should not be ORDER_CANCELLED_BY_OPERATOR for Location Update Job Id:{}",
          jobId);
      throw new InvalidJobStateException(
          "Job Status should not be ORDER_CANCELLED_BY_OPERATOR for Location Update");
    }
  }

  private void pushNotification(JobLocationHistory jobLocationHistory, JobEntity jobEntity) {
    try {
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      String payload = objectMapper.writeValueAsString(
          PinChangeNotificationBody.builder().updatedJobPrice(jobLocationHistory.getRePinNetPrice())
              .jobId(jobLocationHistory.getJobId()).jobPrice(jobLocationHistory.getJobNetPrice())
              .differentialAmount(jobLocationHistory.getDifferenceAmount())
              .updateLocationType(jobLocationHistory.getUpdateLocationType())
              .type(JobConstants.JOB_REPIN_TYPE));
      RiderProfileResponse riderProfileResponse =
          riderProfileProxy.getRiderProfile(jobEntity.getDriverId());
      BroadcastNotification broadcastNotification = BroadcastNotification.builder().payload(payload)
          .arn(riderProfileResponse.getRiderDeviceDetails().getArn())
          .platform(riderProfileResponse.getRiderDeviceDetails().getPlatform().toString())
          .type(JobConstants.JOB_REPIN_TYPE).build();
      notificationPublisher.send(broadcastNotification);
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error("Error sending notification to rider app, jobId: {}, riderId: {}",
          jobEntity.getJobId(), jobEntity.getRiderId());
    }

  }

}
