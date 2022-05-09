package com.scb.job.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class OperationServiceException extends RuntimeException {
    private String errorMessage;
}