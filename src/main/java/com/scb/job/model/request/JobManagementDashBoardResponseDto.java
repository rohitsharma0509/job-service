package com.scb.job.model.request;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobManagementDashBoardResponseDto {

  private static final String JOB_ACTIVE_COUNT = "ActiveJobCount";

  private static final String JOB_COMPLETED_COUNT  = "CompletedJobCount";

  private static final String TOTAL_JOBS = "totalJobs";

  private Long activeJobCount;

  private Long completedJobCount;

  private Long allJobCount;

  public static JobManagementDashBoardResponseDto of(
      Map<String, Long> jobStatusAggregatedCount) {

    final Long defaultCount = (long) 0;
      return  JobManagementDashBoardResponseDto.builder()
            .allJobCount(jobStatusAggregatedCount.getOrDefault(TOTAL_JOBS, defaultCount))
            .completedJobCount(
                jobStatusAggregatedCount.getOrDefault(JOB_COMPLETED_COUNT, defaultCount))
            .activeJobCount(
                jobStatusAggregatedCount.getOrDefault(JOB_ACTIVE_COUNT, defaultCount))
            .build();
  
  }
}
