package com.scb.job.constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum JobStatus {

  NEW(1, "New", "\u0E43\u0E2B\u0E21\u0E48"),
  JOB_ACCEPTED(5, "Assigned", "\u0e44\u0e23\u0e40\u0e14\u0e2d\u0e23\u0e4c\u0e23\u0e31\u0e1a\u0e07\u0e32\u0e19\u0e41\u0e25\u0e49\u0e27"),
  RIDER_NOT_FOUND(4, "Rider not found" , "\u0e44\u0e21\u0e48\u0e1e\u0e1a\u0e44\u0e23\u0e40\u0e14\u0e2d\u0e23\u0e4c"),
  CALLED_MERCHANT(6, "OTW to pickup", "\u0e01\u0e33\u0e25\u0e31\u0e07\u0e44\u0e1b\u0e22\u0e31\u0e07\u0e23\u0e49\u0e32\u0e19\u0e04\u0e49\u0e32"),
  ARRIVED_AT_MERCHANT(6, "OTW to pickup", "\u0e01\u0e33\u0e25\u0e31\u0e07\u0e23\u0e2d\u0e2a\u0e34\u0e19\u0e04\u0e49\u0e32"),
  MEAL_PICKED_UP(7, "OTW to delivery", "\u0e23\u0e31\u0e1a\u0e2a\u0e34\u0e19\u0e04\u0e49\u0e32\u0e41\u0e25\u0e49\u0e27"),
  ARRIVED_AT_CUST_LOCATION(7, "OTW to delivery", "\u0e16\u0e36\u0e07\u0e17\u0e35\u0e48\u0e2d\u0e22\u0e39\u0e48\u0e08\u0e31\u0e14\u0e2a\u0e48\u0e07\u0e41\u0e25\u0e49\u0e27"),
  FOOD_DELIVERED(9, "Completed", "\u0e08\u0e31\u0e14\u0e2a\u0e48\u0e07\u0e2a\u0e34\u0e19\u0e04\u0e49\u0e32\u0e41\u0e25\u0e49\u0e27"),
  ORDER_CANCELLED_BY_OPERATOR(0, "Canceled", "\u0e2d\u0e2d\u0e40\u0e14\u0e2d\u0e23\u0e4c\u0e16\u0e39\u0e01\u0e22\u0e01\u0e40\u0e25\u0e34\u0e01");


  JobStatus(int status, String statusEn, String statusTh) {
    this.status = status;
    this.statusEn = statusEn;
    this.statusTh = statusTh;
  }

  private int status;

  private String statusEn;

  private String statusTh;

  public static JobStatus findByStatus(String jobStatus){
    return Stream.of(JobStatus.values())
        .filter(status -> status.name().equals(jobStatus)).findFirst()
        .orElse(null);

  }

  public static JobStatus findByStatusId(int jobStatus){
    return Stream.of(JobStatus.values())
        .filter(status -> status.getStatus() == jobStatus).findFirst()
        .orElse(null);
  }

  public static List<Integer> getActiveJobStatuses() {
      return Arrays.stream(JobStatus.values())
          .filter(status -> status.getStatus()!=JobStatus.FOOD_DELIVERED.getStatus() &&
        		  status.getStatus()!=JobStatus.ORDER_CANCELLED_BY_OPERATOR.getStatus()
        		  && status.getStatus()!=JobStatus.RIDER_NOT_FOUND.getStatus())
          .map(status -> status.getStatus()).collect(Collectors.toList());
      
    }
  
  
  public static List<Integer> getRunningJobStatuses() {
      return Arrays.stream(JobStatus.values())
          .filter(status -> status.getStatus()!=JobStatus.FOOD_DELIVERED.getStatus() &&
        		  status.getStatus()!=JobStatus.ORDER_CANCELLED_BY_OPERATOR.getStatus()
        		  && status.getStatus()!=JobStatus.RIDER_NOT_FOUND.getStatus() 
        		  && status.getStatus()!=JobStatus.NEW.getStatus())
          .map(status -> status.getStatus()).collect(Collectors.toList());
      
    }
}
