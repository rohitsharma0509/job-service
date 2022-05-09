package com.scb.job.kafka.consumer;

import java.util.Collections;
import java.util.List;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;

public class CustomCooperativeStickyAssignor extends CooperativeStickyAssignor {

  @Override
  public List<RebalanceProtocol> supportedProtocols() {
    return Collections.singletonList(RebalanceProtocol.COOPERATIVE);
  }

}
