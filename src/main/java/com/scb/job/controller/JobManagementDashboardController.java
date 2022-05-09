package com.scb.job.controller;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scb.job.model.request.JobManagementDashBoardResponseDto;
import com.scb.job.service.JobManagementDashBoardService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Validated
@RequestMapping("/job/dashboard")
public class JobManagementDashboardController {

  @Autowired
  private JobManagementDashBoardService jobManagementDashBoardService;

  @ApiOperation(nickname = "get-job-management-dashboard-summary",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Gets job Status Summary Count")
  @GetMapping("/summary")
  public ResponseEntity<JobManagementDashBoardResponseDto> getRiderManagementDashBoardSummary() {

    String requestId = UUID.randomUUID().toString();
    log.info(String.format("Jobs Management DashBoard Request Id - %s", requestId));

    return ResponseEntity.ok(jobManagementDashBoardService.getJobManagementDashBoardSummary(requestId));
  }



}
