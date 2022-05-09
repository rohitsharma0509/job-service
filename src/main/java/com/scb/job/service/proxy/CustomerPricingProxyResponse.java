package com.scb.job.service.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomerPricingProxyResponse {
  private Double normalPrice;
  private Double netPrice;
  private Double discount;
  private Double distance;
  private Integer tripDuration;
}
