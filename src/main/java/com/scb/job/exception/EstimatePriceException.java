package com.scb.job.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class EstimatePriceException extends RuntimeException {
  private String errorCode;

  private String errorMessage;

}
