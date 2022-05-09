package com.scb.job.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.model.kafka.JobAdjustmentEvent;
import com.scb.job.repository.JobRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdjustmentProcess {
	ObjectMapper mapper;
	JobRepository jobRepository;

	@Autowired
	public AdjustmentProcess(ObjectMapper mapper, JobRepository jobRepository) {
		this.mapper = mapper;
		this.jobRepository = jobRepository;
	}

	public void processAdjustmentEvent(String message) {
		try {
			JobAdjustmentEvent adjustmentRequest = mapper.readValue(message, JobAdjustmentEvent.class);
			mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
			log.info("processAdjustmentEvent: ", adjustmentRequest);
			JobEntity jobEntity = jobRepository.getjobById(adjustmentRequest.getJobId());
			if (!ObjectUtils.isEmpty(adjustmentRequest.getOtherDeductions())) {
				log.info("adding other deduction ammount to job-{}", jobEntity.getJobId());
				jobEntity.setOtherDeductions(adjustmentRequest.getOtherDeductions());
				jobEntity.setOtherDeductionsSearch(adjustmentRequest.getOtherDeductions().toString());
			}
			if (adjustmentRequest.getExcessiveWaitTopupAmount() != 0.0) {
				log.info("adding excessive Wait ammount to job-{}", jobEntity.getJobId());
				ExcessiveWaitingTimeDetailsEntity detailsEntity = ExcessiveWaitingTimeDetailsEntity.builder()
						.excessiveWaitTopupAmount(adjustmentRequest.getExcessiveWaitTopupAmount())
						.excessiveWaitTopupAmountSearch(adjustmentRequest.getExcessiveWaitTopupAmount() + StringUtils.EMPTY)
						.excessiveWaitTopupDateTime(adjustmentRequest.getTopupDateTime()).build();
				jobEntity.setExcessiveWaitTimeDetailsEntity(detailsEntity);
			}

			jobRepository.save(jobEntity);
		} catch (Exception e) {
			log.error("Exception occurred while processing adjustment event", e);
		}
	}
}
