package com.scb.job.service.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.exception.OperationServiceException;
import com.scb.job.model.exception.ErrorResponse;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OperationsServiceProxy {

    private RestTemplate restTemplate;
    private String operationsServicePath;
    private ObjectMapper objectMapper;

    @Autowired
    public OperationsServiceProxy(RestTemplate restTemplate,
                                  @Value("${operationsService.path}") String operationsServicePath,
                                  ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.operationsServicePath = operationsServicePath;
        this.objectMapper = objectMapper;
    }

    public ConfigData getConfiguredDataForKey(String key) {
        log.info("Invoking api:{} key {}", operationsServicePath, key);
        String uri = operationsServicePath.concat("/ops/config/").concat(key);
        log.info("Invoking get Configured Data  with URI :{}", uri);
        try {
            ResponseEntity<ConfigData> responseEntity = restTemplate.getForEntity(uri, ConfigData.class);
            log.info("Api invocation successful");
            return responseEntity.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Api request error; ErrorCode:{} ; Message:{}", ex.getStatusCode(), ex.getResponseBodyAsString());
            ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
            throw new OperationServiceException(error.getErrorMessage());
        }
    }

    @SneakyThrows
    private ErrorResponse parseErrorResponse(String errorResponse) {
        return objectMapper.readValue(errorResponse, ErrorResponse.class);

    }

}

