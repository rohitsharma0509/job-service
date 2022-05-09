package com.scb.job.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;

@ExtendWith(MockitoExtension.class)
public class JobHelperTest {

  private JobHelper jobHelper = new JobHelper();

  private JobHelper jobHelperUnderTest;

  @BeforeEach
  void setUp() {
      jobHelperUnderTest = new JobHelper();
  }

  @Test
  public void testJobIdGeneration(){
    String date = new SimpleDateFormat("yyMMdd").format(new Date());
    String jobId = jobHelper.getJobId();
    assertEquals(13, jobId.length());
    assertTrue(jobId.startsWith("S"));
    assertEquals(date ,jobId.substring(1,7));
    assertTrue(StringUtils.isNumeric(jobId.substring(7,13)));

  }

  @Test
  public void testJobIdGenWithArgs(){
    String jobId = JobHelper.getJobId(100001, "3");
    assertEquals(13, jobId.length());
  }

  @Test
  public void testJobIdGenWithArgsPointX(){
    String jobId = JobHelper.getJobId(100001, "4");
    assertEquals(13, jobId.length());
  }

  @Test
  public void testJobIdGenerationBkkTime(){
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
    String date = sdf.format(new Date());
    String jobId = JobHelper.getJobId(123, "3");
    assertEquals(13, jobId.length());
    assertTrue(jobId.startsWith("S"));
    assertEquals(date ,jobId.substring(1,7));
    assertTrue(StringUtils.isNumeric(jobId.substring(7,13)));

  }

  @Test
  public void testBuildDriverImageUrl(){
    String driverUrl = JobHelper.buildDriverImageUrl("http://basepath", "rider1");
    assertEquals("http://basepath/rider/api/get_driver_image/rider1", driverUrl);
  }

  @Test
  void testGetJobId() {
      String result = jobHelperUnderTest.getJobId();
      assertNotNull(result);
  }

  @Test
  void testGetJobStatusList() {
  	List<String> list=Arrays.asList(new String[] {"activejobs","completedjobs","alljobs"});
  	for(String s: list) {
	        List<Integer> activejobs = JobHelper.getJobStatusList(s);
	        assertTrue(activejobs.size()>=1);
  	}
    List<Integer> activejobs = JobHelper.getJobStatusList("");
    assertTrue(activejobs.isEmpty());
  }

  @Test
  void testGetJob_InvalidStatusList() {
  	List<String> list=Arrays.asList(new String[] {"abc"});
  	for(String s: list) {
	        List<Integer> activejobs = JobHelper.getJobStatusList(s);
	        assertTrue(activejobs.isEmpty());
  	}
  }



  @Test
  void testGetLocation() {
/*       JobEntity jobEntity = new JobEntity("id1", "jobId1", "jobDate1", LocalDate.of(2020, 1, 1),
               "customerName", "merchantName", "netPriceSearch", "orderId", 0, "jobStatusKey", "jobStatusEn", "jobStatusTh",
               "jobDesc", "startTime", "finishTime", false, "jobType", "option", 0.0, 0.0, 0.0, "remark", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
               , "callbackUrl", "riderId", "driverId", "driverName", "driverPhone", "driverImageUrl",
               0.0, "trackingUrl", "creationDateTime", "lastUpdatedDateTime", "zoneName", 0, "jobAcceptedTime", "calledMerchantTime",
               "arrivedAtMerchantTime", "mealPickedUpTime", "arrivedAtCustLocationTime", "foodDeliveredTime", "orderCancelledByOperationTime",
               "parkingReceiptPhotoTime", "mealPhotoUrl", "mealDeliveredPhotoUrl", Arrays.asList(new OrderItems("name", 0)),
               "riderNotFoundTime", false, LocalDateTime.of(2020, 1, 1, 0, 0, 0),
               false, false, false,5000, "creationDateTimeTh", "lastUpdatedDateTimeTh", 0.0, 0.0);*/
    JobEntity jobEntity = JobEntity.builder()
            .locationList(Arrays.asList(new JobLocation(0, "type", "addressId", "addressName", "address",
                    "lat", "lng", "contactName", "contactPhone", "actualArriveTime",
                    "mail", "subdistrict")))
            .id("id1")
            .remark("remark")
            .jobId("jobId1")
            .build();

       JobLocation expectedResult = new JobLocation(0, "type", "addressId", "addressName",
               "address", "lat", "lng", "contactName", "contactPhone",
               "actualArriveTime", "mail", "subdistrict");
       JobLocation result = JobHelper.getLocation(jobEntity, 0);
       assertEquals(expectedResult, result);
  }
}
