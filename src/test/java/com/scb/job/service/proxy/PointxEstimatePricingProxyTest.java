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
public class PointxEstimatePricingProxyTest {

  private String pointxEstimatePricingServicePath = "http://customer-pricing-service.default/pricing/pointx-calculate-net-price";

  @InjectMocks
  private PointxEstimatePriceProxy pointxEstimatePriceProxy;

  @Mock
  private RestTemplate restTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    pointxEstimatePriceProxy = new PointxEstimatePriceProxy(restTemplate, pointxEstimatePricingServicePath, objectMapper);
  }

  @Test
  void testGetCalculatedNetPrice(){
    PointXEstimatePricingRequest request = pointXEstimatePricingRequest();

    when(restTemplate
            .postForEntity(anyString(), any(), any()))
            .thenReturn(new ResponseEntity<>(pointXEstimatePricingResponse(), HttpStatus.OK));

    PointXEstimatePricingResponse  pointXEstimatePricingResponse = pointxEstimatePriceProxy
        .getEstimatedPrice(request);

    assertEquals(778.0, pointXEstimatePricingResponse.getNormalPrice());
    assertEquals(778.0, pointXEstimatePricingResponse.getNetPrice());

  }

  @Test
  public void testShouldThrowBadRequestException() {
    String errorResponse = "{\"errorCode\":\"404\",\"errorMessage\":\"Failure\"}";
    PointXEstimatePricingRequest pointXEstimatePricingRequest = pointXEstimatePricingRequest();
    when(restTemplate
        .postForEntity(anyString(), any(), any())).
        thenThrow(
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                errorResponse.getBytes(),
                StandardCharsets.UTF_8));
    EstimatePriceException estimatePriceException = assertThrows(
        EstimatePriceException.class, () -> pointxEstimatePriceProxy.getEstimatedPrice(pointXEstimatePricingRequest));

    assertEquals("Failure", estimatePriceException.getErrorMessage());
    assertEquals("404", estimatePriceException.getErrorCode());
  }

  private PointXEstimatePricingRequest pointXEstimatePricingRequest() {
    return PointXEstimatePricingRequest.builder().apiKey("apiKey").jobType("1").build();
  }

  private PointXEstimatePricingResponse pointXEstimatePricingResponse() {
    return PointXEstimatePricingResponse.builder()
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

