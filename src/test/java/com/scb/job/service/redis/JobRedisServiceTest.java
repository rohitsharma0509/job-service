package com.scb.job.service.redis;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.scb.job.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.scb.job.repository.redis.RiderJobAcceptedRepository;


@ExtendWith(MockitoExtension.class)
class JobRedisServiceTest {

  @InjectMocks
  private JobRedisService jobRedisService;
  
  @Mock
  private RiderJobAcceptedRepository riderJobAcceptedRepository;
  
  @Test
  void addRiderJobAcceptedToRedisTest() {
    jobRedisService.addRiderJobAcceptedToRedis("12", "12");
    verify(riderJobAcceptedRepository,times(1)).save(any());

  }
  @Test()
  void addRiderJobAcceptedToRedisExceptionTest() {
    when(riderJobAcceptedRepository.save(any())).thenThrow(RuntimeException.class);
    jobRedisService.addRiderJobAcceptedToRedis("12", "12");
  }
  
}
