package com.scb.job.controller;


import java.util.List;
import javax.validation.Valid;
import com.scb.job.model.response.DifferentialPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobLocationUpdatedBy;
import com.scb.job.entity.JobLocationHistory;
import com.scb.job.model.exception.ErrorResponse;
import com.scb.job.model.request.JobLocationUpdateDto;
import com.scb.job.service.JobLocationHistoryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Validated
@RequestMapping("/job")
public class JobLocationHistoryController  {

	@Autowired
	private JobLocationHistoryService jobLocationHistoryService;
	
	@GetMapping(value = "/{jobId}/location-update-history")
    public ResponseEntity<List<JobLocationHistory>> getJobLocationHistoryById(@PathVariable("jobId") String jobId){
        return new ResponseEntity<>(jobLocationHistoryService.getJobLocationHistoryById(jobId), HttpStatus.OK);
    }
	
	@GetMapping(value = "/{jobId}/location-update")
    public ResponseEntity<JobLocationHistory> getMerchantCustomerUpdateLocationWithNewPrice(@PathVariable("jobId") String jobId,
        @RequestParam(name = "addressType", required = true) JobLocationUpdatedBy addressType){
        return new ResponseEntity<>(jobLocationHistoryService.getUpdatedJobLocation(jobId, addressType), HttpStatus.OK);
    }

	@GetMapping(value = "/{jobId}/differential-price")
    public ResponseEntity<DifferentialPrice> getCustomerPaymentPrice(@PathVariable("jobId") String jobId){
        List<JobLocationHistory> jobLocationHistoryList = jobLocationHistoryService.getJobLocationHistoryById(jobId);
        return new ResponseEntity<>(JobLocationHistory.getCustomerPaymentPrice(jobLocationHistoryList), HttpStatus.OK);
    }
	
	@ApiOperation( nickname = "Update Merchant Or Customer Location", value = "Job-location", response = JobLocationHistory.class)
    @ApiResponses( value = {
            @ApiResponse( response = JobLocationHistory.class, code = 200, message = "Job Location Change"),
            @ApiResponse( response = ErrorResponse.class, code = 400, message = "Could not update job location!") })
    @PutMapping( value="/{jobId}/location-update")
    public ResponseEntity<JobLocationHistory> updateMerchantCustomerLocation(
            @NonNull @PathVariable( "jobId") String jobId,
            @RequestHeader(name = JobConstants.X_USER_ID, defaultValue = JobConstants.OPS_MEMBER) String userId,
            @Valid @RequestBody JobLocationUpdateDto jobLocationUpdateDto) {
        jobLocationUpdateDto.setUpdatedBy(userId);
        log.info("Updating Customer Or Merchant Location: JobId-{}, RequestBody-{}, ", jobId, jobLocationUpdateDto);
        return new ResponseEntity<>(jobLocationHistoryService.updateLocation(jobId, jobLocationUpdateDto), HttpStatus.OK);
    }

}