package com.scb.job.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.scb.job.model.request.JobManagementDashBoardResponseDto;
import com.scb.job.service.JobManagementDashBoardService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobManagementDashboardControllerTest {

	
	  @InjectMocks
	  private JobManagementDashboardController jobManagedController;

	  @Mock
	  private JobManagementDashBoardService jobManagedService;
	  
	  private static JobManagementDashBoardResponseDto dto;
	  
	  @BeforeAll
	  static void setUp() {
		  dto=JobManagementDashBoardResponseDto
				  .builder().activeJobCount(10L).build();
	  }
	  @Test
	  void getJobManagementDashBoardSummary_Success() {
	    when(jobManagedService.getJobManagementDashBoardSummary(anyString())).thenReturn(dto);
	    ResponseEntity<JobManagementDashBoardResponseDto> jobDetailsController =
	    		jobManagedController.getRiderManagementDashBoardSummary();
	    assertTrue(ObjectUtils.isNotEmpty(jobDetailsController.getBody()));
	    assertEquals(HttpStatus.OK, jobDetailsController.getStatusCode());
	  }
	  
}
