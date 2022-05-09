package com.scb.job.service.proxy;

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

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

@Component
@Slf4j
public class PointxEstimatePriceProxy {

  private RestTemplate restTemplate;
  private String pricingServicePath;
  private ObjectMapper objectMapper;

  @Autowired
  public PointxEstimatePriceProxy(RestTemplate restTemplate,
                                  @Value("${pricingService.path}") String pricingServicePath,
                                  ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.pricingServicePath = pricingServicePath;
    this.objectMapper = objectMapper;
  }

  public PointXEstimatePricingResponse getEstimatedPrice(
      PointXEstimatePricingRequest pointXEstimatePricingRequest){

    log.info("Invoking api:{}", pricingServicePath);
    HttpEntity<Object> entity = new HttpEntity<>(pointXEstimatePricingRequest, headers());
    try {
      ResponseEntity<PointXEstimatePricingResponse> responseEntity = restTemplate
          .postForEntity(getEstimatePriceUrl(), entity, PointXEstimatePricingResponse.class);
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
    return pricingServicePath+"/pricing/pointx-calculate-net-price";
  }

  @SneakyThrows
  private ErrorResponse parseErrorResponse(String errorResponse){
    return objectMapper.readValue(errorResponse, ErrorResponse.class);

  }
}

