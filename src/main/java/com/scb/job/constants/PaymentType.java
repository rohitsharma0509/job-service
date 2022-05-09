package com.scb.job.constants;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentType {
  INVOICE("invoice"),
  CASH("cash"),
  CREDIT_CARD("creditcard"),
  ROBINHOOD("robinhood"),
  POINTS("points");

  private String type;

  PaymentType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }


  public static boolean validatePaymentType(String paymentType) {
    Optional<PaymentType> type = Arrays.stream(PaymentType.values())
        .filter(e -> paymentType.equalsIgnoreCase(e.getType()))
        .findFirst();
    return type.isPresent();


  }
}
