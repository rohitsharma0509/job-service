package com.scb.job.service;

import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobStatus;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.entity.OrderItems;
import com.scb.job.exception.DataNotFoundException;
import com.scb.job.exception.JobCreationException;
import com.scb.job.exception.ResourceNotFoundException;
import com.scb.job.exception.ZoneException;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.model.enumeration.JobTypeMapping;
import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.model.response.*;
import com.scb.job.repository.JobLocationHistoryRepository;
import com.scb.job.repository.JobRepository;
import com.scb.job.repository.SequenceRespository;
import com.scb.job.service.proxy.*;
import com.scb.job.service.proxy.helper.PointXCustomerPriceProxyHelper;
import com.scb.job.service.proxy.helper.PointXEstimatePriceProxyHelper;
import com.scb.job.util.CommonUtils;
import com.scb.job.util.DateUtils;
import com.scb.job.util.JobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.scb.job.constants.JobConstants.*;
import static com.scb.job.entity.JobEntity.extractCustomerRemark;
import static com.scb.job.entity.JobEntity.validateWithholdingTaxAmount;
import static com.scb.job.util.DateUtils.parseDateTimeInBKK;

@Service
@Slf4j
public class JobServiceImpl implements JobService {

  private static final String MIN_DIS_JOB_COMPLETION = "minimumDistanceForJobCompletion";
  @Autowired JobRepository repository;

  @Autowired JobLocationHistoryRepository jobLocationHistoryRepository;

  @Autowired JobHelper jobHelper;

  @Autowired EstimatePriceProxy estimatePriceProxy;


  @Autowired
  private PointXEstimatePriceProxyHelper pointXEstimatePriceProxyHelper;

  @Autowired
  private PointXCustomerPriceProxyHelper pointXCustomerPriceProxyHelper;

  @Autowired
  CustomerPricingProxy customerPricingProxy;

  @Autowired
  private SequenceRespository sequenceRespository;
  @Autowired
  private LocationProxy locationProxy;

  @Autowired
  private OperationsServiceProxy operationsServiceProxy;

