package com.tourino.domain;

import com.tourino.domain.enums.GeometryType;
import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Domain value object representing route geometry as GeoJSON LineString.
 */
@DynamoMappable
@Data
@Builder
public class RouteGeometry {
    
    /**
     * GeoJSON type (always LINESTRING for routes).
     */
    @Builder.Default
    private final GeometryType type = GeometryType.LINESTRING;
    
    /**
     * Array of coordinate pairs [longitude, latitude] defining the route path.
     */
    private final List<List<Double>> coordinates;
}