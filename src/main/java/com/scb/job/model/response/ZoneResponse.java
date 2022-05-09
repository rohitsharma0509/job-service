package com.scb.job.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {

    private Integer zoneId;

    private Integer postalCode;

    private String zoneName;

    private Integer zoneGroup;

}
