package com.scb.job.service;

import com.scb.job.constants.JobConstants;
import com.scb.job.constants.JobStatus;
import com.scb.job.entity.ExcelJobEntity;
import com.scb.job.entity.JobEntity;
import com.scb.job.entity.JobSearchAggregationEntity;
import com.scb.job.model.response.JobDetail;
import com.scb.job.model.response.SearchResponseDto;
import com.scb.job.repository.JobSearchRepository;
import com.scb.job.repository.impl.JobCustomRepositoryImpl;
import com.scb.job.util.CommonUtils;
import com.scb.job.util.JobHelper;
import com.scb.job.util.excelhelper.ExcelCreator;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.scb.job.constants.JobConstants.INITIAL_JOB_STATUS_EN;

@Service
@Log4j2
public class JobSearchService {

  @Autowired
  private JobSearchRepository jobSearchRepository;

  @Autowired
  private JobCustomRepositoryImpl jobCustomRepository;

  @Autowired
  private JobHelper jobHelper;

  public SearchResponseDto getJobDetailsBySearchTermWithFilterQuery(String query,
                                                                    List<String> filterquery, Pageable pageable) {
    log.info("request received at jobService at {}", System.currentTimeMillis());
    if (StringUtils.isEmpty(query)) {
      query = query.trim();
    }

    List<Order> orders = getSortedOrderList(pageable);
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    log.info("Sorting List After Modification-->" + pageable.getSort());

    Page<JobEntity> pageRiderProfile = null;
    List<JobEntity> riderProfiles;

    if (StringUtils.isNotEmpty(query)) {
      log.info("Search By Query text:{}", query);
      long start = System.currentTimeMillis();
      pageRiderProfile = findJobByTerm(query, orders, pageable);
      long end = System.currentTimeMillis();
      log.info("search by text query {} startAt: {}, endAt {}, timeTaken {}ms", query, start, end, (end - start));
    } else {
      log.info("Search By Filter query:{}", filterquery);
      Map<String, String> filtersQuery = new HashMap<>();
      if (!CollectionUtils.isEmpty(filterquery)) {
        filterquery.stream().forEach(filter -> {
          String filterValue[] = filter.split(":");
          if (filterValue.length >= 2) {
            filtersQuery.put(filterValue[0], filterValue[1]);
          }
        });
        long start = System.currentTimeMillis();
        pageRiderProfile = jobCustomRepository.searchJobsByFilterQuery(filtersQuery, pageable);
        long end = System.currentTimeMillis();
        log.info("search by filter query startAt: {}, endAt {}, timeTaken {}ms", start, end, (end-start));
      }
    }

    if (ObjectUtils.isEmpty(pageRiderProfile) || !pageRiderProfile.hasContent()) {
      log.error("Record not found for Query " + query);
      List<JobDetail> jobList = new ArrayList<>();
      log.info("request end in jobService at {}", System.currentTimeMillis());
      return SearchResponseDto.of(jobList, 0, 0, 0);
    }

    riderProfiles = pageRiderProfile.getContent();
    log.debug(String.format("Query Searched - %s", query));

    List<JobDetail> jobList =
        riderProfiles.stream().map(job -> formResponseFromEntity(job)).collect(Collectors.toList());

    log.info("request end in jobService at {}", System.currentTimeMillis());
    return SearchResponseDto.of(jobList, pageRiderProfile.getTotalPages(),
        pageRiderProfile.getTotalElements(), pageRiderProfile.getNumber() + 1);

  }


  @SneakyThrows
  private Page<JobEntity> findJobByTerm(String query, List<Order> orders, Pageable pageable){
    Map<String, Object> sortingMap = new HashMap<>();
    sortingMap.put("jobId", 1);/*Default sort*/
    if(orders != null && orders.size() > 0 ) {
      sortingMap = orders.stream()
              .collect(Collectors.toMap(Order::getProperty, v -> v.getDirection().equals(Sort.Direction.ASC) ? 1 : -1));
    }
    JobSearchAggregationEntity results = jobCustomRepository
            .findJobByTerm(query, pageable.getOffset(), pageable.getPageSize(), sortingMap);
    if(results == null || results.getData().size() == 0){
      return PageableExecutionUtils.getPage(Collections.emptyList(), pageable, ()-> 0);
    }
    return PageableExecutionUtils.getPage(results.getData(), pageable,
            results::getTotal);

  }

