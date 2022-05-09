package com.scb.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.model.response.RiderJobResponse;
import com.scb.job.util.JobHelper;
import com.scb.job.view.View;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import nonapi.io.github.classgraph.json.Id;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Document(collection = "Job")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@CompoundIndexes({
        @CompoundIndex(name="jobId_orderId_riderId_documentType", def = "{'jobId': 1, 'orderId': 1, 'riderId': 1}"),
        @CompoundIndex(name="jobStatus_creationTime", def = "{'creationDateTime': 1, 'jobStatusKey': 1, 'riderId': 1}"),
        @CompoundIndex(name="jobStatus_jobDateField_jobId", def = "{'jobStatus': 1, 'jobDateField': 1, 'jobId': -1}"),
        @CompoundIndex(name="jobTypeEnum_jobId", def = "{'jobTypeEnum': 1, 'jobId': 1}")
})
public class JobEntity {

  @Id private String id;

  @NotNull
  @Indexed(name = "job_id_index", unique = true)
  @JsonView(value = {View.JobDetailsView.class})
  private String jobId;

  private String jobDate;
  
  @JsonIgnore
  @Indexed
  private LocalDate jobDateField; // For Job Detail Search Only
  
  @JsonIgnore
  private String customerName; // For Job Detail Search Only
  
  @JsonIgnore
  private String merchantName; // For Job Detail Search Only
  
  @JsonIgnore
  private String netPriceSearch; // For Job Detail Search Only
 
  @Indexed(name = "order_id_unique_index", unique = true)
  @JsonView(value = {View.JobDetailsView.class})
  private String orderId;

  @Indexed(name = "job_status_search_index")
  private int jobStatus;

  @JsonView(value = {View.JobDetailsView.class})
  private String jobStatusKey;

  private String jobStatusEn;

  @JsonView(value = {View.JobDetailsView.class})
  private JobType jobTypeEnum;

  @JsonView(value = {View.JobDetailsView.class})
  private String jobTypeEnumThai;


  private String jobStatusTh;

  private String jobDesc;

  private String startTime;

  private String finishTime;

  private boolean haveReturn;

  @JsonView(value = {View.JobDetailsView.class})
  private String jobType;

  private String option;

  private Double totalDistance;

  private Double totalWeight;

  private Double totalSize;

  private String remark;

  private int userType;
  @JsonView(value = {View.JobDetailsView.class})

  private Double normalPrice;
  @JsonView(value = {View.JobDetailsView.class})

  private Double netPrice;
  @JsonView(value = {View.JobDetailsView.class})

  private Double customerNetPrice;
  @JsonView(value = {View.JobDetailsView.class})
  
  private Double netPaymentPrice;
  @JsonView(value = {View.JobDetailsView.class})

  private Double taxAmount;

  private Double discount;

  private RiderPrice riderPrice;

  private Double rating;

  private List<JobLocation> locationList;

  private String callbackUrl;

  @JsonView(value = {View.JobDetailsView.class})
  @Indexed(name = "rider_id_search_index", sparse = true)
  private String riderId;

  @JsonView(value = {View.JobDetailsView.class})
  @Indexed(name = "rider_id_index")
  private String driverId;

  private String driverName;

  private String driverPhone;

  private String driverImageUrl;

  private Double driverRating;

  private String trackingUrl;

  @JsonView(value = {View.JobDetailsView.class})
  private String creationDateTime;

  @JsonView(value = {View.JobDetailsView.class})
  private String lastUpdatedDateTime;

  private String zoneName;

  private Integer zoneId;

  private Integer zoneGroup;


  private String jobAcceptedTime;

  private String calledMerchantTime;

  private String arrivedAtMerchantTime;

  private String mealPickedUpTime;

  private String arrivedAtCustLocationTime;

  private String foodDeliveredTime;

  private String orderCancelledByOperationTime;

  private String parkingReceiptPhotoTime;

  private String mealPhotoUrl;

  private String mealDeliveredPhotoUrl;

  private List<OrderItems> orderItems;

  private String riderNotFoundTime;
  
  private boolean merchantConfirm;

  private LocalDateTime merchantConfirmDateTime;
  
  private Boolean isJobPriceModified;
  
  private Boolean isJobLocationUpdateHistory;

  private Double minDistanceForJobCompletion;

