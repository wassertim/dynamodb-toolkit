package com.github.wassertim.dynamodb.toolkit.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Integration tests validating the JavaPoet migration works correctly with real-world Tourino domain classes.
 * These tests verify that the improved JavaPoet code generation handles complex scenarios like:
 * - Primitive double fields (lat/lng coordinates)
 * - Nested number lists (route geometry coordinates)
 * - Complex object lists (waypoints)
 * - Enum handling
 * - Proper dependency injection
 */
public class TourinoMappingTest {

    @Test
    @DisplayName("Validate WaypointMapper code generation")
    void validateWaypointMapperGeneration() throws IOException {
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/WaypointMapper.java");

        assertThat(mapperPath).exists();
        String content = Files.readString(mapperPath);

        // Validate JavaPoet fixes for primitive double fields
        assertThat(content)
            .describedAs("Primitive double fields should have proper semicolons")
            .contains("attributes.put(\"lat\", MappingUtils.createNumberAttribute(waypoint.getLat()));")
            .contains("attributes.put(\"lng\", MappingUtils.createNumberAttribute(waypoint.getLng()));");

        // Validate no string concatenation artifacts
        assertThat(content)
            .describedAs("No JavaPoet generation artifacts")
            .doesNotContain("\\n")
            .doesNotContain("+ \"");

        // Validate proper enum handling
        assertThat(content)
            .contains("waypoint.getType().name()")
            .contains("WaypointType.valueOf(value)");
    }

    @Test
    @DisplayName("Validate RouteGeometryMapper nested number list handling")
    void validateRouteGeometryMapperGeneration() throws IOException {
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteGeometryMapper.java");

        assertThat(mapperPath).exists();
        String content = Files.readString(mapperPath);

        // Validate nested number list stream operations are properly structured
        assertThat(content)
            .describedAs("Nested number list should have proper stream operations")
            .contains("List<AttributeValue> nestedList = routeGeometry.getCoordinates().stream()")
            .contains(".map(innerList -> innerList.stream()")
            .contains(".map(num -> AttributeValue.builder().n(String.valueOf(num)).build())")
            .contains(".collect(Collectors.toList()))")
            .contains(".map(numList -> AttributeValue.builder().l(numList).build())")
            .contains(".collect(Collectors.toList());");

        // Validate deserialization stream operations
        assertThat(content)
            .contains("List<List<Double>> coordinates = nestedListValue.stream()")
            .contains(".map(av -> {")
            .contains("List<AttributeValue> innerList = MappingUtils.getListSafely(av);")
            .contains("return innerList.stream()")
            .contains(".map(numAv -> MappingUtils.getDoubleSafely(numAv))")
            .contains(".filter(Objects::nonNull)")
            .contains("return new ArrayList<Double>();");
    }

    @Test
    @DisplayName("Validate RouteMapper complex list handling")
    void validateRouteMapperGeneration() throws IOException {
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteMapper.java");

        assertThat(mapperPath).exists();
        String content = Files.readString(mapperPath);

        // Validate complex list stream operations are properly structured
        assertThat(content)
            .describedAs("Complex list should have proper stream operations")
            .contains("List<AttributeValue> waypointsList = route.getWaypoints().stream()")
            .contains(".map(waypointMapper::toDynamoDbAttributeValue)")
            .contains(".filter(Objects::nonNull)")
            .contains(".collect(Collectors.toList());");

        // Validate dependency injection
        assertThat(content)
            .contains("private final WaypointMapper waypointMapper;")
            .contains("private final RouteMetadataMapper routeMetadataMapper;")
            .contains("private final RouteGeometryMapper routeGeometryMapper;")
            .contains("public RouteMapper(WaypointMapper waypointMapper, RouteMetadataMapper routeMetadataMapper,")
            .contains("RouteGeometryMapper routeGeometryMapper)");
    }

