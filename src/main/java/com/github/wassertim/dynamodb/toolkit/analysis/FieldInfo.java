package com.github.wassertim.dynamodb.toolkit.analysis;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Information about a field that needs to be mapped to/from DynamoDB.
 * Contains analyzed type information and mapping strategy.
 */
public class FieldInfo {
    private final VariableElement fieldElement;
    private final String fieldName;
    private final TypeMirror fieldType;
    private final String fieldTypeName;
    private final MappingStrategy mappingStrategy;
    private final String mapperDependency; // For complex types that need a mapper

    public FieldInfo(VariableElement fieldElement, String fieldName, TypeMirror fieldType,
                     String fieldTypeName, MappingStrategy mappingStrategy, String mapperDependency) {
        this.fieldElement = fieldElement;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldTypeName = fieldTypeName;
        this.mappingStrategy = mappingStrategy;
        this.mapperDependency = mapperDependency;
    }

    public VariableElement getFieldElement() {
        return fieldElement;
    }

    public String getFieldName() {
        return fieldName;
    }

    public TypeMirror getFieldType() {
        return fieldType;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

    public MappingStrategy getMappingStrategy() {
        return mappingStrategy;
    }

    public String getMapperDependency() {
        return mapperDependency;
    }

    public boolean hasMapperDependency() {
        return mapperDependency != null;
    }

    public String getGetterMethodName() {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public String getSetterMethodName() {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public boolean isPrimitive() {
        return fieldType.getKind().isPrimitive();
    }

    public enum MappingStrategy {
        STRING,           // Direct string mapping
        NUMBER,           // Number mapping (Double, Integer, etc.)
        BOOLEAN,          // Boolean mapping
        INSTANT,          // Instant timestamp mapping
        ENUM,             // Enum name mapping
        STRING_LIST,      // List<String> mapping
        NESTED_NUMBER_LIST, // List<List<Double>> mapping for coordinates
        COMPLEX_OBJECT,   // Nested object requiring mapper
        COMPLEX_LIST,     // List<ComplexObject> requiring mapper
        MAP               // Map<String, ?> mapping
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldTypeName='" + fieldTypeName + '\'' +
                ", mappingStrategy=" + mappingStrategy +
                ", mapperDependency='" + mapperDependency + '\'' +
                '}';
    }
}