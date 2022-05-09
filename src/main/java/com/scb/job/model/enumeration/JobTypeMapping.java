package com.scb.job.model.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum JobTypeMapping {
    J1(JobType.EXPRESS),
    J2(JobType.MART),
    J3(JobType.FOOD),
    J4(JobType.POINTX);


    private JobType value;

    JobTypeMapping(JobType value) {
        this.value = value;
    }
    @JsonValue
    public JobType getValue() {
        return value;
    }
}
