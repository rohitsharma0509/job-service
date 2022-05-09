package com.scb.job.controller;


import static com.scb.job.constants.JobConstants.RIDER_JOB_DETAILS_DOWNLOAD;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scb.job.entity.DocumentType;
import com.scb.job.model.response.JobDetail;
import com.scb.job.service.JobSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(RIDER_JOB_DETAILS_DOWNLOAD)
@Api(value = "Job Search Endpoints")
public class RiderjobExcelDownloadController {

  @Autowired
  private JobSearchService jobSearchService;

  private static final String PATTERN = "dd-M-yyyy_hh:mm:ss";

  @ApiOperation(nickname = "get-search-job-by-from-to-Jobid-OrderId-name-status-merchant-Customer",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Gets jobs details", response = JobDetail.class)
  @GetMapping("/{riderId}")
  public ResponseEntity<byte[]> getjobBySearchTerm(@PathVariable("riderId") String riderId,
      @RequestParam(name = "docType", required = false) DocumentType documentType) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);
    ZonedDateTime currentETime = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
    return ResponseEntity.ok().header("Content-type", "application/octet-stream")
        .header("Content-disposition", "attachment; filename=\"" + riderId + "_All_Jobs_"
            + formatter.format(currentETime) + ".xls\"")
        .body(this.jobSearchService.excelDownloader(riderId));

  }
}
