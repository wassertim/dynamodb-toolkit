package com.tourino.domain;

import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object containing route calculation metadata and statistics.
 * Stores numeric values for calculations, not formatted strings.
 */
@DynamoMappable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteMetadata {
    
    /**
     * Total route distance in meters.
     */
    private Double distance;

    /**
     * Total route duration in seconds.
     */
    private Double duration;

    /**
     * Total elevation gain in meters.
     */
    private Double elevationGain;

    /**
     * Total elevation loss in meters.
     */
    private Double elevationLoss;

    /**
     * Minimum elevation in meters.
     */
    private Double minElevation;

    /**
     * Maximum elevation in meters.
     */
    private Double maxElevation;

    /**
     * Average speed in kilometers per hour.
     */
    private Double averageSpeed;
}