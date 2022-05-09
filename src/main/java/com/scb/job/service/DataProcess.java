package com.scb.job.service;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobStatus;
import com.scb.job.constants.TrackingUrlPath;
import com.scb.job.entity.DistanceResponseEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.entity.JobRequest;
import com.scb.job.entity.RiderEntity;
import com.scb.job.entity.RiderLocationEntity;
import com.scb.job.exception.DataNotFoundException;
import com.scb.job.model.response.AddressResponse;
import com.scb.job.repository.JobRepository;
import com.scb.job.model.response.TrackingURLObject;
import com.scb.job.service.proxy.LocationProxy;
import com.scb.job.service.proxy.RiderProfileDetails;
import com.scb.job.service.proxy.RiderProfileProxy;
import com.scb.job.service.proxy.RiderProfileResponse;
import com.scb.job.service.redis.JobRedisService;
import com.scb.job.util.CommonUtils;
import com.scb.job.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static com.scb.job.constants.JobConstants.CUSTOMER_SEQUENCE;
import static com.scb.job.constants.JobConstants.MERCHANT_SEQUENCE;
import static com.scb.job.constants.JobStatus.ORDER_CANCELLED_BY_OPERATOR;
import static com.scb.job.util.DateUtils.getCurrentDateTimeBKK;
import static com.scb.job.util.DateUtils.parseDateTimeInBKK;

@Component
@Slf4j
public class DataProcess {

  public static final String GET_DRIVER_LOCATION = "/get_driver_location/";

  @Value("${callback.url}")
  private String callbackUrl;

  @Value("${rhinterfaceService.path}")
  private String rhInterfacePath;

  @Value("${operationsPortal.path}")
  private String operationsPortalUrl;

  ObjectMapper mapper;
  JobRepository jobRepository;
  ServiceCall serviceCall;
  RiderProfileProxy riderProfileProxy;
  LocationProxy locationProxy;
  JobRedisService jobRedisService;

  @Autowired
  public DataProcess(ObjectMapper mapper, JobRepository jobRepository, ServiceCall serviceCall,
                     RiderProfileProxy riderProfileProxy, LocationProxy locationProxy,
                     JobRedisService jobRedisService) {
    this.mapper = mapper;
    this.jobRepository = jobRepository;
    this.serviceCall = serviceCall;
    this.riderProfileProxy = riderProfileProxy;
    this.locationProxy = locationProxy;
    this.jobRedisService = jobRedisService;
  }

