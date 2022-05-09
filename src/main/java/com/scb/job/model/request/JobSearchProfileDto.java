package com.scb.job.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Api(value = "RiderProfile")
@ApiModel(value = "RiderProfile")
public class JobSearchProfileDto {

  @ApiModelProperty(notes = "It is used for searching rider.", name = "riderId")
  private String riderId;
  
  private String id;

  @JsonIgnore
  private String firstName;

  @JsonIgnore
  private String lastName;

  @JsonProperty("name")
  private String Name;

  private String phoneNumber;

//  @JsonProperty("status")
//  private RiderStatus status;
//
//
//  public static List<JobSearchProfileDto> of(List<RiderProfile> riderProfiles) {
//
//    List<JobSearchProfileDto> riderProfileDtos = new LinkedList<JobSearchProfileDto>();
//
//    riderProfileDtos = riderProfiles.stream().map(riderProfile -> {
//      JobSearchProfileDto riderProfileDto = JobSearchProfileDto.builder().build();
//      BeanUtils.copyProperties(riderProfile, riderProfileDto);
//      String fullName = !StringUtils.isEmpty(riderProfile.getLastName())
//          ? String.format("%s %s", riderProfile.getFirstName(), riderProfile.getLastName())
//          : riderProfile.getFirstName();
//      if(ObjectUtils.isEmpty(riderProfile.getStatus())) {
//        riderProfileDto.setStatus(RiderStatus.UNAUTHORIZED);
//      }
//      riderProfileDto.setName(fullName);
//      riderProfileDto.setId(riderProfileDto.getId());
//      return riderProfileDto;
//    }).collect(Collectors.toList());
//
//    return riderProfileDtos;
//  }
}
