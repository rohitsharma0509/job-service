package com.scb.job.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.job.view.View;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExcessiveWaitingTimeDetailsEntity {

	  @JsonView(value = {View.JobDetailsView.class})
	  private double excessiveWaitTopupAmount;

	  private String excessiveWaitTopupAmountSearch;
	  
	  private LocalDateTime excessiveWaitTopupDateTime;
}