  public void processKafkaTopic(String message) throws Exception {
    RiderEntity riderInfo = mapper.readValue(message, RiderEntity.class);
    mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
    log.info("Kafka Message processKafkaTopic " + message + " ## " + riderInfo.getJobId());
    JobEntity job = jobRepository.getjobById(riderInfo.getJobId());
    if (null != job) {
      int previousStatus = job.getJobStatus();
      JobStatus newJobStatus = JobStatus.findByStatus(riderInfo.getStatus());
      if (newJobStatus == null) {
        log.warn("Unknown status:{} found. Not processing", riderInfo.getStatus());
        return;
      }
      log.info("processKafkaTopic newJobStatus -" + newJobStatus);

      if (ObjectUtils.isEmpty(job.getCallbackUrl())) {
        job.setCallbackUrl(callbackUrl);
      }
      job.setLastUpdatedDateTime(riderInfo.getDateTime());
      job.setLastUpdatedDateTimeTh(DateUtils.convertToThaiTime(riderInfo.getDateTime()));
      if (newJobStatus.equals(JobStatus.RIDER_NOT_FOUND)) {
        if(!job.getJobStatusKey().equals(JobStatus.NEW.name())){
          log.info("Job:{} is in progress state. Cannot change to RIDER_NOT_FOUND", riderInfo.getJobId());
          return;
        }
        job.setRiderNotFoundTime(riderInfo.getDateTime());

      } else if (newJobStatus.equals(JobStatus.JOB_ACCEPTED)) {
    	  
    	  
        enrichRiderDetails(riderInfo, job);
        job.setJobAcceptedTime(riderInfo.getDateTime());
        job.setEvBikeUser(riderInfo.getEvBikeUser());
        job.setRentingToday(riderInfo.getRentingToday());
        job.setEvBikeVendor(riderInfo.getEvBikeVendor());
        JobLocation jobLocation = getLocation(job, MERCHANT_SEQUENCE);
        log.info("riderId {}, long {}, lat {}", riderInfo.getRiderId(), jobLocation.getLng(), jobLocation.getLat());
        
        fetchRiderLocationAndRiderToMerchantDistance(riderInfo, job, jobLocation);
        
        jobRedisService.addRiderJobAcceptedToRedis(riderInfo.getRiderId(), job.getJobId());
        
      } else if (newJobStatus.equals(JobStatus.CALLED_MERCHANT)) {
        job.setCalledMerchantTime(riderInfo.getDateTime());

      } else if (newJobStatus.equals(JobStatus.ARRIVED_AT_MERCHANT)) {
        JobLocation jobLocation = getLocation(job, MERCHANT_SEQUENCE);
        jobLocation.setActualArriveTime(getCurrentDateTimeBKK());
        job.setArrivedAtMerchantTime(riderInfo.getDateTime());

      } else if (newJobStatus.equals(JobStatus.MEAL_PICKED_UP)) {
        job.setMealPickedUpTime(riderInfo.getDateTime());
        job.setMealPhotoUrl(riderInfo.getImageUrl());

      } else if (newJobStatus.equals(JobStatus.ARRIVED_AT_CUST_LOCATION)) {
        JobLocation jobLocation = getLocation(job, CUSTOMER_SEQUENCE);
        jobLocation.setActualArriveTime(getCurrentDateTimeBKK());
        job.setArrivedAtCustLocationTime(riderInfo.getDateTime());

      } else if (newJobStatus.equals(JobStatus.FOOD_DELIVERED)) {
        job.setFoodDeliveredTime((riderInfo.getDateTime()));
        job.setMealDeliveredPhotoUrl(riderInfo.getImageUrl());

      } else if (newJobStatus.equals(JobStatus.ORDER_CANCELLED_BY_OPERATOR)) {
        job.setUpdatedBy(riderInfo.getUpdatedBy());
        job.setOrderCancelledByOperationTime(riderInfo.getDateTime());
        if (Objects.nonNull(riderInfo.getJobPrice())) {
            Double taxPercentage = 0.0;
            if(job.getNetPrice()!=null && job.getNetPrice() !=0 )
              taxPercentage = job.getTaxAmount() / job.getNetPrice();
        	job.setNetPrice(riderInfo.getJobPrice());
        	job.setNetPaymentPrice(CommonUtils.round(riderInfo.getJobPrice() - riderInfo.getJobPrice() * taxPercentage));
        	job.setTaxAmount(CommonUtils.round(riderInfo.getJobPrice() * taxPercentage));
        	job.setIsJobPriceModified(riderInfo.getIsJobPriceModified());
		}
      }

      job.setJobStatus(newJobStatus.getStatus());
      job.setJobStatusKey(newJobStatus.name());
      job.setJobStatusEn(newJobStatus.getStatusEn());
      job.setJobStatusTh(newJobStatus.getStatusTh());
      jobRepository.save(job);
      if (!newJobStatus.equals(ORDER_CANCELLED_BY_OPERATOR)) {
        serviceCall.callServiceUrl(job.getCallbackUrl(),
            frameJsonRequest(previousStatus, riderInfo));
      }
    } else {
      log.error("Data is not available for rider jobId");
    }
  }

