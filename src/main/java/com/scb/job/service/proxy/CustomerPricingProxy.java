package com.scb.job.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.constants.JobConstants;
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
public class CustomerPricingProxy {

  private RestTemplate restTemplate;
  private String customerPricingServicePath;
  private ObjectMapper objectMapper;

  @Autowired
  public CustomerPricingProxy(RestTemplate restTemplate,
                                @Value("${customerPricingService.path}") String pricingServicePath,
                                ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.customerPricingServicePath = pricingServicePath;
    this.objectMapper = objectMapper;
  }

  public CustomerPricingProxyResponse getCalculatedNetPrice(
      CustomerPricingRequest customerPricingRequest){

    log.info("Invoking api:{}", customerPricingServicePath);
    HttpEntity<Object> entity = new HttpEntity<>(customerPricingRequest, headers());
    try {
      ResponseEntity<CustomerPricingProxyResponse> responseEntity = restTemplate
          .postForEntity(getCalculateNetPriceUrl(customerPricingRequest), entity, CustomerPricingProxyResponse.class);
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

  private String getCalculateNetPriceUrl(CustomerPricingRequest customerPricingRequest){
    String url = customerPricingServicePath+"/customer-pricing";
    if(customerPricingRequest.getJobType().equals(JobConstants.JOB_TYPE_EXPRESS_NUMBER)){
        url = url + "/express-calculate-net-price";
    }else if(customerPricingRequest.getJobType().equals(JobConstants.JOB_TYPE_MART_NUMBER)){
      url = url + "/mart-calculate-net-price";
    } else {
      url = url + "/calculate-net-price";
    }
    return url;
  }

  @SneakyThrows
  private ErrorResponse parseErrorResponse(String errorResponse){
    return objectMapper.readValue(errorResponse, ErrorResponse.class);

  }
}

