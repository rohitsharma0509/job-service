package com.scb.job.service.proxy;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.OperationServiceException;

@ExtendWith(MockitoExtension.class)
class OperationsServiceProxyTest {

	private static final String MINIMUM_DISTANCE_FOR_JOB_COMPLETION = "minimumDistanceForJobCompletion";

	@InjectMocks
	private OperationsServiceProxy operationsServiceProxy;
	
	@Mock
	private RestTemplate restTemplate;
	
	private ObjectMapper objectMapper;
	
	private String operationServiceBasePath = "https://operations-service.api-env.com";
	
	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
		operationsServiceProxy = new OperationsServiceProxy(restTemplate, operationServiceBasePath, objectMapper);
	}
	
	@Test
	void testFetchMinimumDistanceForJobCompletion() {
		when(restTemplate.getForEntity(Mockito.anyString(), Mockito.any()))
			.thenReturn(new ResponseEntity<>(fetchMinimumDistanceForCompletion(), HttpStatus.OK));
		
		ConfigData configuredData = operationsServiceProxy.getConfiguredDataForKey(MINIMUM_DISTANCE_FOR_JOB_COMPLETION);
		
		assertNotNull(configuredData);
		assertEquals("500", configuredData.getValue());
		assertEquals(MINIMUM_DISTANCE_FOR_JOB_COMPLETION, configuredData.getKey());
	}
	
	@Test
	void testThrowsException() {
		String errorResponse = "{\"errorCode\":\"404\",\"errorMessage\":\"Failure\"}";
		when(restTemplate.getForEntity(Mockito.anyString(), Mockito.any()))
		.thenThrow( new HttpClientErrorException(HttpStatus.NOT_FOUND, 
				"Error", errorResponse.getBytes(), StandardCharsets.UTF_8));
		
		OperationServiceException configFetchException = assertThrows(
				OperationServiceException.class, () -> operationsServiceProxy.getConfiguredDataForKey(MINIMUM_DISTANCE_FOR_JOB_COMPLETION));

	    assertEquals("Failure", configFetchException.getErrorMessage());
	}

	private ConfigData fetchMinimumDistanceForCompletion() {
		return new ConfigData("1234", MINIMUM_DISTANCE_FOR_JOB_COMPLETION, "500" );
	}

}
