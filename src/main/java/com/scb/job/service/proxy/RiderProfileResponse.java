package com.scb.job.service.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scb.job.model.response.RiderDeviceDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileResponse {

  @JsonProperty("riderProfileDto")
  private RiderProfileDetails riderProfileDetails;
  @JsonProperty("riderDeviceDetails")
  private RiderDeviceDetails riderDeviceDetails;

}
