package com.scb.job.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class JobStatusAggregateCountEntity {

  private String aggregatestatus;
  private Long jobCount;
  
}
