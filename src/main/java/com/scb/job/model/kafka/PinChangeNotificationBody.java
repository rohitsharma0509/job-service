package com.scb.job.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PinChangeNotificationBody {
    private String updateLocationType;
    private Double jobPrice;
    private Double updatedJobPrice;
    private Double differentialAmount;
    private String jobId;
    private String type;

}
