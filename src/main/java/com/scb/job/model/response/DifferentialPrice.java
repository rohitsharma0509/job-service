package com.scb.job.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DifferentialPrice {
    private Double offlinePaymentAmount;

    private String jobId;

    private String riderId;
}
