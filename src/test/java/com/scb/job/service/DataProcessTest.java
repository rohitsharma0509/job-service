package com.scb.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.scb.job.entity.Coordinates;
import com.scb.job.entity.DistanceResponseEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.entity.RiderLocationEntity;
import com.scb.job.service.proxy.LocationProxy;
import com.scb.job.service.proxy.RiderProfileDetails;
import com.scb.job.service.proxy.RiderProfileProxy;
import com.scb.job.service.proxy.RiderProfileResponse;
import com.scb.job.service.redis.JobRedisService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.constants.JobConstants;
import com.scb.job.entity.JobEntity;
import com.scb.job.repository.JobRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataProcessTest {

    private static final String RIDER_ID = "rd06";
    private static final String LONGITUDE_TO = "1.1";
    private static final String LATITUDE_TO = "1.2";
    private static final double DISTANCE = 10.0;
	
	@InjectMocks
    private DataProcess dataProcessUnderTest;
	
    @Mock
    private JobRepository jobRepository;
    
    @Mock
    private ServiceCall serviceCall;

    @Mock
    private RiderProfileProxy riderProfileProxy;

    @Mock
    private LocationProxy locationProxy;
    
    @Mock
    private JobRedisService jobRedisService;
    
    ObjectMapper mapper=new ObjectMapper();
    
    @BeforeEach
     void setUp() {
        dataProcessUnderTest = new DataProcess(mapper,jobRepository, serviceCall, riderProfileProxy, locationProxy, jobRedisService);
    }

    @Test
    void testProcessKafkaTopic() throws Exception {
        List<String> coordinate = new ArrayList<>();
        coordinate.add("12.10");
        coordinate.add("130.10");
        JobLocation startAddress = JobLocation.builder().seq(1).type("1").addressId("start here").addressName("name").address("").lng(LONGITUDE_TO)
                .lat(LATITUDE_TO).contactName("name").contactPhone("1234567890").mail("bcd@gmail.com").subDistrict("sub district").build();
        JobLocation endAddress = JobLocation.builder().seq(2).type("1").addressId("end here").addressName("name").address("").lng(LONGITUDE_TO)
                .lat(LATITUDE_TO).contactName("name").contactPhone("1234567890").mail("bcd@gmail.com").subDistrict("sub district").build();
    	String jsonReq="{\"riderId\":\"rd06\", \"jobId\": \"RBH201217999003\", \"dateTime\": \"2017-02-25T19:10:20+00:00\", \"status\": \"JOB_ACCEPTED\" }";
        JobEntity job = JobEntity.builder().locationList(Arrays.asList(startAddress, endAddress)).riderId(RIDER_ID).callbackUrl("http://localhost").build();
        when(jobRepository.getjobById("RBH201217999003")).thenReturn(job);
        DistanceResponseEntity distanceResponseEntity = DistanceResponseEntity.builder().distance(DISTANCE).build();
        when(locationProxy.getDistanceFromRidersCurrentLocation(eq(RIDER_ID), eq(LONGITUDE_TO), eq(LATITUDE_TO))).thenReturn(CompletableFuture.completedFuture(distanceResponseEntity));
        when(locationProxy.getRiderCurrentLocation(any())).thenReturn(CompletableFuture.completedFuture(RiderLocationEntity.builder().geom(Coordinates.builder().coordinates(coordinate).build()).build()));
        RiderProfileDetails riderProfileDetails = RiderProfileDetails
            .builder().id(RIDER_ID).riderId(RIDER_ID).firstName("first_name").lastName("last_name").phoneNumber("1234567890")
            .build();
        RiderProfileResponse riderProfileResponse = RiderProfileResponse
            .builder().riderProfileDetails(riderProfileDetails)
            .build();
        
        when(riderProfileProxy.getRiderProfile(anyString())).thenReturn(riderProfileResponse);
        doNothing().when(jobRedisService).addRiderJobAcceptedToRedis(any(), any());
        dataProcessUnderTest.processKafkaTopic(jsonReq);
        verify(serviceCall).callServiceUrl(eq("http://localhost"), any(String.class));
        verify(jobRepository,times(1)).save(any(JobEntity.class));
    }

    @Test
    void testProcessARRIVED_MERCHANTKafkaTopic() throws Exception {
        JobLocation startAddress = new JobLocation(1,"1","name","start here","", "1.1","1.2","name","1234567890",null,"bcd@gmail.com","sub district");
        JobLocation endAddress = new JobLocation(2, "1","name","end here","", "1.1","1.2","name","1234567890",null,"bcd@gmail.com","sub district");
        String jsonReq="{\"riderId\":\"rd06\", \"jobId\": \"RBH201217999003\", \"dateTime\": \"2017-02-25T19:10:20+00:00\", \"status\": \"ARRIVED_AT_MERCHANT\" }";
        when(jobRepository.getjobById("RBH201217999003")).thenReturn(JobEntity.builder()
            .locationList(Arrays.asList(startAddress, endAddress))
            .callbackUrl("http://localhost").build());
        when(jobRepository.save(any(JobEntity.class))).thenReturn(JobEntity.builder().build());
        dataProcessUnderTest.processKafkaTopic(jsonReq);
        verify(serviceCall).callServiceUrl(eq("http://localhost"), any(String.class));
        verify(jobRepository,times(1)).save(any(JobEntity.class));
    }

    @Test
    void testProcessARRIVED_AT_CUST_LOCATIONKafkaTopic() throws Exception {
        JobLocation startAddress = new JobLocation(1,"1","name","start here","", "1.1","1.2","name","1234567890",null,"bcd@gmail.com","sub district");
        JobLocation endAddress = new JobLocation(2, "1","name","end here","", "1.1","1.2","name","1234567890",null,"bcd@gmail.com","sub district");
        String jsonReq="{\"riderId\":\"rd06\", \"jobId\": \"RBH201217999003\", \"dateTime\": \"2017-02-25T19:10:20+00:00\", \"status\": \"ARRIVED_AT_CUST_LOCATION\" }";
        when(jobRepository.getjobById("RBH201217999003")).thenReturn(JobEntity.builder()
            .locationList(Arrays.asList(startAddress, endAddress))
            .callbackUrl("http://localhost").build());
        when(jobRepository.save(any(JobEntity.class))).thenReturn(JobEntity.builder().build());
        dataProcessUnderTest.processKafkaTopic(jsonReq);
        verify(serviceCall).callServiceUrl(eq("http://localhost"), any(String.class));
        verify(jobRepository,times(1)).save(any(JobEntity.class));
    }

    @Test
    void testProcessKafkaTopicOrderCancelled() throws Exception {
        String jsonReq="{\"riderId\":\"rd06\", \"jobId\": \"RBH201217999003\", \"dateTime\": \"2017-02-25T19:10:20+00:00\", \"status\": \"ORDER_CANCELLED_BY_OPERATOR\" }";
        when(jobRepository.getjobById("RBH201217999003")).thenReturn(JobEntity.builder().callbackUrl("http://localhost").build());
        when(jobRepository.save(any(JobEntity.class))).thenReturn(JobEntity.builder().build());
        dataProcessUnderTest.processKafkaTopic(jsonReq);
        verify(serviceCall,times(0)).callServiceUrl(eq("http://localhost"), any(String.class));
        verify(jobRepository,times(1)).save(any(JobEntity.class));
    }

    @Test
    void testProcessKafkaTopicUnknownStatus() throws Exception {
        String jsonReq="{\"riderId\":\"rd06\", \"jobId\": \"RBH201217999003\", \"dateTime\": \"2017-02-25T19:10:20+00:00\", \"status\": \"UNKNOWN\" }";
        when(jobRepository.getjobById("RBH201217999003")).thenReturn(JobEntity.builder().callbackUrl("http://localhost").build());
        dataProcessUnderTest.processKafkaTopic(jsonReq);
        verify(serviceCall, times(0)).callServiceUrl(eq("http://localhost"), any(String.class));
        verify(jobRepository,times(0)).save(any(JobEntity.class));
    }

    @Test
    void testConvertToJobStatus() {
        assertEquals(5, dataProcessUnderTest.convertToJobStatus(JobConstants.JOB_ACC));
        assertEquals(4, dataProcessUnderTest.convertToJobStatus(JobConstants.RID_NOT_FOU));
        assertEquals(6, dataProcessUnderTest.convertToJobStatus(JobConstants.CAL_MER));
        assertEquals(6, dataProcessUnderTest.convertToJobStatus(JobConstants.ARR_MER));
        assertEquals(7, dataProcessUnderTest.convertToJobStatus(JobConstants.MEA_PIC_UP));
        assertEquals(7, dataProcessUnderTest.convertToJobStatus(JobConstants.ARR_CUS_LOC));
        assertEquals(9, dataProcessUnderTest.convertToJobStatus(JobConstants.FOO_DEL));
        assertEquals(0, dataProcessUnderTest.convertToJobStatus("DUMMY"));
    }
    


}