  @Override
  public JobDetail createJob(NewJobRequest job) {

    List<Location> listOfLocations = job.getLocationList();
    List<JobLocation> jobLocations = new ArrayList<>();

    listOfLocations.forEach(
        list -> {
          JobLocation location = new JobLocation();
          location.setAddress(list.getAddress());
          location.setAddressId("123");
          location.setAddressName(list.getAddressName());
          location.setContactName(list.getContactName());
          location.setContactPhone(list.getContactPhone());
          location.setSeq(list.getSeq());
          location.setLat(list.getLat());
          location.setLng(list.getLng());
          location.setType("D");
          if(list.getSeq()==CUSTOMER_SEQUENCE)
          {
            location.setMail(job.getCustomerEmail());
          }

          jobLocations.add(location);
        });

    //call to location service to get zone details
    log.info("getting merchant zone for current location of merchant");
    List<Location> locationList = getMerchantLocationList(listOfLocations);
    ZoneResponse zoneResponse = locationProxy.getMerchantZone(locationList.get(0).getLat(),locationList.get(0).getLng());

    log.info("getting pricing information");

      EstimatePriceProxyResponse estimatePriceProxyResponse = EstimatePriceProxyResponse.builder().build();
      CustomerPricingProxyResponse customerPricingProxyResponse = CustomerPricingProxyResponse.builder().build();
      PointXEstimatePricingResponse pointXEstimatePricingResponse = PointXEstimatePricingResponse.builder().build();
      PointXCustomerPricingResponse pointXCustomerPricingResponse = PointXCustomerPricingResponse.builder().build();

      if (job.getJobType().equals(JobConstants.JOB_TYPE_POINTX_NUMBER)) {
          pointXEstimatePricingResponse = pointXEstimatePriceProxyHelper.callPointXEstimatePricing(job);
          pointXCustomerPricingResponse = pointXCustomerPriceProxyHelper.callPointXCustomerPricing(job);
      } else {
          EstimatePriceRequest estimatePriceRequest = EstimatePriceRequest.builder()
                  .userName(job.getUserName())
                  .apiKey(job.getApiKey())
                  .channel(job.getChannel())
                  .jobType(job.getJobType())
                  .option(job.getOption())
                  .promoCode(job.getPromoCode())
                  .locationList(job.getLocationList()).build();

          estimatePriceProxyResponse = estimatePriceProxy
                  .getEstimatedPrice(estimatePriceRequest);

          CustomerPricingRequest customerPricingRequest = CustomerPricingRequest
                  .builder()
                  .userName(job.getUserName())
                  .apiKey(job.getApiKey())
                  .channel(job.getChannel())
                  .jobType(job.getJobType())
                  .ddFlag(job.getDdFlag())
                  .locationList(job.getLocationList())
                  .zoneGroup(zoneResponse.getZoneGroup()).build();

          customerPricingProxyResponse = customerPricingProxy
                  .getCalculatedNetPrice(customerPricingRequest);
      }

    ConfigData configData = new ConfigData();
    try {
    	configData = operationsServiceProxy.getConfiguredDataForKey(MIN_DIS_JOB_COMPLETION);
	} catch (Exception e) {
		log.info("Error occured while fetching the minimum distance configuration from operation service");
	}
    log.info("Configured min distance {} ",configData.toString());
    //call to location service to get zone details

    JobLocation merchantJobLocation = jobLocations.stream().filter(jobLocation -> jobLocation.getSeq() == MERCHANT_SEQUENCE).collect(Collectors.toList()).get(0);
    JobLocation customerJobLocation = jobLocations.stream().filter(jobLocation -> jobLocation.getSeq() == CUSTOMER_SEQUENCE).collect(Collectors.toList()).get(0);
     //to get sub district
    CompletableFuture<AddressResponse> merchantAddressResponse = locationProxy.getSubDistrict(merchantJobLocation.getLat(),merchantJobLocation.getLng());
    CompletableFuture<AddressResponse> customerAddressResponse = locationProxy.getSubDistrict(customerJobLocation.getLat(),customerJobLocation.getLng());
    CompletableFuture.allOf(merchantAddressResponse,customerAddressResponse).join();

    try {
      merchantJobLocation.setSubDistrict(merchantAddressResponse.get().getSubDistrict());
      customerJobLocation.setSubDistrict(customerAddressResponse.get().getSubDistrict());
    } catch (InterruptedException | ExecutionException e) {
      log.error(String.format("Error occurred while getting sub district %s", e.getMessage()));
    }
    //Using For Search Job Details
    String merchantName = (jobLocations.size() >= 1 && !StringUtils.isEmpty(jobLocations.get(0).getContactName())) ?
        jobLocations.get(0).getContactName() : "";

    //Using For Search Job Details
    String customerName = (jobLocations.size() >= 2 && !StringUtils.isEmpty(jobLocations.get(1).getContactName())) ?
        jobLocations.get(1).getContactName() : "";

    //Using For Search Job Details
    String orderId = (!StringUtils.isEmpty(job.getRefNo()) ? job.getRefNo() : "");

    // forming Job entity to save in Database
    JobEntity entity =
        JobEntity.builder()
            .jobId(JobHelper.getJobId(sequenceRespository.getNextSequence(), job.getJobType()))
            .jobDate(job.getJobDate())
            .customerName(customerName) // For Job Detail Search do not remove
            .merchantName(merchantName) // For Job Detail Search do not remove
            .riderId("") // For Job Detail Search do not remove
            .driverName("") // For Job Detail Search do not remove
            .driverPhone("") // For Job Detail Search do not remove
            .jobDateField(convertDateForJobSearch(job.getJobDate())) // For Job Detail Search do not remove
            .jobStatus(JobStatus.NEW.getStatus())
            .jobStatusEn(JobStatus.NEW.getStatusEn())
            .jobStatusKey(JobStatus.NEW.name())
            .jobStatusTh(JobStatus.NEW.getStatusTh())
            .jobDesc(buildJobDesc(listOfLocations))
            .startTime(job.getStartTime())
            .finishTime(job.getFinishTime())
            .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
            .jobType(job.getJobType())
            .jobTypeEnum(getJobTypeEnum(job.getJobType()))
            .jobTypeEnumThai(getJobTypeEnum(job.getJobType()).getStatus())
            .option(job.getOption())
            .minDistanceForJobCompletion(ObjectUtils.isNotEmpty(configData.getValue())?Double.valueOf(configData.getValue()):null)
            .totalWeight(job.getTotalWeight())
            .totalSize(job.getTotalSize())
            .remark(job.getRemark())
            .userType(JobConstants.DEFAULT_USER_TYPE)
            .riderPrice(estimatePriceProxyResponse.getRiderPrice())
            .rating(0.0)
            .callbackUrl(job.getCallbackUrl())
            .locationList(jobLocations)
            .orderId(orderId)
            .creationDateTime(DateUtils.zonedDateTimeToString(ZonedDateTime.now()))
            .lastUpdatedDateTime(DateUtils.zonedDateTimeToString(ZonedDateTime.now()))
            .creationDateTimeTh(DateUtils.zonedDateTimeThaiToString(ZonedDateTime.now(ZoneId.of("Asia/Bangkok")))) // For Job History Search do not remove
            .lastUpdatedDateTimeTh(DateUtils.zonedDateTimeThaiToString(ZonedDateTime.now(ZoneId.of("Asia/Bangkok")))) // For Job History Search do not remove
            .zoneId(zoneResponse.getZoneId())
            .zoneName(zoneResponse.getZoneName())
            .zoneGroup(zoneResponse.getZoneGroup())
            .isJobPriceModified(Boolean.valueOf(false))
            .isJobLocationUpdateHistory(false)
            .otherDeductionsSearch("")
            .build();

    if(JobConstants.JOB_TYPE_POINTX_NUMBER.equals(job.getJobType())) {
      entity.setGoodsValue(Double.valueOf(job.getGoodsValue()));
      entity.setCustomerNetPrice(pointXCustomerPricingResponse.getCustomerNetPrice());
      entity.setCustomerNormalPrice(pointXCustomerPricingResponse.getCustomerNormalPrice());
      entity.setNormalInsuredPrice(pointXCustomerPricingResponse.getNormalInsuredPrice());
      entity.setNetInsuredPrice(pointXCustomerPricingResponse.getNetInsuredPrice());
      entity.setNormalPrice(pointXEstimatePricingResponse.getNormalPrice());
      entity.setNetPrice(pointXEstimatePricingResponse.getNetPrice());
      entity.setNetPaymentPrice(pointXEstimatePricingResponse.getNetPrice());
      entity.setTaxAmount(0.0);
      entity.setNetPriceSearch(Double.toString(pointXEstimatePricingResponse.getNetPrice()));
      entity.setDiscount(pointXEstimatePricingResponse.getDiscount());
      entity.setTotalDistance(pointXEstimatePricingResponse.getDistance());
    } else {
      entity.setCustomerNetPrice(customerPricingProxyResponse.getNetPrice());
      entity.setNormalPrice(estimatePriceProxyResponse.getNormalPrice());
      entity.setNetPrice(estimatePriceProxyResponse.getNetPrice());
      entity.setNetPaymentPrice(CommonUtils.round(estimatePriceProxyResponse.getNetPaymentPrice()));
      entity.setTaxAmount(CommonUtils.round(estimatePriceProxyResponse.getTaxAmount()));
      entity.setNetPriceSearch(Double.toString(estimatePriceProxyResponse.getNetPrice())); // For Job Detail Search do not remove
      entity.setDiscount(estimatePriceProxyResponse.getDiscount());
      entity.setTotalDistance(estimatePriceProxyResponse.getDistance());
    }

    //save ddFlag in db
    if(job.getDdFlag() != null) {
      entity.setDdFlag(job.getDdFlag());
    }

    entity = parseRemark(job.getRemark(), entity);
    // calling entity to save job in database

    try {
      log.info("saving job:{} to db", entity.getJobId());
      entity = repository.save(entity);
      log.info("job entity is saved");
    } catch (Exception e) {
      log.error("Error while saving job into database", e);
      throw new JobCreationException();
    }

    // Forming the response and sending the response
    return formResponseFromEntity(entity);
  }
  
