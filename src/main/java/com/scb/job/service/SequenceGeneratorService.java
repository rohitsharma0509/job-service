package com.scb.job.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import com.scb.job.entity.DatabaseSequence;

@Service
@Slf4j
public class SequenceGeneratorService {

  private static final String YY_MM_DD = "yyMMdd";

  private MongoOperations mongoOperations;

  private static final long SEQ_START_WITH = 1;
  private static final long INCREMENT_BY = 1;
  
  private static final long LIMIT = 9999999;

  @Autowired
  public SequenceGeneratorService(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  public String generateSequence(String seqName) {

    Query query = new Query(Criteria.where("_id").is(seqName));

    Update update = new Update();
    update.inc("seq", INCREMENT_BY);
    

    FindAndModifyOptions options = new FindAndModifyOptions();
    options.returnNew(true);
    
    log.info("Generating sequence for jobId");
    DatabaseSequence databaseSequence =
        mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);

    
    if (Objects.isNull(databaseSequence)) {
      databaseSequence = mongoOperations.save(new DatabaseSequence(seqName, SEQ_START_WITH));
    }
    
    //Reseting the Sequence for Current Day if greater than 999999
    long dbSequence = databaseSequence.getSeq() % LIMIT;    
        
    log.info("Sequence generated");
    return getJobId(dbSequence);
  }
  
  
  public String getJobId(long databaseSequence) {
    String sequence = Long.toString(databaseSequence);
    
    if(databaseSequence < 100000) {
      sequence = StringUtils.leftPad(sequence, 6, "0");
    }
    return new StringBuilder().
        append(new SimpleDateFormat(YY_MM_DD).format(new Date())).
        append(sequence).toString();
  }
}
