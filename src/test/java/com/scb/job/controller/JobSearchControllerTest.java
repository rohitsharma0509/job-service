package com.scb.job.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.scb.job.constants.JobConstants;
import com.scb.job.entity.JobEntity;
import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.SearchResponseDto;
import com.scb.job.service.JobSearchService;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JobSearchControllerTest {

  @InjectMocks
  private JobSearchController jobSearchController;

  @Mock
  private JobSearchService jobSearchService;

  private static SearchResponseDto searchResponseDto;

  private static Pageable pageable;

  @BeforeAll
  static void setUp() {
    List<JobDetail> jobDetailsDtoList = new ArrayList<JobDetail>();

    jobDetailsDtoList.add(JobDetail.builder().jobId("1112").jobDate("2021-01-01")
        .jobStatus(JobConstants.INITIAL_JOB_STATUS).jobStatusEn(JobConstants.INITIAL_JOB_STATUS_EN)
        .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH).jobDesc("Test").startTime("00:10")
        .finishTime("00:20").haveReturn(JobConstants.DEFAULT_HAVE_RETURN).jobType("Test")
        .option("1").totalDistance(1.1).totalWeight(1.1).totalSize(1.1).remark("1")
        .userType(JobConstants.DEFAULT_USER_TYPE).normalPrice(1.2).netPrice(1.4).discount(0.0)
        .rating(4.0).locationList(null).creationDateTime(LocalDateTime.now().toString())
        .lastUpdatedDateTime(LocalDateTime.now().toString()).build());

    searchResponseDto = SearchResponseDto.of(jobDetailsDtoList, 1, 2, 1);

    pageable = PageRequest.of(0, 5);

  }

  @Test
  void getRiderProfileByTermTest() {

    when(jobSearchService.getJobDetailsBySearchTermWithFilterQuery("John",
        new ArrayList<String>(), pageable)).thenReturn(searchResponseDto);

    ResponseEntity<SearchResponseDto> searchResponseDtoController =
        jobSearchController.getjobBySearchTerm("John", new ArrayList<String>(), pageable);
    assertEquals(searchResponseDtoController.getBody(), searchResponseDto);
    assertTrue(ObjectUtils.isNotEmpty(searchResponseDtoController.getBody()));
    assertEquals(HttpStatus.OK, searchResponseDtoController.getStatusCode());
  }
  
  @Test
  void getJobDetailsByJobIdList_Test() {

    when(jobSearchService.getJobDetailsByJobIdList(Matchers.anyList())).thenReturn(getRiderStatusJob());
    ResponseEntity<List<JobEntity>> jobDetailsController =
        jobSearchController.getJobDetailsByJobIdList( new ArrayList<String>());
    assertTrue(ObjectUtils.isNotEmpty(jobDetailsController.getBody()));
    assertEquals(HttpStatus.OK, jobDetailsController.getStatusCode());
  }
  
  
  private static List<JobEntity> getRiderStatusJob() {
  	JobEntity je=JobEntity.builder().riderId("rider190").build();
  	JobEntity je1=JobEntity.builder().riderId("rider191").build();
  	List<JobEntity> list=new ArrayList<JobEntity>();
  	list.add(je);list.add(je1);
  	return list;
  }
}