    @Test
    @DisplayName("Validate RouteMetadataMapper handles all Double fields correctly")
    void validateRouteMetadataMapperGeneration() throws IOException {
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteMetadataMapper.java");

        assertThat(mapperPath).exists();
        String content = Files.readString(mapperPath);

        // Validate Double wrapper fields are handled with null checks
        assertThat(content)
            .contains("if (routeMetadata.getDistance() != null)")
            .contains("if (routeMetadata.getDuration() != null)")
            .contains("if (routeMetadata.getElevationGain() != null)")
            .contains("if (routeMetadata.getMinElevation() != null)")
            .contains("if (routeMetadata.getMaxElevation() != null)")
            .contains("if (routeMetadata.getAverageSpeed() != null)");
    }

    @Test
    @DisplayName("Validate TableNameResolver includes Route table")
    void validateTableNameResolverInclusion() throws IOException {
        Path resolverPath = Path.of("target/generated-sources/annotations/com/github/wassertim/infrastructure/TableNameResolver.java");

        assertThat(resolverPath).exists();
        String content = Files.readString(resolverPath);

        // Validate Route table is included in resolver
        assertThat(content)
            .contains("case \"com.tourino.domain.Route\" -> \"routes\"")
            .contains("Known tables: com.tourino.domain.Route");
    }

    @Test
    @DisplayName("Measure generated code performance metrics")
    void measureGeneratedCodeMetrics() throws IOException {
        // Analyze all generated Tourino mappers
        Path waypointMapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/WaypointMapper.java");
        Path routeGeometryMapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteGeometryMapper.java");
        Path routeMetadataMapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteMetadataMapper.java");

        assertThat(waypointMapperPath).exists();
        assertThat(routeGeometryMapperPath).exists();
        assertThat(routeMetadataMapperPath).exists();

        // Verify reasonable file sizes for complex mappers
        long waypointSize = Files.size(waypointMapperPath);
        long routeGeometrySize = Files.size(routeGeometryMapperPath);
        long routeMetadataSize = Files.size(routeMetadataMapperPath);

        assertThat(waypointSize).describedAs("Waypoint mapper size").isBetween(4000L, 8000L);
        assertThat(routeGeometrySize).describedAs("RouteGeometry mapper size").isBetween(5000L, 10000L);
        assertThat(routeMetadataSize).describedAs("RouteMetadata mapper size").isBetween(4000L, 9000L);

        // Count methods in WaypointMapper
        String waypointContent = Files.readString(waypointMapperPath);
        long methodCount = Pattern.compile("public .* \\w+\\(.*\\) \\{")
                .matcher(waypointContent)
                .results()
                .count();

        assertThat(methodCount).describedAs("Waypoint mapper method count").isEqualTo(6); // 2 core + 4 convenience
    }

    @Test
    @DisplayName("Validate code consistency across all Tourino mappers")
    void validateCodeConsistencyAcrossTourinoMappers() throws IOException {
        String[] mapperFiles = {
            "target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/WaypointMapper.java",
            "target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteGeometryMapper.java",
            "target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/RouteMetadataMapper.java"
        };

        for (String mapperFile : mapperFiles) {
            Path mapperPath = Path.of(mapperFile);
            assertThat(mapperPath).exists();

            String content = Files.readString(mapperPath);

            // Validate consistent generation patterns
            assertThat(content)
                .describedAs("All mappers should be @ApplicationScoped")
                .contains("@ApplicationScoped");

            assertThat(content)
                .describedAs("All mappers should have proper JavaDoc")
                .contains("/**")
                .contains("Generated DynamoDB mapper for")
                .contains("Generated at:");

            assertThat(content)
                .describedAs("All mappers should have core mapping methods")
                .contains("toDynamoDbAttributeValue")
                .contains("fromDynamoDbAttributeValue");

            assertThat(content)
                .describedAs("All mappers should have convenience methods")
                .contains("fromDynamoDbItem")
                .contains("fromDynamoDbItems")
                .contains("toDynamoDbItem")
                .contains("toDynamoDbItems");
        }
    }
}