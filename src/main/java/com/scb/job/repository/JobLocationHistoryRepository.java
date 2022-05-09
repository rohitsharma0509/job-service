package com.scb.job.repository;


import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocationHistory;


@Repository
public interface JobLocationHistoryRepository extends MongoRepository<JobLocationHistory, String> {
  
  List<JobLocationHistory> getJobLocationHistoryByJobId(String jobId);
  
}