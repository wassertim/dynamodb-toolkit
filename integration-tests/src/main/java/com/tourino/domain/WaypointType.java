package com.tourino.domain;

/**
 * Enumeration of waypoint types for classification and UI display.
 */
public enum WaypointType {
    START("Starting point"),
    END("Destination"),
    INTERMEDIATE("Intermediate stop"),
    LANDMARK("Point of interest"),
    CHECKPOINT("Checkpoint"),
    ACCOMMODATION("Hotel/accommodation"),
    RESTAURANT("Restaurant/food"),
    GAS_STATION("Gas station"),
    PARKING("Parking area"),
    VIEWPOINT("Scenic viewpoint"),
    EMERGENCY("Emergency services");

    private final String description;

    WaypointType(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of this waypoint type.
     * @return the waypoint type description
     */
    public String getDescription() {
        return description;
    }
}