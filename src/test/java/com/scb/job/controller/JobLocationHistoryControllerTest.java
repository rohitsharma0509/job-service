package com.scb.job.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobLocationUpdatedBy;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocationHistory;
import com.scb.job.model.request.JobLocationUpdateDto;
import com.scb.job.model.response.DifferentialPrice;
import com.scb.job.model.response.SearchResponseDto;
import com.scb.job.service.JobLocationHistoryService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;



@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JobLocationHistoryControllerTest {

  @InjectMocks
  private JobLocationHistoryController jobLocationHistoryController;

  @Mock
  private JobLocationHistoryService jobLocationHistoryService;

  private static JobLocationHistory jobLocationHistory;

  private static List<JobLocationHistory> jobLocationHistoryList;

  @BeforeAll
  static void setUp() {
    jobLocationHistoryList = new ArrayList<JobLocationHistory>();

    jobLocationHistory = JobLocationHistory.builder().jobId("S201145632").riderId("RR15346").oldLat("100.755332")
        .oldLong("13.45675").jobNetPrice(66.0).differenceAmount(10.0).netPaymentPrice(65.0)
        .updateLocationType("MERCHANT").newLat("100.955332").newLong("13.55675").rePinNetPrice(76.0)
        .build();

    jobLocationHistoryList.add(jobLocationHistory);

  }

  @Test
  void getJobLocationHistoryByIdTest() {

    when(jobLocationHistoryService.getJobLocationHistoryById(anyString()))
        .thenReturn(jobLocationHistoryList);

    ResponseEntity<List<JobLocationHistory>> jobLocationHistoryList =
        jobLocationHistoryController.getJobLocationHistoryById("S201145632");
    
    assertTrue(ObjectUtils.isNotEmpty(jobLocationHistoryList.getBody()));
    assertEquals(HttpStatus.OK, jobLocationHistoryList.getStatusCode());
  }

  @Test
  void getMerchantCustomerUpdateLocationWithNewPriceTest() {

    when(jobLocationHistoryService.getUpdatedJobLocation(anyString(), any()))
        .thenReturn(jobLocationHistory);

    ResponseEntity<JobLocationHistory> jobLocationHistoryControllerResponse =
        jobLocationHistoryController.getMerchantCustomerUpdateLocationWithNewPrice("S201145632",
            JobLocationUpdatedBy.MERCHANT);

    assertEquals(jobLocationHistoryControllerResponse.getBody(), jobLocationHistory);
    assertTrue(ObjectUtils.isNotEmpty(jobLocationHistoryControllerResponse.getBody()));
    assertEquals(HttpStatus.OK, jobLocationHistoryControllerResponse.getStatusCode());
  }

  @Test
  void updateMerchantCustomerLocationTest() {

    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder().action("CONFIRM")
        .addressType(JobLocationUpdatedBy.MERCHANT).build();
    when(jobLocationHistoryService.updateLocation(anyString(), any()))
        .thenReturn(jobLocationHistory);

    ResponseEntity<JobLocationHistory> jobLocationHistoryControllerResponse =
        jobLocationHistoryController.updateMerchantCustomerLocation("S201145632", JobConstants.OPS_MEMBER, jobLocationUpdateDto);

    assertEquals(jobLocationHistoryControllerResponse.getBody(), jobLocationHistory);
    assertTrue(ObjectUtils.isNotEmpty(jobLocationHistoryControllerResponse.getBody()));
    assertEquals(HttpStatus.OK, jobLocationHistoryControllerResponse.getStatusCode());
  }

  @Test
  void updateMerchantCustomerLocationJobIdNullTest() {

    JobLocationUpdateDto jobLocationUpdateDto = JobLocationUpdateDto.builder().action("CONFIRM")
            .addressType(JobLocationUpdatedBy.MERCHANT).build();

    Exception exception = assertThrows(NullPointerException.class, () -> {
      jobLocationHistoryController.updateMerchantCustomerLocation(null, JobConstants.OPS_MEMBER, jobLocationUpdateDto);
    });
    assertEquals("jobId is marked non-null but is null", exception.getMessage());
  }

  @Test
  void DifferentialPriceTest() {
    DifferentialPrice differentialPrice = DifferentialPrice.builder().jobId("S201145632")
        .offlinePaymentAmount(0.0).riderId("RR15346").build();

    when(jobLocationHistoryService.getJobLocationHistoryById(anyString()))
        .thenReturn(jobLocationHistoryList);

    ResponseEntity<DifferentialPrice> jobLocationHistoryControllerResponse =
        jobLocationHistoryController.getCustomerPaymentPrice("S201145632");

    assertEquals(jobLocationHistoryControllerResponse.getBody(), differentialPrice);
    assertTrue(ObjectUtils.isNotEmpty(jobLocationHistoryControllerResponse.getBody()));
    assertEquals(HttpStatus.OK, jobLocationHistoryControllerResponse.getStatusCode());

  }
}
