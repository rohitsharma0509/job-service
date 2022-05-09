package com.scb.job.model.response;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RiderLocation {

  private String riderId;

  private Double lat;

  private Double lon;

  private String dateTime;

  private String phoneDateTime;
  private String channel;

}
