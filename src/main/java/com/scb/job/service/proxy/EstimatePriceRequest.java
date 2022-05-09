package com.scb.job.service.proxy;

import com.scb.job.model.request.Location;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EstimatePriceRequest {
  private String userName;

  private String apiKey;

  private String channel;

  private String jobType;

  private String option;

  private String promoCode;

  private List<Location> locationList;

}
