package com.github.wassertim.dynamodb.toolkit.generation;

import com.palantir.javapoet.*;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeExtractor;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import com.github.wassertim.dynamodb.toolkit.analysis.FieldInfo;
import com.github.wassertim.dynamodb.toolkit.mapping.FieldMappingCodeGenerator;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * JavaPoet-based implementation of mapper class generation.
 * Orchestrates the generation of DynamoDB mapper classes from analyzed type information.
 * Creates CDI-compatible beans with bidirectional mapping methods.
 *
 * This class follows the Single Responsibility Principle by delegating specific
 * generation tasks to specialized classes while orchestrating the overall process.
 */
public class MapperGenerator extends AbstractJavaPoetGenerator {

    private final TypeExtractor typeExtractor;
    private final FieldMappingCodeGenerator fieldMappingCodeGenerator;

    public MapperGenerator(Filer filer, Messager messager) {
        super(filer, messager);
        this.typeExtractor = new TypeExtractor();
        this.fieldMappingCodeGenerator = new FieldMappingCodeGenerator(typeExtractor);
    }

    /**
     * Generates a complete mapper class for the given type information.
     */
    public void generateMapper(TypeInfo typeInfo) throws IOException {
        String packageName = getTargetPackage(typeInfo);
        TypeSpec mapperClass = buildMapperClass(typeInfo);
        writeJavaFile(packageName, mapperClass);
    }

    private TypeSpec buildMapperClass(TypeInfo typeInfo) {
        String className = typeInfo.getClassName();
        String mapperClassName = typeInfo.getMapperClassName();
        Set<String> dependencies = typeInfo.getDependencies();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(mapperClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ApplicationScoped.class)
                .addJavadoc(createGeneratedJavadoc(
                        "Generated DynamoDB mapper for " + className + ".\n" +
                        "Provides bidirectional conversion between " + className + " and DynamoDB AttributeValue."
                ));

        // Add dependency injection (fields and constructor)
        addDependencyInjection(classBuilder, typeInfo, dependencies);

        // Add core mapping methods
        classBuilder.addMethod(buildToAttributeValueMethod(typeInfo));
        classBuilder.addMethod(buildFromAttributeValueMethod(typeInfo));

        // Add convenience methods
        addConvenienceMethods(classBuilder, typeInfo);

        return classBuilder.build();
    }

    private void addDependencyInjection(TypeSpec.Builder classBuilder, TypeInfo typeInfo, Set<String> dependencies) {
        if (dependencies.isEmpty()) {
            return;
        }

        // Add dependency fields
        for (String dependency : dependencies) {
            String simpleClassName = typeExtractor.extractSimpleTypeName(dependency);
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);

            FieldSpec dependencyField = FieldSpec.builder(
                    ClassName.bestGuess(simpleClassName),
                    fieldName,
                    Modifier.PRIVATE, Modifier.FINAL)
                    .build();
            classBuilder.addField(dependencyField);
        }

        // Add constructor
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (String dependency : dependencies) {
            String simpleClassName = typeExtractor.extractSimpleTypeName(dependency);
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);

