package com.scb.job.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.scb.job.entity.JobSearchAggregationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.scb.job.entity.JobEntity;

public interface JobSearchRepository extends MongoRepository<JobEntity, String> {

  final String filterByStatusAndQueryOnFields =
      "{'$and':[{'$and':[{'jobStatus': { $in:?5}},{'jobId': { $regex: /.*?0.*/, $options: 'i'}},"
          + "{'creationDateTime': { $regex: /.*?8.*/, $options: 'i'}},"
          + "{'lastUpdatedDateTime': { $regex: /.*?9.*/, $options: 'i'}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?6.*/, $options: 'i'},seq:1}}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?7.*/, $options: 'i'},seq:2}}},"
          + "{'orderId': { $regex: /.*?3.*/, $options: 'i'}}," 
          + "{'netPriceSearch':{ $regex: /^?11.*/, $options: 'i'}},"
          + "{'jobTypeEnumThai':{ $regex: /.*?12.*/, $options: 'i'}},"
          + "{'$or':[{'jobStatusTh': { $regex: /.*?10.*/, $options: 'i'}}"
          + "{'jobStatusKey': { $regex: /.*?10.*/, $options: 'i'}}]}]},"
          + "{'riderId': { $regex: /.*?1.*/, $options: 'i'}}, "
          + "{'driverName': { $regex: /.*?2.*/, $options: 'i'}},"
          + "{'driverPhone': { $regex: /.*?4.*/, $options: 'i'}},"
          + "]}";

  final String filterByStatusAndQueryOnFieldsWithDate =
          "{'$and':[{jobDateField:{'$gte': ?11,'$lte': ?12}},"
                  +"{'$and':[{'jobStatus': { $in:?5}},{'jobId': { $regex: /.*?0.*/, $options: 'i'}},"
                  + "{'creationDateTime': { $regex: /.*?8.*/, $options: 'i'}},"
                  + "{'lastUpdatedDateTime': { $regex: /.*?9.*/, $options: 'i'}},"
                  + "{locationList:{$elemMatch:{contactName:{$regex: /.*?6.*/, $options: 'i'},seq:1}}},"
                  + "{locationList:{$elemMatch:{contactName:{$regex: /.*?7.*/, $options: 'i'},seq:2}}},"
                  + "{'orderId': { $regex: /.*?3.*/, $options: 'i'}},"
                  + "{'netPriceSearch':{ $regex: /^?13.*/, $options: 'i'}},"
                  + "{'jobTypeEnumThai':{ $regex: /.*?14.*/, $options: 'i'}},"
                  + "{'$or':[{'jobStatusTh': { $regex: /.*?10.*/, $options: 'i'}}"
                  + "{'jobStatusKey': { $regex: /.*?10.*/, $options: 'i'}}]}]},"
                  + "{'riderId': { $regex: /.*?1.*/, $options: 'i'}}, "
                  + "{'driverName': { $regex: /.*?2.*/, $options: 'i'}},"
                  + "{'driverPhone': { $regex: /.*?4.*/, $options: 'i'}},"
                  + "]}";
  
  final String filterByStatusAndQueryOnFieldsWithCurrentDay =
      "{'$and':[{'$and':[{'jobStatus': { $in:?5}},{'jobDateField':?12},{'jobId': { $regex: /.*?0.*/, $options: 'i'}},"
          + "{'creationDateTime': { $regex: /.*?8.*/, $options: 'i'}},"
          + "{'lastUpdatedDateTime': { $regex: /.*?9.*/, $options: 'i'}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?6.*/, $options: 'i'},seq:1}}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?7.*/, $options: 'i'},seq:2}}},"
          + "{'orderId': { $regex: /.*?3.*/, $options: 'i'}}," 
          + "{'netPriceSearch':{ $regex: /^?11.*/, $options: 'i'}},"
          + "{'jobTypeEnumThai':{ $regex: /.*?12.*/, $options: 'i'}},"
          + "{'$or':[{'jobStatusTh': { $regex: /.*?10.*/, $options: 'i'}}"
          + "{'jobStatusKey': { $regex: /.*?10.*/, $options: 'i'}}]}]},"
          + "{'riderId': { $regex: /.*?1.*/, $options: 'i'}}, "
          + "{'driverName': { $regex: /.*?2.*/, $options: 'i'}},"
          + "{'driverPhone': { $regex: /.*?4.*/, $options: 'i'}},"
          + "]}";

