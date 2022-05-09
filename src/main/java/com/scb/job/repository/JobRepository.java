package com.scb.job.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobStatusAggregateCountEntity;

@Repository
public interface JobRepository extends MongoRepository<JobEntity, String>, JobCustomRepository {

	@Transactional
	@Query(value = "{'jobId' : ?0}")
	JobEntity getjobById(String id);

	@Aggregation(pipeline = { "{$match:{'jobStatus':{$in:?0}}}", "{$count: 'jobIds'}",
			"{$project: {_id:'$jobs','aggregatestatus':'ActiveJobCount',jobCount:'$jobIds'}}" })
	AggregationResults<JobStatusAggregateCountEntity> groupByActiveJobStatus(List<Integer> activeJobStatuses);

	@Aggregation(pipeline = { "{$match:{'jobStatus':?0, 'jobDateField':?1}}", "{$count: 'jobIds'}",
			"{$project: {_id:'$jobs','aggregatestatus':'CompletedJobCount',jobCount:'$jobIds'}}" })
	AggregationResults<JobStatusAggregateCountEntity> groupByCompletedJobStatus(int jobstatus, LocalDate todaysDate);

	@Aggregation(pipeline = { "{$match:{'jobDateField':?0}}", "{$count: 'jobIds'}",
			"{$project: {_id:'$jobs','aggregatestatus':'totalJobs',jobCount:'$jobIds'}}" })
	AggregationResults<JobStatusAggregateCountEntity> groupByAllJob(LocalDate todaysDate);

	@Transactional
	@Query(value = "{'jobStatus':{$in:?0}}")
	List<JobEntity> getjobByStatus(List<Integer> jobStatuses);

	@Transactional
	@Query(value = "{$and:[{jobStatus:{$in:?0}}, {driverId:?1}]}")
	List<JobEntity> getJobByRiderIdStatus(List<Integer> list, String riderId);



}