package com.scb.job.service.proxy;

import java.util.concurrent.CompletableFuture;

import com.scb.job.entity.DistanceResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.entity.RiderLocationEntity;
import com.scb.job.exception.SubDistrictNotFoundException;
import com.scb.job.exception.ZoneException;
import com.scb.job.exception.ZoneNotFoundException;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.response.AddressResponse;
import com.scb.job.model.response.ZoneResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocationProxy {

    private RestTemplate restTemplate;

    private String locationServicePath;
    private ObjectMapper objectMapper;

    @Autowired
    public LocationProxy(RestTemplate restTemplate,
                         @Value("${locationService.path}") String locationServicePath,ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.locationServicePath = locationServicePath;
        this.objectMapper = objectMapper;
    }

    public ZoneResponse getMerchantZone(String lat, String lng) {

        ResponseEntity<ZoneResponse> zoneResponse;
        try {
            String url = locationServicePath + "/api/zone/?location=" + lng + "," + lat;
            log.info("Invoking api:{}", url);
            zoneResponse =
                    restTemplate.getForEntity(url, ZoneResponse.class);
            log.info("Api invocation successful");

            if (zoneResponse.hasBody()) {
                return zoneResponse.getBody();
            } else {
                throw new ZoneNotFoundException("Zone not present");
            }
        } catch (HttpClientErrorException | HttpServerErrorException  ex) {
            log.error("Api request error; ErrorCode:{} ; Message:{}", ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
            throw new ZoneException(error.getErrorMessage());
        }catch (Exception ex){
            log.error("Api request error; Unknown exception", ex);
            throw new ZoneException("Problem occurred while fetching zone ");

        }
    }

    @Async
    public CompletableFuture<AddressResponse> getSubDistrict(String lat, String lng) {

        ResponseEntity<AddressResponse> addressResponse;
        try {
            String url = locationServicePath + "/api/address?longitude=" + lng + "&latitude=" + lat;
            log.info("Invoking api:{}", url);
            addressResponse =
                    restTemplate.getForEntity(url, AddressResponse.class);
            log.info("Api invocation successful");

            if (addressResponse.hasBody()) {
                return CompletableFuture.completedFuture(addressResponse.getBody());
            } else {
                log.info("Response body empty - Returning sub district as null");
                AddressResponse addressResponse1 = AddressResponse.builder().subDistrict(null).build();
                return CompletableFuture.completedFuture(addressResponse1);
            }
        } catch (HttpClientErrorException | HttpServerErrorException  ex) {
            log.error("Api request error; ErrorCode:{} ; Message:{}", ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            log.info("Returning sub district as null");
            AddressResponse addressResponse1 = AddressResponse.builder().subDistrict(null).build();
            return CompletableFuture.completedFuture(addressResponse1);
        }catch (Exception ex){
            log.error("Api request error; Unknown exception", ex);
            log.info("Returning sub district as null");
            AddressResponse addressResponse1 = AddressResponse.builder().subDistrict(null).build();
            return CompletableFuture.completedFuture(addressResponse1);

        }
    }
    
    @Async
    public CompletableFuture<RiderLocationEntity> getRiderCurrentLocation(String riderId) {

      ResponseEntity<RiderLocationEntity> riderLocation;
      try {
          String url = locationServicePath + "/api/rider/" + riderId;
          log.info("Invoking api:{}", url);
          riderLocation =
                  restTemplate.getForEntity(url, RiderLocationEntity.class);
          log.info("Api invocation successful");

          if (riderLocation.hasBody()) {
              return CompletableFuture.completedFuture(riderLocation.getBody());
          } else {
              throw new ZoneNotFoundException("RiderLocation not present");
          }
      } catch (HttpClientErrorException | HttpServerErrorException  ex) {
          log.error("Api request error; ErrorCode:{} ; Message:{}", ex.getStatusCode(),
                  ex.getResponseBodyAsString());
          ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
          throw new ZoneException(error.getErrorMessage());
      }catch (Exception ex){
          log.error("Api request error; Unknown exception", ex);
          throw new ZoneException("Problem occurred while fetching RiderLocation");

      }
  }
    
  @Async
  public CompletableFuture<DistanceResponseEntity> getDistanceFromRidersCurrentLocation(String riderId, String longitudeTo, String latitudeTo) {
        ResponseEntity<DistanceResponseEntity> distanceEntity;
        try {
            StringBuilder url = new StringBuilder(locationServicePath);
            url.append("/api/distance/").append(riderId);
            url.append("?longitudeTo=").append(longitudeTo).append("&latitudeTo=").append(latitudeTo);
            log.info("Invoking api:{}", url);
            distanceEntity = restTemplate.getForEntity(url.toString(), DistanceResponseEntity.class);
            log.info("Api invocation successful");

            if (distanceEntity.hasBody()) {
                return CompletableFuture.completedFuture(distanceEntity.getBody());
            } else {
                throw new ZoneNotFoundException("RiderLocation not present");
            }
        } catch (HttpClientErrorException | HttpServerErrorException  ex) {
            log.error("Api request error; ErrorCode:{} ; Message:{}", ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
            throw new ZoneException(error.getErrorMessage());
        }catch (Exception ex){
            log.error("Api request error; Unknown exception", ex);
            throw new ZoneException("Problem occurred while fetching RiderLocation");
        }
  }

    @SneakyThrows
    private ErrorResponse parseErrorResponse(String errorResponse){
        return objectMapper.readValue(errorResponse, ErrorResponse.class);

    }
}