package com.scb.job.repository.impl;

import com.mongodb.ReadConcern;
import com.scb.job.constants.JobConstants;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobSearchAggregationEntity;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.repository.JobCustomRepository;
import com.scb.job.util.DateUtils;
import com.scb.job.util.JobHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Repository
public class JobCustomRepositoryImpl implements JobCustomRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<JobEntity> findJobDetails(LocalDateTime from, LocalDateTime to, Boolean isEvBikeRider, String status, EvBikeVendors evBikeVendor, Boolean rentingToday) {
        Query query = new Query();
        List<Criteria> whereClauses = new ArrayList<>();
        whereClauses.add(Criteria.where("creationDateTime").gte(DateUtils.convertLocalDateTimeToString(from, JobConstants.SEARCH_DATE_FORMAT)));
        whereClauses.add(Criteria.where("creationDateTime").lte(DateUtils.convertLocalDateTimeToString(to, JobConstants.SEARCH_DATE_FORMAT)));
        if(Objects.nonNull(isEvBikeRider)) {
            whereClauses.add(Criteria.where("evBikeUser").is(isEvBikeRider));
        }
        if(StringUtils.isNotBlank(status)) {
            whereClauses.add(Criteria.where("jobStatusKey").is(status));
        }
        if(Objects.nonNull(evBikeVendor)) {
            whereClauses.add(Criteria.where("evBikeVendor").is(evBikeVendor));
        }
        if(Objects.nonNull(rentingToday)) {
            whereClauses.add(Criteria.where("rentingToday").is(rentingToday));
        }
        Criteria criteria = new Criteria().andOperator(whereClauses.toArray(new Criteria[whereClauses.size()]));
        query.addCriteria(criteria);

