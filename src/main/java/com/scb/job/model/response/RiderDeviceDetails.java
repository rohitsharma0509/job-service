package com.scb.job.model.response;


import com.scb.job.model.enumeration.Platform;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document
@AllArgsConstructor
@NoArgsConstructor
public class RiderDeviceDetails {

    private String id;
    private String profileId;
    private String deviceToken;
    private Platform platform;
    private String arn;
}
