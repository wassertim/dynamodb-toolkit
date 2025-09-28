package com.github.wassertim.dynamodb.toolkit.generation;

import com.palantir.javapoet.*;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.*;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

/**
 * JavaPoet-based convenience methods generator for mapper classes.
 * Generates convenience methods for mapper classes to reduce boilerplate code.
 * Provides common patterns like converting lists of DynamoDB items to domain objects.
 */
public class ConvenienceMethodGenerator {

    /**
     * Generates convenience methods for common DynamoDB operations using JavaPoet.
     */
    public List<MethodSpec> generateConvenienceMethods(TypeInfo typeInfo) {
        String className = typeInfo.getClassName();
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName domainClass = ClassName.bestGuess(className);

        List<MethodSpec> methods = new ArrayList<>();
        methods.add(generateFromDynamoDbItemMethod(className, domainClass, attributeValue));
        methods.add(generateFromDynamoDbItemsMethod(className, domainClass, attributeValue));
        methods.add(generateToDynamoDbItemMethod(className, domainClass, attributeValue));
        methods.add(generateToDynamoDbItemsMethod(className, domainClass, attributeValue));
        return methods;
    }

    private MethodSpec generateFromDynamoDbItemMethod(String className, ClassName domainClass, ClassName attributeValue) {
        return MethodSpec.methodBuilder("fromDynamoDbItem")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get("java.util", "Optional"), domainClass))
                .addParameter(ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    attributeValue), "item")
                .addJavadoc("Convenience method to convert a single DynamoDB item to a domain object.\n")
                .addJavadoc("Handles the common pattern of mapping GetItemResponse.item() to domain objects.\n")
                .addJavadoc("\n")
                .addJavadoc("@param item DynamoDB item from GetItemResponse.item()\n")
                .addJavadoc("@return Optional of $L object, empty if item is null or conversion fails\n", className)
                .beginControlFlow("if (item == null || item.isEmpty())")
                .addStatement("return $T.empty()", ClassName.get("java.util", "Optional"))
                .endControlFlow()
                .addStatement("$T result = fromDynamoDbAttributeValue($T.builder().m(item).build())",
                    domainClass, attributeValue)
                .addStatement("return $T.ofNullable(result)", ClassName.get("java.util", "Optional"))
                .build();
    }

    private MethodSpec generateFromDynamoDbItemsMethod(String className, ClassName domainClass, ClassName attributeValue) {
        return MethodSpec.methodBuilder("fromDynamoDbItems")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), domainClass))
                .addParameter(ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(String.class),
                        attributeValue)), "items")
                .addJavadoc("Convenience method to convert a list of DynamoDB items to domain objects.\\n")
                .addJavadoc("Handles the common pattern of mapping QueryResponse.items() to domain objects.\\n")
                .addJavadoc("\\n")
                .addJavadoc("@param items List of DynamoDB items from QueryResponse.items() or ScanResponse.items()\\n")
                .addJavadoc("@return List of $L objects, filtering out any null results\\n", className)
                .beginControlFlow("if (items == null || items.isEmpty())")
                .addStatement("return new $T<>()", ClassName.get("java.util", "ArrayList"))
                .endControlFlow()
                .addStatement("return items.stream()")
                .addStatement("    .map(item -> $T.builder().m(item).build())", attributeValue)
                .addStatement("    .map(this::fromDynamoDbAttributeValue)")
                .addStatement("    .filter($T::nonNull)", ClassName.get(Objects.class))
                .addStatement("    .collect($T.toList())", ClassName.get(Collectors.class))
                .build();
    }

    private MethodSpec generateToDynamoDbItemMethod(String className, ClassName domainClass, ClassName attributeValue) {
        return MethodSpec.methodBuilder("toDynamoDbItem")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    attributeValue))
                .addParameter(domainClass, "object")
                .addJavadoc("Convenience method to convert a single domain object to a DynamoDB item.\\n")
                .addJavadoc("Useful for PutItem operations.\\n")
                .addJavadoc("\\n")
                .addJavadoc("@param object The $L object to convert\\n", className)
                .addJavadoc("@return DynamoDB item (Map<String, AttributeValue>), or null if input is null or conversion fails\\n")
                .beginControlFlow("if (object == null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("$T av = toDynamoDbAttributeValue(object)", attributeValue)
                .addStatement("return av != null ? av.m() : null")
                .build();
    }

    private MethodSpec generateToDynamoDbItemsMethod(String className, ClassName domainClass, ClassName attributeValue) {
        return MethodSpec.methodBuilder("toDynamoDbItems")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(String.class),
                        attributeValue)))
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), domainClass), "objects")
                .addJavadoc("Convenience method to convert a list of domain objects to DynamoDB items.\\n")
                .addJavadoc("Useful for batch operations like batchWriteItem.\\n")
                .addJavadoc("\\n")
                .addJavadoc("@param objects List of $L objects to convert\\n", className)
                .addJavadoc("@return List of DynamoDB items (Map<String, AttributeValue>), filtering out any null results\\n")
                .beginControlFlow("if (objects == null || objects.isEmpty())")
                .addStatement("return new $T<>()", ClassName.get("java.util", "ArrayList"))
                .endControlFlow()
                .addStatement("return objects.stream()")
                .addStatement("    .map(this::toDynamoDbAttributeValue)")
                .addStatement("    .filter($T::nonNull)", ClassName.get(Objects.class))
                .addStatement("    .map(av -> av.m())")
                .addStatement("    .filter(map -> map != null && !map.isEmpty())")
                .addStatement("    .collect($T.toList())", ClassName.get(Collectors.class))
                .build();
    }

    /**
     * @deprecated Use generateConvenienceMethods(TypeInfo) instead
     */
    @Deprecated
    public void generateConvenienceMethods(java.io.PrintWriter writer, TypeInfo typeInfo) {
        List<MethodSpec> methods = generateConvenienceMethods(typeInfo);
        for (MethodSpec method : methods) {
            String[] lines = method.toString().split("\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    writer.println("    " + line);
                } else {
                    writer.println();
                }
            }
        }
    }
}