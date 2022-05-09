package com.scb.job.validation;

import com.scb.job.constants.PaymentType;
import com.scb.job.exception.JobRequestFieldException;
import com.scb.job.exception.LocationDeliveryListException;
import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.util.DateUtils;

import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobRequestValidator {

  public void validateNewJobRequest(NewJobRequest newJobRequest){
    validate(()->DateUtils.parseDateTime(newJobRequest.getJobDate()), "jobDate not in yyyy-mm-dd format");
    validate(()->DateUtils.parseTime(newJobRequest.getStartTime()), "startTime not in HH:mm format");
    validate(()-> PaymentType.validatePaymentType(newJobRequest.getPaymentType()), "supported values are invoice/cash/creditcard/robinhood/points");
    String jobType = newJobRequest.getJobType();
    validate(()-> jobType.equals("1") || jobType.equals("2") || jobType.equals("3") || jobType.equals("4"), "jobType supported values are 1,2,3,4");
    List<Location> locationList = newJobRequest.getLocationList();
    if(locationList == null || locationList.size() != 2){
      log.info("List of Locations is either null or size is invalid");
      throw new LocationDeliveryListException();
    }
    for (Location location: locationList ) {
      double lat = Double.parseDouble(location.getLat());
      double lng = Double.parseDouble(location.getLng());
      validate(()-> (lat >= -90.0 && lat <= 90.0) && (lng >=-180.0 && lng <= 180.0 ), "Invalid location values");
    }
  }

  void validate(Supplier<Boolean> isValid, String failureError){
    if (!isValid.get())
      throw new JobRequestFieldException(failureError);
  }

}
