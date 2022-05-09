package com.scb.job.service;

import com.scb.job.config.RBHConfiguration;
import com.scb.job.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCallTest {
	
	@Mock
    private ServiceCall serviceCallUnderTest;

	@Mock
	RestTemplate restTemplate;

	@Mock
	HttpEntity<String> entity;

	@Mock
  RBHConfiguration rbhConfiguration;
	
    @BeforeEach
    void setUp() {
        serviceCallUnderTest = new ServiceCall(restTemplate, rbhConfiguration);
    }

	@Test
    void testCallServiceUrl() {
      String url="http://localhost";
    when(rbhConfiguration.getApikey()).thenReturn("apikey");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-api-key", "apikey");
		entity = new HttpEntity<>("{\"job_id\":\"J20100595771\",\"status_before\":5,\"status_after\":6,\"callback_desc\":\"\",\"status_datetime\":\"2020-06-01 15:21:08\",\"seq\":1}",
        headers);
	    ResponseEntity<Void> res = new ResponseEntity<Void>(headers, HttpStatus.OK);
	    when(restTemplate.postForEntity(eq(url), eq(entity),  Matchers.<Class<Void>> any())).thenReturn(res);
		serviceCallUnderTest.callServiceUrl(url, "{\"job_id\":\"J20100595771\",\"status_before\":5,\"status_after\":6,\"callback_desc\":\"\",\"status_datetime\":\"2020-06-01 15:21:08\",\"seq\":1}");
		verify(restTemplate, times(1)).postForEntity(eq(url), any(HttpEntity.class) , any());
    }

  @Test
  void testCallServiceUrlResourceNotFound() {
    String url="http://localhost";
    when(rbhConfiguration.getApikey()).thenReturn("apikey");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-api-key", "apikey");
    entity = new HttpEntity<>("{\"job_id\":\"J20100595771\",\"status_before\":5,\"status_after\":6,\"callback_desc\":\"\",\"status_datetime\":\"2020-06-01 15:21:08\",\"seq\":1}",
        headers);
    ResponseEntity<Void> res = new ResponseEntity<Void>(headers, HttpStatus.NOT_FOUND);
    when(restTemplate.postForEntity(eq(url), eq(entity),  Matchers.<Class<Void>> any())).thenReturn(res);
    assertThrows(ResourceNotFoundException.class, ()-> serviceCallUnderTest.callServiceUrl(url, "{\"job_id\":\"J20100595771\",\"status_before\":5,\"status_after\":6,\"callback_desc\":\"\",\"status_datetime\":\"2020-06-01 15:21:08\",\"seq\":1}"));
    verify(restTemplate, times(1)).postForEntity(eq(url), any(HttpEntity.class) , any());
  }

    @Test
    void testRecover1() {
        ResourceNotFoundException t = new ResourceNotFoundException("exception message");
        serviceCallUnderTest.recover(t);
    }

    @Test
    void testRecover2() {
        final Exception t = new Exception("exception message");
        serviceCallUnderTest.recover(t);
    }
}
