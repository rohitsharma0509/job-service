package com.scb.job.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import com.scb.job.constants.JobStatus;
import com.scb.job.entity.JobStatusAggregateCountEntity;
import com.scb.job.model.request.JobManagementDashBoardResponseDto;
import com.scb.job.repository.JobRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class JobManagementDashBoardService {

	@Autowired
	private JobRepository jobRepository;


	public JobManagementDashBoardResponseDto getJobManagementDashBoardSummary(String requestId) {

		Map<String, Long> jobStatusAggregatedCount = new HashMap<String, Long>();

		AggregationResults<JobStatusAggregateCountEntity> countActiveJobAgg = jobRepository
				.groupByActiveJobStatus(JobStatus.getActiveJobStatuses());
		
		log.info(String.format("Jobs Management DashBoard countActiveJobAgg Request Id - %s", requestId));

		AggregationResults<JobStatusAggregateCountEntity> countCompletedJobAgg = jobRepository
				.groupByCompletedJobStatus(JobStatus.FOOD_DELIVERED.getStatus(), LocalDate.now());
		
		log.info(String.format("Jobs Management DashBoard countCompletedJobAgg Request Id - %s", requestId));

		AggregationResults<JobStatusAggregateCountEntity> countAllJobAgg = jobRepository.groupByAllJob(LocalDate.now());
		
		log.info(String.format("Jobs Management DashBoard countAllJobAgg Request Id - %s", requestId));
		
		countActiveJobAgg.getMappedResults().stream().forEach(
				aggResult -> jobStatusAggregatedCount.put(aggResult.getAggregatestatus(), aggResult.getJobCount()));

		countCompletedJobAgg.getMappedResults().stream().forEach(
				aggResult -> jobStatusAggregatedCount.put(aggResult.getAggregatestatus(), aggResult.getJobCount()));

		countAllJobAgg.getMappedResults().stream().forEach(
				aggResult -> jobStatusAggregatedCount.put(aggResult.getAggregatestatus(), aggResult.getJobCount()));
		log.info("generated data for dashboard");
		
		log.info(String.format("Jobs Management DashBoard resonse Request Id - %s", requestId));
		
		return JobManagementDashBoardResponseDto.of(jobStatusAggregatedCount);
	}

}
