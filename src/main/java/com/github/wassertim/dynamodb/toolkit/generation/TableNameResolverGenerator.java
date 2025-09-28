package com.github.wassertim.dynamodb.toolkit.generation;

import com.palantir.javapoet.*;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaPoet-based TableNameResolver class generator.
 * Generates a complete TableNameResolver class that automatically includes
 * all @Table annotated classes in switch cases. This eliminates the need
 * for manual maintenance of hardcoded switch statements.
 */
public class TableNameResolverGenerator extends AbstractJavaPoetGenerator {

    public TableNameResolverGenerator(Filer filer, Messager messager) {
        super(filer, messager);
    }

    /**
     * Generates a complete TableNameResolver class for all discovered @Table annotated types.
     */
    public void generateTableNameResolver(List<TypeInfo> allTableTypes) throws IOException {
        if (allTableTypes.isEmpty()) {
            messager.printMessage(javax.tools.Diagnostic.Kind.WARNING,
                    "No @Table annotated types found, skipping TableNameResolver generation");
            return;
        }

        String packageName = "com.github.wassertim.infrastructure";
        TypeSpec tableNameResolverClass = buildTableNameResolverClass(allTableTypes);
        writeJavaFile(packageName, tableNameResolverClass);

        messager.printMessage(javax.tools.Diagnostic.Kind.NOTE,
                "Generated TableNameResolver with " + allTableTypes.size() + " table mappings");
    }

    private TypeSpec buildTableNameResolverClass(List<TypeInfo> allTableTypes) {
        String className = "TableNameResolver";
        int tableCount = allTableTypes.size();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(createGeneratedJavadoc(
                        "Generated utility class for resolving base DynamoDB table names from domain entities.\n" +
                        "Returns only the base table name without any environment-specific prefixes.\n" +
                        "Automatically includes all @Table annotated classes in switch cases.\n" +
                        "Covers " + tableCount + " table" + (tableCount == 1 ? "" : "s") + ".\n" +
                        "DO NOT EDIT - This file is generated automatically"
                ));

        // Add resolveTableName method
        MethodSpec resolveTableNameMethod = buildResolveTableNameMethod(allTableTypes);
        classBuilder.addMethod(resolveTableNameMethod);

        return classBuilder.build();
    }

    private MethodSpec buildResolveTableNameMethod(List<TypeInfo> allTableTypes) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("resolveTableName")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)), "entityClass")
                .addJavadoc("Resolves the base table name from a @Table annotated domain entity class.\n")
                .addJavadoc("Returns only the base table name without any environment-specific prefixes.\n")
                .addJavadoc("Automatically generated to include all discovered @Table classes.\n")
                .addJavadoc("\n")
                .addJavadoc("@param entityClass the @Table annotated domain entity class\n")
                .addJavadoc("@return the base table name without any prefix\n")
                .addJavadoc("@throws IllegalArgumentException if the class is not a known @Table entity\n");

        // Build switch statement
        CodeBlock.Builder switchBuilder = CodeBlock.builder()
                .add("return switch (entityClass.getName()) {\n");

        // Generate switch cases for all table types
        for (TypeInfo typeInfo : allTableTypes) {
            String fullyQualifiedClassName = typeInfo.getFullyQualifiedClassName();
            String tableName = typeInfo.getTableName();
            switchBuilder.add("    case $S -> $S;\n", fullyQualifiedClassName, tableName);
        }

        // Generate default case
        String knownTablesList = allTableTypes.stream()
                .map(TypeInfo::getFullyQualifiedClassName)
                .collect(Collectors.joining(", "));

        switchBuilder.add("    default -> throw new $T(\n", IllegalArgumentException.class)
                .add("        $S +\n", "Unknown @Table annotated class: ")
                .add("        entityClass.getName() +\n")
                .add("        $S);\n", ". Known tables: " + knownTablesList)
                .add("};\n");

        methodBuilder.addCode(switchBuilder.build());
        return methodBuilder.build();
    }

    @Override
    protected String getTargetPackage(TypeInfo typeInfo) {
        return "com.github.wassertim.infrastructure";
    }
}