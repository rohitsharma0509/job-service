package com.scb.job.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.RiderProfileServiceException;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.response.RiderJobDetailsResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RiderProfileProxy {

  private RestTemplate restTemplate;
  private String riderProfilePath;
  private ObjectMapper objectMapper;

  @Autowired
  public RiderProfileProxy(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      @Value("${riderProfileService.path}") String riderProfilePath) {
    this.restTemplate = restTemplate;
    this.riderProfilePath = riderProfilePath;
    this.objectMapper = objectMapper;
  }

  public RiderProfileResponse getRiderProfile(String riderId) {
    String uri = riderProfilePath.concat("/profile/details/").concat(riderId);
    log.info("Invoking get rider profile api:{}", uri);
    try {
      ResponseEntity<RiderProfileResponse> responseEntity =
          restTemplate.getForEntity(uri, RiderProfileResponse.class);
      log.info("Api invocation successful");
      return responseEntity.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error(
          "Api request error; ErrorCode:{} ; Message:{}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      throw new RiderProfileServiceException(error.getErrorMessage());
    }
  }

  public RiderJobDetailsResponse getRiderJobDetails(String jobId) {
    String uri = riderProfilePath.concat("/profile/job/").concat(jobId);
    log.info("Invoking get rider job details api:{}", uri);
    try {
      ResponseEntity<RiderJobDetailsResponse> responseEntity =
          restTemplate.getForEntity(uri, RiderJobDetailsResponse.class);
      log.info("Api invocation successful");
      return responseEntity.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error(
          "Api request error; ErrorCode:{} ; Message:{}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      throw new RiderProfileServiceException(error.getErrorMessage());
    }
  }

  @SneakyThrows
  private ErrorResponse parseErrorResponse(String errorResponse) {
    return objectMapper.readValue(errorResponse, ErrorResponse.class);
  }
}

