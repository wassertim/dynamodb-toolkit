package com.github.wassertim.dynamodb.toolkit.mapping;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ClassName;
import com.github.wassertim.dynamodb.toolkit.analysis.FieldInfo;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeExtractor;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * JavaPoet-based field mapping code generator for converting between domain objects and DynamoDB AttributeValue format.
 * Handles the complex switch statements for different field mapping strategies using type-safe code generation.
 */
public class FieldMappingCodeGenerator {

    private final MappingCodeGeneratorUtils utils;

    public FieldMappingCodeGenerator(TypeExtractor typeExtractor) {
        this.utils = new MappingCodeGeneratorUtils(typeExtractor);
    }

    /**
     * Generates code to convert a domain object field to DynamoDB AttributeValue.
     */
    public CodeBlock generateToAttributeValueMapping(FieldInfo field, String objectName) {
        String fieldName = field.getFieldName();
        boolean isPrimitive = field.isPrimitive();
        String getterCall = utils.createGetterCall(objectName, fieldName);

        return switch (field.getMappingStrategy()) {
            case STRING -> generateStringMapping(fieldName, getterCall, isPrimitive);
            case NUMBER -> generateNumberMapping(fieldName, getterCall, isPrimitive);
            case BOOLEAN -> generateBooleanMapping(fieldName, getterCall, isPrimitive);
            case INSTANT -> generateInstantMapping(fieldName, getterCall);
            case ENUM -> generateEnumMapping(fieldName, getterCall);
            case STRING_LIST -> generateStringListMapping(fieldName, getterCall);
            case NUMBER_LIST -> generateNumberListMapping(fieldName, getterCall);
            case NESTED_NUMBER_LIST -> generateNestedNumberListMapping(fieldName, getterCall);
            case COMPLEX_OBJECT -> generateComplexObjectMapping(field, fieldName, getterCall);
            case COMPLEX_LIST -> generateComplexListMapping(field, fieldName, getterCall);
            case MAP -> generateMapMapping(fieldName, getterCall);
            default -> CodeBlock.of("// Unsupported mapping strategy: $L\n", field.getMappingStrategy());
        };
    }

    private CodeBlock generateStringMapping(String fieldName, String getterCall, boolean isPrimitive) {
        CodeBlock putStatement = CodeBlock.of("$L", utils.createAttributePut(fieldName, utils.createStringAttribute(getterCall)));

        if (isPrimitive) {
            return CodeBlock.builder()
                    .addStatement("$L", putStatement)
                    .build();
        } else {
            return CodeBlock.builder()
                    .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                    .addStatement("$L", putStatement)
                    .endControlFlow()
                    .build();
        }
    }

    private CodeBlock generateNumberMapping(String fieldName, String getterCall, boolean isPrimitive) {
        CodeBlock putStatement = CodeBlock.of("$L", utils.createAttributePut(fieldName, utils.createNumberAttribute(getterCall)));

        if (isPrimitive) {
            return CodeBlock.builder()
                    .addStatement("$L", putStatement)
                    .build();
        } else {
            return CodeBlock.builder()
                    .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                    .addStatement("$L", putStatement)
                    .endControlFlow()
                    .build();
        }
    }

    private CodeBlock generateBooleanMapping(String fieldName, String getterCall, boolean isPrimitive) {
        CodeBlock putStatement = CodeBlock.of("$L", utils.createAttributePut(fieldName, utils.createBooleanAttribute(getterCall)));

        if (isPrimitive) {
            return CodeBlock.builder()
                    .addStatement("$L", putStatement)
                    .build();
        } else {
            return CodeBlock.builder()
                    .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                    .addStatement("$L", putStatement)
                    .endControlFlow()
                    .build();
        }
    }

    private CodeBlock generateInstantMapping(String fieldName, String getterCall) {
        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                .addStatement("$L", utils.createAttributePut(fieldName, utils.createStringAttribute(getterCall + ".toString()")))
                .endControlFlow()
                .build();
    }

    private CodeBlock generateEnumMapping(String fieldName, String getterCall) {
        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                .addStatement("$L", utils.createAttributePut(fieldName, utils.createStringAttribute(getterCall + ".name()")))
                .endControlFlow()
                .build();
    }

