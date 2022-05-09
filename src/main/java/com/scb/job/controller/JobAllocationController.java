package com.scb.job.controller;


import javax.validation.Valid;

import com.scb.job.entity.ExcessiveWaitingTimeDetailsEntity;
import com.scb.job.model.enumeration.EvBikeVendors;
import com.scb.job.model.enumeration.JobType;
import com.scb.job.model.response.JobReconciliationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scb.job.entity.JobEntity;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.model.response.JobConfirmResponse;
import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.RiderJobResponse;
import com.scb.job.service.JobService;
import com.scb.job.validation.JobRequestValidator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/job")
public class JobAllocationController  {

	@Autowired
	private JobService jobService;
	
	@Autowired
	private JobRequestValidator jobRequestValidator;


	@PostMapping(value = "/create-job")
	public ResponseEntity<JobDetail> createNewJob(@Valid @RequestBody NewJobRequest job)
	{
		jobRequestValidator.validateNewJobRequest(job);
		return new ResponseEntity<>(jobService.createJob(job), HttpStatus.CREATED);
	}
	
	
	@GetMapping(value = "/{jobId}")
	public ResponseEntity<JobEntity> getJobById(@PathVariable("jobId") String jobId){
		return new ResponseEntity<>(jobService.getJobById(jobId), HttpStatus.OK);
	}

	@ApiOperation( nickname = "Update Job Confirmation", value = "Job-Confirmation", response = JobConfirmResponse.class)
	@ApiResponses( value = {
			@ApiResponse( response = JobConfirmResponse.class, code = 200, message = "Job confirmed"),
			@ApiResponse( response = ErrorResponse.class, code = 400, message = "Could not update job confirmation!") })
	@PutMapping( value="/{jobId}/confirm")
	public ResponseEntity<JobConfirmResponse> getJobConfirmation(@NonNull @PathVariable( "jobId") String jobId) {
		log.info("Updating Job Confirmation for JobId:{}", jobId);
		return new ResponseEntity<>(jobService.confirmJobByJobId(jobId), HttpStatus.OK);
	}
   
  @ApiOperation(nickname = "get-rider-status-by-Id",
	      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
	      value = "Gets Rider status", response = RiderJobResponse.class)
   @GetMapping(value = "/running/rider/{riderId}")
   public ResponseEntity<RiderJobResponse> getJobByRiderIdStatus(@PathVariable("riderId") String riderId){
         return new ResponseEntity<>(jobService.getJobByRiderIdStatus(riderId), HttpStatus.OK);
   }

  @ApiOperation(nickname = "get-rider-hung-job-details",
	      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
	      value = "Gets job details", response = RiderJobResponse.class)
   @GetMapping(value = "/running/rider/job/{jobId}")
   public ResponseEntity<RiderJobResponse> getRunningJobDetails(@PathVariable("jobId") String jobId){
         return new ResponseEntity<>(jobService.getRunningJobDetails(jobId), HttpStatus.OK);
   }
  
	@GetMapping
	public ResponseEntity<List<JobDetail>> getJobDetails(
			@RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(name = "isEvBikeRider", required = false) Boolean isEvBikeRider,
			@RequestParam(name = "jobStatus", required = false) String status,
			@RequestParam(name = "evBikeVendor", required = false) EvBikeVendors evBikeVendor,
			@RequestParam(name = "rentingToday", required = false) Boolean rentingToday
	) {
		return new ResponseEntity<>(jobService.getJobDetails(from, to, isEvBikeRider, status, evBikeVendor, rentingToday), HttpStatus.OK);
	}

	@PutMapping(value = "/{jobId}/ewt")
	public ResponseEntity<Void> updateEwtAmount(@PathVariable("jobId") String jobId, @RequestBody ExcessiveWaitingTimeDetailsEntity ewt) {
		jobService.updateExcessiveWaitTimeAmount(jobId, ewt);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/reconciliation")
	public ResponseEntity<List<JobReconciliationResponse>> getJobsToReconcile(
			@RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(name = "jobType", required = false) List<JobType> jobType,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "jobId") String sort
	) {
		log.info("getting jobs for reconciliation from {} to {}", from ,to);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc(sort)));
		return ResponseEntity.ok(JobReconciliationResponse.of(jobService.getJobsToReconciliation(from, to, jobType, pageable)));
	}
}