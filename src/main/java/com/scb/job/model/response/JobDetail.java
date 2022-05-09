package com.scb.job.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.entity.OrderItems;
import com.scb.job.model.enumeration.EvBikeVendors;

import com.scb.job.model.enumeration.JobType;
import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
@Builder
public class JobDetail {

  private String jobId;
  private String jobDate;
  private int jobStatus;
  private String jobStatusEn;
  private String jobStatusTh;
  private String jobDesc;
  private String startTime;
  private String finishTime;
  private boolean haveReturn;
  private String refNo;
  private String jobType;
  private String option;
  private Double totalDistance;
  private Double totalWeight;
  private Double totalSize;
  private String remark;
  private int userType;
  private Double normalPrice;
  private Double netPrice;
  private Double netPaymentPrice;
  private Double taxAmount;
  private Double discount;
  private Double rating;
  private List<JobLocation> locationList;
  private String orderId;
  private List<OrderItems> orderItems;
  private String riderId;
  @JsonProperty("riderName")
  private String driverName;
  @JsonProperty("riderPhoneNumber")
  private String driverPhone;
  private String creationDateTime;
  private String lastUpdatedDateTime;
  private String zoneName;
  private Integer zoneId;
  private Boolean isJobPriceModified;
  private Double minDistanceForJobCompletion;
  private Double distanceToMerchant;
  private Boolean evBikeUser;
  private Boolean rentingToday;
  private EvBikeVendors evBikeVendor;
  private double distanceToMerchantRepin;
  private double merchantToCustomerRepin;
  private Boolean isJobLocationUpdateHistory;

  private Double otherDeductions;
  private String customerRemark;
  private ExcessiveWaitingTimeDetailsEntity excessiveWaitTimeDetailsEntity;
  private JobType jobTypeEnum;
  private String jobTypeEnumThai;

  private Boolean ddFlag;
  private Double customerNetPrice;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double customerNormalPrice;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double normalInsuredPrice;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double netInsuredPrice;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double goodsValue;

  private String shopLandmark;
}
