package com.scb.job.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequest {
	
	private int status_after;

    private String callback_desc;

    private String job_id;

    private int status_before;
    
    private String status_datetime;

}
