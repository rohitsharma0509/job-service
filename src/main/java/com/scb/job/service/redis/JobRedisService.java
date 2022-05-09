package com.scb.job.service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scb.job.entity.redis.RiderJobAcceptedEntity;
import com.scb.job.repository.redis.RiderJobAcceptedRepository;
import lombok.extern.slf4j.Slf4j;
import static com.scb.job.constants.JobConstants.DEFAULT_REDIS_CACHE_TTL;

@Slf4j
@Service
public class JobRedisService {

  @Autowired
  private RiderJobAcceptedRepository riderJobAcceptedRepository;


  public void addRiderJobAcceptedToRedis(String riderId, String jobId) {
    try {
      log.info("Adding JobId:{}, RiderId:{} to Redis", jobId, riderId);
      RiderJobAcceptedEntity riderJobAcceptedEntity = RiderJobAcceptedEntity.builder().jobId(jobId)
          .riderId(riderId).ttl(DEFAULT_REDIS_CACHE_TTL).build();
      riderJobAcceptedRepository.save(riderJobAcceptedEntity);
      log.info("Saved JobId:{}, RiderId:{} to Redis", jobId, riderId);
    } catch (Exception ex) {
      log.error("Error Occured while inserting in Redis rider Id: {}, JobId:{}, Exception:{}",
          riderId, jobId, ex);
    }

  }

}
