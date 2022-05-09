package com.scb.job.service.proxy;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
@Builder
public class PointXCustomerPricingResponse {

    private Double customerNormalPrice;
    private Double customerNetPrice;
    private Double discount;
    private Double distance;
    private Integer tripDuration;
    private Double normalInsuredPrice;
    private Double netInsuredPrice;
    private Double goodsValue;

}
