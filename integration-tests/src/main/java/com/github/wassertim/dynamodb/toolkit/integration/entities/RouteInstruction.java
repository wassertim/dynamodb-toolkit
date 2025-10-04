package com.github.wassertim.dynamodb.toolkit.integration.entities;

import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@DynamoMappable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteInstruction {
    private String text;
    private Double distance;
    private Double duration;
    private String type;

    // This field causes the generation error
    private List<Integer> waypointIndices;
}
