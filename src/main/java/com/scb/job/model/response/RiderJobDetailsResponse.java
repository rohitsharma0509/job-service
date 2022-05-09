package com.scb.job.model.response;

import com.scb.job.constants.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderJobDetailsResponse {

  private String id;
  private String profileId;
  private String jobId;
  private JobStatus jobStatus;
  private String mealPhotoUrl;
  private String mealDeliveredPhotoUrl;
  private String parkingPhotoUrl;
  private BigDecimal parkingFee;
  private String remarks;
  private LocalDateTime jobAcceptedTime;
  private LocalDateTime calledMerchantTime;
  private LocalDateTime arrivedAtMerchantTime;
  private LocalDateTime mealPickedUpTime;
  private LocalDateTime arrivedAtCustLocationTime;
  private LocalDateTime foodDeliveredTime;
  private LocalDateTime orderCancelledByOperationTime;
  private LocalDateTime parkingReceiptPhotoTime;

}
