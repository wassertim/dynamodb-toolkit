package com.github.wassertim.dynamodb.toolkit.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.github.wassertim.dynamodb.toolkit.integration.entities.RouteInstruction;
import com.github.wassertim.dynamodb.toolkit.mappers.RouteInstructionMapper;

import java.util.Arrays;
import java.util.Map;

/**
 * Integration test to verify that the DynamoDB Toolkit correctly handles
 * List<Integer> fields in @DynamoMappable entities.
 */
public class ListIntegerMappingTest {

    private RouteInstruction routeInstruction;
    private RouteInstructionMapper mapper;

    @BeforeEach
    void setUp() {
        routeInstruction = RouteInstruction.builder()
            .text("Turn left on Main Street")
            .distance(150.5)
            .duration(45.0)
            .type("turn")
            .waypointIndices(Arrays.asList(0, 3, 7, 12))
            .build();

        mapper = new RouteInstructionMapper();
    }

    @Test
    @DisplayName("List<Integer> field should be correctly mapped to DynamoDB")
    void testListIntegerMapping() {
        // Convert to DynamoDB AttributeValue
        AttributeValue attributeValue = mapper.toDynamoDbAttributeValue(routeInstruction);

        assertThat(attributeValue).isNotNull();
        assertThat(attributeValue.hasM()).isTrue();

        Map<String, AttributeValue> map = attributeValue.m();

        // Verify all fields are present
        assertThat(map).containsKey("text");
        assertThat(map).containsKey("distance");
        assertThat(map).containsKey("duration");
        assertThat(map).containsKey("type");
        assertThat(map).containsKey("waypointIndices");

        // Verify waypointIndices is a list
        AttributeValue waypointIndicesValue = map.get("waypointIndices");
        assertThat(waypointIndicesValue.hasL()).isTrue();
        assertThat(waypointIndicesValue.l()).hasSize(4);

        // Verify each element is a number
        assertThat(waypointIndicesValue.l().get(0).n()).isEqualTo("0");
        assertThat(waypointIndicesValue.l().get(1).n()).isEqualTo("3");
        assertThat(waypointIndicesValue.l().get(2).n()).isEqualTo("7");
        assertThat(waypointIndicesValue.l().get(3).n()).isEqualTo("12");
    }

    @Test
    @DisplayName("List<Integer> field should support round-trip conversion")
    void testListIntegerRoundTrip() {
        // Convert to DynamoDB and back
        AttributeValue attributeValue = mapper.toDynamoDbAttributeValue(routeInstruction);
        RouteInstruction converted = mapper.fromDynamoDbAttributeValue(attributeValue);

        assertThat(converted).isNotNull();
        assertThat(converted.getText()).isEqualTo(routeInstruction.getText());
        assertThat(converted.getDistance()).isEqualTo(routeInstruction.getDistance());
        assertThat(converted.getDuration()).isEqualTo(routeInstruction.getDuration());
        assertThat(converted.getType()).isEqualTo(routeInstruction.getType());
        assertThat(converted.getWaypointIndices()).isEqualTo(routeInstruction.getWaypointIndices());
    }

    @Test
    @DisplayName("Null List<Integer> field should be handled correctly")
    void testNullListInteger() {
        RouteInstruction withNullList = RouteInstruction.builder()
            .text("Continue straight")
            .distance(200.0)
            .duration(60.0)
            .type("straight")
            .waypointIndices(null)
            .build();

        AttributeValue attributeValue = mapper.toDynamoDbAttributeValue(withNullList);
        assertThat(attributeValue).isNotNull();

        Map<String, AttributeValue> map = attributeValue.m();
        assertThat(map).doesNotContainKey("waypointIndices");

        // Round-trip conversion
        RouteInstruction converted = mapper.fromDynamoDbAttributeValue(attributeValue);
        assertThat(converted.getWaypointIndices()).isNull();
    }

    @Test
    @DisplayName("Empty List<Integer> field should be handled correctly")
    void testEmptyListInteger() {
        RouteInstruction withEmptyList = RouteInstruction.builder()
            .text("Arrive at destination")
            .distance(0.0)
            .duration(0.0)
            .type("arrive")
            .waypointIndices(Arrays.asList())
            .build();

        AttributeValue attributeValue = mapper.toDynamoDbAttributeValue(withEmptyList);
        assertThat(attributeValue).isNotNull();

        Map<String, AttributeValue> map = attributeValue.m();
        assertThat(map).containsKey("waypointIndices");

        AttributeValue waypointIndicesValue = map.get("waypointIndices");
        assertThat(waypointIndicesValue.hasL()).isTrue();
        assertThat(waypointIndicesValue.l()).isEmpty();

        // Round-trip conversion
        RouteInstruction converted = mapper.fromDynamoDbAttributeValue(attributeValue);
        assertThat(converted.getWaypointIndices()).isEmpty();
    }
}
