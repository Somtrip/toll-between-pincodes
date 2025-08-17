package com.som.toll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {
    private String sourcePincode;
    private String destinationPincode;
    private double distanceInKm;
}

