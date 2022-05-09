package com.scb.job.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DistanceResponseEntity {
    private Double longitudeFrom;
    private Double latitudeFrom;

    private Double longitudeTo;
    private Double latitudeTo;

    private Double distance;
    private Double duration;

    public Double getDistanceInKms(){
        Double totalDistance = distance / 1000;
        return Double.valueOf(Math.round(totalDistance));
    }
}

