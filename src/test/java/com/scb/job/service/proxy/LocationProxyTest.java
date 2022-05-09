package com.scb.job.service.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.ZoneException;
import com.scb.job.entity.DistanceResponseEntity;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.response.ZoneResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationProxyTest {

    private static final String RIDER_ID = "RR0001";
    private static final String LONGITUDE_TO = "100.53";
    private static final String LATITUDE_TO = "13.75";
    private static final double DISTANCE = 10.0;

    @Mock
    private RestTemplate mockRestTemplate;

    @InjectMocks
    private LocationProxy locationProxyUnderTest;

    @BeforeEach
    void setUp() {
        locationProxyUnderTest = new LocationProxy(mockRestTemplate, "locationServicePath", new ObjectMapper());
    }

    @Test
    void testGetMerchantZone() {
         ZoneResponse expectedResult = new ZoneResponse(0, 0, "zoneName",1);
         ResponseEntity<ZoneResponse> zoneResponseEntity = new ResponseEntity<>(new ZoneResponse(0, 0, "zoneName",1), HttpStatus.CONTINUE);
        when(mockRestTemplate.getForEntity(eq("locationServicePath/api/zone/?location=lng,lat"), eq(ZoneResponse.class), any(Object.class))).thenReturn(zoneResponseEntity);
        ZoneResponse result = locationProxyUnderTest.getMerchantZone("lat", "lng");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMerchantZone_RestTemplateThrowsRestClientException() {
        ZoneResponse expectedResult = new ZoneResponse(0, 0, "zoneName",1);
        when(mockRestTemplate.getForEntity(eq("url"), eq(ZoneResponse.class), any(Object.class))).thenThrow(RestClientException.class);
        assertThrows(ZoneException.class, () -> locationProxyUnderTest.getMerchantZone("lat", "lng"));

    }

    @Test
    void getDistanceFromRidersCurrentLocationShouldThrowExceptionForHttpServerErrorException() throws JsonProcessingException {
        ErrorResponse errorResponse = ErrorResponse.builder().errorCode(HttpStatus.BAD_REQUEST.name())
                .errorMessage(HttpStatus.BAD_REQUEST.getReasonPhrase()).build();
        byte[] bytes = new ObjectMapper().writeValueAsBytes(errorResponse);



        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(), bytes, StandardCharsets.UTF_8);
        when(mockRestTemplate.getForEntity(anyString(), eq(DistanceResponseEntity.class))).thenThrow(exception);
        assertThrows(ZoneException.class, () -> locationProxyUnderTest.getDistanceFromRidersCurrentLocation(RIDER_ID, LONGITUDE_TO, LATITUDE_TO));
    }

    @Test
    void getDistanceFromRidersCurrentLocationShouldThrowExceptionForOtherException() {
        when(mockRestTemplate.getForEntity(anyString(), eq(DistanceResponseEntity.class))).thenThrow(new NullPointerException());
        assertThrows(ZoneException.class, () -> locationProxyUnderTest.getDistanceFromRidersCurrentLocation(RIDER_ID, LONGITUDE_TO, LATITUDE_TO));
    }

    @Test
    void getDistanceFromRidersCurrentLocationForSuccessCase() throws InterruptedException, ExecutionException {
        ResponseEntity<DistanceResponseEntity> response = ResponseEntity.ok(DistanceResponseEntity.builder().distance(DISTANCE).build());
        when(mockRestTemplate.getForEntity(anyString(), eq(DistanceResponseEntity.class))).thenReturn(response);
        CompletableFuture<DistanceResponseEntity> result = locationProxyUnderTest.getDistanceFromRidersCurrentLocation(RIDER_ID, LONGITUDE_TO, LATITUDE_TO);
        assertEquals(DISTANCE, result.get().getDistance());
    }

}
