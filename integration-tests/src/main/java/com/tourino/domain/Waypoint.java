package com.tourino.domain;

import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enhanced waypoint entity with coordinates, metadata, and type information.
 */
@DynamoMappable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waypoint {
    
    /**
     * Latitude coordinate in decimal degrees.
     */
    private double lat;

    /**
     * Longitude coordinate in decimal degrees.
     */
    private double lng;

    /**
     * Display name for this waypoint (e.g., "Home", "Office", "Mountain Peak").
     */
    private String name;

    /**
     * Optional description providing additional context about this waypoint.
     */
    private String description;

    /**
     * Type classification for this waypoint.
     */
    private WaypointType type;
}