package com.scb.job.service;


import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.model.response.JobConfirmResponse;
import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.RiderJobResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface JobService {

  JobDetail createJob(NewJobRequest job);

  JobEntity getJobById(String jobId);

  JobConfirmResponse confirmJobByJobId(String jobId);

  RiderJobResponse getJobByRiderIdStatus(String riderId);
  
  RiderJobResponse getRunningJobDetails(String jobId);

  List<JobDetail> getJobDetails(LocalDateTime from, LocalDateTime to, Boolean isEvBikeRider, String status, EvBikeVendors evBikeVendor, Boolean rentingToday);

  List<JobEntity> getJobsToReconciliation(LocalDateTime from, LocalDateTime to, List<JobType> jobType, Pageable pageable);

  void updateExcessiveWaitTimeAmount(String jobId, ExcessiveWaitingTimeDetailsEntity ewt);
}
