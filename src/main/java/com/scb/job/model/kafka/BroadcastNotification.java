package com.scb.job.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BroadcastNotification {

    private String type;
    private String platform;
    private String payload;
    private String arn;

}
