package com.scb.job.entity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.job.model.response.DifferentialPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nonapi.io.github.classgraph.json.Id;


@Document(collection = "JobLocationUpdateHistory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class JobLocationHistory {

  @Id
  @JsonIgnore
  private String id;

  private String updateLocationType;

  private String oldLat;

  private String oldLong;
  
  private String previousAddress;
  
  private String newLat;

  private String newLong;

  private Double jobNetPrice;

  private Double rePinNetPrice;

  private LocalDateTime updatedTime;
  
  @JsonIgnore
  private String updatedTimeTh;
  
  @Indexed
  private String jobId;
  
  private String riderId;
  
  private String status;
  
  private Double normalPrice;
  
  private Double netPaymentPrice;
  
  private Double taxAmount;
  
  private Double differenceAmount;
  
  private double merchantToCustomerRepin;

  @JsonIgnore
  private String updatedBy;

  public static DifferentialPrice getCustomerPaymentPrice(List<JobLocationHistory> jobLocationHistoryList){
    DifferentialPrice differentialPrice = DifferentialPrice.builder()
            .offlinePaymentAmount(0.0)
            .build();
    Collections.sort(jobLocationHistoryList, new SortByDate());
    for(JobLocationHistory jobLocationHistory: jobLocationHistoryList){
      differentialPrice.setRiderId(jobLocationHistory.getRiderId());
      differentialPrice.setJobId(jobLocationHistory.getJobId());
      if(ObjectUtils.isEmpty(jobLocationHistory.getDifferenceAmount())){
        log.info("offline payment amount is null for jobId " + jobLocationHistory.jobId);
        continue;
      }
      if(jobLocationHistory.updateLocationType.equals("CUSTOMER") && jobLocationHistory.status.equals("Accepted")){
        differentialPrice.setOfflinePaymentAmount(jobLocationHistory.getDifferenceAmount());
      }
    }
    return differentialPrice;
  }
  static class SortByDate implements Comparator<JobLocationHistory> {
    @Override
    public int compare(JobLocationHistory o1, JobLocationHistory o2) {
      return o1.updatedTime.compareTo(o2.updatedTime);
    }
  }

}