  private LocalDate convertDateForJobSearch(String jobDate) {
    try {
      return LocalDate.parse(jobDate);
    } catch (DateTimeParseException ex) {
      log.error("Date Format Exception in Rider Job Search");
    }
    return LocalDate.now();
  }
  
  public JobDetail formResponseFromEntity(JobEntity entity) {
    log.info(entity.getJobId());
    extractCustomerRemark(entity);
    JobDetail jobDetail = JobDetail.builder()
        .jobId(entity.getJobId())
        .jobDate(entity.getJobDate())
        .jobStatus(entity.getJobStatus())
        .jobStatusEn(entity.getJobStatusEn())
        .jobStatusTh(entity.getJobStatusTh())
        .jobDesc(entity.getJobDesc())
        .startTime(entity.getStartTime())
        .finishTime(entity.getFinishTime())
        .haveReturn(entity.isHaveReturn())
        .jobType(entity.getJobType())
        .jobTypeEnum(entity.getJobTypeEnum())
        .option(entity.getOption())
        .totalDistance(entity.getTotalDistance())
        .totalWeight(entity.getTotalWeight())
        .totalSize(entity.getTotalSize())
        .remark(entity.getRemark())
        .userType(entity.getUserType())
        .normalPrice(entity.getNormalPrice())
        .netPrice(entity.getNetPrice())
        .customerNetPrice(entity.getCustomerNetPrice())
        .netPaymentPrice(CommonUtils.round(entity.getNetPaymentPrice()))
        .taxAmount(CommonUtils.round(entity.getTaxAmount()))
        .discount(entity.getDiscount())
        .rating(entity.getRating())
        .locationList(entity.getLocationList())
        .remark(entity.getRemark())
        .orderId(entity.getOrderId())
        .orderItems(entity.getOrderItems())
        .minDistanceForJobCompletion(entity.getMinDistanceForJobCompletion())
        .riderId(entity.getRiderId())
        .driverName(entity.getDriverName())
        .driverPhone(entity.getDriverPhone())
        .evBikeUser(entity.getEvBikeUser())
        .distanceToMerchant(entity.getDistanceToMerchant())
        .evBikeVendor(entity.getEvBikeVendor())
        .rentingToday(entity.getRentingToday())
        .isJobLocationUpdateHistory(entity.getIsJobLocationUpdateHistory())
        .distanceToMerchantRepin(entity.getDistanceToMerchantRepin())
        .merchantToCustomerRepin(entity.getMerchantToCustomerRepin())
        .ddFlag(entity.getDdFlag())
            .shopLandmark(entity.getShopLandmark())
        .build();

      if(JobConstants.JOB_TYPE_POINTX_NUMBER.equals(entity.getJobType())) {
          jobDetail.setCustomerNormalPrice(entity.getCustomerNormalPrice());
          jobDetail.setNormalInsuredPrice(entity.getNormalInsuredPrice());
          jobDetail.setNetInsuredPrice(entity.getNetInsuredPrice());
          jobDetail.setGoodsValue(Double.valueOf(entity.getGoodsValue()));
      }

      return jobDetail;
  }

