package com.som.toll.service;

import com.som.toll.client.GoogleMapsClient;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final GoogleMapsClient mapsClient;

    public RouteService(GoogleMapsClient mapsClient) {
        this.mapsClient = mapsClient;
    }

    public double distanceKm(String sourcePincode, String destPincode) {
        return mapsClient.fetchDistanceKm(sourcePincode, destPincode);
    }
}
