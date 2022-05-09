package com.scb.job.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public final class KafkaReadService {
	
	@Autowired
	DataProcess process;

    @Autowired
    private AdjustmentProcess adjustmentProcess;
	
    private static final Logger logger = LoggerFactory.getLogger(KafkaReadService.class);

    @KafkaListener(topics = "${kafka.topic}")
    public void consume(String message) throws Exception {
    	process.processKafkaTopic(message);
    	logger.info(String.format("Data Consumed from Kafka topic: %s ", message));
    }

    @KafkaListener(topics = "${kafka.adjustment-topic}")
    public void consumeAdjustmentEvents(String message) {
        adjustmentProcess.processAdjustmentEvent(message);
        logger.info(String.format("Data Consumed from Kafka adjustment-topic: %s ", message));
    }
}
