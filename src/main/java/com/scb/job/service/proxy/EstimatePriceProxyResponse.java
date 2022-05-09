package com.scb.job.service.proxy;

import com.scb.job.entity.RiderPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EstimatePriceProxyResponse {

  private Double normalPrice;
  private Double netPrice;
  private Double discount;
  private Double distance;
  private Integer tripDuration;
  private Double netPaymentPrice;
  private Double taxAmount;
  private RiderPrice riderPrice;

}
