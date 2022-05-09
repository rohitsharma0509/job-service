package com.scb.job.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.constants.JobStatus;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.exception.JobCreationException;
import com.scb.job.exception.ResourceNotFoundException;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.model.response.JobConfirmResponse;
import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.JobReconciliationResponse;
import com.scb.job.model.response.RiderJobResponse;
import com.scb.job.repository.JobRepository;
import com.scb.job.service.JobService;
import com.scb.job.validation.JobRequestValidator;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(controllers = JobAllocationController.class)
@ActiveProfiles("test")
public class JobAllocationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private JobService jobService;

    @Mock
    JobRepository repository;

    @MockBean
    private JobRequestValidator jobRequestValidator;

    @MockBean
    private MeterRegistry meterRegistry;

    private NewJobRequest jobReqBad;
    private NewJobRequest jobReqGood;
    private JobDetail jobRes;
    private String jobId;
    public static JobDetail createResponse(){
        JobDetail jobDetail = new JobDetail();
        return jobDetail;
    }
    public static NewJobRequest createJob() {
        NewJobRequest job = new NewJobRequest();
        return job;
    }
    public static JobEntity createJobEntity(){
    	JobEntity jobEntity = new JobEntity();
        return jobEntity;
    }
    public static JobConfirmResponse createJobConfirmResponse() {
        JobConfirmResponse jobConfirmResponse = new JobConfirmResponse("2020-12-23T12:12:12:333+05:30");
        return jobConfirmResponse;
    }

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
        jobReqBad = createJob();
        jobReqGood = createJobWithFields();
        jobRes = createResponse();
        jobId="RBH201218330255";
    }

    @Test
     void controllerCreateJobWithEmptyFields() throws Exception{

        Mockito.when(jobService.createJob(jobReqBad)).thenReturn(jobRes);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jobReqBad);

        this.mvc.perform(post("/job/create-job")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .characterEncoding("utf-8"))
            .andExpect(status().isBadRequest())
            .andReturn();

    }
    @Test
     void controllerCreateJobWithFields() throws Exception{
        Mockito.when(jobService.createJob(jobReqGood)).thenReturn(jobRes);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jobReqGood);

        this.mvc.perform(post("/job/create-job")
            .contentType(MediaType.APPLICATION_JSON).content(json)
            .characterEncoding("utf-8"))
            .andExpect(status().is(201))
            .andReturn();

    }

    @Test
    void controllerCreateJobWithInvalidLocationFields() throws Exception{
        Location startAddress = new Location("name","start here",null,"1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        jobReqGood.setLocationList(locationList);
        Mockito.when(jobService.createJob(jobReqGood)).thenReturn(jobRes);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jobReqGood);

        MvcResult result = this.mvc.perform(post("/job/create-job")
            .contentType(MediaType.APPLICATION_JSON).content(json)
            .characterEncoding("utf-8"))
            .andExpect(status().is(400))
            .andReturn();
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertTrue(response.getErrorMessage().contains(".lat must not be blank"));

    }

    @Test
    void controllerCreateJobWithInvalidLocationAddressField() throws Exception{
        Location startAddress = new Location("name","","1.1","1.2","name","1234567890","19",1);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        jobReqGood.setLocationList(locationList);
        Mockito.when(jobService.createJob(jobReqGood)).thenReturn(jobRes);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jobReqGood);

        MvcResult result = this.mvc.perform(post("/job/create-job")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .characterEncoding("utf-8"))
                .andExpect(status().is(400))
                .andReturn();
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertTrue(response.getErrorMessage().contains(".address must not be blank"));

    }

    @Test
    void controllerCreateJobWithInvalidSeqInLocation() throws Exception{
        Location startAddress = new Location("name","start","1.1","1.2","name","1234567890","19",null);
        Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",2);
        List<Location> locationList = new ArrayList<>();
        locationList.add(startAddress);
        locationList.add(endAddress);
        jobReqGood.setLocationList(locationList);
        Mockito.when(jobService.createJob(jobReqGood)).thenReturn(jobRes);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jobReqGood);

        MvcResult result = this.mvc.perform(post("/job/create-job")
            .contentType(MediaType.APPLICATION_JSON).content(json)
            .characterEncoding("utf-8"))
            .andExpect(status().is(400))
            .andReturn();
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertTrue(response.getErrorMessage().contains("locationList[0].seq must not be null"));

    }


    @Test
    void getRiderById() throws Exception{
       Mockito.when(jobService.getJobById(jobId)).thenReturn(createJobEntity());
       this.mvc.perform(get("/job/"+jobId)
           .contentType(MediaType.APPLICATION_JSON)
           .characterEncoding("utf-8"))
           .andExpect(status().is(200))
           .andReturn();
    }

    @Test
    void getJobByIdWhenInternalServerError() throws Exception{
        Mockito.when(jobService.getJobById(jobId)).thenThrow(new JobCreationException());
        mvc.perform(get("/job/"+jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }

    @Test
    void getRiderById_RDTC_291_WhenJobIdIsNull() throws Exception{
        final String jobId = "null";
        when(jobService.getJobById(jobId)).thenThrow(new ResourceNotFoundException("Rider jobId null not found"));
        this.mvc.perform(get("/job/"+jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NOT_FOUND")))
                .andExpect(jsonPath("$.errorMessage", is("Rider jobId null not found")));
    }

    @Test
    void getRiderById_RDTC_292_WhenJobIdIsEmpty() throws Exception{
        final String jobId = "";
        when(jobService.getJobById(jobId)).thenThrow(new ResourceNotFoundException("Rider jobId null not found"));
        this.mvc.perform(get("/job/"+jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("103")))
                .andExpect(jsonPath("$.errorMessage", is("from parameter is missing")));
    }

    @Test
    void getRiderById_RDTC_293_WhenNotSendJobId() throws Exception{
        final String jobId = "";
        when(jobService.getJobById(jobId)).thenThrow(new ResourceNotFoundException("Rider jobId null not found"));
        this.mvc.perform(get("/job/")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("103")))
                .andExpect(jsonPath("$.errorMessage", is("from parameter is missing")));
    }

    @Test
    void getRiderById_RDTC_294_WhenJobIdIsSpace() throws Exception{
        final String jobId = " ";
        when(jobService.getJobById(jobId)).thenThrow(new ResourceNotFoundException("Rider jobId not found"));
        this.mvc.perform(get("/job/"+jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NOT_FOUND")))
                .andExpect(jsonPath("$.errorMessage", is("Rider jobId not found")));
    }

    @Test
    void getJobConfirmation() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(jobService.confirmJobByJobId(jobId)).thenReturn(createJobConfirmResponse());
        MvcResult result  = this.mvc.perform(put("/job/"+jobId+"/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().is(200))
                .andReturn();
        JobConfirmResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), JobConfirmResponse.class);
        assertNotNull(response.getConfirmTime());
    }
    
    @Test
    void getJobByRiderIdStatus_Success() throws Exception{
  	  String riderId="rider190";
  	  ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(jobService.getJobByRiderIdStatus(anyString())).thenReturn(getRiderJobResponse(riderId));
        MvcResult result  = this.mvc.perform(get("/job/running/rider/"+riderId)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8"))
            .andExpect(status().is(200))
            .andReturn();
        RiderJobResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), RiderJobResponse.class);
        assertEquals(riderId, response.getRiderId());
    }

    @Test
    void testGetJobDetails() throws Exception {
        Mockito.when(jobService.getJobDetails(any(), any(), eq(true), eq("FOOD_DELIVERED"), eq(EvBikeVendors.ETRAN), eq(true))).thenReturn(Arrays.asList(getJobDetail()));
        MvcResult result  = mvc.perform(get("/job")
                .param("from", "2021-03-23T12:41:35")
                .param("to", "2021-03-23T12:41:35")
                .param("isEvBikeRider", "true")
                .param("jobStatus", "FOOD_DELIVERED")
                .param("evBikeVendor", String.valueOf(EvBikeVendors.ETRAN))
                .param("rentingToday", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().is(200))
                .andReturn();
        List<JobDetail> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<List<JobDetail>>(){});
        assertEquals("RR00001", response.get(0).getRiderId());
        assertFalse(response.get(0).getEvBikeUser());
    }

    @Test
    void testUpdateEwtAmount() throws Exception {
        Mockito.doNothing().when(jobService).updateExcessiveWaitTimeAmount(anyString(), any());
        ExcessiveWaitingTimeDetailsEntity ewtRequest = ExcessiveWaitingTimeDetailsEntity.builder()
                .excessiveWaitTopupAmount(10.0).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(ewtRequest);
        mvc.perform(put("/job/"+jobId+"/ewt")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .characterEncoding("utf-8"))
                .andExpect(status().is(200))
                .andReturn();
        Mockito.verify(jobService, Mockito.times(1)).updateExcessiveWaitTimeAmount(anyString(), any());
    }

    @Test
    void testGetJobsToReconcile() throws Exception {
        JobEntity jobEntity = JobEntity.builder().jobId("1").netPrice(10.0).netPaymentPrice(10.0).taxAmount(0.0)
                .jobStatusKey(JobStatus.FOOD_DELIVERED.name()).build();
        Mockito.when(jobService.getJobsToReconciliation(any(), any(), anyList(), any(Pageable.class))).thenReturn(Arrays.asList(jobEntity));
        MvcResult result  = mvc.perform(get("/job/reconciliation")
                .param("from", "2021-03-23T12:41:35")
                .param("to", "2021-03-23T12:41:35")
                .param("jobType", JobType.FOOD.name())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().is(200))
                .andReturn();
        List<JobReconciliationResponse> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<JobReconciliationResponse>>(){});
        assertEquals("1", response.get(0).getJobId());
    }

    private JobDetail getJobDetail() {
        return JobDetail.builder().evBikeUser(false).riderId("RR00001").build();
    }
    
      private static RiderJobResponse getRiderJobResponse(String riderId) {
      	RiderJobResponse response=RiderJobResponse.builder()
      			.riderId(riderId).build();
      	return response;
      }

    @Test
    void getRunningJobDetailsTest() throws Exception{
        String jobId="SA12";
        RiderJobResponse res =RiderJobResponse.builder()
                .jobId(jobId).build();
        Mockito.when(jobService.getRunningJobDetails(jobId)).thenReturn(res);
        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result  = this.mvc.perform(get("/job/running/rider/job/"+jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().is(200))
                .andReturn();
        RiderJobResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), RiderJobResponse.class);
        assertEquals(jobId, response.getJobId());
    }

    @Test
    void createNewJobAndReturnInternalServerError() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jobReqGood);

        doThrow(new JobCreationException()).when(jobRequestValidator).validateNewJobRequest(any());
        mvc.perform(post("/job/create-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }
}