            constructorBuilder.addParameter(ClassName.bestGuess(simpleClassName), fieldName);
            constructorBuilder.addStatement("this.$L = $L", fieldName, fieldName);
        }

        classBuilder.addMethod(constructorBuilder.build());
    }

    private MethodSpec buildToAttributeValueMethod(TypeInfo typeInfo) {
        String className = typeInfo.getClassName();
        String paramName = typeExtractor.getParameterName(className);
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName domainClass = ClassName.bestGuess(className);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDynamoDbAttributeValue")
                .addModifiers(Modifier.PUBLIC)
                .returns(attributeValue)
                .addParameter(domainClass, paramName)
                .addJavadoc("Converts a $L object to DynamoDB AttributeValue format.\\n", className)
                .addJavadoc("\\n")
                .addJavadoc("@param $L The $L object to convert\\n", paramName, className)
                .addJavadoc("@return AttributeValue in Map format, or null if input is null\\n");

        // Null check
        methodBuilder.beginControlFlow("if ($L == null)", paramName)
                .addStatement("return null")
                .endControlFlow()
                .addStatement("")
                .addStatement("$T<$T, $T> attributes = new $T<>()",
                    Map.class, String.class, AttributeValue.class, HashMap.class)
                .addStatement("");

        // Generate field mappings
        for (FieldInfo field : typeInfo.getFields()) {
            CodeBlock mappingCode = fieldMappingCodeGenerator.generateToAttributeValueMapping(field, paramName);
            methodBuilder.addCode(mappingCode);
            methodBuilder.addStatement("");
        }

        methodBuilder.addStatement("return $T.builder().m(attributes).build()", attributeValue);

        return methodBuilder.build();
    }

    private MethodSpec buildFromAttributeValueMethod(TypeInfo typeInfo) {
        String className = typeInfo.getClassName();
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName domainClass = ClassName.bestGuess(className);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDynamoDbAttributeValue")
                .addModifiers(Modifier.PUBLIC)
                .returns(domainClass)
                .addParameter(attributeValue, "attributeValue")
                .addJavadoc("Converts a DynamoDB AttributeValue to a $L object.\\n", className)
                .addJavadoc("\\n")
                .addJavadoc("@param attributeValue The DynamoDB AttributeValue to convert (must be in Map format)\\n")
                .addJavadoc("@return $L object, or null if input is null or invalid\\n", className);

        // Null check
        methodBuilder.beginControlFlow("if (attributeValue == null || attributeValue.m() == null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("")
                .addStatement("$T<$T, $T> item = attributeValue.m()",
                    Map.class, String.class, AttributeValue.class)
                .addStatement("var builder = $T.builder()", domainClass)
                .addStatement("");

        // Generate field mappings
        for (FieldInfo field : typeInfo.getFields()) {
            CodeBlock mappingCode = fieldMappingCodeGenerator.generateFromAttributeValueMapping(field);
            methodBuilder.addCode(mappingCode);
        }

        methodBuilder.addStatement("return builder.build()");

        return methodBuilder.build();
    }

    private void addConvenienceMethods(TypeSpec.Builder classBuilder, TypeInfo typeInfo) {
        String className = typeInfo.getClassName();
        ClassName attributeValue = ClassName.get(AttributeValue.class);
        ClassName domainClass = ClassName.bestGuess(className);

        // fromDynamoDbItem method
        MethodSpec fromDynamoDbItem = MethodSpec.methodBuilder("fromDynamoDbItem")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get("java.util", "Optional"), domainClass))
                .addParameter(ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    attributeValue), "item")
                .addJavadoc("Convenience method to convert a single DynamoDB item to a domain object.\\n")
                .addJavadoc("Handles the common pattern of mapping GetItemResponse.item() to domain objects.\\n")
                .addJavadoc("\\n")
                .addJavadoc("@param item DynamoDB item from GetItemResponse.item()\\n")
                .addJavadoc("@return Optional of $L object, empty if item is null or conversion fails\\n", className)
                .beginControlFlow("if (item == null || item.isEmpty())")
                .addStatement("return $T.empty()", ClassName.get("java.util", "Optional"))
                .endControlFlow()
                .addStatement("$T result = fromDynamoDbAttributeValue($T.builder().m(item).build())",
                    domainClass, attributeValue)
                .addStatement("return $T.ofNullable(result)", ClassName.get("java.util", "Optional"))
                .build();

        // fromDynamoDbItems method
        MethodSpec fromDynamoDbItems = MethodSpec.methodBuilder("fromDynamoDbItems")
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

        // toDynamoDbItem method
        MethodSpec toDynamoDbItem = MethodSpec.methodBuilder("toDynamoDbItem")
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

        // toDynamoDbItems method
        MethodSpec toDynamoDbItems = MethodSpec.methodBuilder("toDynamoDbItems")
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

        classBuilder.addMethod(fromDynamoDbItem);
        classBuilder.addMethod(fromDynamoDbItems);
        classBuilder.addMethod(toDynamoDbItem);
        classBuilder.addMethod(toDynamoDbItems);
    }

    @Override
    protected String getTargetPackage(TypeInfo typeInfo) {
        return typeInfo.getPackageName();
    }
}