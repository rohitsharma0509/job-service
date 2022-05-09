package com.scb.job.controller;


import static com.scb.job.constants.JobConstants.JOB_SEARCH;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.job.entity.JobEntity;
import com.scb.job.model.response.SearchResponseDto;
import com.scb.job.view.View;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.SearchResponseDto;
import com.scb.job.service.JobSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping(JOB_SEARCH)
@Api(value = "Job Search Endpoints")
public class JobSearchController {

  @Autowired
  private JobSearchService jobSearchService;
  
  @ApiOperation(nickname = "get-search-job-by-from-to-Jobid-OrderId-name-status-merchant-Customer",
	      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
	      value = "Gets jobs details", response = JobDetail.class)
  @GetMapping
  public ResponseEntity<SearchResponseDto> getjobBySearchTerm(
      @ApiParam(value = "q", example = "rr",
          required = true) @RequestParam(name = "q", required = false, defaultValue = "") String query,
      @ApiParam(value = "filterquery", example = "viewby:allJobs",
          required = false) @RequestParam(name = "filterquery", required = false) List<String> filterquery,
      @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults(@SortDefault(sort = "jobId",
          direction = Sort.Direction.ASC)) Pageable pageable) {
    log.info(String.format("Query Searched - %s", query));
    if(!ObjectUtils.isEmpty(filterquery)) {
      filterquery.forEach(obj ->  log.info(obj.toString()));
    }
    return ResponseEntity.ok(this.jobSearchService.getJobDetailsBySearchTermWithFilterQuery(query, filterquery, pageable ));
  }
    @PostMapping("/byJobIds")
    @JsonView(value = View.JobDetailsView.class)
    public ResponseEntity<List<JobEntity>> getJobDetailsByJobIdList(@RequestBody List<String> jobList) {
        return ResponseEntity.ok(this.jobSearchService.getJobDetailsByJobIdList(jobList));
    }
}
