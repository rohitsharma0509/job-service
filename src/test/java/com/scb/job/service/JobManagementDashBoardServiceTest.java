package com.scb.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.scb.job.entity.JobStatusAggregateCountEntity;
import com.scb.job.model.request.JobManagementDashBoardResponseDto;
import com.scb.job.repository.JobRepository;

@ExtendWith(MockitoExtension.class)
class JobManagementDashBoardServiceTest {

    @Mock
    private JobRepository mockJobRepository;

    @InjectMocks
    private JobManagementDashBoardService jobManagementDashBoardServiceUnderTest;

    @Test
    void testGetJobManagementDashBoardSummary() {
        JobManagementDashBoardResponseDto expectedResult = new JobManagementDashBoardResponseDto(0L, 0L, 0L);

        JobStatusAggregateCountEntity jobStatusAggregateCountEntity = new JobStatusAggregateCountEntity();
        jobStatusAggregateCountEntity.setAggregatestatus("aggregatestatus");
        jobStatusAggregateCountEntity.setJobCount(0L);
        AggregationResults<JobStatusAggregateCountEntity> jobStatusAggregateCountEntities = new AggregationResults<>(Arrays.asList(jobStatusAggregateCountEntity), new Document("key", "value"));
        when(mockJobRepository.groupByActiveJobStatus(any())).thenReturn(jobStatusAggregateCountEntities);

        JobStatusAggregateCountEntity jobStatusAggregateCountEntity1 = new JobStatusAggregateCountEntity();
        jobStatusAggregateCountEntity1.setAggregatestatus("aggregatestatus");
        jobStatusAggregateCountEntity1.setJobCount(0L);
        AggregationResults<JobStatusAggregateCountEntity> jobStatusAggregateCountEntities1 = new AggregationResults<>(Arrays.asList(jobStatusAggregateCountEntity1), new Document("key", "value"));
        when(mockJobRepository.groupByCompletedJobStatus(anyInt(), any())).thenReturn(jobStatusAggregateCountEntities1);

        JobStatusAggregateCountEntity jobStatusAggregateCountEntity2 = new JobStatusAggregateCountEntity();
        jobStatusAggregateCountEntity2.setAggregatestatus("aggregatestatus");
        jobStatusAggregateCountEntity2.setJobCount(0L);
        AggregationResults<JobStatusAggregateCountEntity> jobStatusAggregateCountEntities2 = new AggregationResults<>(Arrays.asList(jobStatusAggregateCountEntity2), new Document("key", "value"));
        when(mockJobRepository.groupByAllJob(any())).thenReturn(jobStatusAggregateCountEntities2);

        JobManagementDashBoardResponseDto result = jobManagementDashBoardServiceUnderTest.getJobManagementDashBoardSummary("requestId");

        assertEquals(expectedResult, result);
    }
}
