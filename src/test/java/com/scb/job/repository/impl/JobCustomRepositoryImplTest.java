package com.scb.job.repository.impl;

import com.google.common.io.Resources;
import com.mongodb.ReadConcern;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.scb.job.constants.JobConstants;
import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobSearchAggregationEntity;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCustomRepositoryImplTest {

    private static final String JOB_ID = "S220418064853";
    private static final String RIDER_ID = "RR00001";
    private static final String FOOD_DELIVERED = "FOOD_DELIVERED";
    private static final String TEST = "test";
    private static final double AMOUNT = 10.0;

    @InjectMocks
    private JobCustomRepositoryImpl jobCustomRepositoryImpl;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void testFindJobDetailsForZeroRecords() {
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(Collections.emptyList());
        List<JobEntity> result = jobCustomRepositoryImpl.findJobDetails(LocalDateTime.now(), LocalDateTime.now(), Boolean.FALSE, FOOD_DELIVERED, EvBikeVendors.ETRAN, Boolean.FALSE);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testFindJobByTerm() {
        Map<String, Object> sortingMap = new HashMap<>();
        sortingMap.put("jobId", 1);
        AggregateIterable iterable = mock(AggregateIterable.class);
        MongoConverter mockConverter = mock(MongoConverter.class);
        MongoCollection<Document> mockCollection = mock(MongoCollection.class);
        when(mongoTemplate.getCollection("Job")).thenReturn(mockCollection);
        when(mockCollection.withReadConcern(ReadConcern.LOCAL)).thenReturn(mockCollection);
        when(mockCollection.aggregate(anyList())).thenReturn(iterable);
        when(mongoTemplate.getConverter()).thenReturn(mockConverter);
        when(mockConverter.read(any(), any(Document.class))).thenReturn(new JobSearchAggregationEntity());

        when(iterable.first()).thenReturn(Document.parse(getTestData("data/data_delivered.json")));

        JobSearchAggregationEntity result = jobCustomRepositoryImpl.findJobByTerm("S210712176524", 0, 1, sortingMap);
        Assertions.assertNotNull(result);
    }

    @Test
    void testFindJobDetails() {
        List<JobEntity> mappedResults = new ArrayList<>();
        mappedResults.add(getJobEntity());
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(mappedResults);
        List<JobEntity> result = jobCustomRepositoryImpl.findJobDetails(LocalDateTime.now(), LocalDateTime.now(), Boolean.FALSE, FOOD_DELIVERED, EvBikeVendors.ETRAN, Boolean.FALSE);
        Assertions.assertEquals(RIDER_ID, result.get(0).getRiderId());
    }

    @Test
    void testGetJobsToReconciliation() {
        List<JobEntity> mappedResults = new ArrayList<>();
        mappedResults.add(getJobEntity());
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(mappedResults);
        List<JobEntity> result = jobCustomRepositoryImpl.getJobsToReconciliation(LocalDateTime.now(), LocalDateTime.now(), Arrays.asList(JobType.FOOD), Pageable.unpaged());
        Assertions.assertEquals(RIDER_ID, result.get(0).getRiderId());
    }

    @Test
    void testSearchJobByQuery() {
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(Arrays.asList(getJobEntity()));
        Page<JobEntity> result = jobCustomRepositoryImpl.searchJobByQuery(RIDER_ID, RIDER_ID, RIDER_ID, Pageable.unpaged());
        Assertions.assertEquals(RIDER_ID, result.getContent().get(0).getRiderId());
    }

    @Test
    void shouldUpdateExcessiveWaitTimeAmount() {
        ExcessiveWaitingTimeDetailsEntity ewt = ExcessiveWaitingTimeDetailsEntity.builder().excessiveWaitTopupAmount(AMOUNT).build();
        JobEntity jobEntity = JobEntity.builder().jobId(JOB_ID).excessiveWaitTimeDetailsEntity(ewt).build();
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(JobEntity.class))).thenReturn(jobEntity);
        JobEntity result = jobCustomRepositoryImpl.updateExcessiveWaitTimeAmount(JOB_ID, ewt);
        Assertions.assertEquals(JOB_ID, result.getJobId());
        Assertions.assertEquals(AMOUNT, result.getExcessiveWaitTimeDetailsEntity().getExcessiveWaitTopupAmount());
    }

    @Test
    void searchJobsByFilterQueryTestForDatePeriod() {
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(Arrays.asList(getJobEntity()));
        Map<String, String> filterQuery = new HashMap<>();
        filterQuery.put(JobConstants.VIEW_BY, JobConstants.RIDER_JOBS);
        filterQuery.put(JobConstants.RIDER_ID, RIDER_ID);
        filterQuery.put(JobConstants.JOB_STATUS_TH, TEST);
        filterQuery.put(JobConstants.FROM_DATE, "2021-10-01");
        filterQuery.put(JobConstants.TO_DATE, "2021-10-01");
        Page<JobEntity> result = jobCustomRepositoryImpl.searchJobsByFilterQuery(filterQuery, Pageable.unpaged());
        Assertions.assertEquals(RIDER_ID, result.getContent().get(0).getRiderId());
    }

    @Test
    void searchJobsByFilterQueryTest() {
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(Arrays.asList(getJobEntity()));
        Map<String, String> filterQuery = new HashMap<>();
        filterQuery.put(JobConstants.VIEW_BY, JobConstants.ALL_JOBS);
        filterQuery.put(JobConstants.RIDER_ID, RIDER_ID);
        filterQuery.put(JobConstants.JOB_STATUS, TEST);
        Page<JobEntity> result = jobCustomRepositoryImpl.searchJobsByFilterQuery(filterQuery, Pageable.unpaged());
        Assertions.assertEquals(RIDER_ID, result.getContent().get(0).getRiderId());
    }

    @Test
    void testFindJobDetailsForZeroRecordsWithNullChecks() {
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(Collections.emptyList());
        List<JobEntity> result = jobCustomRepositoryImpl.findJobDetails(LocalDateTime.now(), LocalDateTime.now(), null, null, null, null);
        Assertions.assertEquals(0, result.size());
    }
    @Test
    void testGetJobsToReconciliationWithNullJobType() {
        List<JobEntity> mappedResults = new ArrayList<>();
        mappedResults.add(getJobEntity());
        when(mongoTemplate.find(any(Query.class), eq(JobEntity.class))).thenReturn(mappedResults);
        List<JobEntity> result = jobCustomRepositoryImpl.getJobsToReconciliation(LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), Pageable.unpaged());
        Assertions.assertEquals(RIDER_ID, result.get(0).getRiderId());
    }


    private static JobEntity getJobEntity() {
        return JobEntity.builder().riderId(RIDER_ID).build();
    }

    private String getTestData(String path) {
        try {
            return Resources.toString(Resources.getResource(path), Charset.defaultCharset());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
