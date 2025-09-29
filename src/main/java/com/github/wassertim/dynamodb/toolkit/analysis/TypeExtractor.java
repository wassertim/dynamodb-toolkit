package com.github.wassertim.dynamodb.toolkit.analysis;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

/**
 * Utility class for extracting and analyzing Java types.
 * Handles type name extraction, generic type analysis, and type conversions.
 */
public class TypeExtractor {

    /**
     * Extracts the simple class name from a fully qualified type name.
     */
    public String extractSimpleTypeName(String fullyQualifiedTypeName) {
        int lastDot = fullyQualifiedTypeName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedTypeName.substring(lastDot + 1) : fullyQualifiedTypeName;
    }

    /**
     * Extracts the simple element type from a List field for code generation.
     */
    public String extractListElementType(FieldInfo field) {
        if (field.getFieldType() instanceof DeclaredType declaredType) {
            var typeArguments = declaredType.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                var elementType = typeArguments.get(0);
                if (elementType instanceof DeclaredType elementDeclaredType) {
                    var element = elementDeclaredType.asElement();
                    if (element instanceof TypeElement typeElement) {
                        return typeElement.getSimpleName().toString();
                    }
                }
            }
        }
        return "Object";
    }

    /**
     * Extracts the qualified element type name from a List field for JavaPoet imports.
     */
    public String extractListElementQualifiedType(FieldInfo field) {
        if (field.getFieldType() instanceof DeclaredType declaredType) {
            var typeArguments = declaredType.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                var elementType = typeArguments.get(0);
                if (elementType instanceof DeclaredType elementDeclaredType) {
                    var element = elementDeclaredType.asElement();
                    if (element instanceof TypeElement typeElement) {
                        return typeElement.getQualifiedName().toString();
                    }
                }
            }
        }
        return "java.lang.Object";
    }

    /**
     * Extracts the fully qualified element type from a List field for imports.
     */
    public String extractFullyQualifiedListElementType(FieldInfo field) {
        if (field.getFieldType() instanceof DeclaredType declaredType) {
            var typeArguments = declaredType.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                var elementType = typeArguments.get(0);
                if (elementType instanceof DeclaredType elementDeclaredType) {
                    var element = elementDeclaredType.asElement();
                    if (element instanceof TypeElement typeElement) {
                        return typeElement.getQualifiedName().toString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the appropriate MappingUtils method name for a numeric type.
     */
    public String getNumericMethodForType(String typeName) {
        if (typeName.equals("java.lang.Integer") || typeName.equals("Integer") || typeName.equals("int")) {
            return "getIntegerSafely";
        } else if (typeName.equals("java.lang.Long") || typeName.equals("Long") || typeName.equals("long")) {
            return "getLongSafely";
        } else if (typeName.equals("java.lang.Double") || typeName.equals("Double") || typeName.equals("double")) {
            return "getDoubleSafely";
        } else if (typeName.equals("java.lang.Float") || typeName.equals("Float") || typeName.equals("float")) {
            return "getFloatSafely";
        } else if (typeName.equals("java.math.BigDecimal") || typeName.equals("BigDecimal")) {
            return "getBigDecimalSafely";
        }
        return "getDoubleSafely"; // Default fallback
    }

    /**
     * Returns the Java type name for numeric field types.
     */
    public String getJavaTypeForNumeric(String typeName) {
        if (typeName.equals("java.lang.Integer") || typeName.equals("Integer") || typeName.equals("int")) {
            return "Integer";
        } else if (typeName.equals("java.lang.Long") || typeName.equals("Long") || typeName.equals("long")) {
            return "Long";
        } else if (typeName.equals("java.lang.Double") || typeName.equals("Double") || typeName.equals("double")) {
            return "Double";
        } else if (typeName.equals("java.lang.Float") || typeName.equals("Float") || typeName.equals("float")) {
            return "Float";
        } else if (typeName.equals("java.math.BigDecimal") || typeName.equals("BigDecimal")) {
            return "BigDecimal";
        }
        return "Double"; // Default fallback
    }

    /**
     * Generates a field name from a dependency class name (converts to camelCase).
     */
    public String getFieldNameForDependency(String dependencyClassName) {
        String simpleClassName = extractSimpleTypeName(dependencyClassName);
        return Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
    }

    /**
     * Generates a parameter name from a class name (converts to camelCase).
     */
    public String getParameterName(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}