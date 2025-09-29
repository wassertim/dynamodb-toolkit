package com.github.wassertim.dynamodb.toolkit.mapping;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ClassName;
import com.github.wassertim.dynamodb.toolkit.analysis.FieldInfo;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeExtractor;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.time.Instant;

/**
 * Utility class for generating mapping code with JavaPoet.
 * Provides reusable patterns for converting between domain objects and DynamoDB AttributeValue.
 */
public class MappingCodeGeneratorUtils {

    private static final ClassName ATTRIBUTE_VALUE = ClassName.get(AttributeValue.class);
    private static final ClassName MAPPING_UTILS = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");
    private static final ClassName INSTANT = ClassName.get(Instant.class);

    private final TypeExtractor typeExtractor;

    public MappingCodeGeneratorUtils(TypeExtractor typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    /**
     * Creates a getter call expression for a field.
     */
    public String createGetterCall(String objectName, String fieldName) {
        return objectName + ".get" +
               Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "()";
    }

    /**
     * Creates a setter call expression for a field.
     */
    public String createSetterCall(String fieldName) {
        return fieldName + "(";
    }

    /**
     * Generates a null check condition for non-primitive fields.
     */
    public CodeBlock createNullCheck(String expression) {
        return CodeBlock.of("$L != null", expression);
    }

    /**
     * Generates a null and empty check for collections.
     */
    public CodeBlock createNullAndEmptyCheck(String expression) {
        return CodeBlock.of("$L != null && !$L.isEmpty()", expression, expression);
    }

    /**
     * Creates an AttributeValue map put statement.
     */
    public CodeBlock createAttributePut(String fieldName, CodeBlock valueExpression) {
        return CodeBlock.of("attributes.put($S, $L)", fieldName, valueExpression);
    }

    /**
     * Creates a simple string attribute value.
     */
    public CodeBlock createStringAttribute(String valueExpression) {
        return CodeBlock.of("$T.createStringAttribute($L)", MAPPING_UTILS, valueExpression);
    }

    /**
     * Creates a simple number attribute value.
     */
    public CodeBlock createNumberAttribute(String valueExpression) {
        return CodeBlock.of("$T.createNumberAttribute($L)", MAPPING_UTILS, valueExpression);
    }

    /**
     * Creates a boolean attribute value.
     */
    public CodeBlock createBooleanAttribute(String valueExpression) {
        return CodeBlock.of("$T.builder().bool($L).build()", ATTRIBUTE_VALUE, valueExpression);
    }

    /**
     * Creates a string set attribute value.
     */
    public CodeBlock createStringSetAttribute(String valueExpression) {
        return CodeBlock.of("$T.builder().ss($L).build()", ATTRIBUTE_VALUE, valueExpression);
    }

    /**
     * Creates a list attribute value.
     */
    public CodeBlock createListAttribute(String valueExpression) {
        return CodeBlock.of("$T.builder().l($L).build()", ATTRIBUTE_VALUE, valueExpression);
    }

    /**
     * Gets the numeric method name for a given type.
     */
    public String getNumericMethodForType(String typeName) {
        return typeExtractor.getNumericMethodForType(typeName);
    }

    /**
     * Gets the Java type for a numeric type.
     */
    public String getJavaTypeForNumeric(String typeName) {
        return typeExtractor.getJavaTypeForNumeric(typeName);
    }

    /**
     * Extracts simple type name from a fully qualified type.
     */
    public String extractSimpleTypeName(String typeName) {
        return typeExtractor.extractSimpleTypeName(typeName);
    }

    /**
     * Gets the field name for a mapper dependency.
     */
    public String getFieldNameForDependency(String dependency) {
        return typeExtractor.getFieldNameForDependency(dependency);
    }

    /**
     * Extracts list element type.
     */
    public String extractListElementType(FieldInfo field) {
        return typeExtractor.extractListElementType(field);
    }

    /**
     * Extracts the qualified element type from a List field for JavaPoet imports.
     */
    public String extractListElementQualifiedType(FieldInfo field) {
        return typeExtractor.extractListElementQualifiedType(field);
    }

    /**
     * Creates a try-catch block for enum parsing.
     */
    public CodeBlock createEnumParseBlock(com.palantir.javapoet.ClassName enumType, String valueVar, String fieldName) {
        return CodeBlock.builder()
                .beginControlFlow("try")
                .addStatement("builder.$L($T.valueOf($L))", fieldName, enumType, valueVar)
                .nextControlFlow("catch ($T e)", IllegalArgumentException.class)
                .addStatement("// Skip invalid enum value")
                .endControlFlow()
                .build();
    }

    /**
     * Creates a try-catch block for instant parsing.
     */
    public CodeBlock createInstantParseBlock(String valueVar, String fieldName) {
        return CodeBlock.builder()
                .beginControlFlow("try")
                .addStatement("builder.$L($T.parse($L))", fieldName, INSTANT, valueVar)
                .nextControlFlow("catch ($T e)", Exception.class)
                .addStatement("// Skip invalid instant value")
                .endControlFlow()
                .build();
    }
}