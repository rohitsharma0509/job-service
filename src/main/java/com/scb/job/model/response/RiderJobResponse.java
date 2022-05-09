package com.scb.job.model.response;

import java.util.List;

import com.scb.job.entity.LatLongLocation;
import com.scb.job.entity.OrderItems;

import com.scb.job.model.enumeration.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderJobResponse {

	  private String riderId;
	  private String riderJobStatus;
	  private String jobId;
	  private String merchantName;
	  private String merchantAddress;
	  private String merchantPhone;
	  private LatLongLocation merchantLocation;
	  private String customerName;
	  private String customerAddress;
	  private String customerPhone;
	  private LatLongLocation customerLocation;
	  private String expiry;
	  private String orderId;
	  private List<OrderItems> orderItems;
	  private String remark;
	  private double price;
	  private double distance;
	  private Double minDistanceForJobCompletion;
	  private String customerRemark;
	  private String arrivedAtMerchantTime;
	  private String jobAcceptedTime;

	  private String calledMerchantTime;
	  private JobType jobTypeEnum;

	  private String shopLandmark;
}
