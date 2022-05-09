package com.scb.job.service.proxy;

import com.scb.job.model.request.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CustomerPricingRequest {
  private String userName;

  private String apiKey;

  private String channel;

  private String jobType;

  private List<Location> locationList;

  private Boolean ddFlag;

  private Integer zoneGroup;

}