        return mongoTemplate.find(query, JobEntity.class);
    }

    @Override
    public List<JobEntity> getJobsToReconciliation(LocalDateTime from, LocalDateTime to, List<JobType> jobType, Pageable pageable) {
        final Query query = new Query();

        Criteria foodDeliveredCriteria = Criteria.where(JobConstants.FOOD_DELIVERED_TIME)
                .gte(DateUtils.convertLocalDateTimeToString(from, JobConstants.SEARCH_DATE_FORMAT))
                .lt(DateUtils.convertLocalDateTimeToString(to, JobConstants.SEARCH_DATE_FORMAT));
        Criteria orderCancelledCriteria = Criteria.where(JobConstants.ORDER_CANCELLED_BY_OPERATION_TIME)
                .gte(DateUtils.convertLocalDateTimeToString(from, JobConstants.SEARCH_DATE_FORMAT))
                .lt(DateUtils.convertLocalDateTimeToString(to, JobConstants.SEARCH_DATE_FORMAT));

        Criteria finalCriteria = new Criteria().orOperator(foodDeliveredCriteria, orderCancelledCriteria);
        query.addCriteria(finalCriteria);
        if(!CollectionUtils.isEmpty(jobType)) {
            query.addCriteria(jobType.size() == 1 ? Criteria.where(JobConstants.JOB_TYPE_ENUM).is(jobType.get(0))
                    : Criteria.where(JobConstants.JOB_TYPE_ENUM).in(jobType));
        }

        log.info("get reconciliation jobs query: {}", query);
        return mongoTemplate.find(query.with(pageable), JobEntity.class);
    }

    @Override
    public JobEntity updateExcessiveWaitTimeAmount(String jobId, ExcessiveWaitingTimeDetailsEntity ewt) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(JobConstants.JOB_ID).is(jobId));
        Update update = new Update();
        update.set("excessiveWaitTimeDetailsEntity", ewt);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        return mongoTemplate.findAndModify(query, update, options, JobEntity.class);
    }

    @Override
    public Page<JobEntity> searchJobByQuery(String jobId, String orderId, String riderId, Pageable pageable) {
        final Query query = new Query();
        query.withHint("jobId_orderId_riderId_documentType");
        List<Criteria> whereClauses = new ArrayList<>();
        whereClauses.add(Criteria.where("jobId").is(jobId));
        whereClauses.add(Criteria.where("orderId").is(orderId));
        whereClauses.add(Criteria.where("riderId").is(riderId));
        Criteria finalCriteria = new Criteria().orOperator(whereClauses.toArray(new Criteria[whereClauses.size()]));
        query.addCriteria(finalCriteria);
        List<JobEntity> results = mongoTemplate.find(query.with(pageable), JobEntity.class);
        return PageableExecutionUtils.getPage(results, pageable, () -> mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1), JobEntity.class));
    }

    @Override
    public Page<JobEntity> searchJobsByFilterQuery(Map<String, String> filtersQuery, Pageable pageable) {
        String viewBy = filtersQuery.getOrDefault(JobConstants.VIEW_BY, StringUtils.EMPTY).toLowerCase();
        String jobId = filtersQuery.getOrDefault(JobConstants.JOB_ID, StringUtils.EMPTY);
        String orderId = filtersQuery.getOrDefault(JobConstants.ORDER_ID, StringUtils.EMPTY);
        String riderId = filtersQuery.getOrDefault(JobConstants.RIDER_ID, StringUtils.EMPTY);
        String jobStatusEn = filtersQuery.getOrDefault(JobConstants.JOB_STATUS_EN, StringUtils.EMPTY);
        String jobStatusTh = filtersQuery.getOrDefault(JobConstants.JOB_STATUS_TH, StringUtils.EMPTY);
        String riderName = filtersQuery.getOrDefault(JobConstants.RIDER_NAME, StringUtils.EMPTY);
        String merchant = filtersQuery.getOrDefault(JobConstants.MERCHANT_NAME, StringUtils.EMPTY);
        String customer = filtersQuery.getOrDefault(JobConstants.CUSTOMER_NAME, StringUtils.EMPTY);
        String phoneNumber = filtersQuery.getOrDefault(JobConstants.PHONE_NUMBER, StringUtils.EMPTY);
        String createdDate = filtersQuery.getOrDefault(JobConstants.CREATED_DATE, StringUtils.EMPTY);
        String lastUpdatedDate = filtersQuery.getOrDefault(JobConstants.LAST_UPDATED_DATE, StringUtils.EMPTY);
        String fromDateStr = filtersQuery.getOrDefault(JobConstants.FROM_DATE, StringUtils.EMPTY);
        String toDateStr = filtersQuery.getOrDefault(JobConstants.TO_DATE, StringUtils.EMPTY);
        String netPrice = filtersQuery.getOrDefault(JobConstants.NET_PRICE, StringUtils.EMPTY);
        String otherDeductions = filtersQuery.getOrDefault(JobConstants.OTHER_DEDUCTIONS, StringUtils.EMPTY);
        String jobTypeEnum = filtersQuery.getOrDefault(JobConstants.JOB_TYPE_ENUM_THAI, StringUtils.EMPTY);
        String jobStatus = filtersQuery.getOrDefault(JobConstants.JOB_STATUS, StringUtils.EMPTY);
        String statusText = StringUtils.isEmpty(jobStatusTh) ? jobStatusEn : jobStatusTh;
        List<Integer> statusIds = getJobStatusIds(viewBy, jobStatus);
        LocalDate fromDate = DateUtils.parseToLocalDate(fromDateStr);
        LocalDate toDate = DateUtils.parseToLocalDate(toDateStr);
        LocalDate currentDateFilter = null;
        if ((JobConstants.ALL_JOBS.equals(viewBy) || JobConstants.COMPLETED_JOBS.equals(viewBy))
                && StringUtils.isBlank(fromDateStr) && StringUtils.isBlank(toDateStr)) {
            currentDateFilter = LocalDate.now();
        }

        final Query query = new Query();
        List<Criteria> whereClauses = getCriteriaList(viewBy, jobId, riderId, riderName, orderId, phoneNumber, createdDate
                , lastUpdatedDate, netPrice, jobTypeEnum, otherDeductions, statusIds, merchant
                , customer, statusText, currentDateFilter, fromDate, toDate);
        query.addCriteria(new Criteria().andOperator(whereClauses.toArray(new Criteria[whereClauses.size()])));

        List<JobEntity> results = mongoTemplate.find(query.with(pageable), JobEntity.class);
        return PageableExecutionUtils.getPage(results, pageable, () -> mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1), JobEntity.class));
    }

    private List<Integer> getJobStatusIds(String viewBy, String jobStatus) {
        if (JobConstants.RIDER_JOBS.equals(viewBy) && StringUtils.isEmpty(jobStatus)) {
            return JobHelper.getJobStatusList(JobConstants.ALL_JOBS);
        } else if (JobConstants.RIDER_JOBS.equals(viewBy) && StringUtils.isNotBlank(jobStatus)) {
            return JobHelper.getJobStatusList(jobStatus);
        } else {
            return JobHelper.getJobStatusList(viewBy);
        }
    }

    private List<Criteria> getCriteriaList(String viewBy, String jobId, String riderId, String riderName, String orderId, String phoneNumber, String createdDate
            , String lastUpdatedDate, String netPrice, String jobTypeEnum, String otherDeductions, List<Integer> statusIds, String merchant
            , String customer, String statusText, LocalDate currentDateFilter, LocalDate fromDate, LocalDate toDate) {
        List<Criteria> whereClauses = new ArrayList<>();
        addRegexCriteriaForValidValue(whereClauses, JobConstants.JOB_ID, jobId);
        if (JobConstants.RIDER_JOBS.equals(viewBy) && StringUtils.isNotBlank(riderId)) {
            whereClauses.add(Criteria.where(JobConstants.RIDER_ID).is(riderId));
        } else {
            addRegexCriteriaForValidValue(whereClauses, JobConstants.RIDER_ID, riderId);
        }
        addRegexCriteriaForValidValue(whereClauses, JobConstants.DRIVER_NAME, riderName);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.ORDER_ID, orderId);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.DRIVER_PHONE, phoneNumber);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.CREATED_DATE, createdDate);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.LAST_UPDATED_DATE, lastUpdatedDate);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.NET_PRICE_SEARCH, netPrice);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.JOB_TYPE_ENUM_THAI, jobTypeEnum);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.OTHER_DEDUCTIONS_SEARCH, otherDeductions);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.MERCHANT_NAME, merchant);
        addRegexCriteriaForValidValue(whereClauses, JobConstants.CUSTOMER_NAME, customer);

        if (!CollectionUtils.isEmpty(statusIds)) {
            whereClauses.add(Criteria.where(JobConstants.JOB_STATUS).in(statusIds));
        }
        if (StringUtils.isNotBlank(statusText)) {
            whereClauses.add(Criteria.where(JobConstants.JOB_STATUS_TH).regex(".*" + statusText + ".*", "i")
                    .orOperator(Criteria.where("jobStatusKey").regex(".*" + statusText + ".*", "i")));
        }
        if (Objects.nonNull(currentDateFilter)) {
            whereClauses.add(Criteria.where(JobConstants.JOB_DATE_FIELD).is(currentDateFilter));
        }
        if (Objects.nonNull(fromDate)) {
            whereClauses.add(Criteria.where(JobConstants.JOB_DATE_FIELD).gte(fromDate));
        }
        if (Objects.nonNull(toDate)) {
            whereClauses.add(Criteria.where(JobConstants.JOB_DATE_FIELD).lte(toDate));
        }
        return whereClauses;
    }

    private void addRegexCriteriaForValidValue(List<Criteria> whereClauses, String fieldName, String fieldValue) {
        if (StringUtils.isNotBlank(fieldValue)) {
            whereClauses.add(Criteria.where(fieldName).regex(".*" + fieldValue + ".*", "i"));
        }
    }

    @Override
    @SneakyThrows
    public JobSearchAggregationEntity findJobByTerm(String query, long skip,
                                                    long limit, Map<String, Object> sort) {
        Document result = mongoTemplate.getCollection("Job").withReadConcern(ReadConcern.LOCAL)
                .aggregate(Arrays.asList(new Document("$search",
                                new Document("index", "job_search")
                                        .append("text",
                                                new Document("query", query)
                                                        .append("path", Arrays.asList("jobId", "orderId", "riderId")))),
                        new Document("$facet",
                                new Document("metadata", Arrays.asList(new Document("$count", "total")))
                                        .append("data", Arrays.asList(new Document("$skip", skip),
                                                new Document("$limit", limit),
                                                new Document("$sort",
                                                        new Document(sort))))),
                        new Document("$project",
                                new Document("data", 1L)
                                        .append("total",
                                                new Document("$arrayElemAt", Arrays.asList("$metadata.total", 0L)))))).first();

        return result != null ? mongoTemplate.getConverter().read(JobSearchAggregationEntity.class, result) : null;

    }
}
