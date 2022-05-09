package com.scb.job.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JobSearchAggregationEntity {

    private List<JobEntity> data;

    private long total;
}