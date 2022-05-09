package com.scb.job.service.proxy;

import com.scb.job.model.request.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointXCustomerPricingRequest {
    @NotBlank
    private String userName;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String channel;

    @NotBlank
    private String jobType;

    @NotBlank
    private String goodsValue;

    private String option;

    private String promoCode;

    private List<Location> locationList;

}
