package com.scb.job.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.EstimatePriceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerPricingProxyTest {

  private String customerPricingServicePath = "http://customer-pricing-service.default/customer-pricing/calculate-net-price";

  @InjectMocks
  private CustomerPricingProxy customerPricingProxy;

  @Mock
  private RestTemplate restTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    customerPricingProxy = new CustomerPricingProxy(restTemplate, customerPricingServicePath, objectMapper);
  }

  @Test
  void testGetCalculatedNetPrice(){
    CustomerPricingRequest request = customerPricingRequest();
    when(restTemplate
        .postForEntity(anyString(), any(), any()))
        .thenReturn(new ResponseEntity<>(customerPricingResponse(), HttpStatus.OK));

    CustomerPricingProxyResponse customerPricingProxyResponse = customerPricingProxy
        .getCalculatedNetPrice(request);

    assertEquals(778.0, customerPricingProxyResponse.getNormalPrice());
    assertEquals(778.0, customerPricingProxyResponse.getNetPrice());

  }

  @Test
  void testGetCalculatedNetPriceJobType2(){
    CustomerPricingRequest request = customerPricingRequest();
    request.setJobType("2");
    when(restTemplate
            .postForEntity(anyString(), any(), any()))
            .thenReturn(new ResponseEntity<>(customerPricingResponse(), HttpStatus.OK));

    CustomerPricingProxyResponse customerPricingProxyResponse = customerPricingProxy
            .getCalculatedNetPrice(request);

    assertEquals(778.0, customerPricingProxyResponse.getNormalPrice());
    assertEquals(778.0, customerPricingProxyResponse.getNetPrice());

  }

  @Test
  void testGetCalculatedNetPriceJobType3(){
    CustomerPricingRequest request = customerPricingRequest();
    request.setJobType("3");
    when(restTemplate
            .postForEntity(anyString(), any(), any()))
            .thenReturn(new ResponseEntity<>(customerPricingResponse(), HttpStatus.OK));

    CustomerPricingProxyResponse customerPricingProxyResponse = customerPricingProxy
            .getCalculatedNetPrice(request);

    assertEquals(778.0, customerPricingProxyResponse.getNormalPrice());
    assertEquals(778.0, customerPricingProxyResponse.getNetPrice());

  }

  @Test
  public void testShouldThrowBadRequestException() {
    String errorResponse = "{\"errorCode\":\"404\",\"errorMessage\":\"Failure\"}";
    CustomerPricingRequest request = customerPricingRequest();
    when(restTemplate
        .postForEntity(anyString(), any(), any())).
        thenThrow(
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                errorResponse.getBytes(),
                StandardCharsets.UTF_8));
    EstimatePriceException estimatePriceException = assertThrows(
        EstimatePriceException.class, () -> customerPricingProxy.getCalculatedNetPrice(request));

    assertEquals("Failure", estimatePriceException.getErrorMessage());
    assertEquals("404", estimatePriceException.getErrorCode());
  }

  private CustomerPricingRequest customerPricingRequest() {
    return CustomerPricingRequest.builder().apiKey("apiKey").jobType("1").build();
  }

  private CustomerPricingProxyResponse customerPricingResponse() {
    return CustomerPricingProxyResponse.builder()
            .normalPrice(778.0)
            .netPrice(778.0)
            .build();
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
    return headers;
  }

}

