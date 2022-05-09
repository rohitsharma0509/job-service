package com.scb.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.config.RBHConfiguration;
import com.scb.job.model.response.CallbackUrlResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.scb.job.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceCall {

	private static final String API_KEY_HEADER = "x-api-key";
	private static final Integer CODE_ERROR_CALL_BACK_URL = 1999;

	private RestTemplate restTemplate;

	private RBHConfiguration rbhConfiguration;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	public ServiceCall(RestTemplate restTemplate, RBHConfiguration rbhConfiguration) {
		this.restTemplate = restTemplate;
		this.rbhConfiguration = rbhConfiguration;
	}

	@Retryable(include=Exception.class, backoff = @Backoff(delay = 500, maxDelay = 1000), maxAttempts = 3)
	public void callServiceUrl(String url, String request) {
		log.info("Invoking callback with url:{} and payload: {}", url, request);
		HttpEntity<String> entity = new HttpEntity<>(request, headers());
		ResponseEntity<String> response = restTemplate.postForEntity(url, entity , String.class);
		if(response.getStatusCodeValue()==200) {
			log.info("Callback URL, successfully invoked. Response:{}",response.getBody());
			if (!ObjectUtils.isEmpty(response.getBody())) {
				try {
					CallbackUrlResponse responseCallBack = objectMapper.readValue(response.getBody(), CallbackUrlResponse.class);
					if (responseCallBack.getStatus().getCode().compareTo(CODE_ERROR_CALL_BACK_URL) == 0) {
						log.error("Seems like resource is not available and trying to make attempt again: ");
						log.error("Error response:{}",response.getBody());
						throw new ResourceNotFoundException("Seems like resource is not available and trying to make attempt again: ");
					}
				} catch (JsonProcessingException ex) {
					log.error("Cannot parse response body to object CallbackUrlResponse: ");
					log.error("Error response:{}",ex.getMessage());
				}
			}
		}
		else {
			log.error("Seems like resource is not available and trying to make attempt again: ");
			log.error("Error response:{}",response.getBody());
			throw new ResourceNotFoundException("Seems like resource is not available and trying to make attempt again: ");
		}
	}
	
	@Recover
	public void recover(ResourceNotFoundException t) {
		log.error("Seems like Resource is not available, recover exception", t);
	}
	@Recover
	public void recover(Exception t) {
		log.error("Exception occurred when callback", t);
	}

	private HttpHeaders headers(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(API_KEY_HEADER, rbhConfiguration.getApikey());
		return headers;
	}

}
