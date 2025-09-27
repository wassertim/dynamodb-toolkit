package io.github.wassertim.dynamodb.toolkit.mapping;

import java.io.PrintWriter;

import io.github.wassertim.dynamodb.toolkit.analysis.FieldInfo;
import io.github.wassertim.dynamodb.toolkit.analysis.TypeExtractor;

/**
 * Generates field mapping code for converting between domain objects and DynamoDB AttributeValue format.
 * Handles the complex switch statements for different field mapping strategies.
 */
public class FieldMappingCodeGenerator {

    private final TypeExtractor typeExtractor;

    public FieldMappingCodeGenerator(TypeExtractor typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    /**
     * Generates code to convert a domain object field to DynamoDB AttributeValue.
     */
    public void generateToAttributeValueMapping(PrintWriter writer, FieldInfo field, String objectName) {
        String fieldName = field.getFieldName();
        boolean isPrimitive = field.isPrimitive();
        String getterCall = objectName + ".get" +
            Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "()";

        switch (field.getMappingStrategy()) {
            case STRING:
                if (isPrimitive) {
                    writer.println("        attributes.put(\"" + fieldName +
                        "\", MappingUtils.createStringAttribute(" + getterCall + "));");
                } else {
                    writer.println("        if (" + getterCall + " != null) {");
                    writer.println("            attributes.put(\"" + fieldName +
                        "\", MappingUtils.createStringAttribute(" + getterCall + "));");
                    writer.println("        }");
                }
                break;

            case NUMBER:
                if (isPrimitive) {
                    writer.println("        attributes.put(\"" + fieldName +
                        "\", MappingUtils.createNumberAttribute(" + getterCall + "));");
                } else {
                    writer.println("        if (" + getterCall + " != null) {");
                    writer.println("            attributes.put(\"" + fieldName +
                        "\", MappingUtils.createNumberAttribute(" + getterCall + "));");
                    writer.println("        }");
                }
                break;

            case BOOLEAN:
                if (isPrimitive) {
                    writer.println("        attributes.put(\"" + fieldName +
                        "\", AttributeValue.builder().bool(" + getterCall + ").build());");
                } else {
                    writer.println("        if (" + getterCall + " != null) {");
                    writer.println("            attributes.put(\"" + fieldName +
                        "\", AttributeValue.builder().bool(" + getterCall + ").build());");
                    writer.println("        }");
                }
                break;

            case INSTANT:
                writer.println("        if (" + getterCall + " != null) {");
                writer.println("            attributes.put(\"" + fieldName +
                    "\", MappingUtils.createStringAttribute(" + getterCall + ".toString()));");
                writer.println("        }");
                break;

            case ENUM:
                writer.println("        if (" + getterCall + " != null) {");
                writer.println("            attributes.put(\"" + fieldName +
                    "\", MappingUtils.createStringAttribute(" + getterCall + ".name()));");
                writer.println("        }");
                break;

            case STRING_LIST:
                writer.println("        if (" + getterCall + " != null && !" + getterCall + ".isEmpty()) {");
                writer.println("            attributes.put(\"" + fieldName +
                    "\", AttributeValue.builder().ss(" + getterCall + ").build());");
                writer.println("        }");
                break;

            case NESTED_NUMBER_LIST:
                writer.println("        if (" + getterCall + " != null && !" + getterCall + ".isEmpty()) {");
                writer.println("            List<AttributeValue> nestedList = " + getterCall + ".stream()");
                writer.println("                .map(innerList -> innerList.stream()");
                writer.println("                    .map(num -> AttributeValue.builder().n(String.valueOf(num)).build())");
                writer.println("                    .collect(Collectors.toList()))");
                writer.println("                .map(numList -> AttributeValue.builder().l(numList).build())");
                writer.println("                .collect(Collectors.toList());");
                writer.println("            if (!nestedList.isEmpty()) {");
                writer.println("                attributes.put(\"" + fieldName +
                    "\", AttributeValue.builder().l(nestedList).build());");
                writer.println("            }");
                writer.println("        }");
                break;

            case COMPLEX_OBJECT:
                String mapperField = typeExtractor.getFieldNameForDependency(field.getMapperDependency());
                writer.println("        if (" + getterCall + " != null) {");
                writer.println("            AttributeValue " + fieldName + "Value = " + mapperField +
                    ".toDynamoDbAttributeValue(" + getterCall + ");");
                writer.println("            if (" + fieldName + "Value != null) {");
                writer.println("                attributes.put(\"" + fieldName + "\", " + fieldName + "Value);");
                writer.println("            }");
                writer.println("        }");
                break;

            case COMPLEX_LIST:
                String listMapperField = typeExtractor.getFieldNameForDependency(field.getMapperDependency());
                writer.println("        if (" + getterCall + " != null && !" + getterCall + ".isEmpty()) {");
                writer.println("            List<AttributeValue> " + fieldName + "List = " + getterCall + ".stream()");
                writer.println("                .map(" + listMapperField + "::toDynamoDbAttributeValue)");
                writer.println("                .filter(Objects::nonNull)");
                writer.println("                .collect(Collectors.toList());");
                writer.println("            if (!" + fieldName + "List.isEmpty()) {");
                writer.println("                attributes.put(\"" + fieldName +
                    "\", AttributeValue.builder().l(" + fieldName + "List).build());");
                writer.println("            }");
                writer.println("        }");
                break;

            case MAP:
                writer.println("        // TODO: Implement MAP mapping for " + fieldName);
                writer.println("        // if (" + getterCall + " != null) { ... }");
                break;

            default:
                writer.println("        // Unsupported mapping strategy: " + field.getMappingStrategy());
                break;
        }
    }

    /**
     * Generates code to convert a DynamoDB AttributeValue to a domain object field.
     */
    public void generateFromAttributeValueMapping(PrintWriter writer, FieldInfo field) {
        String fieldName = field.getFieldName();

        writer.println("        if (item.containsKey(\"" + fieldName + "\")) {");
        writer.println("            AttributeValue " + fieldName + "Attr = item.get(\"" + fieldName + "\");");

        switch (field.getMappingStrategy()) {
            case STRING:
                writer.println("            String value = MappingUtils.getStringSafely(" + fieldName + "Attr);");
                writer.println("            if (value != null) {");
                writer.println("                builder." + fieldName + "(value);");
                writer.println("            }");
                break;

            case NUMBER:
                String numericMethod = typeExtractor.getNumericMethodForType(field.getFieldTypeName());
                String javaType = typeExtractor.getJavaTypeForNumeric(field.getFieldTypeName());

                if (field.isPrimitive()) {
                    writer.println("            " + javaType + " value = MappingUtils." + numericMethod +
                        "(" + fieldName + "Attr);");
                    writer.println("            if (value != null) {");
                    writer.println("                builder." + fieldName + "(value);");
                    writer.println("            }");
                } else {
                    writer.println("            " + javaType + " value = MappingUtils." + numericMethod +
                        "(" + fieldName + "Attr);");
                    writer.println("            if (value != null) {");
                    writer.println("                builder." + fieldName + "(value);");
                    writer.println("            }");
                }
                break;

            case BOOLEAN:
                if (field.isPrimitive()) {
                    writer.println("            if (" + fieldName + "Attr.bool() != null) {");
                    writer.println("                builder." + fieldName + "(" + fieldName + "Attr.bool());");
                    writer.println("            }");
                } else {
                    writer.println("            Boolean value = " + fieldName + "Attr.bool();");
                    writer.println("            if (value != null) {");
                    writer.println("                builder." + fieldName + "(value);");
                    writer.println("            }");
                }
                break;

            case INSTANT:
                writer.println("            String value = MappingUtils.getStringSafely(" + fieldName + "Attr);");
                writer.println("            if (value != null) {");
                writer.println("                try {");
                writer.println("                    builder." + fieldName + "(Instant.parse(value));");
                writer.println("                } catch (Exception e) {");
                writer.println("                    // Skip invalid instant value");
                writer.println("                }");
                writer.println("            }");
                break;

            case ENUM:
                String enumType = typeExtractor.extractSimpleTypeName(field.getFieldTypeName());
                writer.println("            String value = MappingUtils.getStringSafely(" + fieldName + "Attr);");
                writer.println("            if (value != null) {");
                writer.println("                try {");
                writer.println("                    builder." + fieldName + "(" + enumType + ".valueOf(value));");
                writer.println("                } catch (IllegalArgumentException e) {");
                writer.println("                    // Skip invalid enum value");
                writer.println("                }");
                writer.println("            }");
                break;

            case STRING_LIST:
                writer.println("            if (" + fieldName + "Attr.ss() != null) {");
                writer.println("                builder." + fieldName + "(" + fieldName + "Attr.ss());");
                writer.println("            }");
                break;

            case NESTED_NUMBER_LIST:
                writer.println("            List<AttributeValue> nestedListValue = MappingUtils.getListSafely(" +
                    fieldName + "Attr);");
                writer.println("            if (nestedListValue != null) {");
                writer.println("                List<List<Double>> coordinates = nestedListValue.stream()");
                writer.println("                    .map(av -> {");
                writer.println("                        List<AttributeValue> innerList = MappingUtils.getListSafely(av);");
                writer.println("                        if (innerList != null) {");
                writer.println("                            return innerList.stream()");
                writer.println("                                .map(numAv -> MappingUtils.getDoubleSafely(numAv))");
                writer.println("                                .filter(Objects::nonNull)");
                writer.println("                                .collect(Collectors.toList());");
                writer.println("                        }");
                writer.println("                        return new ArrayList<Double>();");
                writer.println("                    })");
                writer.println("                    .filter(list -> !list.isEmpty())");
                writer.println("                    .collect(Collectors.toList());");
                writer.println("                if (!coordinates.isEmpty()) {");
                writer.println("                    builder." + fieldName + "(coordinates);");
                writer.println("                }");
                writer.println("            }");
                break;

            case COMPLEX_OBJECT:
                String mapperField = typeExtractor.getFieldNameForDependency(field.getMapperDependency());
                writer.println("            " + typeExtractor.extractSimpleTypeName(field.getFieldTypeName()) +
                    " value = " + mapperField + ".fromDynamoDbAttributeValue(" + fieldName + "Attr);");
                writer.println("            if (value != null) {");
                writer.println("                builder." + fieldName + "(value);");
                writer.println("            }");
                break;

            case COMPLEX_LIST:
                String listMapperField = typeExtractor.getFieldNameForDependency(field.getMapperDependency());
                String elementType = typeExtractor.extractListElementType(field);
                writer.println("            List<AttributeValue> listValue = MappingUtils.getListSafely(" +
                    fieldName + "Attr);");
                writer.println("            if (listValue != null) {");
                writer.println("                List<" + elementType + "> " + fieldName + "List = listValue.stream()");
                writer.println("                    .map(" + listMapperField + "::fromDynamoDbAttributeValue)");
                writer.println("                    .filter(Objects::nonNull)");
                writer.println("                    .collect(Collectors.toList());");
                writer.println("                if (!" + fieldName + "List.isEmpty()) {");
                writer.println("                    builder." + fieldName + "(" + fieldName + "List);");
                writer.println("                }");
                writer.println("            }");
                break;

            case MAP:
                writer.println("            // TODO: Implement MAP mapping for " + fieldName);
                break;

            default:
                writer.println("            // Unsupported mapping strategy: " + field.getMappingStrategy());
                break;
        }

        writer.println("        }");
        writer.println();
    }
}