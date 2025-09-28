package com.github.wassertim.dynamodb.toolkit.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify DynamoDB table information for domain entities.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * The table name. Defaults to class name if not specified.
     * @return the table name
     */
    String name() default "";
}