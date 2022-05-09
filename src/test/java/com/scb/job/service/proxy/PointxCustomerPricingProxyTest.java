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
public class PointxCustomerPricingProxyTest {

  private String pointxCustomerPricingServicePath = "http://customer-pricing-service.default/customer-pricing/pointx-calculate-net-price";

  @InjectMocks
  private PointXCustomerPricingProxy pointXCustomerPricingProxy;

  @Mock
  private RestTemplate restTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    pointXCustomerPricingProxy = new PointXCustomerPricingProxy(restTemplate, pointxCustomerPricingServicePath, objectMapper);
  }

  @Test
  void testGetCalculatedNetPrice(){
    PointXCustomerPricingRequest request = pointXCustomerPricingRequest();

    when(restTemplate
            .postForEntity(anyString(), any(), any()))
            .thenReturn(new ResponseEntity<>(pointXCustomerPricingResponse(), HttpStatus.OK));

    PointXCustomerPricingResponse  pointXCustomerPricingResponse = pointXCustomerPricingProxy
        .getCalculatedNetPrice(request);

    assertEquals(778.0, pointXCustomerPricingResponse.getCustomerNormalPrice());
    assertEquals(778.0, pointXCustomerPricingResponse.getCustomerNetPrice());

  }

  @Test
  public void testShouldThrowBadRequestException() {
    String errorResponse = "{\"errorCode\":\"404\",\"errorMessage\":\"Failure\"}";
    PointXCustomerPricingRequest pointXCustomerPricingRequest = pointXCustomerPricingRequest();
    when(restTemplate
        .postForEntity(anyString(), any(), any())).
        thenThrow(
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                errorResponse.getBytes(),
                StandardCharsets.UTF_8));
    EstimatePriceException estimatePriceException = assertThrows(
        EstimatePriceException.class, () -> pointXCustomerPricingProxy.getCalculatedNetPrice(pointXCustomerPricingRequest));

    assertEquals("Failure", estimatePriceException.getErrorMessage());
    assertEquals("404", estimatePriceException.getErrorCode());
  }

  private PointXCustomerPricingRequest pointXCustomerPricingRequest() {
    return PointXCustomerPricingRequest.builder().apiKey("apiKey").jobType("1").build();
  }

  private PointXCustomerPricingResponse pointXCustomerPricingResponse() {
    return PointXCustomerPricingResponse.builder()
            .customerNetPrice(778.0)
            .customerNormalPrice(778.0)
            .build();
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
    return headers;
  }

}

