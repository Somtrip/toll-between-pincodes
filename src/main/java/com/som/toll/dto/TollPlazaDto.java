package com.som.toll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TollPlazaDto {
    private String name;
    private double latitude;
    private double longitude;
    private double distanceFromSource; // km
    private String geoState;           // NEW field
}
