package com.scb.job.service.proxy;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.EstimatePriceException;
import com.scb.job.model.exception.ErrorResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class EstimatePriceProxy {

  private RestTemplate restTemplate;
  private String pricingServicePath;
  private ObjectMapper objectMapper;

  @Autowired
  public EstimatePriceProxy(RestTemplate restTemplate,
      @Value("${pricingService.path}") String pricingServicePath,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.pricingServicePath = pricingServicePath;
    this.objectMapper = objectMapper;
  }

  public EstimatePriceProxyResponse getEstimatedPrice(
      EstimatePriceRequest estimatePriceRequest){

    log.info("Invoking api:{}", pricingServicePath);
    HttpEntity<Object> entity = new HttpEntity<>(estimatePriceRequest, headers());
    try {
      ResponseEntity<EstimatePriceProxyResponse> responseEntity = restTemplate
          .postForEntity(getEstimatePriceUrl(), entity, EstimatePriceProxyResponse.class);
      log.info("Api invocation successful");
      return responseEntity.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error("Api request error; ErrorCode:{} ; Message:{}", ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      throw new EstimatePriceException(error.getErrorCode(), error.getErrorMessage());
    }
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
    return headers;
  }

  private String getEstimatePriceUrl(){
    return pricingServicePath+"/pricing/estimate-price";
  }

  @SneakyThrows
  private ErrorResponse parseErrorResponse(String errorResponse){
    return objectMapper.readValue(errorResponse, ErrorResponse.class);

  }
}

