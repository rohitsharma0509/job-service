package com.scb.job.repository;

import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobSearchAggregationEntity;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface JobCustomRepository {

    List<JobEntity> findJobDetails(LocalDateTime from, LocalDateTime to, Boolean isEvBikeRider, String status, EvBikeVendors evBikeVendor, Boolean rentingToday);
    JobSearchAggregationEntity findJobByTerm(String query, long skip, long limit, Map<String, Object> sort);
    Page<JobEntity> searchJobByQuery(String jobId, String orderId, String riderId, Pageable pageable);
    Page<JobEntity> searchJobsByFilterQuery(Map<String, String> filtersQuery, Pageable pageable);
    List<JobEntity> getJobsToReconciliation(LocalDateTime from, LocalDateTime to, List<JobType> jobType, Pageable pageable);
    JobEntity updateExcessiveWaitTimeAmount(String jobId, ExcessiveWaitingTimeDetailsEntity ewt);
}
