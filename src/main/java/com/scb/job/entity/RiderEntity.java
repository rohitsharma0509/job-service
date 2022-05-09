package com.scb.job.entity;


import com.scb.job.model.enumeration.EvBikeVendors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collation = "RiderInfo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderEntity {
	
	@Id
	@JsonProperty("jobId")
	private String jobId;
	
	@JsonProperty("riderId")
	private String riderId;

	@JsonProperty("dateTime")
	private String dateTime;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("jobPrice")
	private Double jobPrice;
	
	@JsonProperty("isJobPriceModified")
	private Boolean isJobPriceModified;

	@JsonProperty("imageUrl")
	private String imageUrl;

	@JsonProperty("updatedBy")
	private String updatedBy;

	@JsonProperty("evBikeUser")
	private Boolean evBikeUser;

	@JsonProperty("evBikeVendor")
	private EvBikeVendors evBikeVendor;

	@JsonProperty("rentingToday")
	private Boolean rentingToday;
	
	@JsonProperty("driverName")
	private String driverName;

	@JsonProperty("driverPhone")
	private String driverPhone;

	@JsonProperty("driverImageUrl")
	private String driverImageUrl;

	@JsonProperty("riderRRid")
    private String riderRRid;

	
}
