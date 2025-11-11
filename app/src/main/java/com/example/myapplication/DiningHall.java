package com.example.myapplication;

import android.widget.FrameLayout;

/**
 * DiningHall - Data class representing a dining hall with location information
 *
 * This class stores dining hall information including coordinates for distance calculations
 * and UI components for reorganization animations.
 *
 * Coordinates are based on actual MSU campus locations for accurate distance calculation.
 */
public class DiningHall {
    private final String name;
    private final String displayName;
    private final double latitude;
    private final double longitude;
    private final int viewId;
    private FrameLayout frameLayout;
    private double distanceFromUser;
    private int originalPosition;

    /**
     * Constructor for DiningHall
     *
     * @param name Display name of the dining hall
     * @param displayName Full display name
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param viewId Resource ID of the FrameLayout
     */
    public DiningHall(String name, String displayName, double latitude, double longitude, int viewId) {
        this.name = name;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.viewId = viewId;
        this.distanceFromUser = Double.MAX_VALUE;
        this.originalPosition = -1;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getViewId() { return viewId; }

    public FrameLayout getFrameLayout() { return frameLayout; }
    public void setFrameLayout(FrameLayout frameLayout) { this.frameLayout = frameLayout; }

    public double getDistanceFromUser() { return distanceFromUser; }
    public void setDistanceFromUser(double distance) { this.distanceFromUser = distance; }

    public int getOriginalPosition() { return originalPosition; }
    public void setOriginalPosition(int position) { this.originalPosition = position; }

    /**
     * Creates the complete list of MSU dining halls with their actual coordinates
     *
     * @return Array of all dining halls with coordinates
     */
    public static DiningHall[] getAllDiningHalls() {
        return new DiningHall[] {
            // Top row - Full width featured dining hall (accurate coordinates)
            new DiningHall("Snyder-Phillips", "Snyder-Phillips Hall",
                42.73022289349873, -84.47344892521157, R.id.boxSnyderPhillips),

            // Grid rows - Remaining 6 dining halls with precise GPS coordinates
            new DiningHall("Brody", "Brody Dining Hall",
                42.731379562909424, -84.49526567905579, R.id.boxBrody),
            new DiningHall("Case", "Case Dining Hall",
                42.724567591646384, -84.48870729559268, R.id.boxCase),
            new DiningHall("Owen", "Owen Dining Hall",
                42.72657035421706, -84.47055947007354, R.id.boxOwen),
            new DiningHall("Shaw", "Shaw Dining Hall",
                42.726786523682144, -84.47529606042431, R.id.boxShaw),
            new DiningHall("Akers", "Akers Dining Hall",
                42.72434170664002, -84.46480484532314, R.id.boxAkers),
            new DiningHall("Landon", "Landon Dining Hall",
                42.73380788983037, -84.48514502978489, R.id.boxLandon)
        };
    }

    /**
     * Utility method to calculate distance between two geographic points using Haversine formula
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in meters
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_M = 6371000; // Earth radius in meters

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double straightLineDistance = EARTH_RADIUS_M * c;

        // Apply walking distance multiplier for campus paths
        // Research shows walking distance is typically 1.3-1.5x straight-line distance on campus
        final double WALKING_MULTIPLIER = 1.4;

        return straightLineDistance * WALKING_MULTIPLIER;
    }

    @Override
    public String toString() {
        return String.format("%s (%.0fm away)", displayName, distanceFromUser);
    }
}