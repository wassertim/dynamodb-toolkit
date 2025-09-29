package com.tourino.domain;

import com.github.wassertim.dynamodb.toolkit.api.annotations.AttributeType;
import com.github.wassertim.dynamodb.toolkit.api.annotations.PartitionKey;
import com.github.wassertim.dynamodb.toolkit.api.annotations.SortKey;
import com.github.wassertim.dynamodb.toolkit.api.annotations.Table;
import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Main user route entity for persistent route management.
 * Represents a user's saved route with full lifecycle support and rich metadata.
 */
@DynamoMappable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "routes")
public class Route {
    
    /**
     * User ID - partition key for data isolation and efficient queries.
     */
    @PartitionKey(attributeType = AttributeType.STRING)
    private String userId;

    /**
     * Unique route identifier - UUID for global uniqueness.
     */
    @SortKey(attributeType = AttributeType.STRING)
    private String routeId;

    /**
     * User-defined route name (3-100 characters).
     */
    private String name;
    
    /**
     * Optional description providing additional context about the route.
     */
    private String description;
    
    /**
     * Route type classification for filtering and organization.
     */
    private RouteType type;

    /**
     * Difficulty level assessment for user guidance.
     */
    private Difficulty difficulty;

    /**
     * The routing profile used for route calculation (e.g., "cycling-regular", "walking", "driving-car").
     * This preserves the exact calculation parameters used and ensures route consistency.
     */
    private String routingProfile;

    /**
     * Ordered collection of waypoints defining the route path.
     */
    private List<Waypoint> waypoints;

    /**
     * Route geometry containing path coordinates and shape data.
     */
    private RouteGeometry routeGeometry;

    /**
     * Calculated route statistics and measurements.
     */
    private RouteMetadata metadata;
    
    
    /**
     * Route creation timestamp for audit trail.
     */
    private Instant createdAt;

    /**
     * Last modification timestamp for change tracking.
     */
    private Instant updatedAt;

    /**
     * Last accessed timestamp for usage analytics and recent route queries.
     */
    private Instant lastUsed;

    /**
     * User-defined tags for route categorization and search.
     */
    private List<String> tags;
}