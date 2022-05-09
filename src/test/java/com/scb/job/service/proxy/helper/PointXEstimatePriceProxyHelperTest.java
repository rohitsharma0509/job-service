package com.scb.job.service.proxy.helper;

import com.scb.job.model.request.Location;
import com.scb.job.model.request.NewJobRequest;
import com.scb.job.service.proxy.PointXCustomerPricingResponse;
import com.scb.job.service.proxy.PointXEstimatePricingResponse;
import com.scb.job.service.proxy.PointxEstimatePriceProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointXEstimatePriceProxyHelperTest {

    @InjectMocks
    private PointXEstimatePriceProxyHelper pointXEstimatePriceProxyHelper;

    @Mock
    private PointxEstimatePriceProxy pointxEstimatePriceProxy;

    private NewJobRequest createJobRequest() {
        NewJobRequest job = new NewJobRequest();
        job.setUserName("userName");
        job.setApiKey("apiKey");
        job.setChannel("channel");
        job.setJobType("jobType");
        job.setGoodsValue("goodsValue");
        job.setOption("option");
        job.setPromoCode("promoCode");
        job.setLocationList(List.of(new Location()));
        return job;
    }

    @Test
    void testGetCalculatedNetPrice() {
        PointXEstimatePricingResponse pointXEstimatePricingResponse = PointXEstimatePricingResponse.builder().build();
        NewJobRequest request = createJobRequest();
        when(pointxEstimatePriceProxy.getEstimatedPrice(any()))
                .thenReturn(pointXEstimatePricingResponse);

        PointXEstimatePricingResponse response = pointXEstimatePriceProxyHelper.callPointXEstimatePricing(request);
        assertNotNull(response);

    }
}

