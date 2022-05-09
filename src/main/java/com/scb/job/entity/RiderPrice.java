package com.scb.job.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class RiderPrice {

    private Double peakHourPrice;
    private Double specialOccasionPrice;
    private Double totalPrice;
}

