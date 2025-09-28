package com.github.wassertim.dynamodb.runtime;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Runtime utilities for DynamoDB mapping operations.
 * Provides common conversion methods used by generated mappers.
 */
public final class MappingUtils {

    private MappingUtils() {
        // Utility class - no instantiation
    }

    /**
     * Safely converts a string to an Instant, returning null if parsing fails.
     *
     * @param value the string representation of an Instant
     * @return the parsed Instant or null if parsing fails
     */
    public static Instant parseInstantSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Safely converts a string to an enum value, returning null if parsing fails.
     *
     * @param value the string representation of the enum
     * @param enumClass the enum class
     * @param <E> the enum type
     * @return the parsed enum value or null if parsing fails
     */
    public static <E extends Enum<E>> E parseEnumSafely(String value, Class<E> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Converts a list of AttributeValues to a list of mapped objects.
     *
     * @param attributeValues the list of AttributeValues
     * @param mapper function to map each AttributeValue to the target type
     * @param <T> the target type
     * @return the list of mapped objects
     */
    public static <T> List<T> mapList(List<AttributeValue> attributeValues,
                                      java.util.function.Function<AttributeValue, T> mapper) {
        if (attributeValues == null) {
            return null;
        }
        return attributeValues.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of objects to a list of AttributeValues.
     *
     * @param objects the list of objects
     * @param mapper function to map each object to AttributeValue
     * @param <T> the source type
     * @return the list of AttributeValues
     */
    public static <T> List<AttributeValue> mapToAttributeValueList(List<T> objects,
                                                                   java.util.function.Function<T, AttributeValue> mapper) {
        if (objects == null || objects.isEmpty()) {
            return null;
        }
        return objects.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Safely gets a string value from an AttributeValue.
     *
     * @param attributeValue the AttributeValue
     * @return the string value or null if not present
     */
    public static String getStringSafely(AttributeValue attributeValue) {
        return attributeValue != null ? attributeValue.s() : null;
    }

    /**
     * Safely gets a number value from an AttributeValue and converts to Double.
     *
     * @param attributeValue the AttributeValue
     * @return the double value or null if not present or invalid
     */
    public static Double getDoubleSafely(AttributeValue attributeValue) {
        if (attributeValue == null || attributeValue.n() == null) {
            return null;
        }
        try {
            return Double.parseDouble(attributeValue.n());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely gets a number value from an AttributeValue and converts to Integer.
     *
     * @param attributeValue the AttributeValue
     * @return the integer value or null if not present or invalid
     */
    public static Integer getIntegerSafely(AttributeValue attributeValue) {
        if (attributeValue == null || attributeValue.n() == null) {
            return null;
        }
        try {
            return Integer.parseInt(attributeValue.n());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely gets a map value from an AttributeValue.
     *
     * @param attributeValue the AttributeValue
     * @return the map value or null if not present
     */
    public static Map<String, AttributeValue> getMapSafely(AttributeValue attributeValue) {
        return attributeValue != null ? attributeValue.m() : null;
    }

    /**
     * Safely gets a list value from an AttributeValue.
     *
     * @param attributeValue the AttributeValue
     * @return the list value or null if not present
     */
    public static List<AttributeValue> getListSafely(AttributeValue attributeValue) {
        return attributeValue != null ? attributeValue.l() : null;
    }

    /**
     * Creates a string AttributeValue, handling null values.
     *
     * @param value the string value
     * @return the AttributeValue or null if value is null
     */
    public static AttributeValue createStringAttribute(String value) {
        return value != null ? AttributeValue.builder().s(value).build() : null;
    }

    /**
     * Creates a number AttributeValue from a Double, handling null values.
     *
     * @param value the Double value
     * @return the AttributeValue or null if value is null
     */
    public static AttributeValue createNumberAttribute(Double value) {
        return value != null ? AttributeValue.builder().n(String.valueOf(value)).build() : null;
    }

    /**
     * Creates a number AttributeValue from a primitive double.
     *
     * @param value the double value
     * @return the AttributeValue
     */
    public static AttributeValue createNumberAttribute(double value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    /**
     * Creates a number AttributeValue from an Integer, handling null values.
     *
     * @param value the Integer value
     * @return the AttributeValue or null if value is null
     */
    public static AttributeValue createNumberAttribute(Integer value) {
        return value != null ? AttributeValue.builder().n(String.valueOf(value)).build() : null;
    }

    /**
     * Creates a list AttributeValue, handling null and empty lists.
     *
     * @param values the list of AttributeValues
     * @return the list AttributeValue or null if list is null or empty
     */
    public static AttributeValue createListAttribute(List<AttributeValue> values) {
        return (values != null && !values.isEmpty())
                ? AttributeValue.builder().l(values).build()
                : null;
    }

    /**
     * Creates a map AttributeValue, handling null maps.
     *
     * @param values the map of AttributeValues
     * @return the map AttributeValue or null if map is null
     */
    public static AttributeValue createMapAttribute(Map<String, AttributeValue> values) {
        return values != null
                ? AttributeValue.builder().m(values).build()
                : null;
    }

    // Parameter value utilities for DynamoDB queries

    /**
     * Creates a parameter AttributeValue for a string value.
     * Convenience method for creating query parameter values.
     *
     * @param value the string value
     * @return the AttributeValue for use in query parameters
     * @throws IllegalArgumentException if value is null
     */
    public static AttributeValue paramValueOf(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }
        return AttributeValue.builder().s(value).build();
    }

    /**
     * Creates a parameter AttributeValue for a number value.
     * Convenience method for creating query parameter values.
     *
     * @param value the number value
     * @return the AttributeValue for use in query parameters
     * @throws IllegalArgumentException if value is null
     */
    public static AttributeValue paramValueOf(Number value) {
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    /**
     * Creates a parameter AttributeValue for a boolean value.
     * Convenience method for creating query parameter values.
     *
     * @param value the boolean value
     * @return the AttributeValue for use in query parameters
     */
    public static AttributeValue paramValueOf(boolean value) {
        return AttributeValue.builder().bool(value).build();
    }
}