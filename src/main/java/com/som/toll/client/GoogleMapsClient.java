package com.som.toll.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleMapsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.api.distance-matrix-url:https://maps.googleapis.com/maps/api/distancematrix/json}")
    private String distanceMatrixUrl;

    @Value("${google.api.geocode-url:https://maps.googleapis.com/maps/api/geocode/json}")
    private String geocodeUrl;

    /**
     * Fetch distance (km) between two pincodes using Google Distance Matrix API.
     */
    public double fetchDistanceKm(String fromPincode, String toPincode) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(distanceMatrixUrl)
                    .queryParam("origins", fromPincode)
                    .queryParam("destinations", toPincode)
                    .queryParam("key", apiKey)
                    .toUriString();

            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            JsonNode root = mapper.readTree(resp.getBody());

            String status = root.path("status").asText();
            if (!"OK".equalsIgnoreCase(status)) {
                throw new RuntimeException("Distance Matrix API error: " + status);
            }

            JsonNode element = root.path("rows").get(0).path("elements").get(0);
            String elemStatus = element.path("status").asText();
            if (!"OK".equalsIgnoreCase(elemStatus)) {
                throw new RuntimeException("Distance Matrix element error: " + elemStatus);
            }

            double distanceMeters = element.path("distance").path("value").asDouble();
            return distanceMeters / 1000.0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get distance from Google", e);
        }
    }

    /**
     * Geocode a pincode to lat/lng using Google Geocoding API.
     */
    public LatLng geocodePincode(String pincode) {
    try {
        // 1️⃣ First try with address
        String url = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
                .queryParam("address", pincode + ", India")
                .queryParam("key", apiKey)
                .toUriString();

        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        LatLng result = parseLatLngFromResponse(resp.getBody(), pincode);
        if (result != null) return result;

        // 2️⃣ Fallback to components if address fails
        String url2 = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
                .queryParam("components", "country:IN|postal_code:" + pincode)
                .queryParam("key", apiKey)
                .toUriString();

        resp = restTemplate.getForEntity(url2, String.class);
        result = parseLatLngFromResponse(resp.getBody(), pincode);
        if (result != null) return result;

        throw new IllegalArgumentException("No geocoding result for: " + pincode);

    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid pincode or geocoding failed for: " + pincode, e);
    }
}

private LatLng parseLatLngFromResponse(String body, String pincode) throws Exception {
    System.out.println("Geocode response for " + pincode + ":\n" + body);

    JsonNode root = mapper.readTree(body);
    String status = root.path("status").asText();

    if (!"OK".equalsIgnoreCase(status)) {
        return null; // let caller try fallback
    }

    JsonNode results = root.path("results");
    if (!results.isArray() || results.isEmpty()) {
        return null;
    }

    JsonNode geometry = results.get(0).path("geometry").path("location");
    double lat = geometry.path("lat").asDouble();
    double lng = geometry.path("lng").asDouble();

    return new LatLng(lat, lng);
}

    /**
     * Simple record to hold lat/lng pair.
     */
    public record LatLng(double lat, double lng) {}
}
