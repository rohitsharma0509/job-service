package com.scb.job.service.proxy.helper;

import com.scb.job.model.request.NewJobRequest;
import com.scb.job.service.proxy.PointXEstimatePricingRequest;
import com.scb.job.service.proxy.PointXEstimatePricingResponse;
import com.scb.job.service.proxy.PointxEstimatePriceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PointXEstimatePriceProxyHelper {

    @Autowired
    PointxEstimatePriceProxy pointxEstimatePriceProxy;

    public PointXEstimatePricingResponse callPointXEstimatePricing(NewJobRequest job) {

        PointXEstimatePricingRequest pointXEstimatePricingRequest = PointXEstimatePricingRequest.builder()
                .userName(job.getUserName())
                .apiKey(job.getApiKey())
                .channel(job.getChannel())
                .jobType(job.getJobType())
                .option(job.getOption())
                .promoCode(job.getPromoCode())
                .locationList(job.getLocationList()).build();

        return pointxEstimatePriceProxy
                .getEstimatedPrice(pointXEstimatePricingRequest);

    }

}
