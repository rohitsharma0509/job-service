package com.scb.job.repository.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.scb.job.entity.redis.RiderJobAcceptedEntity;

@Repository
public interface RiderJobAcceptedRepository extends CrudRepository<RiderJobAcceptedEntity, String> {
    
}
