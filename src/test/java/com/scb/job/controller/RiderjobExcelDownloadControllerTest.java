package com.scb.job.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.ObjectUtils;
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
import static org.mockito.ArgumentMatchers.anyString;
import com.scb.job.entity.DocumentType;
import com.scb.job.service.JobSearchService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RiderjobExcelDownloadControllerTest {

	@InjectMocks
	RiderjobExcelDownloadController jobExcelDownloadController;
	
	@Mock
	private  JobSearchService jobSearchService;

	  @Test
	  void getjobBySearchTerm_Success() {
		String riderId="rider190";
		byte[] resp=riderId.getBytes();
	    when(jobSearchService.excelDownloader(anyString())).thenReturn(resp);
	    ResponseEntity<byte[]> jobDetailsController =
	    		jobExcelDownloadController.getjobBySearchTerm(riderId, DocumentType.RIDER_JOB_DETAILS);
	    assertTrue(ObjectUtils.isNotEmpty(jobDetailsController.getBody()));
	    assertEquals(HttpStatus.OK, jobDetailsController.getStatusCode());
	  }
	  
}
