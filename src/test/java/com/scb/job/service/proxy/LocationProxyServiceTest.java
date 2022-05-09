package com.scb.job.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.entity.Coordinates;
import com.scb.job.entity.RiderLocationEntity;
import com.scb.job.exception.ZoneException;
import com.scb.job.exception.ZoneNotFoundException;
import com.scb.job.model.response.AddressResponse;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class LocationProxyServiceTest {

    private String locationServicePath = "http://location-service";
    @InjectMocks
    private LocationProxy locationProxy;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        locationProxy = new LocationProxy(restTemplate, locationServicePath, objectMapper);
    }

    @Test
    void getSubDistrictAsNullHttpException(){
        String errorResponse = "error occurred";
        AddressResponse addressResponse = null;
        Mockito.when(restTemplate.getForEntity("locationServicePath", AddressResponse.class)).thenThrow(
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                        errorResponse.getBytes(),
                        StandardCharsets.UTF_8));
        CompletableFuture<AddressResponse> addressResponseCompletableFuture = locationProxy.getSubDistrict("100","100");
        try {
            addressResponse = addressResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(null, addressResponse.getSubDistrict());
    }

    @Test
    void getSubDistrictAsNullAndHttpException(){
        String errorResponse = "error occurred";
        AddressResponse addressResponse = null;
        Mockito.when(restTemplate.getForEntity(locationServicePath.concat("/api/address?longitude=100&latitude=100"), AddressResponse.class)).thenThrow(
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                        errorResponse.getBytes(),
                        StandardCharsets.UTF_8));
        CompletableFuture<AddressResponse> addressResponseCompletableFuture = locationProxy.getSubDistrict("100","100");
        try {
            addressResponse = addressResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(null, addressResponse.getSubDistrict());
    }

    @Test
    void getSubDistrictAsNullEmptyResponse(){
        AddressResponse addressResponse = null;
        Mockito.when(restTemplate.getForEntity(locationServicePath.concat("/api/address?longitude=100&latitude=100"), AddressResponse.class)).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        CompletableFuture<AddressResponse> addressResponseCompletableFuture = locationProxy.getSubDistrict("100","100");
        try {
            addressResponse = addressResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(null, addressResponse.getSubDistrict());
    }

    @Test
    void getSubDistrictSuccesss(){
        AddressResponse addressResponse = null;
        AddressResponse addressResponse1 = AddressResponse.builder().subDistrict("sub").build();
        Mockito.when(restTemplate.getForEntity(locationServicePath.concat("/api/address?longitude=100&latitude=100"), AddressResponse.class)).thenReturn(new ResponseEntity<>(addressResponse1, HttpStatus.OK));
        CompletableFuture<AddressResponse> addressResponseCompletableFuture = locationProxy.getSubDistrict("100","100");
        try {
            addressResponse = addressResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Assert.assertEquals("sub", addressResponse.getSubDistrict());
    }

    @Test
    void getRiderCurrentLocationTest() throws InterruptedException, ExecutionException{
        RiderLocationEntity riderLocationEntity = RiderLocationEntity.builder()
                .riderId("rider1")
                .geom(Coordinates.builder().coordinates(Arrays.asList("100.3", "30.6")).build())
                .build();

        Mockito.when(restTemplate.getForEntity(locationServicePath.concat("/api/rider/rider1"), RiderLocationEntity.class))
                .thenReturn(new ResponseEntity<>(riderLocationEntity, HttpStatus.OK));

        CompletableFuture<RiderLocationEntity> response = locationProxy.getRiderCurrentLocation("rider1");

        assertEquals("rider1", response.get().getRiderId());
        assertEquals("100.3", response.get().getGeom().getCoordinates().get(0));

    }

    @Test
    void getRiderCurrentLocationExceptionTest(){
        RiderLocationEntity riderLocationEntity = RiderLocationEntity.builder()
                .riderId("rider1")
                .geom(Coordinates.builder().coordinates(Arrays.asList("100.3", "30.6")).build())
                .build();

        Mockito.when(restTemplate.getForEntity(locationServicePath.concat("/api/rider/rider1"), RiderLocationEntity.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ZoneException ex = assertThrows(ZoneException.class,
                () -> locationProxy.getRiderCurrentLocation("rider1"));
        assertEquals("Problem occurred while fetching RiderLocation", ex.getMessage());


    }


}
