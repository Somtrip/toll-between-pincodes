package com.som.toll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TollPlazaResponse {
    private RouteDto route;
    private List<TollPlazaDto> tollPlazas;
}

