package com.scb.job.service.proxy;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
@Builder
public class PointXEstimatePricingResponse {

    private Double normalPrice;
    private Double netPrice;
    private Double discount;
    private Double distance;
    private Integer tripDuration;

}