  @Override
  public JobEntity getJobById(String jobId) {
    JobEntity jobEntity = repository.getjobById(jobId);
    if (!ObjectUtils.isEmpty(jobEntity)) {
      extractCustomerRemark(jobEntity);
      validateWithholdingTaxAmount(jobEntity); 
      return jobEntity;
    }
    throw new ResourceNotFoundException(String.format("Rider jobId %s not found", jobId));
  }

  private String buildJobDesc(List<Location> locationList) {
    Location fromLoc =
        locationList.stream()
            .filter(loc -> loc.getSeq() == MERCHANT_SEQUENCE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Error fetching location data from job data"));
    Location toLoc =
        locationList.stream()
            .filter(loc -> loc.getSeq() == CUSTOMER_SEQUENCE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Error fetching location data from job data"));

    return JOB_DESC_THAI_FROM
        + fromLoc.getAddressName()
        + " > "
        + JOB_DESC_THAI_TO
        + toLoc.getAddressName();
  }

  private JobEntity parseRemark(String remark, JobEntity jobEntity) {
    if(StringUtils.isBlank(remark)){
      return jobEntity
              .toBuilder()
              .orderItems(Collections.EMPTY_LIST)
              .shopLandmark(null)
              .remark("")
              .build();
    }

    String[] remarks = remark.split("\n");
    String remarkField = "";
    int subAtPosition = 1;
    List<OrderItems> orderItems = new ArrayList<>();
    String shopLandmark = null;

    try {
      remarkField = remarks[0];
      if(remarks.length > 1) {
        if(remarks[1].trim().startsWith(SHOP_LANDMARK_PREFIX)) {
          subAtPosition = 2;
          shopLandmark = remarks[1].trim()
                  .replaceFirst(SHOP_LANDMARK_PREFIX, "")
                  .replaceAll("-", "")
                  .trim();
          if(StringUtils.isEmpty(shopLandmark)){
            shopLandmark = null;
          }
        }
        orderItems = Arrays.asList(remarks).subList(subAtPosition, remarks.length)
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(this::parseOrderNameQty)
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("Error parsing remarks:{}", remark);
      log.error("Exception occured ", e);
    }
    return jobEntity.toBuilder().shopLandmark(shopLandmark).orderItems(orderItems).remark(remarkField).build();
  }

  private OrderItems parseOrderNameQty(String orderString) {
    String[] orderNameQty = orderString.trim().split("\\s+");
    String orderName;
    String qty;
    if (orderNameQty.length == 1) {
      return OrderItems.builder().name(orderNameQty[0]).quantity(0).build();
    } else if (orderNameQty.length == 2) {
      return OrderItems.builder()
          .name(orderNameQty[0])
          .quantity(Integer.parseInt(orderNameQty[1]))
          .build();
    } else {

      int lastIndexOf = orderString.lastIndexOf(" ");
      orderName = orderString.substring(0, lastIndexOf);
      qty = orderString.substring(lastIndexOf + 1);
    }

    return OrderItems.builder().name(orderName).quantity(Integer.parseInt(qty)).build();
  }

  private List<Location> getMerchantLocationList(List<Location> locationList){
    try {
      return locationList.stream()
          .filter(location -> (Optional.ofNullable(location.getSeq()).isPresent()
              && location.getSeq() == JobConstants.MERCHANT_SEQUENCE))
          .collect(Collectors.toList());
    }
    catch (IndexOutOfBoundsException e) {
      throw new ZoneException("Error occurred while fetching Zone of given merchant location");
    }
  }

  @Override
  public JobConfirmResponse confirmJobByJobId(String jobId) {
    JobEntity jobEntity = repository.getjobById(jobId);
    Date today = new Date();
    LocalDateTime currentDate =  LocalDateTime.ofInstant(today.toInstant(), ZoneId.systemDefault());
    if(jobEntity != null) {
      jobEntity.setMerchantConfirm(true);
      jobEntity.setMerchantConfirmDateTime(currentDate);
      repository.save(jobEntity);
      return new JobConfirmResponse(parseDateTimeInBKK(today));
    } else {
      throw new DataNotFoundException("Invalid JobId : "+jobId);
    }
  }
  
  @Override
  public RiderJobResponse getJobByRiderIdStatus(String riderId) {
         RiderJobResponse response= RiderJobResponse.builder().build();
         List<Integer> statusList=JobStatus.getRunningJobStatuses();
         List<JobEntity> jobsList=repository.getJobByRiderIdStatus(statusList, riderId);
       jobsList.sort( (JobEntity o1, JobEntity o2)
                 ->o2.getJobAcceptedTime().compareTo(o1.getJobAcceptedTime()) );

         if(jobsList.size()>=1) {
           JobEntity jobEntity = jobsList.get(0);
           log.info("address name {}", jobEntity.getLocationList().get(1).getAddressName());
           extractCustomerRemark(jobEntity);
           log.info("customer remark {}" , jobEntity.getCustomerRemark());
           response = JobEntity.of(jobEntity);
         }
         else
             throw new ResourceNotFoundException("Running job is not available for rider id:"+riderId);
         return response;
    }

  @Override
  public List<JobDetail> getJobDetails(LocalDateTime from, LocalDateTime to, Boolean isEvBikeRider, String status,
                                       EvBikeVendors evBikeVendor, Boolean rentingToday) {
    List<JobEntity> jobEntities = repository.findJobDetails(from, to, isEvBikeRider, status, evBikeVendor, rentingToday);
    return jobEntities.stream().map(entity -> formResponseFromEntity(entity)).collect(Collectors.toList());
  }

  @Override
  public List<JobEntity> getJobsToReconciliation(LocalDateTime from, LocalDateTime to, List<JobType> jobType, Pageable pageable) {
    log.info("getting jobs for reconciliation for jobType {}", jobType);
    return repository.getJobsToReconciliation(from, to, jobType, pageable);
  }

  @Override
  public void updateExcessiveWaitTimeAmount(String jobId, ExcessiveWaitingTimeDetailsEntity ewt) {
    log.info("adding excessive wait amount to job-{}", jobId);
    ewt.setExcessiveWaitTopupAmountSearch(ewt.getExcessiveWaitTopupAmount() + StringUtils.EMPTY);
    repository.updateExcessiveWaitTimeAmount(jobId, ewt);
    log.info("added excessive wait amount to job-{} successfully", jobId);
  }

  @Override
  public RiderJobResponse getRunningJobDetails(String jobId) {

	  JobEntity job=repository.getjobById(jobId);
	  log.info("hung job details for jobId-{}",jobId);


      if(ObjectUtils.isEmpty(job))
         throw new ResourceNotFoundException(" job is not available for job id:"+jobId);
      log.info("address name {}", job.getLocationList().get(1).getAddressName());
      extractCustomerRemark(job);
      log.info("customer remark {}" , job.getCustomerRemark());
      return JobEntity.of(job);
    }

    private JobType getJobTypeEnum(String jobType){
      return JobTypeMapping.valueOf("J"+jobType).getValue();
    }
  
}
