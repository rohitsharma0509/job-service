package com.scb.job.model.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum JobType {
	EXPRESS("ด่วน"),
    MART("มาร์ท"),
    FOOD("อาหาร"),
    POINTX("pointX");


    private String status;

    JobType(String status) {
        this.status = status;
    }
    
    
    public String getStatus() {
        return status;
    }
    
}