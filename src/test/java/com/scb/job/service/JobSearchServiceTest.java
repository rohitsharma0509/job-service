package com.scb.job.service;

import static com.scb.job.constants.JobConstants.INITIAL_JOB_STATUS_EN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import com.scb.job.entity.JobSearchAggregationEntity;
import com.scb.job.repository.impl.JobCustomRepositoryImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.scb.job.constants.JobConstants;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobLocation;
import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.SearchResponseDto;
import com.scb.job.repository.JobSearchRepository;
import com.scb.job.util.JobHelper;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JobSearchServiceTest {
  @Mock
  private JobSearchRepository jobSearchRepository;

  @Mock
  private JobCustomRepositoryImpl jobCustomRepository;

  @Mock
  JobHelper jobHelper;

  @InjectMocks
  private JobSearchService jobSearchService;

  private JobEntity job;

  private static Pageable pageable;

  private static Page<JobEntity> jobEntityPaged;

  private static SearchResponseDto searchResponseDto;

  public static JobEntity createJobWithFields() {
    JobEntity job = JobEntity.builder().jobId("1112").jobDate("2021-01-01")
        .jobStatus(JobConstants.INITIAL_JOB_STATUS).jobStatusEn(INITIAL_JOB_STATUS_EN)
        .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH).jobDesc("Test").startTime("00:10")
        .finishTime("00:20").haveReturn(JobConstants.DEFAULT_HAVE_RETURN).jobType("Test")
        .option("1").totalDistance(1.1).totalWeight(1.1).totalSize(1.1).remark("1")
        .userType(JobConstants.DEFAULT_USER_TYPE).normalPrice(1.2).netPrice(1.4).discount(0.0)
        .rating(4.0).callbackUrl("Test").locationList(null)
        .creationDateTime(LocalDateTime.now().toString())
        .lastUpdatedDateTime(LocalDateTime.now().toString()).zoneId(12).zoneName("Test")
            .merchantConfirm(true).merchantConfirmDateTime(LocalDateTime.now())
            .build();
    return job;

  }

  @BeforeEach
  public void initTest() {
    job = createJobWithFields();
    pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name"));
    List<JobEntity> jobDetailsList = new ArrayList<>();
    jobDetailsList.add(job);
    jobEntityPaged = new PageImpl(jobDetailsList);

    List<JobDetail> jobDetailsDtoList = new ArrayList<>();

    jobDetailsDtoList.add(JobDetail.builder().jobId("1112").jobDate("2021-01-01")
        .jobStatus(JobConstants.INITIAL_JOB_STATUS).jobStatusEn(INITIAL_JOB_STATUS_EN)
        .jobStatusTh(JobConstants.INITIAL_JOB_STATUS_TH).jobDesc("Test").startTime("00:10")
        .finishTime("00:20").haveReturn(JobConstants.DEFAULT_HAVE_RETURN).jobType("Test")
        .option("1").totalDistance(1.1).totalWeight(1.1).totalSize(1.1).remark("1")
        .userType(JobConstants.DEFAULT_USER_TYPE).normalPrice(1.2).netPrice(1.4).discount(0.0)
        .rating(4.0).locationList(null).creationDateTime(LocalDateTime.now().toString())
        .lastUpdatedDateTime(LocalDateTime.now().toString()).build());

    searchResponseDto = SearchResponseDto.of(jobDetailsDtoList, 1, 2, 1);
  }

  @Test
  void getRiderProfileBySearchTermTestForEmptyResponse() {
    jobEntityPaged = new PageImpl(new ArrayList<>());
    when(jobCustomRepository.searchJobByQuery("Test", "Test", "Test", pageable)).thenReturn(jobEntityPaged);
    SearchResponseDto searchedJob = jobSearchService.getJobDetailsBySearchTermWithFilterQuery("Test", new ArrayList<String>(List.of("default")), pageable);
    assertTrue(searchedJob.getJobDetails().isEmpty());
  }

  @Test
  void getRiderProfileBySearchTermTest() {

    JobSearchAggregationEntity jobSearchAggregationEntity = new JobSearchAggregationEntity(jobEntityPaged.getContent(), 1);
    when(jobCustomRepository.findJobByTerm(anyString(), anyLong(),anyLong(),any(HashMap.class)))
            .thenReturn(jobSearchAggregationEntity);

    SearchResponseDto searchedJob = jobSearchService
            .getJobDetailsBySearchTermWithFilterQuery("Test", new ArrayList<String>(List.of("default:default")), pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedJob));
    assertEquals(searchedJob.getJobDetails().get(0).getJobId(),
            searchResponseDto.getJobDetails().get(0).getJobId());
    verify(jobCustomRepository, times(1))
            .findJobByTerm(anyString(), anyLong(),anyLong(),any(HashMap.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"viewby:activeJobs", "viewby:empty"})
  void getActiveAndCompletedJobsByFieldSearchTest(String filter) {

    List<String> filters = new ArrayList<>();
    filters.add(filter);

    when(jobCustomRepository.searchJobsByFilterQuery(any(), any())).thenReturn(jobEntityPaged);
    
    SearchResponseDto searchedJob =
        jobSearchService.getJobDetailsBySearchTermWithFilterQuery("", filters, pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedJob));
    assertEquals(searchedJob.getJobDetails().get(0).getJobId(),
        searchResponseDto.getJobDetails().get(0).getJobId());
  }

  @ParameterizedTest
  @ValueSource(strings = {"viewby:activeJobs","viewby:alljobs"})
  void searchActiveJobsByDateTest(String filter) {
    List<String> filters = new ArrayList<>();
    filters.add(filter);
    filters.add("fromDate:2021-08-18");
    filters.add("toDate:2021-08-20");
    List<Integer> l1= new ArrayList<>();
    l1.add(5);

    when(jobCustomRepository.searchJobsByFilterQuery(any(), any())).thenReturn(jobEntityPaged);
    
    SearchResponseDto searchedJob =
            jobSearchService.getJobDetailsBySearchTermWithFilterQuery("", filters, pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedJob));
    assertEquals(searchedJob.getJobDetails().get(0).getJobId(),
            searchResponseDto.getJobDetails().get(0).getJobId());
    List<String> filters1 = new ArrayList<>();
    filters1.add(filter);

    when(jobCustomRepository.searchJobsByFilterQuery(any(), any())).thenReturn(jobEntityPaged);
    SearchResponseDto searchedJob1 =
            jobSearchService.getJobDetailsBySearchTermWithFilterQuery("", filters1, pageable);
    
    
    assertTrue(ObjectUtils.isNotEmpty(searchedJob1));
   
  }

  @ParameterizedTest
  @ValueSource(strings = { "viewby:riderjobs"})
  void getAllJobsByFieldSearchTest(String filter) {

    List<String> filters = new ArrayList<>();
    filters.add(filter);

    when(jobCustomRepository.searchJobsByFilterQuery(any(), any())).thenReturn(jobEntityPaged);
    
    SearchResponseDto searchedJob =
        jobSearchService.getJobDetailsBySearchTermWithFilterQuery("", filters, pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedJob));
  }
  
  @Test
  void test_Excel_Downloader() {
      List<JobEntity> jobEntities = getJobEntityLists();
      when(jobSearchRepository.findRiderJobs("riderId")).thenReturn(jobEntities);
      byte[] result = jobSearchService.excelDownloader("riderId");
      assertNotNull(result);
  }
  
  @Test
  void getJobDetailsByJobIdList_Test() {
       List<JobEntity> jobEntities = getJobEntityLists();
       when(jobSearchRepository.findByJobIdIn(any())).thenReturn(jobEntities);
       List<JobEntity> result = jobSearchService.getJobDetailsByJobIdList(Arrays.asList("test1"));
       assertSame(jobEntities, result);
  }
  
  @Test
  void getJobDetailsByJobIdList_Test_Empty_Items() {
      when(jobSearchRepository.findByJobIdIn(any())).thenReturn(Collections.emptyList());
      List<JobEntity> result = jobSearchService.getJobDetailsByJobIdList(Arrays.asList("test"));
      assertTrue(result.isEmpty());
  }
  
 @Test
 void formResponseFromEntityTest(){
   List<JobEntity> jobEntities = getJobEntityLists();
   jobEntities.get(0).setJobStatusKey(INITIAL_JOB_STATUS_EN);
   assertNotNull(jobSearchService.formResponseFromEntity(jobEntities.get(0)));
 }
  
  private static List<JobEntity> getJobEntityLists() {
  	JobLocation location1= new JobLocation();
  	JobLocation location2= new JobLocation();
  	List<JobLocation> locaList= new ArrayList<>();
  	locaList.add(location1);locaList.add(location2);
  	JobEntity jent=JobEntity.builder().riderId("riderId").
  				jobAcceptedTime("2021-02-04T17:45:55").locationList(locaList).build();
  	JobEntity jent1=JobEntity.builder().riderId("riderId1")
  			.jobAcceptedTime("2021-02-04T16:25:45").locationList(locaList).build();
  	List<JobEntity> list=new ArrayList<>();
  	list.add(jent);list.add(jent1);
  	return list;
  }
}
