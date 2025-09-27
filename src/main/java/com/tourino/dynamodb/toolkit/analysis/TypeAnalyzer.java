package com.tourino.dynamodb.toolkit.analysis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.tourino.dynamodb.toolkit.api.annotations.DynamoMappable;
import com.tourino.dynamodb.toolkit.api.annotations.Table;

/**
 * Analyzes types annotated with @DynamoMappable to determine mapping strategies
 * and dependencies for code generation.
 */
public class TypeAnalyzer {

    private final Elements elementUtils;
    private final Messager messager;

    public TypeAnalyzer(Elements elementUtils, Messager messager) {
        this.elementUtils = elementUtils;
        this.messager = messager;
    }

    /**
     * Analyzes a type element and returns comprehensive type information
     * needed for mapper generation.
     */
    public TypeInfo analyzeType(TypeElement typeElement) {
        String originalPackageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        String tableName = extractTableName(typeElement);

        List<FieldInfo> fields = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();

        // Analyze all fields in the class
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                VariableElement fieldElement = (VariableElement) enclosedElement;

                // Skip static fields (class-level constants)
                // Note: Don't skip final fields as Lombok immutable objects use final instance fields
                if (fieldElement.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }

                FieldInfo fieldInfo = analyzeField(fieldElement);
                fields.add(fieldInfo);

                // Collect mapper dependencies
                if (fieldInfo.hasMapperDependency()) {
                    dependencies.add(fieldInfo.getMapperDependency());
                }
            }
        }

        return new TypeInfo(typeElement, originalPackageName, className, tableName, fields, dependencies);
    }

    private FieldInfo analyzeField(VariableElement fieldElement) {
        String fieldName = fieldElement.getSimpleName().toString();
        TypeMirror fieldType = fieldElement.asType();

        // Get clean type name without annotations
        String fieldTypeName = getCleanTypeName(fieldType);

        FieldInfo.MappingStrategy strategy = determineMappingStrategy(fieldType);
        String mapperDependency = determineMapperDependency(fieldType, strategy);

        return new FieldInfo(fieldElement, fieldName, fieldType, fieldTypeName, strategy, mapperDependency);
    }

    private FieldInfo.MappingStrategy determineMappingStrategy(TypeMirror fieldType) {
        String typeName = getCleanTypeName(fieldType);

        // Handle strings first
        if (isStringType(typeName) || typeName.equals("java.lang.String")) {
            return FieldInfo.MappingStrategy.STRING;
        }

        // Handle numbers (both primitive and wrapper types)
        if (isNumberType(typeName) || isNumericPrimitive(fieldType)) {
            return FieldInfo.MappingStrategy.NUMBER;
        }

        // Handle boolean (both primitive and wrapper)
        if (isBooleanType(typeName) || fieldType.getKind() == TypeKind.BOOLEAN) {
            return FieldInfo.MappingStrategy.BOOLEAN;
        }

        // Handle Instant
        if (typeName.equals(Instant.class.getName())) {
            return FieldInfo.MappingStrategy.INSTANT;
        }

        // Handle enums
        if (fieldType instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            if (element instanceof TypeElement typeElement && typeElement.getKind() == ElementKind.ENUM) {
                return FieldInfo.MappingStrategy.ENUM;
            }
        }

        // Handle collections
        if (isListType(fieldType)) {
            TypeMirror elementType = getListElementType(fieldType);
            if (elementType != null) {
                String elementTypeName = elementType.toString();
                if (isStringType(elementTypeName)) {
                    return FieldInfo.MappingStrategy.STRING_LIST;
                } else if (isNestedNumberList(elementType)) {
                    return FieldInfo.MappingStrategy.NESTED_NUMBER_LIST;
                } else if (hasDynamoMappableAnnotation(elementType)) {
                    return FieldInfo.MappingStrategy.COMPLEX_LIST;
                }
            }
        }

        // Handle Maps
        if (isMapType(fieldType)) {
            return FieldInfo.MappingStrategy.MAP;
        }

        // Default to complex object if it has @DynamoMappable
        if (hasDynamoMappableAnnotation(fieldType)) {
            return FieldInfo.MappingStrategy.COMPLEX_OBJECT;
        }

        // Default fallback
        messager.printMessage(Diagnostic.Kind.WARNING,
                "Unknown mapping strategy for type: " + fieldType + ", defaulting to COMPLEX_OBJECT");
        return FieldInfo.MappingStrategy.COMPLEX_OBJECT;
    }

    private String determineMapperDependency(TypeMirror fieldType, FieldInfo.MappingStrategy strategy) {
        switch (strategy) {
            case COMPLEX_OBJECT:
                return getMapperClassName(fieldType);
            case COMPLEX_LIST:
                TypeMirror elementType = getListElementType(fieldType);
                return elementType != null ? getMapperClassName(elementType) : null;
            default:
                return null;
        }
    }

    private String getMapperClassName(TypeMirror type) {
        if (type instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            if (element instanceof TypeElement typeElement) {
                String className = typeElement.getSimpleName().toString();
                return TypeInfo.MAPPER_PACKAGE + "." + className + "Mapper";
            }
        }
        return null;
    }

    private boolean isStringType(String typeName) {
        return typeName.equals("java.lang.String") || typeName.equals("String");
    }

    private boolean isNumberType(String typeName) {
        return typeName.equals("java.lang.Double") || typeName.equals("Double") ||
                typeName.equals("java.lang.Integer") || typeName.equals("Integer") ||
                typeName.equals("java.lang.Long") || typeName.equals("Long") ||
                typeName.equals("java.lang.Float") || typeName.equals("Float") ||
                typeName.equals("double") || typeName.equals("int") ||
                typeName.equals("long") || typeName.equals("float");
    }

    private boolean isBooleanType(String typeName) {
        return typeName.equals("java.lang.Boolean") || typeName.equals("Boolean") ||
                typeName.equals("boolean");
    }

    private boolean isNumericPrimitive(TypeMirror type) {
        if (!type.getKind().isPrimitive()) {
            return false;
        }
        TypeKind kind = type.getKind();
        return kind == TypeKind.DOUBLE || kind == TypeKind.FLOAT ||
                kind == TypeKind.LONG || kind == TypeKind.INT ||
                kind == TypeKind.SHORT || kind == TypeKind.BYTE;
    }

    private boolean isNestedNumberList(TypeMirror type) {
        // Check if this is List<List<Double>> or similar nested number list
        if (isListType(type)) {
            TypeMirror innerElementType = getListElementType(type);
            if (innerElementType != null) {
                String innerTypeName = getCleanTypeName(innerElementType);
                return isNumberType(innerTypeName) || isNumericPrimitive(innerElementType);
            }
        }
        return false;
    }

    private boolean isListType(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) type;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        return typeElement.getQualifiedName().toString().equals("java.util.List");
    }

    private boolean isMapType(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) type;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        return typeElement.getQualifiedName().toString().equals("java.util.Map");
    }

    private TypeMirror getListElementType(TypeMirror listType) {
        if (listType instanceof DeclaredType declaredType) {
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                return typeArguments.get(0);
            }
        }
        return null;
    }

    private boolean hasDynamoMappableAnnotation(TypeMirror type) {
        if (type instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            return element.getAnnotation(DynamoMappable.class) != null;
        }
        return false;
    }

    /**
     * Gets the clean type name without annotation information.
     * Handles annotated types by extracting the raw type.
     */
    private String getCleanTypeName(TypeMirror type) {
        if (type instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            if (element instanceof TypeElement typeElement) {
                return typeElement.getQualifiedName().toString();
            }
        }

        // For primitive types and others, use the kind-based approach
        switch (type.getKind()) {
            case BOOLEAN:
                return "boolean";
            case BYTE:
                return "byte";
            case SHORT:
                return "short";
            case INT:
                return "int";
            case LONG:
                return "long";
            case CHAR:
                return "char";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            default:
                // For other types, try to clean the string representation
                String typeName = type.toString();
                // Remove annotation information (everything before the actual type)
                if (typeName.contains(" ")) {
                    String[] parts = typeName.split("\\s+");
                    // The last part should be the actual type
                    return parts[parts.length - 1];
                }
                return typeName;
        }
    }

    /**
     * Extracts the table name from the @Table annotation.
     * If no annotation is present or name is empty, defaults to class name.
     */
    private String extractTableName(TypeElement typeElement) {
        Table tableAnnotation = typeElement.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        // Default to class name if no explicit table name is provided
        return typeElement.getSimpleName().toString().toLowerCase();
    }
}