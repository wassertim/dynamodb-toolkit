package com.tourino.domain.enums;

/**
 * Enumeration of supported GeoJSON geometry types for route data.
 */
public enum GeometryType {
    LINESTRING("LineString"),
    POINT("Point"),
    POLYGON("Polygon");

    private final String value;

    GeometryType(String value) {
        this.value = value;
    }

    /**
     * Gets the string value for this geometry type.
     * @return the GeoJSON geometry type name used in API serialization
     */
    public String getValue() {
        return value;
    }

    /**
     * Finds the GeometryType enum by its string value.
     * @param value the GeoJSON geometry type string
     * @return the matching GeometryType enum
     * @throws IllegalArgumentException if no matching enum is found
     */
    public static GeometryType fromValue(String value) {
        for (GeometryType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No GeometryType found for value: " + value);
    }
}