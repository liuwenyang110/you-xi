package com.nongzhushou.match.model;

public final class GeoDistanceUtils {

    private GeoDistanceUtils() {
    }

    public static int calcTier(Double distanceKm) {
        if (distanceKm == null || distanceKm <= 3D) {
            return 1;
        }
        if (distanceKm <= 8D) {
            return 2;
        }
        if (distanceKm <= 15D) {
            return 3;
        }
        return -1;
    }
}