    private CodeBlock generateStringListMapping(String fieldName, String getterCall) {
        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullAndEmptyCheck(getterCall))
                .addStatement("$L", utils.createAttributePut(fieldName, utils.createStringSetAttribute(getterCall)))
                .endControlFlow()
                .build();
    }

    private CodeBlock generateNumberListMapping(String fieldName, String getterCall) {
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName list = ClassName.get(List.class);
        ClassName collectors = ClassName.get(Collectors.class);

        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                .add("$T<$T> $LList = $L.stream()\n", list, attributeValue, fieldName, getterCall)
                .add("    .map(val -> $T.builder().n($T.valueOf(val)).build())\n", attributeValue, String.class)
                .addStatement("    .collect($T.toList())", collectors)
                .addStatement("$L", utils.createAttributePut(fieldName, utils.createListAttribute(fieldName + "List")))
                .endControlFlow()
                .build();
    }

    private CodeBlock generateNestedNumberListMapping(String fieldName, String getterCall) {
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName list = ClassName.get(List.class);
        ClassName collectors = ClassName.get(Collectors.class);

        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullAndEmptyCheck(getterCall))
                .add("$T<$T> nestedList = $L.stream()\n", list, attributeValue, getterCall)
                .add("    .map(innerList -> innerList.stream()\n")
                .add("        .map(num -> $T.builder().n($T.valueOf(num)).build())\n", attributeValue, String.class)
                .add("        .collect($T.toList()))\n", collectors)
                .add("    .map(numList -> $T.builder().l(numList).build())\n", attributeValue)
                .addStatement("    .collect($T.toList())", collectors)
                .beginControlFlow("if (!nestedList.isEmpty())")
                .addStatement("$L", utils.createAttributePut(fieldName, utils.createListAttribute("nestedList")))
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private CodeBlock generateComplexObjectMapping(FieldInfo field, String fieldName, String getterCall) {
        String mapperField = utils.getFieldNameForDependency(field.getMapperDependency());
        ClassName attributeValue = ClassName.get(AttributeValue.class);

        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullCheck(getterCall))
                .addStatement("$T $LValue = $L.toDynamoDbAttributeValue($L)", attributeValue, fieldName, mapperField, getterCall)
                .beginControlFlow("if ($LValue != null)", fieldName)
                .addStatement("attributes.put($S, $LValue)", fieldName, fieldName)
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private CodeBlock generateComplexListMapping(FieldInfo field, String fieldName, String getterCall) {
        String listMapperField = utils.getFieldNameForDependency(field.getMapperDependency());
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName list = ClassName.get(List.class);
        ClassName objects = ClassName.get(Objects.class);
        ClassName collectors = ClassName.get(Collectors.class);

        return CodeBlock.builder()
                .beginControlFlow("if ($L)", utils.createNullAndEmptyCheck(getterCall))
                .add("$T<$T> $LList = $L.stream()\n", list, attributeValue, fieldName, getterCall)
                .add("    .map($L::toDynamoDbAttributeValue)\n", listMapperField)
                .add("    .filter($T::nonNull)\n", objects)
                .addStatement("    .collect($T.toList())", collectors)
                .beginControlFlow("if (!$LList.isEmpty())", fieldName)
                .addStatement("$L", utils.createAttributePut(fieldName, utils.createListAttribute(fieldName + "List")))
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private CodeBlock generateMapMapping(String fieldName, String getterCall) {
        return CodeBlock.builder()
                .addStatement("// TODO: Implement MAP mapping for $L", fieldName)
                .addStatement("// if ($L != null) { ... }", getterCall)
                .build();
    }

    /**
     * Generates code to convert a DynamoDB AttributeValue to a domain object field.
     */
    public CodeBlock generateFromAttributeValueMapping(FieldInfo field) {
        String fieldName = field.getFieldName();
        ClassName attributeValue = ClassName.get(AttributeValue.class);

        CodeBlock mappingLogic = switch (field.getMappingStrategy()) {
            case STRING -> generateStringDeserialization(fieldName);
            case NUMBER -> generateNumberDeserialization(field, fieldName);
            case BOOLEAN -> generateBooleanDeserialization(field, fieldName);
            case INSTANT -> generateInstantDeserialization(fieldName);
            case ENUM -> generateEnumDeserialization(field, fieldName);
            case STRING_LIST -> generateStringListDeserialization(fieldName);
            case NUMBER_LIST -> generateNumberListDeserialization(field, fieldName);
            case NESTED_NUMBER_LIST -> generateNestedNumberListDeserialization(fieldName);
            case COMPLEX_OBJECT -> generateComplexObjectDeserialization(field, fieldName);
            case COMPLEX_LIST -> generateComplexListDeserialization(field, fieldName);
            case MAP -> generateMapDeserialization(fieldName);
            default -> CodeBlock.of("// Unsupported mapping strategy: $L\n", field.getMappingStrategy());
        };

        return CodeBlock.builder()
                .beginControlFlow("if (item.containsKey($S))", fieldName)
                .addStatement("$T $LAttr = item.get($S)", attributeValue, fieldName, fieldName)
                .add(mappingLogic)
                .endControlFlow()
                .build();
    }

    private CodeBlock generateStringDeserialization(String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");

        return CodeBlock.builder()
                .addStatement("$T value = $T.getStringSafely($LAttr)", String.class, mappingUtils, fieldName)
                .beginControlFlow("if (value != null)")
                .addStatement("builder.$L(value)", fieldName)
                .endControlFlow()
                .build();
    }

    private CodeBlock generateNumberDeserialization(FieldInfo field, String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");
        String numericMethod = utils.getNumericMethodForType(field.getFieldTypeName());
        String javaType = utils.getJavaTypeForNumeric(field.getFieldTypeName());

        return CodeBlock.builder()
                .addStatement("$L value = $T.$L($LAttr)", javaType, mappingUtils, numericMethod, fieldName)
                .beginControlFlow("if (value != null)")
                .addStatement("builder.$L(value)", fieldName)
                .endControlFlow()
                .build();
    }

    private CodeBlock generateBooleanDeserialization(FieldInfo field, String fieldName) {
        if (field.isPrimitive()) {
            return CodeBlock.builder()
                    .beginControlFlow("if ($LAttr.bool() != null)", fieldName)
                    .addStatement("builder.$L($LAttr.bool())", fieldName)
                    .endControlFlow()
                    .build();
        } else {
            return CodeBlock.builder()
                    .addStatement("$T value = $LAttr.bool()", Boolean.class, fieldName)
                    .beginControlFlow("if (value != null)")
                    .addStatement("builder.$L(value)", fieldName)
                    .endControlFlow()
                    .build();
        }
    }

    private CodeBlock generateInstantDeserialization(String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");

        return CodeBlock.builder()
                .addStatement("$T value = $T.getStringSafely($LAttr)", String.class, mappingUtils, fieldName)
                .beginControlFlow("if (value != null)")
                .add(utils.createInstantParseBlock("value", fieldName))
                .endControlFlow()
                .build();
    }

    private CodeBlock generateEnumDeserialization(FieldInfo field, String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");
        ClassName enumType = ClassName.bestGuess(field.getFieldTypeName());

        return CodeBlock.builder()
                .addStatement("$T value = $T.getStringSafely($LAttr)", String.class, mappingUtils, fieldName)
                .beginControlFlow("if (value != null)")
                .add(utils.createEnumParseBlock(enumType, "value", fieldName))
                .endControlFlow()
                .build();
    }

    private CodeBlock generateStringListDeserialization(String fieldName) {
        return CodeBlock.builder()
                .beginControlFlow("if ($LAttr.ss() != null)", fieldName)
                .addStatement("builder.$L($LAttr.ss())", fieldName, fieldName)
                .endControlFlow()
                .build();
    }

    private CodeBlock generateNumberListDeserialization(FieldInfo field, String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName list = ClassName.get(List.class);
        ClassName objects = ClassName.get(Objects.class);
        ClassName collectors = ClassName.get(Collectors.class);

        // Determine the element type from the field
        String elementTypeQualified = utils.extractListElementQualifiedType(field);
        String numericMethod = utils.getNumericMethodForType(elementTypeQualified);
        String javaType = utils.getJavaTypeForNumeric(elementTypeQualified);

        return CodeBlock.builder()
                .addStatement("$T<$T> listValue = $T.getListSafely($LAttr)", list, attributeValue, mappingUtils, fieldName)
                .beginControlFlow("if (listValue != null)")
                .add("$T<$L> $LList = listValue.stream()\n", list, javaType, fieldName)
                .add("    .map($T::$L)\n", mappingUtils, numericMethod)
                .add("    .filter($T::nonNull)\n", objects)
                .addStatement("    .collect($T.toList())", collectors)
                .addStatement("builder.$L($LList)", fieldName, fieldName)
                .endControlFlow()
                .build();
    }

    private CodeBlock generateNestedNumberListDeserialization(String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName list = ClassName.get(List.class);
        ClassName objects = ClassName.get(Objects.class);
        ClassName collectors = ClassName.get(Collectors.class);
        ClassName arrayList = ClassName.get(ArrayList.class);

        return CodeBlock.builder()
                .addStatement("$T<$T> nestedListValue = $T.getListSafely($LAttr)", list, attributeValue, mappingUtils, fieldName)
                .beginControlFlow("if (nestedListValue != null)")
                .add("$T<$T<$T>> coordinates = nestedListValue.stream()\n", list, list, Double.class)
                .add("    .map(av -> {\n")
                .add("        $T<$T> innerList = $T.getListSafely(av);\n", list, attributeValue, mappingUtils)
                .add("        if (innerList != null) {\n")
                .add("            return innerList.stream()\n")
                .add("                .map(numAv -> $T.getDoubleSafely(numAv))\n", mappingUtils)
                .add("                .filter($T::nonNull)\n", objects)
                .add("                .collect($T.toList());\n", collectors)
                .add("        }\n")
                .add("        return new $T<$T>();\n", arrayList, Double.class)
                .add("    })\n")
                .add("    .filter(list -> !list.isEmpty())\n")
                .addStatement("    .collect($T.toList())", collectors)
                .beginControlFlow("if (!coordinates.isEmpty())")
                .addStatement("builder.$L(coordinates)", fieldName)
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private CodeBlock generateComplexObjectDeserialization(FieldInfo field, String fieldName) {
        String mapperField = utils.getFieldNameForDependency(field.getMapperDependency());
        ClassName complexType = ClassName.bestGuess(field.getFieldTypeName());

        return CodeBlock.builder()
                .addStatement("$T value = $L.fromDynamoDbAttributeValue($LAttr)", complexType, mapperField, fieldName)
                .beginControlFlow("if (value != null)")
                .addStatement("builder.$L(value)", fieldName)
                .endControlFlow()
                .build();
    }

    private CodeBlock generateComplexListDeserialization(FieldInfo field, String fieldName) {
        ClassName mappingUtils = ClassName.get("com.github.wassertim.dynamodb.runtime", "MappingUtils");
        String listMapperField = utils.getFieldNameForDependency(field.getMapperDependency());
        String elementTypeQualified = utils.extractListElementQualifiedType(field);
        ClassName elementType = ClassName.bestGuess(elementTypeQualified);
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName list = ClassName.get(List.class);
        ClassName objects = ClassName.get(Objects.class);
        ClassName collectors = ClassName.get(Collectors.class);

        return CodeBlock.builder()
                .addStatement("$T<$T> listValue = $T.getListSafely($LAttr)", list, attributeValue, mappingUtils, fieldName)
                .beginControlFlow("if (listValue != null)")
                .add("$T<$T> $LList = listValue.stream()\n", list, elementType, fieldName)
                .add("    .map($L::fromDynamoDbAttributeValue)\n", listMapperField)
                .add("    .filter($T::nonNull)\n", objects)
                .addStatement("    .collect($T.toList())", collectors)
                .beginControlFlow("if (!$LList.isEmpty())", fieldName)
                .addStatement("builder.$L($LList)", fieldName, fieldName)
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private CodeBlock generateMapDeserialization(String fieldName) {
        return CodeBlock.builder()
                .addStatement("// TODO: Implement MAP mapping for $L", fieldName)
                .build();
    }

}