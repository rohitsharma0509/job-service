package com.scb.job.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.scb.job.exception.JobRequestFieldException;
import com.scb.job.exception.LocationDeliveryListException;
import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class JobRequestValidatorTest {

  private JobRequestValidator jobRequestValidator = new JobRequestValidator();



  @Test
  public void testJobTypeValidation(){
    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("5");
    JobRequestFieldException jobRequestFieldException = assertThrows(JobRequestFieldException.class,
        ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
    assertEquals("jobType supported values are 1,2,3,4", jobRequestFieldException.getMessage());

  }

  @Test
  public void passingIncompleteLocationList() {
    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("1");
    Location startAddress = new Location("name","start here","1.1","1.2","name","1234567890","19",1);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    newJobRequest.setLocationList(locationList);

    assertThrows(LocationDeliveryListException.class,
        ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
  }

  @Test
  public void passingEmptyLocationList() {
    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setLocationList(null);

    assertThrows(LocationDeliveryListException.class,
        ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
  }

  @Test
  public void testLatLngRangeValidationLatLess(){

    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("2");
    Location startAddress = new Location("name","start here","-93","1.2","name","1234567890","19",1);
    Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    locationList.add(endAddress);
    newJobRequest.setLocationList(locationList);

    JobRequestFieldException jobRequestFieldException = assertThrows(JobRequestFieldException.class,
            ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
    assertEquals("Invalid location values", jobRequestFieldException.getMessage());
  }

  @Test
  public void testLatLngRangeValidationLatGreater(){

    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("3");
    Location startAddress = new Location("name","start here","99","1.2","name","1234567890","19",1);
    Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    locationList.add(endAddress);
    newJobRequest.setLocationList(locationList);

    JobRequestFieldException jobRequestFieldException = assertThrows(JobRequestFieldException.class,
            ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
    assertEquals("Invalid location values", jobRequestFieldException.getMessage());
  }

  @Test
  public void testLatLngRangeValidationLngLess(){

    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("4");
    Location startAddress = new Location("name","start here","1.1","-199","name","1234567890","19",1);
    Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    locationList.add(endAddress);
    newJobRequest.setLocationList(locationList);

    JobRequestFieldException jobRequestFieldException = assertThrows(JobRequestFieldException.class,
            ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
    assertEquals("Invalid location values", jobRequestFieldException.getMessage());
  }

  @Test
  public void testLatLngRangeValidationLngGreater(){

    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("1");
    Location startAddress = new Location("name","start here","1.1","199","name","1234567890","19",1);
    Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    locationList.add(endAddress);
    newJobRequest.setLocationList(locationList);

    JobRequestFieldException jobRequestFieldException = assertThrows(JobRequestFieldException.class,
            ()-> jobRequestValidator.validateNewJobRequest(newJobRequest));
    assertEquals("Invalid location values", jobRequestFieldException.getMessage());
  }

  @Test
  public void testPassValidation(){

    NewJobRequest newJobRequest = createJobWithFields();
    newJobRequest.setJobType("4");
    Location startAddress = new Location("name","start here","1","1.2","name","1234567890","19",1);
    Location endAddress = new Location("name","end here","1.1","1.2","name","1234567890","19",1);
    List<Location> locationList = new ArrayList<>();
    locationList.add(startAddress);
    locationList.add(endAddress);
    newJobRequest.setLocationList(locationList);

    jobRequestValidator.validateNewJobRequest(newJobRequest);
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

}
