package com.scb.job.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Location {
	private String addressName;
	@NotBlank
	private String address;
	@NotBlank
	private String lat;
	@NotBlank
	private String lng;
	private String contactName;
	private String contactPhone;
	private String cashFee;
	@NotNull
	private Integer seq;

}
