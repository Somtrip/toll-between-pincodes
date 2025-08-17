package com.som.toll.service;


import org.springframework.stereotype.Service;

import com.som.toll.client.GoogleMapsClient;

@Service
public class GeocodingService {

    private final GoogleMapsClient mapsClient;

    public GeocodingService(GoogleMapsClient mapsClient) {
        this.mapsClient = mapsClient;
    }

    public double[] geocode(String pincode) {
        var latLng = mapsClient.geocodePincode(pincode);
        return new double[]{ latLng.lat(), latLng.lng() };
    }
}

