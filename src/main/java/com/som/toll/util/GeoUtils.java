package com.som.toll.util;



public final class GeoUtils {

    private GeoUtils() {}

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * Cross-track distance from point P to great-circle path AB (km).
     * Using spherical law of cosines approximation suitable for corridor test.
     */
    public static double crossTrackDistanceKm(double latA, double lonA,
                                              double latB, double lonB,
                                              double latP, double lonP) {
        final double R = 6371.0;
        double d13 = angularDistance(latA, lonA, latP, lonP);
        double brng13 = initialBearing(latA, lonA, latP, lonP);
        double brng12 = initialBearing(latA, lonA, latB, lonB);
        double xt = Math.asin(Math.sin(d13) * Math.sin(brng13 - brng12)) * R;
        return Math.abs(xt); // km
    }

    /** Along-track distance from A to projection of P on path AB (km, can be negative or >AB). */
    public static double alongTrackDistanceKm(double latA, double lonA,
                                              double latB, double lonB,
                                              double latP, double lonP) {
        final double R = 6371.0;
        double d13 = angularDistance(latA, lonA, latP, lonP);
        double brng13 = initialBearing(latA, lonA, latP, lonP);
        double brng12 = initialBearing(latA, lonA, latB, lonB);
        double at = Math.acos(Math.cos(d13) / Math.cos(Math.asin(Math.sin(d13) * Math.sin(brng13 - brng12)))) * R;
        return at; // km
    }

    private static double angularDistance(double lat1, double lon1, double lat2, double lon2) {
        double d = haversineKm(lat1, lon1, lat2, lon2) / 6371.0;
        return d;
    }

    private static double initialBearing(double lat1, double lon1, double lat2, double lon2) {
        double φ1 = Math.toRadians(lat1), φ2 = Math.toRadians(lat2);
        double λ1 = Math.toRadians(lon1), λ2 = Math.toRadians(lon2);
        double y = Math.sin(λ2 - λ1) * Math.cos(φ2);
        double x = Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(λ2 - λ1);
        return Math.atan2(y, x);
    }
}

