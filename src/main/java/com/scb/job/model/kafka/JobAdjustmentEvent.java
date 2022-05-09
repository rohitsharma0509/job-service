package com.scb.job.model.kafka;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobAdjustmentEvent {
    private String jobId;
    private Double otherDeductions;

    private double excessiveWaitTopupAmount; 

	private LocalDateTime topupDateTime;
    
}
