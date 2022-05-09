package com.scb.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.job.entity.JobEntity;
import com.scb.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdjustmentProcessTest {

    private static final String JOB_ID = "S210105687934";
    private static final int ZERO = 0;
    private static final int ONE = 1;

    @InjectMocks
    private AdjustmentProcess adjustmentProcess;

    @Mock
    private JobRepository jobRepository;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        adjustmentProcess = new AdjustmentProcess(mapper, jobRepository);
    }

    @Test
    void processAdjustmentEventTestForException() {
        when(jobRepository.getjobById(eq(JOB_ID))).thenThrow(new NullPointerException());
        adjustmentProcess.processAdjustmentEvent(getAdjustmentEvent());
        verify(jobRepository, times(ZERO)).save(any(JobEntity.class));
    }

    @Test
    void processAdjustmentEventTestForValidMsg() {
        when(jobRepository.getjobById(eq(JOB_ID))).thenReturn(new JobEntity());
        adjustmentProcess.processAdjustmentEvent(getAdjustmentEvent());
        verify(jobRepository, times(ONE)).save(any(JobEntity.class));
    }

    private String getAdjustmentEvent() {
        StringBuilder message = new StringBuilder("{\"jobId\":");
        message.append("\"").append(JOB_ID).append("\",");
        message.append("\"excessiveWaitTopupAmount\":10.0").append(",");
        message.append("\"otherDeductions\":10.0}");
        return message.toString();
        
    }
}