  private Boolean ddFlag;

  @JsonIgnore
  private String creationDateTimeTh;

  @JsonIgnore
  private String lastUpdatedDateTimeTh;
  
  private Double rePinDifferenceAmountMerchant;
  
  private Double rePinDifferenceAmountCustomer;

  private Double goodsValue;

  private Double normalInsuredPrice;

  private Double netInsuredPrice;

  private Double customerNormalPrice;

  @JsonIgnore
  private String updatedBy;

  private Double distanceToMerchant;
  private Boolean evBikeUser;
  private Boolean rentingToday;

  private EvBikeVendors evBikeVendor;

  private double distanceToMerchantRepin;
  private double merchantToCustomerRepin;
  private Map<String, String> riderInitialLatLong;

  private Double otherDeductions;
  private String otherDeductionsSearch;

  @JsonView(value = {View.JobDetailsView.class})
  private ExcessiveWaitingTimeDetailsEntity excessiveWaitTimeDetailsEntity;
  private String customerRemark;
  private Double withholdingTaxAmount;
  private String shopLandmark;

  public static RiderJobResponse of(JobEntity e) {
      JobLocation merchantloc= JobHelper.getLocation(e, 1);
      JobLocation customerLoc=JobHelper.getLocation(e, 2);

      LatLongLocation merchantlatLng=LatLongLocation.builder()
                      .latitude(merchantloc.getLat()).longitude(merchantloc.getLng()).build();
      LatLongLocation customerlatLng=LatLongLocation.builder()
                      .latitude(customerLoc.getLat()).longitude(customerLoc.getLng()).build();
      RiderJobResponse response=RiderJobResponse.builder()
					           .riderId(e.getDriverId()).riderJobStatus(e.getJobStatusKey())
					           .jobId(e.getJobId()).merchantName(e.getMerchantName())
					           .orderId(e.getOrderId()).remark(e.getRemark())
					           .price(e.getNetPrice()==null?0.0:e.getNetPrice())
					           .merchantAddress(merchantloc.getAddress()).merchantLocation(merchantlatLng)
					           .merchantPhone(merchantloc.getContactPhone()).customerPhone(customerLoc.getContactPhone())
					           .customerAddress(customerLoc.getAddress()).customerLocation(customerlatLng)
                               .customerRemark(e.getCustomerRemark())
					           .customerName(e.getCustomerName())
                               .jobTypeEnum(e.jobTypeEnum)
					           .minDistanceForJobCompletion(e.getMinDistanceForJobCompletion())
                              .shopLandmark(e.getShopLandmark())
                              .build();

      if(ObjectUtils.isEmpty(e.getOrderItems()))
      	   response.setOrderItems(Collections.EMPTY_LIST);
      else
   	   	   response.setOrderItems(e.orderItems);
      
      if(StringUtils.isNotEmpty(e.getArrivedAtMerchantTime()))
    	  response.setArrivedAtMerchantTime(e.getArrivedAtMerchantTime());
      
      if(StringUtils.isNotEmpty(e.getJobAcceptedTime()))
    	  response.setJobAcceptedTime(e.getJobAcceptedTime());
      
      if(StringUtils.isNotEmpty(e.getCalledMerchantTime()))
    	  response.setCalledMerchantTime(e.getCalledMerchantTime());
      
    return  response;
  }

    public static void extractCustomerRemark(JobEntity job) {
        String colon = ":";
        String emptyString = "";

        List<JobLocation> locations = job.getLocationList();
        job.setCustomerRemark(emptyString);
        if (ObjectUtils.isNotEmpty(locations)) {
          for (JobLocation location : locations) {
            if (ObjectUtils.isNotEmpty(location.getSeq()) && location.getSeq() == 2) {
              String customerAddressName = location.getAddressName();
              if (StringUtils.isNotBlank(customerAddressName)) {
                String customerRemark = customerAddressName.split(colon).length>1?customerAddressName.split(colon)[1]:"";
                job.setCustomerRemark(customerRemark);

              }
            }
          }
      }
    }

    public static void validateWithholdingTaxAmount(JobEntity job) {
      if (ObjectUtils.isEmpty(job.getWithholdingTaxAmount())) {
        job.setWithholdingTaxAmount(0.0);
      }
    }
}
