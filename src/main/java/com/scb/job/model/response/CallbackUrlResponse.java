package com.scb.job.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CallbackUrlResponse {
    private StatusResponse status;
    private DataResponse data;

    @Data
    public static class StatusResponse {
        private Integer code;
        private String header;
        private String description;
    }

    @Data
    public static class DataResponse {
        private String orderId;
    }
}