  private void fetchRiderLocationAndRiderToMerchantDistance(RiderEntity riderInfo, JobEntity job,
      JobLocation jobLocation) throws InterruptedException, ExecutionException {
    Map<String, String> riderInitialLatLong = new HashMap<>();
    double distanceToMerchant = 0.0;
    riderInitialLatLong.put("lat", "0.0");
    riderInitialLatLong.put("lng", "0.0");
    try {
      CompletableFuture<DistanceResponseEntity> responseEntity = locationProxy.getDistanceFromRidersCurrentLocation(
          riderInfo.getRiderId(), jobLocation.getLng(), jobLocation.getLat());
      CompletableFuture<RiderLocationEntity> riderLocation = locationProxy.getRiderCurrentLocation(riderInfo.getRiderId());
      CompletableFuture.allOf(responseEntity, riderLocation).join();
      riderInitialLatLong.put("lat", riderLocation.get().getGeom().getCoordinates().get(1));
      riderInitialLatLong.put("lng", riderLocation.get().getGeom().getCoordinates().get(0));
      distanceToMerchant = responseEntity.get().getDistanceInKms();
    }catch(Exception ex) {
      log.error("Error Occured while fetch rider location Error:{}", ex);
    }
    job.setDistanceToMerchant(distanceToMerchant);
    job.setRiderInitialLatLong(riderInitialLatLong);
  }

  public int convertToJobStatus(String status) {
    switch (status) {
      case JobConstants.JOB_ACC:
        return 5;
      case JobConstants.RID_NOT_FOU:
        return 4;
      case JobConstants.CAL_MER:
      case JobConstants.ARR_MER:
        return 6;
      case JobConstants.MEA_PIC_UP:
      case JobConstants.ARR_CUS_LOC:
        return 7;
      case JobConstants.FOO_DEL:
        return 9;
    }
    return 0;
  }

  private String frameJsonRequest(int previousStatus, RiderEntity riderInfo) throws Exception {
    String KAFKA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    if(riderInfo == null)
      throw new DataNotFoundException("Error fetching rider data");

    JobStatus jobStatus = JobStatus.findByStatus(riderInfo.getStatus());
    if(jobStatus == null)
      throw new DataNotFoundException("Error fetching rider data");

    JobRequest jREntity = new JobRequest(jobStatus.getStatus(),null, riderInfo.getJobId(),
            previousStatus, parseDateTimeInBKK(KAFKA_DATE_FORMAT, riderInfo.getDateTime()));

    return mapper.writeValueAsString(jREntity);
  }

  private JobLocation getLocation(JobEntity jobEntity, int seq) {
    if(jobEntity == null)
      throw new RuntimeException("Error fetching location data from job data");

    return jobEntity.getLocationList().stream().filter(loc -> loc.getSeq() == seq).findFirst()
        .orElseThrow(() -> new RuntimeException("Error fetching location data from job data"));

  }

  private JobEntity enrichRiderDetails(RiderEntity riderInfo, JobEntity job) {

    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();

    TrackingURLObject trackingURLObject = TrackingURLObject
            .builder()
            .internalURL(operationsPortalUrl + TrackingUrlPath.trackingUrl + job.getJobId())
            .consumerURL(GET_DRIVER_LOCATION + riderInfo.getRiderId() + "/" + job.getJobId())
            .build();

	//RiderProfileResponse riderProfileResponse = riderProfileProxy.getRiderProfile(riderId);
   // RiderProfileDetails riderProfileDetails = riderProfileResponse.getRiderProfileDetails();
    job.setDriverId(riderInfo.getRiderId());
    job.setRiderId(riderInfo.getRiderRRid());
    job.setDriverName(riderInfo.getDriverName());
    job.setDriverPhone(riderInfo.getDriverPhone());
    job.setDriverImageUrl(riderInfo.getDriverImageUrl());
    job.setDriverRating(0.0);
    job.setTrackingUrl(gson.toJson(trackingURLObject));

    log.info("enrichRiderDetails job --> " + job.getJobId());
    return job;

  }

}
