package com.scb.job.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaReadServiceTest {

  @Mock
  private DataProcess process;

  @Mock
  private AdjustmentProcess adjustmentProcess;

  @InjectMocks
  private KafkaReadService kafkaReadService;

  @Test
  void testConsume() throws Exception {
    doNothing().when(process).processKafkaTopic(anyString());
    kafkaReadService.consume("{\"message\": \"test\"}");
    verify(process, times(1)).processKafkaTopic(anyString());
    verifyNoMoreInteractions(adjustmentProcess);
  }

  @Test
  void testConsumeAdjustmentEvents() {
    doNothing().when(adjustmentProcess).processAdjustmentEvent(anyString());
    kafkaReadService.consumeAdjustmentEvents("{\"jobId\": \"1\"}");
    verify(adjustmentProcess, times(1)).processAdjustmentEvent(anyString());
    verifyNoMoreInteractions(process);
  }

}
