package io.github.wassertim.dynamodb.toolkit.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to indicate that a type should have a DynamoDB mapper generated.
 *
 * Classes annotated with @DynamoMappable will have type-safe mappers generated at compile time
 * that convert between the domain object and DynamoDB AttributeValue format.
 *
 * The annotation processor will:
 * 1. Analyze the type structure and field types
 * 2. Detect dependencies on other @DynamoMappable types
 * 3. Generate bidirectional mapper classes with proper CDI injection
 * 4. Handle primitive types, collections, enums, and nested complex objects
 *
 * Generated mappers are placed in the {@code io.github.wassertim.dynamodb.toolkit.mappers} package.
 *
 * Example usage:
 * <pre>
 * @DynamoMappable
 * @Table(name = "routes")
 * public class Route {
 *     private String userId;
 *     private List&lt;Waypoint&gt; waypoints; // Waypoint must also be @DynamoMappable
 *     private RouteGeometry geometry;  // RouteGeometry must also be @DynamoMappable
 * }
 * Generates: io.github.wassertim.dynamodb.toolkit.mappers.RouteMapper
 * </pre>
 *
 * This will generate a RouteMapper class with automatic dependency injection for
 * WaypointMapper and RouteGeometryMapper.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DynamoMappable {
    // Marker annotation - no parameters needed
}