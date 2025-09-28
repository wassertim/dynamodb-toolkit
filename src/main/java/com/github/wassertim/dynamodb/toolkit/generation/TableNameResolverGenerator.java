package com.github.wassertim.dynamodb.toolkit.generation;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;

/**
 * Generates a complete TableNameResolver class that automatically includes
 * all @Table annotated classes in switch cases. This eliminates the need
 * for manual maintenance of hardcoded switch statements.
 */
public class TableNameResolverGenerator {

    private final Filer filer;
    private final Messager messager;

    public TableNameResolverGenerator(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }

    /**
     * Generates a complete TableNameResolver class for all discovered @Table annotated types.
     */
    public void generateTableNameResolver(List<TypeInfo> allTableTypes) throws IOException {
        if (allTableTypes.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "No @Table annotated types found, skipping TableNameResolver generation");
            return;
        }

        String packageName = "com.github.wassertim.infrastructure";
        String className = "TableNameResolver";
        String fullyQualifiedName = packageName + "." + className;

        JavaFileObject sourceFile = filer.createSourceFile(fullyQualifiedName);

        try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
            generateTableNameResolverClass(writer, allTableTypes, packageName, className);
        }

        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated TableNameResolver with " + allTableTypes.size() + " table mappings: " + fullyQualifiedName);
    }

    private void generateTableNameResolverClass(PrintWriter writer, List<TypeInfo> allTableTypes,
                                                String packageName, String className) {
        // Package declaration
        writer.println("package " + packageName + ";");
        writer.println();

        // Imports
        generateImports(writer);
        writer.println();

        // Class declaration with documentation
        generateClassDeclaration(writer, className, allTableTypes.size());

        // Generate resolveTableName method
        generateResolveTableNameMethod(writer, allTableTypes);

        // Close class
        writer.println("}");
    }

    private void generateImports(PrintWriter writer) {
        // No imports needed for pure table name resolution
    }

    private void generateClassDeclaration(PrintWriter writer, String className, int tableCount) {
        writer.println("/**");
        writer.println(" * Generated utility class for resolving base DynamoDB table names from domain entities.");
        writer.println(" * Returns only the base table name without any environment-specific prefixes.");
        writer.println(" * Automatically includes all @Table annotated classes in switch cases.");
        writer.println(" * Generated at: " + Instant.now());
        writer.println(" * Covers " + tableCount + " table" + (tableCount == 1 ? "" : "s") + ".");
        writer.println(" * DO NOT EDIT - This file is generated automatically");
        writer.println(" */");
        writer.println("public class " + className + " {");
        writer.println();
    }

    private void generateResolveTableNameMethod(PrintWriter writer, List<TypeInfo> allTableTypes) {
        writer.println("    /**");
        writer.println("     * Resolves the base table name from a @Table annotated domain entity class.");
        writer.println("     * Returns only the base table name without any environment-specific prefixes.");
        writer.println("     * Automatically generated to include all discovered @Table classes.");
        writer.println("     *");
        writer.println("     * @param entityClass the @Table annotated domain entity class");
        writer.println("     * @return the base table name without any prefix");
        writer.println("     * @throws IllegalArgumentException if the class is not a known @Table entity");
        writer.println("     */");
        writer.println("    public static String resolveTableName(Class<?> entityClass) {");
        writer.println("        return switch (entityClass.getName()) {");

        // Generate switch cases for all table types
        for (TypeInfo typeInfo : allTableTypes) {
            String fullyQualifiedClassName = typeInfo.getFullyQualifiedClassName();
            String tableName = extractTableName(typeInfo);
            writer.println("            case \"" + fullyQualifiedClassName + "\" -> \"" + tableName + "\";");
        }

        writer.println("            default -> throw new IllegalArgumentException(");
        writer.println("                \"Unknown @Table annotated class: \" + entityClass.getName() +");
        writer.println("                \". Known tables: " + generateKnownTablesList(allTableTypes) + "\");");
        writer.println("        };");
        writer.println("    }");
        writer.println();
    }

    /**
     * Extracts the table name from the TypeInfo which contains the @Table annotation value.
     */
    private String extractTableName(TypeInfo typeInfo) {
        return typeInfo.getTableName();
    }

    private String generateKnownTablesList(List<TypeInfo> allTableTypes) {
        return allTableTypes.stream()
                .map(TypeInfo::getFullyQualifiedClassName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }
}