  final String filterByAllJobsAndQueryOnFields =
      "{$and:[{jobDateField:{'$gte': ?11,'$lte': ?12}},"
      + "{'$and':[{'jobStatus': { $in:?5}}, {'jobId': { $regex: /.*?0.*/, $options: 'i'}},"
          + "{'creationDateTime': { $regex: /.*?8.*/, $options: 'i'}},"
          + "{'lastUpdatedDateTime': { $regex: /.*?9.*/, $options: 'i'}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?6.*/, $options: 'i'},seq:1}}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?7.*/, $options: 'i'},seq:2}}},"
          + "{'orderId': { $regex: /.*?3.*/, $options: 'i'}},"
          + "{'netPriceSearch':{ $regex: /^?13.*/, $options: 'i'}},"
          + "{'jobTypeEnumThai':{ $regex: /.*?14.*/, $options: 'i'}},"
          + "{'$or':[{'jobStatusTh': { $regex: /.*?10.*/, $options: 'i'}},"
          + "{'jobStatusKey': { $regex: /.*?10.*/, $options: 'i'}}]},"
          + "{'riderId': { $regex: /.*?1.*/, $options: 'i'}},"
          + "{'driverName': { $regex: /.*?2.*/, $options: 'i'}},"
          + "{'driverPhone': { $regex: /.*?4.*/, $options: 'i'}}]},"
          + "]}";

  final String filterRiderJobsByStatusAndQueryOnFields =
      "{$and:[{'riderId': ?1},{'$and':[{'jobId': { $regex: /.*?0.*/, $options: 'i'}},"
          + "{'driverName': { $regex: /.*?2.*/, $options: 'i'}},"
          + "{'driverPhone': { $regex: /.*?4.*/, $options: 'i'}},"
          + "{'creationDateTimeTh': { $regex: /.*?8.*/, $options: 'i'}},"
          + "{'lastUpdatedDateTimeTh': { $regex: /.*?9.*/, $options: 'i'}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?6.*/, $options: 'i'},seq:1}}},"
          + "{locationList:{$elemMatch:{contactName:{$regex: /.*?7.*/, $options: 'i'},seq:2}}},"
          + "{'orderId': { $regex: /.*?3.*/, $options: 'i'}}," 
          + "{'netPriceSearch':{ $regex: /^?11.*/, $options: 'i'}},"
          + "{'otherDeductionsSearch':{ $regex: /^?12.*/, $options: 'i'}},"
          + "{'jobTypeEnumThai':{ $regex: /.*?13.*/, $options: 'i'}},"
          + "{'jobStatus': { $in:?5}},"
          + "{'$or':[{'jobStatusTh': { $regex: /.*?10.*/, $options: 'i'}},"
          + "{'jobStatusKey': { $regex: /.*?10.*/, $options: 'i'}}]}]}]}";


/*  @Aggregation(pipeline = {"{$search: {index: 'job_search',text: {query: ?0,path: ['jobId', 'orderId', 'riderId']}}}",
          "{$facet: {metadata: [ { $count: 'total' } ],data: [ { $skip: ?1 }, { $limit: ?2 }, { $sort: ?3 } ]}}",
          "{$project: { data: 1,total: { $arrayElemAt: [ '$metadata.total', 0 ] }}}"})
  AggregationResults<JobSearchAggregationEntity> findJobByTerm(String query, long skip, long limit, Map<String, Integer> sort);*/


  @Query(filterByStatusAndQueryOnFields)
  Page<JobEntity> getJobsByStatusAndQueryOnFields(String jobId, String riderId, String name,
      String orderId, String phoneNumber, List<Integer> viewBy, String merchant, String customer,
      String createdDate, String lastUpdatedDate, String status, String netPrice,String jobTypeEnum, Pageable pageable);

  @Query(filterByStatusAndQueryOnFieldsWithDate)
  Page<JobEntity> getJobsByStatusAndQueryOnFields(String jobId, String riderId, String name, String orderId, String phoneNumber, List<Integer> viewBy, String merchant
          , String customer, String createdDate, String lastUpdatedDate, String status, LocalDate fromDate, LocalDate toDate, String netPrice,String jobTypeEnum, Pageable pageable);
  
  @Query(filterByStatusAndQueryOnFieldsWithCurrentDay)
  Page<JobEntity> getJobsByStatusAndQueryOnFieldsWithCurrentDate(String jobId, String riderId, String name,
      String orderId, String phoneNumber, List<Integer> viewBy, String merchant, String customer,
      String createdDate, String lastUpdatedDate, String status, String netPrice, LocalDate currentDate,String jobTypeEnum, Pageable pageable);



  @Query(filterByAllJobsAndQueryOnFields)
  Page<JobEntity> getAllJobsAndQueryOnFields(String jobId, String riderId, String riderName,
      String orderId, String phoneNumber, List<Integer> viewStatus, String merchant,
      String customer, String createdDate, String lastUpdatedDate, String status,
      LocalDate fromDate, LocalDate toDate, String netPrice,String jobTypeEnum, 
      Pageable pageable);

  @Transactional
  @Query(value = "{'riderId':?0}")
  List<JobEntity> findRiderJobs(String riderId);

  @Query(filterRiderJobsByStatusAndQueryOnFields)
  Page<JobEntity> findRiderJobs(String jobId, String riderId, String name, String orderId,
      String phoneNumber, List<Integer> viewBy, String merchant, String customer,
      String createdDate, String lastUpdatedDate, String status, 
      String netPrice, String otherDeductions,String jobTypeEnum,  Pageable pageable);


  List<JobEntity> findByJobIdIn(List<String> jobIdList);

}
