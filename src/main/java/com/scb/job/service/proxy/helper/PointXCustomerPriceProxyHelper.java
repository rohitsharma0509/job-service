package com.scb.job.service.proxy.helper;

import com.scb.job.model.request.NewJobRequest;
import com.scb.job.service.proxy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PointXCustomerPriceProxyHelper {

    @Autowired
    private PointXCustomerPricingProxy pointXCustomerPricingProxy;

    public PointXCustomerPricingResponse callPointXCustomerPricing(NewJobRequest job){
        PointXCustomerPricingRequest pointXCustomerPricingRequest = PointXCustomerPricingRequest
            .builder()
            .userName(job.getUserName())
            .apiKey(job.getApiKey())
            .channel(job.getChannel())
            .jobType(job.getJobType())
            .goodsValue(job.getGoodsValue())
            .option(job.getOption())
            .promoCode(job.getPromoCode())
            .locationList(job.getLocationList()).build();

    return  pointXCustomerPricingProxy
            .getCalculatedNetPrice(pointXCustomerPricingRequest);

    }

}
