package com.scb.job.service.proxy;

import static com.scb.job.model.enumeration.Platform.APNS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scb.job.exception.RiderProfileServiceException;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.response.RiderDeviceDetails;
import com.scb.job.model.response.RiderJobDetailsResponse;
import java.nio.charset.Charset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RiderProfileProxyTest {

    @Mock
    private RestTemplate mockRestTemplate;

    private RiderProfileProxy riderProfileProxyUnderTest;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        riderProfileProxyUnderTest = new RiderProfileProxy(mockRestTemplate, new ObjectMapper(), "riderProfilePath");
    }

    @Test
    void testGetRiderProfile() {
        RiderProfileResponse expectedResult = new RiderProfileResponse(new RiderProfileDetails("id", "riderId", "firstName", "lastName", "availabilityStatus", "phoneNumber", "profilePhotoExternalUrl"),
                RiderDeviceDetails.builder()
                        .id("id").profileId("profileId")
                        .deviceToken("deviceToken").platform(APNS)
                        .build());
        ResponseEntity<RiderProfileResponse> riderProfileResponseEntity =
                new ResponseEntity<>(new RiderProfileResponse(new RiderProfileDetails("id", "riderId", "firstName", "lastName", "availabilityStatus", "phoneNumber", "profilePhotoExternalUrl"),
                        RiderDeviceDetails.builder()
                                .id("id").profileId("profileId")
                                .deviceToken("deviceToken").platform(APNS)
                                .build()),
                        HttpStatus.CONTINUE);
        when(mockRestTemplate.getForEntity(eq("riderProfilePath/profile/details/riderId"), eq(RiderProfileResponse.class), any(Object.class))).thenReturn(riderProfileResponseEntity);
        RiderProfileResponse result = riderProfileProxyUnderTest.getRiderProfile("riderId");
        assertEquals(result.getRiderProfileDetails().getRiderId(), expectedResult.getRiderProfileDetails().getRiderId());
        assertEquals(result.getRiderProfileDetails().getAvailabilityStatus(), expectedResult.getRiderProfileDetails().getAvailabilityStatus());
        assertEquals(result.getRiderDeviceDetails().getId(), expectedResult.getRiderDeviceDetails().getId());
        assertEquals(result.getRiderDeviceDetails().getDeviceToken(), expectedResult.getRiderDeviceDetails().getDeviceToken() );

    }

    @Test
    void testGetRiderJobDetails() {
        RiderJobDetailsResponse expectedResult = RiderJobDetailsResponse.builder()
            .id("id").jobId("jobId")
            .build();
        ResponseEntity<RiderJobDetailsResponse> riderJobDetailsResponseResponseEntity = new ResponseEntity<>(expectedResult, HttpStatus.CONTINUE);
        when(mockRestTemplate.getForEntity(eq("riderProfilePath/profile/job/jobId"), eq(RiderJobDetailsResponse.class)))
            .thenReturn(riderJobDetailsResponseResponseEntity);
        RiderJobDetailsResponse result = riderProfileProxyUnderTest.getRiderJobDetails("jobId");
        assertEquals(expectedResult, result);
    }

    @Test
    void testGetRiderJobDetailsException() throws JsonProcessingException {
        RiderJobDetailsResponse expectedResult = RiderJobDetailsResponse.builder()
            .id("id").jobId("jobId")
            .build();
        ErrorResponse response = new ErrorResponse("errorCode", "errorMessage");

        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", objectMapper.writeValueAsBytes(response),
            Charset.defaultCharset()))
            .when(mockRestTemplate).getForEntity(eq("riderProfilePath/profile/job/jobId"), eq(RiderJobDetailsResponse.class));
        assertThrows(RiderProfileServiceException.class, ()-> riderProfileProxyUnderTest.getRiderJobDetails("jobId"));
    }

}
