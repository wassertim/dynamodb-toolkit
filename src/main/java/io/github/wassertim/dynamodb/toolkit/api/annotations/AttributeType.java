package io.github.wassertim.dynamodb.toolkit.api.annotations;

/**
 * Enum representing DynamoDB attribute types for domain entity annotations.
 */
public enum AttributeType {
    /**
     * DynamoDB String type (S).
     */
    STRING,

    /**
     * DynamoDB Number type (N).
     */
    NUMBER,

    /**
     * DynamoDB Binary type (B).
     */
    BINARY,

    /**
     * DynamoDB Boolean type (BOOL).
     */
    BOOL,

    /**
     * DynamoDB List type (L).
     */
    LIST,

    /**
     * DynamoDB Map type (M).
     */
    MAP,

    /**
     * DynamoDB String Set type (SS).
     */
    STRING_SET,

    /**
     * DynamoDB Number Set type (NS).
     */
    NUMBER_SET,

    /**
     * DynamoDB Binary Set type (BS).
     */
    BINARY_SET,

    /**
     * DynamoDB Null type (NULL).
     */
    NULL
}