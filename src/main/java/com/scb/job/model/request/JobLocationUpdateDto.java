package com.scb.job.model.request;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.job.constants.JobLocationUpdatedBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class JobLocationUpdateDto {
    @NotNull
    private JobLocationUpdatedBy addressType;
	@NotNull
	@NotBlank
	private String action;
	@JsonIgnore
	private String updatedBy;
}
