package com.scb.job.service;

import static com.scb.job.constants.JobConstants.JOB_DESC_THAI_FROM;
import static com.scb.job.constants.JobConstants.JOB_DESC_THAI_TO;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobLocationUpdatedBy;
import com.scb.job.constants.JobStatus;
import com.scb.job.entity.Coordinates;
import com.scb.job.entity.DistanceResponseEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.entity.JobLocationHistory;
import com.scb.job.entity.RiderLocationEntity;
import com.scb.job.exception.DataNotFoundException;
import com.scb.job.exception.InvalidJobStateException;
import com.scb.job.kafka.NotificationPublisher;
import com.scb.job.model.enumeration.Platform;
import com.scb.job.model.kafka.BroadcastNotification;
import com.scb.job.model.request.JobLocationUpdateDto;
import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.model.response.AddressResponse;
import com.scb.job.model.response.DifferentialPrice;
import com.scb.job.model.response.RiderDeviceDetails;
import com.scb.job.repository.JobLocationHistoryRepository;
import com.scb.job.repository.JobRepository;
import com.scb.job.service.proxy.EstimatePriceProxy;
import com.scb.job.service.proxy.EstimatePriceProxyResponse;
import com.scb.job.service.proxy.LocationProxy;
import com.scb.job.service.proxy.RiderProfileProxy;
import com.scb.job.service.proxy.RiderProfileResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JobLocationHistoryServiceTest {
  @Mock
  JobRepository repository;

  @Mock
  JobLocationHistoryRepository jobLocationHistoryRepository;

  @Mock
  EstimatePriceProxy estimatePriceProxy;

  @Mock
  LocationProxy locationProxy;

  @Mock
  ObjectMapper objectMapper;

  @Mock
  RiderProfileProxy riderProfileProxy;

  @Mock
  NotificationPublisher notificationPublisher;

  @InjectMocks
  private JobLocationHistoryService jobLocationHistoryService;


  private NewJobRequest job;

  private JobEntity jobEntity;

  private DistanceResponseEntity responseEntity;

  private Map<String, String> riderInitialLatLong;

  public static NewJobRequest createJobWithFields() {
    NewJobRequest job = new NewJobRequest();
    job.setApiKey("xHji908Klj74efghkJSGHlLGyfd543GFh");
    job.setUserName("user@gmail.com");
    job.setChannel("food");
    job.setCustomerMobile("0851111111");
    job.setCustomerEmail("test@gmail.com");
    job.setJobDate("2015-05-01");
    job.setStartTime("15:13");
    job.setFinishTime("16:13");
    job.setOption("1,2,3");
    job.setJobType("1");
    job.setCallbackUrl("url here");
    job.setLocationList(null);
    job.setPaymentType("cash");
    return job;
  }

  @BeforeEach
  public void initTest() {
    job = createJobWithFields();
    riderInitialLatLong = new HashMap<>();
    riderInitialLatLong.put("lat", "13.10");
    riderInitialLatLong.put("lng", "100.14");
    List<JobLocation> jobLocations = new ArrayList<>();
    jobLocations.add(new JobLocation(1, "D", "1", "Test", "Test", "11.2", "21.2", "Test", "Test",
        "20:00", "Test", ""));
    jobLocations.add(new JobLocation(2, "D", "1", "Test", "Test", "11.2", "21.2", "Test", "Test",
        "20:00", "Test", ""));
    jobEntity = JobEntity.builder().jobId("12").jobDate(job.getJobDate())
        .jobStatusKey(JobStatus.ARRIVED_AT_MERCHANT.name())
        .jobStatus(JobStatus.ARRIVED_AT_MERCHANT.getStatus())
        .jobStatusEn(JobStatus.ARRIVED_AT_MERCHANT.getStatusEn())
        .jobStatusTh(JobStatus.ARRIVED_AT_MERCHANT.getStatusTh())
        .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO).startTime(job.getStartTime())
        .finishTime(job.getFinishTime()).haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
        .jobType(job.getJobType()).option(job.getOption()).totalDistance(null).totalWeight(0.0)
        .totalSize(0.0).remark("good").userType(JobConstants.DEFAULT_USER_TYPE).normalPrice(10.0)
        .netPrice(10.0).netPaymentPrice(9.0).taxAmount(1.0).discount(0.0).driverId("RR1100")
        .rating(null).locationList(jobLocations).isJobLocationUpdateHistory(false)
        .riderInitialLatLong(riderInitialLatLong).distanceToMerchant(100.0).build();

    responseEntity = DistanceResponseEntity.builder().distance(1000.0).build();
  }

  @Test
  public void updateLocation() throws JsonProcessingException {
    Location startAddress =
        new Location("name", "start here", "1.1", "1.2", "name", "1234567890", "19", 1);
    Location endAddress =
        new Location("name", "end here", "1.1", "1.2", "name", "1234567890", "19", 2);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    locationList.add(endAddress);
    job.setLocationList(locationList);
    job.setRemark("Tower A the 3 floor\\n 1 abc");
    AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
    List<JobLocation> jobLocations = new ArrayList<>();
    jobLocations.add(new JobLocation(1, "D", "1", "Test", "Test", "11.2", "21.2", "Test", "Test",
        "20:00", "Test", ""));
    JobEntity entity = JobEntity.builder().jobId("12").jobDate(job.getJobDate())
        .jobStatusKey(JobStatus.ARRIVED_AT_MERCHANT.name())
        .jobStatus(JobStatus.ARRIVED_AT_MERCHANT.getStatus())
        .jobStatusEn(JobStatus.ARRIVED_AT_MERCHANT.getStatusEn())
        .jobStatusTh(JobStatus.ARRIVED_AT_MERCHANT.getStatusTh())
        .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO).startTime(job.getStartTime())
        .finishTime(job.getFinishTime()).haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
        .jobType(job.getJobType()).option(job.getOption()).totalDistance(null).totalWeight(0.0)
        .totalSize(0.0).remark("good").userType(JobConstants.DEFAULT_USER_TYPE).normalPrice(10.0)
        .netPrice(10.0).netPaymentPrice(9.0).taxAmount(1.0).discount(0.0).driverId("RR1100")
        .rating(null).locationList(jobLocations).isJobLocationUpdateHistory(false)
        .riderInitialLatLong(riderInitialLatLong).build();
    Mockito.when(estimatePriceProxy.getEstimatedPrice(any()))
        .thenReturn(EstimatePriceProxyResponse.builder().discount(0.0).netPrice(20.0)
            .normalPrice(20.0).netPaymentPrice(19.0).distance(1000.0).taxAmount(1.0).build());

    Mockito.when(repository.getjobById(anyString())).thenReturn(entity);
    Mockito.when(repository.save(any())).thenReturn(entity);
    List<String> latLongList = new ArrayList<>();
    latLongList.add("11");
    latLongList.add("12");
    RiderLocationEntity riderLocation = RiderLocationEntity.builder().riderId("RR1110")
        .geom(Coordinates.builder().coordinates(latLongList).build()).build();
    Mockito.when(locationProxy.getDistanceFromRidersCurrentLocation(any(), any(), any()))
        .thenReturn(CompletableFuture
            .completedFuture(DistanceResponseEntity.builder().distance(1000.0).build()));
    Mockito.when(locationProxy.getRiderCurrentLocation(anyString()))
        .thenReturn(CompletableFuture.completedFuture(riderLocation));
    Mockito.when(objectMapper.writeValueAsString(any())).thenReturn("payload");
    RiderDeviceDetails riderDeviceDetails =
        RiderDeviceDetails.builder().arn("arn").platform(Platform.ADM).build();
    Mockito.when(riderProfileProxy.getRiderProfile(anyString()))
        .thenReturn(RiderProfileResponse.builder().riderDeviceDetails(riderDeviceDetails).build());


    doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.MERCHANT).action("CONFIRM").build();
    JobLocationHistory jobDetail =
        jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto);
    assertNotNull(jobDetail);
  }

  @Test
  public void updateLocationCustomerRejected() throws JsonProcessingException {
    Mockito.when(estimatePriceProxy.getEstimatedPrice(any()))
        .thenReturn(EstimatePriceProxyResponse.builder().discount(0.0).netPrice(12.0)
            .normalPrice(12.0).distance(1000.0).netPaymentPrice(11.0).taxAmount(1.0).build());

    Mockito.when(repository.getjobById(anyString())).thenReturn(jobEntity);
    List<String> latLongList = new ArrayList<>();
    latLongList.add("11");
    latLongList.add("12");
    RiderLocationEntity riderLocation = RiderLocationEntity.builder().riderId("RR1110")
        .geom(Coordinates.builder().coordinates(latLongList).build()).build();
    Mockito.when(locationProxy.getRiderCurrentLocation(anyString()))
        .thenReturn(CompletableFuture.completedFuture(riderLocation));
    Mockito.when(repository.save(any())).thenReturn(getJobEntity());
    Mockito.when(objectMapper.writeValueAsString(any())).thenReturn("payload");
    RiderDeviceDetails riderDeviceDetails =
        RiderDeviceDetails.builder().arn("arn").platform(Platform.ADM).build();
    Mockito.when(riderProfileProxy.getRiderProfile(anyString()))
        .thenReturn(RiderProfileResponse.builder().riderDeviceDetails(riderDeviceDetails).build());
    doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
    Mockito.when(locationProxy.getDistanceFromRidersCurrentLocation(any(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(responseEntity));
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.CUSTOMER).action("REJECTED").build();
    JobLocationHistory jobDetail =
        jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto);
    assertNotNull(jobDetail);
  }

  @Test
  public void updateLocationCustomer() throws JsonProcessingException {
    Mockito.when(estimatePriceProxy.getEstimatedPrice(any()))
        .thenReturn(EstimatePriceProxyResponse.builder().discount(0.0).netPrice(12.0)
            .normalPrice(12.0).distance(1000.0).netPaymentPrice(11.0).taxAmount(1.0).build());

    Mockito.when(repository.getjobById(anyString())).thenReturn(jobEntity);
    Mockito.when(repository.save(any())).thenReturn(getJobEntity());
    Mockito.when(objectMapper.writeValueAsString(any())).thenReturn("payload");
    RiderDeviceDetails riderDeviceDetails =
        RiderDeviceDetails.builder().arn("arn").platform(Platform.ADM).build();
    Mockito.when(riderProfileProxy.getRiderProfile(anyString()))
        .thenReturn(RiderProfileResponse.builder().riderDeviceDetails(riderDeviceDetails).build());
    doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
    List<String> latLongList = new ArrayList<>();
    latLongList.add("11");
    latLongList.add("12");
    RiderLocationEntity riderLocation = RiderLocationEntity.builder().riderId("RR1110")
        .geom(Coordinates.builder().coordinates(latLongList).build()).build();
    Mockito.when(locationProxy.getRiderCurrentLocation(anyString()))
        .thenReturn(CompletableFuture.completedFuture(riderLocation));
    Mockito.when(locationProxy.getDistanceFromRidersCurrentLocation(any(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(responseEntity));
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.CUSTOMER).action("CONFIRM").build();
    JobLocationHistory jobDetail =
        jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto);
    assertNotNull(jobDetail);
  }

  @Test
  public void updateLocationWithNegativeNetPrice() throws JsonProcessingException {
    Mockito.when(estimatePriceProxy.getEstimatedPrice(any()))
        .thenReturn(EstimatePriceProxyResponse.builder().discount(0.0).netPrice(6.0)
            .normalPrice(6.0).netPaymentPrice(5.8).distance(1000.0).taxAmount(1.0).build());

    Mockito.when(repository.getjobById(anyString())).thenReturn(jobEntity);
    List<String> latLongList = new ArrayList<>();
    latLongList.add("11");
    latLongList.add("12");
    RiderLocationEntity riderLocation = RiderLocationEntity.builder().riderId("RR1110")
        .geom(Coordinates.builder().coordinates(latLongList).build()).build();
    Mockito.when(locationProxy.getRiderCurrentLocation(anyString()))
        .thenReturn(CompletableFuture.completedFuture(riderLocation));
    Mockito.when(repository.getjobById(anyString())).thenReturn(getJobEntity());
    Mockito.when(repository.save(any())).thenReturn(getJobEntity());
    Mockito.when(objectMapper.writeValueAsString(any())).thenReturn("payload");
    Mockito.when(locationProxy.getDistanceFromRidersCurrentLocation(any(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(responseEntity));
    RiderDeviceDetails riderDeviceDetails =
        RiderDeviceDetails.builder().arn("arn").platform(Platform.ADM).build();
    Mockito.when(riderProfileProxy.getRiderProfile(anyString()))
        .thenReturn(RiderProfileResponse.builder().riderDeviceDetails(riderDeviceDetails).build());
    doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.MERCHANT).action("CONFIRM").build();
    JobLocationHistory jobDetail =
        jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto);
    assertNotNull(jobDetail);
  }

  @Test
  public void getJobLocationHistoryByIdTest() {
    List<JobLocationHistory> jobLocationHistoryList = new ArrayList<JobLocationHistory>();
    jobLocationHistoryList.add(JobLocationHistory.builder().updateLocationType("MERCHANT")
        .oldLat("14.7518789").oldLong("113.7263246").previousAddress("281/28").newLat("10.7518789")
        .newLong("126.7263246").jobNetPrice(60.0).rePinNetPrice(65.0).differenceAmount(5.0)
        .jobId("58405d66-da5f-4c2a-b1da-eebd68b787ad").riderId("RR1022").build());
    Mockito.when(jobLocationHistoryRepository.getJobLocationHistoryByJobId(anyString()))
        .thenReturn(jobLocationHistoryList);

    List<JobLocationHistory> jobLocationHistoryTest =
        jobLocationHistoryService.getJobLocationHistoryById(anyString());
    assertEquals(jobLocationHistoryTest, jobLocationHistoryList);
  }

  @Test
  public void validateJobStateFoodDeliveredTest() {
    jobEntity.setJobStatusKey("FOOD_DELIVERED");
    Mockito.when(repository.getjobById(anyString())).thenReturn(jobEntity);
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.MERCHANT).action("CONFIRM").build();
    InvalidJobStateException exception = assertThrows(InvalidJobStateException.class,
        () -> jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto));
    assertEquals("Job Status should not be FOOD_DELIVERED for Location Update",
        exception.getMessage());
  }

  @Test
  public void validateJobStateOrderCancelledTest() {
    jobEntity.setJobStatusKey("ORDER_CANCELLED_BY_OPERATOR");
    Mockito.when(repository.getjobById(anyString())).thenReturn(jobEntity);
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.MERCHANT).action("CONFIRM").build();
    InvalidJobStateException exception = assertThrows(InvalidJobStateException.class,
        () -> jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto));
    assertEquals("Job Status should not be ORDER_CANCELLED_BY_OPERATOR for Location Update",
        exception.getMessage());
  }


  @Test
  void testRiderLocationNotFound() {
    Mockito.when(repository.getjobById(anyString())).thenReturn(jobEntity);
    Mockito.when(locationProxy.getRiderCurrentLocation(anyString())).thenReturn(null);
    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder()
        .addressType(JobLocationUpdatedBy.MERCHANT).action("CONFIRM").build();

    DataNotFoundException exception = assertThrows(DataNotFoundException.class,
        () -> jobLocationHistoryService.updateLocation("Test", jobLocationUpdateDto));

    assertEquals("No Location Found for Rider", exception.getMessage());

  }


  @Test
  void getCustomerPaymentPriceTest() {

    List<JobLocationHistory> jobLocationHistoryList = new ArrayList<>();
    jobLocationHistoryList.add(JobLocationHistory.builder().riderId("RR11111")
        .updatedTime(LocalDateTime.now()).updateLocationType("CUSTOMER").status("Accepted")
        .jobId("S210202020").differenceAmount(10.0).build());
    jobLocationHistoryList
        .add(JobLocationHistory.builder().riderId("RR11111").updatedTime(LocalDateTime.now())
            .updateLocationType("CUSTOMER").status("Accepted").jobId("S210202020").build());

    DifferentialPrice differentialPrice =
        JobLocationHistory.getCustomerPaymentPrice(jobLocationHistoryList);
    assertNotNull(differentialPrice);
  }


  private JobEntity getJobEntity() {
    List<JobLocation> jobLocations = new ArrayList<>();
    jobLocations.add(new JobLocation(1, "D", "1", "Test", "Test", "11.2", "21.2", "Test", "Test",
        "20:00", "Test", ""));

    JobEntity entity = JobEntity.builder().jobId("12").jobDate(job.getJobDate())
        .jobStatusKey(JobStatus.ARRIVED_AT_MERCHANT.name())
        .jobStatus(JobStatus.ARRIVED_AT_MERCHANT.getStatus())
        .jobStatusEn(JobStatus.ARRIVED_AT_MERCHANT.getStatusEn())
        .jobStatusTh(JobStatus.ARRIVED_AT_MERCHANT.getStatusTh())
        .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO).startTime(job.getStartTime())
        .finishTime(job.getFinishTime()).haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
        .jobType(job.getJobType()).option(job.getOption()).totalDistance(null).totalWeight(0.0)
        .totalSize(0.0).remark("good").userType(JobConstants.DEFAULT_USER_TYPE).normalPrice(10.0)
        .netPrice(10.0).netPaymentPrice(9.0).taxAmount(1.0).discount(0.0).driverId("RR1100")
        .rating(null).locationList(jobLocations).isJobLocationUpdateHistory(false)
        .riderInitialLatLong(riderInitialLatLong).build();
    return entity;
  }
}
