package com.tourino.dynamodb.toolkit.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify DynamoDB sort key information for domain entity fields.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SortKey {
    /**
     * The DynamoDB attribute name. Defaults to field name if not specified.
     * @return the attribute name
     */
    String attributeName() default "";

    /**
     * The DynamoDB attribute type.
     * @return the attribute type
     */
    AttributeType attributeType() default AttributeType.STRING;
}