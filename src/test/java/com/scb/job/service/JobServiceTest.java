package com.scb.job.service;

import com.scb.job.constants.JobConstants;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.exception.JobCreationException;
import com.scb.job.exception.ResourceNotFoundException;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.model.response.*;
import com.scb.job.repository.JobRepository;
import com.scb.job.repository.SequenceRespository;
import com.scb.job.service.proxy.*;
import com.scb.job.service.proxy.helper.PointXCustomerPriceProxyHelper;
import com.scb.job.service.proxy.helper.PointXEstimatePriceProxyHelper;
import com.scb.job.util.JobHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.scb.job.constants.JobConstants.JOB_DESC_THAI_FROM;
import static com.scb.job.constants.JobConstants.JOB_DESC_THAI_TO;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

    private static final String RIDER_ID = "RR0001";

    @Mock
    JobRepository repository;
    

    @Mock
    EstimatePriceProxy estimatePriceProxy;

    @Mock
    CustomerPricingProxy customerPricingProxy;

    @Mock
    OperationsServiceProxy operationsServiceProxy;

    
    @Mock
    LocationProxy locationProxy;

    @Mock
    JobHelper jobHelper;

    @InjectMocks
    private JobServiceImpl jobService;

    @Mock
    private SequenceRespository sequenceRespository;

    @Mock
    private PointXCustomerPriceProxyHelper pointXCustomerPriceProxyHelper;

    @Mock
    private PointXEstimatePriceProxyHelper pointXEstimatePriceProxyHelper;

    private NewJobRequest job;

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

    }
    @Test
    public void passingAllArguments() {
        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("Tower A the 3 floor\\n 1 abc");
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<JobLocation> jobLocations = new ArrayList<>();
        ConfigData configData= new ConfigData("1234", "minimumDistanceForJobCompletion", "500");
        JobEntity entity = JobEntity.builder()
                .jobId("12")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .jobType(job.getJobType())
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .remark("good")
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .rating(null)
                .locationList(jobLocations)
                .build();
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
            EstimatePriceProxyResponse.builder()
                .discount(0.0)
                .netPrice(10.0)
                .normalPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .build());

        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .discount(0.0)
                        .distance(15.0)
                        .tripDuration(5)
                        .build());
        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
            ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        Mockito.when(repository.save(any())).thenReturn(entity);
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(configData);
        JobDetail jobDetail = jobService.createJob(job);
        assertEquals(jobDetail.getJobDate(),job.getJobDate());
        assertEquals(0.0, jobDetail.getDiscount());
        assertEquals(10.0, jobDetail.getNetPrice());
        assertEquals(10.0, jobDetail.getNormalPrice());
        assertEquals(9.0, jobDetail.getNetPaymentPrice());
        assertEquals(1.0, jobDetail.getTaxAmount());
    }

    @Test
    public void passingBlankRemark() {
        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("");
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<JobLocation> jobLocations = new ArrayList<>();
        ConfigData configData= new ConfigData("1234", "minimumDistanceForJobCompletion", "500");
        JobEntity entity = JobEntity.builder()
                .jobId("12")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .jobType(job.getJobType())
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .remark("good")
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .rating(null)
                .locationList(jobLocations)
                .build();
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
            EstimatePriceProxyResponse.builder()
                .discount(0.0)
                .netPrice(10.0)
                .normalPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .build());

        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .build());
        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
                ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        Mockito.when(repository.save(any())).thenReturn(entity);
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(configData);
        JobDetail jobDetail = jobService.createJob(job);
        assertEquals(jobDetail.getJobDate(),job.getJobDate());
        assertEquals(0.0, jobDetail.getDiscount());
        assertEquals(10.0, jobDetail.getNetPrice());
        assertEquals(10.0, jobDetail.getNormalPrice());
        assertEquals(9.0, jobDetail.getNetPaymentPrice());
        assertEquals(1.0, jobDetail.getTaxAmount());
    }

    @Test
    public void passingNoDDFlag() {
        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("");
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<JobLocation> jobLocations = new ArrayList<>();
        ConfigData configData= new ConfigData("1234", "minimumDistanceForJobCompletion", "500");
        JobEntity entity = JobEntity.builder()
                .jobId("12")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .jobType(job.getJobType())
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .remark("good")
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .rating(null)
                .locationList(jobLocations)
                .build();
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
                EstimatePriceProxyResponse.builder()
                        .discount(0.0)
                        .netPrice(10.0)
                        .normalPrice(10.0)
                        .netPaymentPrice(9.0)
                        .taxAmount(1.0)
                        .build());

        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .build());

        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
                ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        Mockito.when(repository.save(any())).thenReturn(entity);
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(configData);
        JobDetail jobDetail = jobService.createJob(job);
        assertEquals(jobDetail.getJobDate(),job.getJobDate());
        assertEquals(0.0, jobDetail.getDiscount());
        assertEquals(10.0, jobDetail.getNetPrice());
        assertEquals(10.0, jobDetail.getNormalPrice());
        assertEquals(9.0, jobDetail.getNetPaymentPrice());
        assertEquals(1.0, jobDetail.getTaxAmount());
    }

    @Test
    public void passingDDFlagWithValueAsTrue() {
        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setDdFlag(true);
        job.setJobType("3");
        job.setLocationList(locationList);
        job.setRemark("");
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<JobLocation> jobLocations = new ArrayList<>();
        ConfigData configData= new ConfigData("1234", "minimumDistanceForJobCompletion", "500");
        JobEntity entity = JobEntity.builder()
                .jobId("12")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .jobType(job.getJobType())
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .remark("good")
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(12.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .rating(null)
                .locationList(jobLocations)
                .build();
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
                EstimatePriceProxyResponse.builder()
                        .discount(0.0)
                        .netPrice(10.0)
                        .normalPrice(10.0)
                        .netPaymentPrice(9.0)
                        .taxAmount(1.0)
                        .build());
        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .build());
        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
                ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        Mockito.when(repository.save(any())).thenReturn(entity);
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(configData);
        JobDetail jobDetail = jobService.createJob(job);
        assertEquals(jobDetail.getJobDate(),job.getJobDate());
        assertEquals(0.0, jobDetail.getDiscount());
        assertEquals(12.0, jobDetail.getNetPrice());
        assertEquals(10.0, jobDetail.getNormalPrice());
        assertEquals(9.0, jobDetail.getNetPaymentPrice());
        assertEquals(1.0, jobDetail.getTaxAmount());
    }

    @Test
    public void savingDataException() {

        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("Tower A the 3 floor\n xyz 2\n pqr 4\n pqr\n pqr abcd 4");
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
            EstimatePriceProxyResponse.builder()
                .discount(0.0)
                .netPrice(10.0)
                .normalPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .build());

        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .build());

        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
            ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        doThrow(new IllegalArgumentException()).when(repository).save(any());
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(new ConfigData("1234", "minimumDistanceForJobCompletion", "500"));
        assertThrows(JobCreationException.class, () -> {
            jobService.createJob(job);
        });
    }
    
    
    @Test
    public void savingDataWithBlankRemarkException() {

        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("Tower A the 3 floor\n 2 abc");
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
            EstimatePriceProxyResponse.builder()
                .discount(0.0)
                .netPrice(10.0)
                .normalPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .build());

        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .build());

        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
            ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        doThrow(new IllegalArgumentException()).when(repository).save(any());
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(new ConfigData("1234", "minimumDistanceForJobCompletion", "500"));
        assertThrows(JobCreationException.class, () -> {
            jobService.createJob(job);
        });
    }
    
    @Test
    void testFindById(){
        when(repository.getjobById("JobId")).thenReturn(getJob());
        JobEntity job = jobService.getJobById("JobId");
        assertEquals("JobId", job.getJobId());
    }

    @Test
    void testFindByIdNotFound(){
        when(repository.getjobById("RH202012250921")).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(
        		ResourceNotFoundException.class, () -> jobService.getJobById("RH202012250921"));
        assertEquals(String.format("Rider jobId %s not found", "RH202012250921"),   exception.getMessage());
    }
    
    private static JobEntity getJob() {
    	List<JobLocation> jobLocationList = new ArrayList<>();
        JobLocation jobLocation1 = JobLocation.builder().seq(1).build();
        JobLocation jobLocation2 = JobLocation.builder().seq(2).build();
        jobLocationList.add(jobLocation1);
        jobLocationList.add(jobLocation2);        
    	JobEntity je=JobEntity.builder().jobId("JobId").jobType("2")
    			.locationList(jobLocationList).riderId(RIDER_ID).build();
    	return je;
    }

    @Test
    void testConfirmJobByJobId() {
        when(repository.getjobById("JobId")).thenReturn(getJob());
        JobConfirmResponse merchantConfirmDateTime = jobService.confirmJobByJobId("JobId");
        try {
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .parse(merchantConfirmDateTime.getConfirmTime());
        }catch (Exception ex){
            fail();
        }
    }

    @Test
    void getJobByRiderIdStatus_Success(){
    	String riderId="rider190";
        when(repository.getJobByRiderIdStatus(anyList(), anyString())).thenReturn(getRiderLists(riderId));
        RiderJobResponse response = jobService.getJobByRiderIdStatus(riderId);
        assertEquals(riderId, response.getRiderId());
        assertEquals("job1", response.getJobId());
    }
    
    
    private static List<JobEntity> getRiderLists(String riderId) {
    	JobLocation location1= new JobLocation();
    	location1.setSeq(1);
      	JobLocation location2= new JobLocation();
      	location2.setSeq(2);
    	List<JobLocation> locaList= new ArrayList<JobLocation>();
    	locaList.add(location1);locaList.add(location2);
    	JobEntity jent=JobEntity.builder().riderId(riderId).driverId(riderId).jobId("job1").
    				jobAcceptedTime("2021-02-04T17:45:55").locationList(locaList).build();
    	JobEntity jent1=JobEntity.builder().driverId(riderId+"1").jobId("job2")
    			.jobAcceptedTime("2021-02-04T16:25:45").locationList(locaList).build();
    	List<JobEntity> list=new ArrayList<JobEntity>();
    	list.add(jent);list.add(jent1);
    	return list;
    }

    @Test
    public void createJob_WithCallToOpServiceFailed_AndNullMinimumJobCompletionDistance() {
        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("Tower A the 3 floor\\n 1 abc");
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<JobLocation> jobLocations = new ArrayList<>();
        ConfigData configData= new ConfigData("1234", "minimumDistanceForJobCompletion", "500");
        JobEntity entity = JobEntity.builder()
                .jobId("12")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .jobType(job.getJobType())
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .remark("good")
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .rating(null)
                .locationList(jobLocations)
                .build();
        Mockito.when(estimatePriceProxy.getEstimatedPrice(any())).thenReturn(
            EstimatePriceProxyResponse.builder()
                .discount(0.0)
                .netPrice(10.0)
                .normalPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .build());

        Mockito.when(customerPricingProxy.getCalculatedNetPrice(any())).thenReturn(
                CustomerPricingProxyResponse.builder()
                        .netPrice(12.0)
                        .normalPrice(12.0)
                        .build());

        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
            ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        Mockito.when(repository.save(any())).thenReturn(entity);
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenThrow(RuntimeException.class);
        JobDetail jobDetail = jobService.createJob(job);
        assertEquals(jobDetail.getJobDate(),job.getJobDate());
        assertEquals(0.0, jobDetail.getDiscount());
        assertEquals(10.0, jobDetail.getNetPrice());
        assertEquals(10.0, jobDetail.getNormalPrice());
        assertEquals(9.0, jobDetail.getNetPaymentPrice());
        assertEquals(1.0, jobDetail.getTaxAmount());
        assertNull(jobDetail.getMinDistanceForJobCompletion());
    }

    @Test
    void testGetJobDetails() {
        Mockito.when(repository.findJobDetails(any(), any(), eq(Boolean.FALSE), eq("FOOD_DELIVERED"), eq(EvBikeVendors.ETRAN), eq(true))).thenReturn(Arrays.asList(getJob()));
        List<JobDetail> result = jobService.getJobDetails(LocalDateTime.now(), LocalDateTime.now(), Boolean.FALSE, "FOOD_DELIVERED", EvBikeVendors.ETRAN, Boolean.TRUE);
        assertEquals(RIDER_ID, result.get(0).getRiderId());
    }

    @Test
    void testGetJobsToReconciliation() {
        JobEntity jobEntity = JobEntity.builder().riderId(RIDER_ID).build();
        Mockito.when(repository.getJobsToReconciliation(any(LocalDateTime.class), any(LocalDateTime.class), anyList(),
                any(Pageable.class))).thenReturn(Arrays.asList(jobEntity));
        Pageable pageable = PageRequest.of(1, 50);
        List<JobEntity> result = jobService.getJobsToReconciliation(LocalDateTime.now(), LocalDateTime.now(), Arrays.asList(JobType.FOOD), pageable);
        assertEquals(RIDER_ID, result.get(0).getRiderId());
    }
    
    @Test
    void testGetRunningJobDetails() {
        Mockito.when(repository.getjobById(anyString())).thenReturn(getJob());

        RiderJobResponse result = jobService.getRunningJobDetails(anyString());
        assertNotNull(result);
    }
    
    @Test
    void testGetRunningJobEmptyDetails() {
        Mockito.when(repository.getjobById(anyString())).thenReturn(null);
      
        assertThrows(ResourceNotFoundException.class, ()->jobService.getRunningJobDetails(anyString()));
    }

    @Test
    void updateExcessiveWaitTimeAmount() {
        ExcessiveWaitingTimeDetailsEntity ewtDetails = ExcessiveWaitingTimeDetailsEntity.builder()
                .excessiveWaitTopupAmount(10.0).excessiveWaitTopupDateTime(LocalDateTime.now()).build();
        jobService.updateExcessiveWaitTimeAmount("123", ewtDetails);
        Mockito.verify(repository, Mockito.times(1)).updateExcessiveWaitTimeAmount(anyString(), any(ExcessiveWaitingTimeDetailsEntity.class));
    }
    @Test
    public void createJobWhenJobTypeIsPointX() {

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
        job.setJobType("4");
        job.setCallbackUrl("url here");
        job.setLocationList(null);
        job.setPaymentType("cash");
        job.setGoodsValue("2");

        Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        job.setLocationList(locationList);
        job.setRemark("Tower A the 3 floor\\n 1 abc");
        AddressResponse addressResponse = AddressResponse.builder().subDistrict("sub district").build();
        List<JobLocation> jobLocations = new ArrayList<>();
        ConfigData configData= new ConfigData("1234", "minimumDistanceForJobCompletion", "500");
        JobEntity entity = JobEntity.builder()
                .jobId("12")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .jobType(job.getJobType())
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .remark("good")
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .goodsValue(2.0)
                .rating(null)
                .locationList(jobLocations)
                .build();
        Mockito.when(pointXEstimatePriceProxyHelper.callPointXEstimatePricing(any())).thenReturn(
                PointXEstimatePricingResponse.builder()
                        .discount(0.0)
                        .netPrice(10.0)
                        .normalPrice(10.0)
                        .distance(10.0)
                        .build());

        Mockito.when(pointXCustomerPriceProxyHelper.callPointXCustomerPricing(any())).thenReturn(
                PointXCustomerPricingResponse.builder()
                        .discount(0.0)
                        .normalInsuredPrice(10.0)
                        .customerNormalPrice(10.0)
                        .tripDuration(1)
                        .distance(10.0)
                        .normalInsuredPrice(10.0)
                        .goodsValue(2.0)
                        .build());
        Mockito.when(locationProxy.getMerchantZone(any(String.class),any(String.class))).thenReturn(
                ZoneResponse.builder().zoneId(1).zoneName("Khlong").build());
        Mockito.when(locationProxy.getSubDistrict(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(addressResponse));
        Mockito.when(repository.save(any())).thenReturn(entity);
        when(sequenceRespository.getNextSequence()).thenReturn(1);
        Mockito.when(operationsServiceProxy.getConfiguredDataForKey(Mockito.anyString())).thenReturn(configData);
        JobDetail jobDetail = jobService.createJob(job);
        assertEquals(jobDetail.getJobDate(),job.getJobDate());
        assertEquals(0.0, jobDetail.getDiscount());
        assertEquals(10.0, jobDetail.getNetPrice());
        assertEquals(10.0, jobDetail.getNormalPrice());
        assertEquals(9.0, jobDetail.getNetPaymentPrice());
        assertEquals(1.0, jobDetail.getTaxAmount());
    }

    @Test
    public void passingRemarkFromMart() {
        List<JobLocation> jobLocations = new ArrayList<>();
        JobEntity entity = JobEntity.builder()
                .jobId("LOCAL0000001")
                .jobType("2")
                .remark("บ้านเลขที่ 99/99 ซอย 4 บ้านอยู่หลังสุดท้ายทางขวามือ\nslmตรงข้ามคิงพาวเวอร์ซอยรางน้ำ\nเนื้อวัว 1\nนมตราหมี 2\nมะม่วง 1\n")
                .jobDate(job.getJobDate())
                .jobStatus(JobConstants.INITIAL_JOB_STATUS)
                .jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
                .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH)
                .jobDesc(JOB_DESC_THAI_FROM + JOB_DESC_THAI_TO)
                .startTime(job.getStartTime())
                .finishTime(job.getFinishTime())
                .haveReturn(JobConstants.DEFAULT_HAVE_RETURN)
                .option(job.getOption())
                .totalDistance(null)
                .totalWeight(0.0)
                .totalSize(0.0)
                .userType(JobConstants.DEFAULT_USER_TYPE)
                .normalPrice(10.0)
                .netPrice(10.0)
                .netPaymentPrice(9.0)
                .taxAmount(1.0)
                .discount(0.0)
                .rating(null)
                .locationList(jobLocations)
                .build();

        JobEntity result = ReflectionTestUtils.invokeMethod(jobService, "parseRemark", entity.getRemark(), entity);

        assertNotNull(result);
        assertEquals("บ้านเลขที่ 99/99 ซอย 4 บ้านอยู่หลังสุดท้ายทางขวามือ", result.getRemark());
        assertEquals("ตรงข้ามคิงพาวเวอร์ซอยรางน้ำ", result.getShopLandmark());
        assertEquals(3, result.getOrderItems().size());
    }
}
