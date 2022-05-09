package com.scb.job.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDto {

  private Integer totalPages;

  private Long totalCount;
  
  private Integer currentPage;

  private List<JobDetail> jobDetails;


  public static SearchResponseDto of(List<JobDetail> jobs, int totalPages, long l, int currentPageNumber) {
    return SearchResponseDto.builder().jobDetails(jobs)
        .totalPages(totalPages).totalCount(l).currentPage(currentPageNumber).build();
    
  }
}
