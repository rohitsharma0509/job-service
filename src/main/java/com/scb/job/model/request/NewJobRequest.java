package com.scb.job.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.*;

import com.scb.job.model.enumeration.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewJobRequest {

	@NotNull
  @NotBlank
  private String userName;

  @NotNull
  @NotBlank
  private String apiKey;

  @NotNull
  @NotBlank
  private String channel;

  @Pattern(regexp = "[0-9]+")
  @Size(max = 10)
  private String customerMobile;

  @Email
  private String customerEmail;

  @Max(4)
  @Min(1)
  @NotNull
  @NotBlank
  private String jobType;

  @NotBlank
  private String jobDate;

  @NotNull
  @NotBlank
  private String startTime;

  private String finishTime;

  @Valid
  private List<Location> locationList;

  @NotNull
  @NotBlank
  private String paymentType;

  private Double totalSize;

  private Double totalWeight;

  private String promoCode;

  @Size(max=3000)
  private String remark;

  @Size(max=50)
  private String refNo;

  private String merchantConfirm;

  private String callbackUrl;

  private String option;

  private Boolean ddFlag;

  private String goodsValue;
}
