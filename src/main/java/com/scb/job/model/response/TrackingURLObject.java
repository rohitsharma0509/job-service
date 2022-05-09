package com.scb.job.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingURLObject {
    private String internalURL;
    private String consumerURL;
}
