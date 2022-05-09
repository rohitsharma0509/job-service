package com.scb.job.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobStatus;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;

@Component
public class JobHelper {

  public String getJobId() {

    return new StringBuilder().
        append(JobConstants.JOB_ID_PREFIX).
        append(new SimpleDateFormat("yyMMdd").format(new Date())).
        append(100000 + new Random().nextInt(899999)).toString();
  }

	public static String getJobId(Integer sequenceId, String jobType) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
		String sequence = String.valueOf(sequenceId);
		if(sequenceId < 100000) {
			sequence = StringUtils.leftPad(sequence, 6, "0");
		}
		return new StringBuilder().
				append(JobConstants.JOB_TYPE_POINTX_NUMBER.equalsIgnoreCase(jobType)
						? JobConstants.POINTX_JOB_ID_PREFIX : JobConstants.JOB_ID_PREFIX).
				append(sdf.format(new Date())).
				append(sequence).toString();

	}

  public static String buildDriverImageUrl(String basePath, String riderId){
    return basePath + "/rider/api/get_driver_image/"+riderId;

  }

	public static List<Integer> getJobStatusList(String jobType) {
		List<Integer> jobStatuses;
		switch (jobType.toLowerCase()) {
			case JobConstants.ACTIVE_JOBS:
				jobStatuses = new ArrayList<>();
				jobStatuses.addAll(JobStatus.getActiveJobStatuses());
				break;
			case JobConstants.COMPLETED_JOBS:
				jobStatuses = new ArrayList<>();
				jobStatuses.add(JobStatus.FOOD_DELIVERED.getStatus());
				break;
			case JobConstants.ALL_JOBS:
				jobStatuses = new ArrayList<>();
				jobStatuses.addAll(JobStatus.getActiveJobStatuses());
				jobStatuses.add(JobStatus.FOOD_DELIVERED.getStatus());
				jobStatuses.add(JobStatus.RIDER_NOT_FOUND.getStatus());
				jobStatuses.add(JobStatus.ORDER_CANCELLED_BY_OPERATOR.getStatus());
				break;
			default:
				jobStatuses = new ArrayList<>();
		}
		return jobStatuses;
	}
  
  public static JobLocation getLocation(JobEntity jobEntity, int seq) {
	    return jobEntity.getLocationList().stream().filter(loc -> loc.getSeq() == seq).findFirst()
	        .orElseThrow(() -> new RuntimeException("Error fetching location data from job data"));
  }
}