  public JobDetail formResponseFromEntity(JobEntity entity) {

    String jobStatus = "";
    if (!StringUtils.isEmpty(entity.getJobStatusKey())
        && entity.getJobStatusKey().equals(INITIAL_JOB_STATUS_EN))
      jobStatus = INITIAL_JOB_STATUS_EN;
    else {
      JobStatus status = JobStatus.findByStatusId(entity.getJobStatus());
      if (status != null)
        jobStatus = status.name();
    }

    return JobDetail.builder().jobId(entity.getJobId()).jobDate(entity.getJobDate())
        .orderId(entity.getOrderId()).jobStatus(entity.getJobStatus()).jobStatusEn(jobStatus)
        .jobStatusTh(entity.getJobStatusTh()).jobDesc(entity.getJobDesc())
        .startTime(entity.getStartTime()).finishTime(entity.getFinishTime())
        .haveReturn(entity.isHaveReturn()).jobType(entity.getJobType()).option(entity.getOption())
        .totalDistance(entity.getTotalDistance()).totalWeight(entity.getTotalWeight())
        .totalSize(entity.getTotalSize()).remark(entity.getRemark()).userType(entity.getUserType())
        .normalPrice(entity.getNormalPrice()).netPrice(entity.getNetPrice())
        .netPaymentPrice(CommonUtils.round(entity.getNetPaymentPrice())).taxAmount(CommonUtils.round(entity.getTaxAmount()))
        .discount(entity.getDiscount()).rating(entity.getRating())
        .locationList(entity.getLocationList()).driverName(entity.getDriverName())
        .driverPhone(entity.getDriverPhone()).creationDateTime(entity.getCreationDateTime())
        .lastUpdatedDateTime(entity.getLastUpdatedDateTime()).riderId(entity.getRiderId())
        .driverPhone(entity.getDriverPhone()).zoneId(entity.getZoneId()).otherDeductions(entity.getOtherDeductions())
        .jobTypeEnum(entity.getJobTypeEnum())
        .jobTypeEnumThai(entity.getJobTypeEnumThai())
        .zoneName(entity.getZoneName()).excessiveWaitTimeDetailsEntity(entity.getExcessiveWaitTimeDetailsEntity()).build();
  }

  public byte[] excelDownloader(String riderId) {

    List<JobEntity> jobList = jobSearchRepository.findRiderJobs(riderId);

    return ExcelCreator.excelCreator(jobList.stream()
        .map(job -> ExcelJobEntity.formExcelResponseFromEntity(job)).collect(Collectors.toList()),
        riderId);

  }

  public List<JobEntity> getJobDetailsByJobIdList(List<String> jobList) {

    return jobSearchRepository.findByJobIdIn(jobList);
  }


  public List<Order> getSortedOrderList(Pageable pageable) {
    List<Order> orders = new ArrayList<>();
    log.info("Sorting List Before Modification-->" + pageable.getSort());
    pageable.getSort().forEach(sortOrder -> {
      if (!(sortOrder.getProperty().equals("DESC") || sortOrder.getProperty().equals("ASC"))) {
        if (sortOrder.getProperty().equals(JobConstants.DESC) || sortOrder.getProperty().equals(JobConstants.ASC)) {
          Order orderLastElemnt = orders.get(orders.size() - 1);
          Direction direction =
              sortOrder.getProperty().equals(JobConstants.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
          orders.set(orders.size() - 1, getSortedField(orderLastElemnt.getProperty(), direction));
        } else {
          orders.add(getSortedField(sortOrder.getProperty(), sortOrder.getDirection()));
        }
        log.info(String.format("Sorting field %s order %s", sortOrder.getProperty(),
            sortOrder.getDirection()));
      }
    });
    
    if (ObjectUtils.isEmpty(orders)) {
      log.info(String.format("Sorting field %s order %s", "batchId", "ASC"));
      orders.add(new Order(Sort.Direction.ASC, JobConstants.JOB_ID));
    }

    return orders;
  }

  private Order getSortedField(String fieldName, Direction Direction) {
    return Direction.equals(Sort.Direction.ASC) ? new Order(Sort.Direction.ASC, fieldName)
        : new Order(Sort.Direction.DESC, fieldName);

  }

}
