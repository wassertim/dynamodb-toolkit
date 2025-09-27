package io.github.wassertim.dynamodb.toolkit.generation;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import io.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import io.github.wassertim.dynamodb.toolkit.analysis.FieldInfo;

/**
 * Generates field constant classes containing type-safe field name constants
 * for DynamoDB operations. These constants eliminate hardcoded strings in
 * queries and provide compile-time safety for field references.
 */
public class FieldConstantsGenerator {

    private final Filer filer;
    private final Messager messager;

    public FieldConstantsGenerator(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }

    /**
     * Generates a field constants class for the given type information.
     */
    public void generateFieldConstants(TypeInfo typeInfo) throws IOException {
        String packageName = getFieldConstantsPackage(typeInfo);
        String constantsClassName = typeInfo.getClassName() + "Fields";
        String fullyQualifiedConstantsName = packageName + "." + constantsClassName;

        JavaFileObject sourceFile = filer.createSourceFile(fullyQualifiedConstantsName);

        try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
            generateFieldConstantsClass(writer, typeInfo, constantsClassName);
        }

        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated field constants: " + fullyQualifiedConstantsName);
    }

    private void generateFieldConstantsClass(PrintWriter writer, TypeInfo typeInfo, String constantsClassName) {
        String className = typeInfo.getClassName();
        String packageName = getFieldConstantsPackage(typeInfo);

        // Package declaration
        writer.println("package " + packageName + ";");
        writer.println();

        // Class declaration with documentation
        generateClassDeclaration(writer, className, constantsClassName);

        // Generate field constants
        for (FieldInfo field : typeInfo.getFields()) {
            generateFieldConstant(writer, field);
        }

        // Close class
        writer.println("}");
    }

    private void generateClassDeclaration(PrintWriter writer, String className, String constantsClassName) {
        writer.println("/**");
        writer.println(" * Generated field constants for " + className + ".");
        writer.println(" * Provides type-safe field name constants for DynamoDB operations,");
        writer.println(" * eliminating hardcoded strings and enabling compile-time validation.");
        writer.println(" * Generated at: " + Instant.now());
        writer.println(" */");
        writer.println("public final class " + constantsClassName + " {");
        writer.println();
        writer.println("    private " + constantsClassName + "() {");
        writer.println("        // Utility class - prevent instantiation");
        writer.println("    }");
        writer.println();
    }

    private void generateFieldConstant(PrintWriter writer, FieldInfo field) {
        String fieldName = field.getFieldName();

        writer.println("    /**");
        writer.println("     * Field name constant for '" + fieldName + "' field.");
        writer.println("     */");
        writer.println("    public static final String " + fieldName + " = \"" + fieldName + "\";");
        writer.println();
    }

    /**
     * Determines the package name for field constants.
     * Uses the fields package under the toolkit namespace.
     */
    private String getFieldConstantsPackage(TypeInfo typeInfo) {
        return "io.github.wassertim.dynamodb.toolkit.fields";
    }
}