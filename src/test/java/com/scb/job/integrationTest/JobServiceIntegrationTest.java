package com.scb.job.integrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import com.scb.job.entity.JobEntity;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.request.NewJobRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Disabled
//Disabled for postgress seq. Fixing in master
public class JobServiceIntegrationTest extends AbstractRestApiIntegrationTest{

  public static final String VALID_REQUEST_PAYLOAD = "json/valid_request_payload.json";
  public static final String CREATE_JOB_PAYLOAD_LOCATION_EMPTY = "json/request_payload_location_empty.json";
  public static final String VALID_REQUEST_PAYLOAD_INVALID_REMARK = "json/valid_request_payload_invalid_remark.json";

  @Test
  public void testCreateNewJob() throws Exception {

    // prepare data and mock's behaviour
    NewJobRequest newJobRequest =
        getObjectFromJSONFile(VALID_REQUEST_PAYLOAD, NewJobRequest.class);

    String json = objectMapper.writeValueAsString(newJobRequest);
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .post("/job/create-job")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

    List<JobEntity> jobEntityList = jobRepository.findAll();
    assertEquals(1, jobEntityList.size());
    assertEquals("Tower A the 3 floor",jobEntityList.get(0).getRemark());

  }

  @Test
  public void testCreateNewJobLocationEmpty() throws Exception {

    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .post("/job/create-job")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(getDataFromFile(CREATE_JOB_PAYLOAD_LOCATION_EMPTY)))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");


  }

  @Test
  public void testCreateNewJob_InvalidRemark() throws Exception {

    // prepare data and mock's behaviour
    NewJobRequest newJobRequest =
        getObjectFromJSONFile(VALID_REQUEST_PAYLOAD_INVALID_REMARK, NewJobRequest.class);

    String json = objectMapper.writeValueAsString(newJobRequest);
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .post("/job/create-job")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

    List<JobEntity> jobEntityList = jobRepository.findAll();
    assertEquals(1, jobEntityList.size());

    assertEquals(0,jobEntityList.get(0).getOrderItems().size());

  }

  @Test
  public void testCreateNewJobMissingMandatoryField() throws Exception {

    // prepare data and mock's behaviour
    NewJobRequest newJobRequest =
        getObjectFromJSONFile(VALID_REQUEST_PAYLOAD, NewJobRequest.class);
    newJobRequest.setJobDate(null);

    String json = objectMapper.writeValueAsString(newJobRequest);
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .post("/job/create-job")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
    assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
    assertEquals(response.getErrorMessage(), "jobDate must not be blank", "mandatory field missing validation");

  }

  public static <T> T getObjectFromJSONFile(String fileName, Class<T> t) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
    String json = Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
    return objectMapper.readValue(json, t);
  }

  public static String getDataFromFile(String fileName) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());
    return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
  }



}

