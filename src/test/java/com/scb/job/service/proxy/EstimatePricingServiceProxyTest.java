package com.scb.job.service.proxy;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.EstimatePriceException;
import java.nio.charset.StandardCharsets;
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

@ExtendWith(MockitoExtension.class)
public class EstimatePricingServiceProxyTest {

  private String pricingServicePath = "http://pricing-service.default/pricing/estimate-price";

  @InjectMocks
  private EstimatePriceProxy estimatePriceProxy;

  @Mock
  private RestTemplate restTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    estimatePriceProxy = new EstimatePriceProxy(restTemplate, pricingServicePath, objectMapper);
  }

  @Test
  void testEstimatePrice(){
    EstimatePriceRequest request = estimatePriceRequest();
    when(restTemplate
        .postForEntity(anyString(), any(), any()))
        .thenReturn(new ResponseEntity<>(estimatePriceResponse(), HttpStatus.OK));

    EstimatePriceProxyResponse estimatePriceResponse = estimatePriceProxy
        .getEstimatedPrice(request);

    assertEquals(0.0, estimatePriceResponse.getDiscount());
    assertEquals(10.0, estimatePriceResponse.getNetPrice());
    assertEquals(9.0, estimatePriceResponse.getNetPaymentPrice());
    assertEquals(1.0, estimatePriceResponse.getTaxAmount());

  }

  @Test
  public void testShouldThrowBadRequestException() {
    String errorResponse = "{\"errorCode\":\"404\",\"errorMessage\":\"Failure\"}";
    EstimatePriceRequest request = estimatePriceRequest();
    when(restTemplate
        .postForEntity(anyString(), any(), any())).
        thenThrow(
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                errorResponse.getBytes(),
                StandardCharsets.UTF_8));
    EstimatePriceException estimatePriceException = assertThrows(
        EstimatePriceException.class, () -> estimatePriceProxy.getEstimatedPrice(request));

    assertEquals("Failure", estimatePriceException.getErrorMessage());
    assertEquals("404", estimatePriceException.getErrorCode());
  }

  private EstimatePriceRequest estimatePriceRequest() {
    return EstimatePriceRequest.builder().apiKey("apiKey").build();
  }

  private EstimatePriceProxyResponse estimatePriceResponse() {
    return EstimatePriceProxyResponse.builder().discount(0.00).netPrice(10.00)
            .taxAmount(1.00)
            .netPaymentPrice(9.00).build();
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
    return headers;
  }

}

