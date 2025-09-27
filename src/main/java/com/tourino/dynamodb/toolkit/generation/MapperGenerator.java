package com.tourino.dynamodb.toolkit.generation;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.tourino.dynamodb.toolkit.analysis.TypeExtractor;
import com.tourino.dynamodb.toolkit.analysis.TypeInfo;
import com.tourino.dynamodb.toolkit.analysis.FieldInfo;
import com.tourino.dynamodb.toolkit.mapping.ImportResolver;
import com.tourino.dynamodb.toolkit.mapping.FieldMappingCodeGenerator;
import com.tourino.dynamodb.toolkit.injection.DependencyInjectionGenerator;

/**
 * Orchestrates the generation of DynamoDB mapper classes from analyzed type information.
 * Creates CDI-compatible beans with bidirectional mapping methods.
 *
 * This class follows the Single Responsibility Principle by delegating specific
 * generation tasks to specialized classes while orchestrating the overall process.
 */
public class MapperGenerator {

    private final Filer filer;
    private final Messager messager;
    private final TypeExtractor typeExtractor;
    private final ImportResolver importResolver;
    private final DependencyInjectionGenerator dependencyInjectionGenerator;
    private final FieldMappingCodeGenerator fieldMappingCodeGenerator;
    private final ConvenienceMethodGenerator convenienceMethodGenerator;

    public MapperGenerator(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
        this.typeExtractor = new TypeExtractor();
        this.importResolver = new ImportResolver(typeExtractor);
        this.dependencyInjectionGenerator = new DependencyInjectionGenerator(typeExtractor);
        this.fieldMappingCodeGenerator = new FieldMappingCodeGenerator(typeExtractor);
        this.convenienceMethodGenerator = new ConvenienceMethodGenerator();
    }

    /**
     * Generates a complete mapper class for the given type information.
     */
    public void generateMapper(TypeInfo typeInfo) throws IOException {
        String mapperPackage = typeInfo.getPackageName();
        String mapperClassName = typeInfo.getMapperClassName();
        String fullyQualifiedMapperName = mapperPackage + "." + mapperClassName;

        JavaFileObject sourceFile = filer.createSourceFile(fullyQualifiedMapperName);

        try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
            generateMapperClass(writer, typeInfo);
        }

        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated mapper: " + fullyQualifiedMapperName);
    }

    private void generateMapperClass(PrintWriter writer, TypeInfo typeInfo) {
        String packageName = typeInfo.getPackageName();
        String className = typeInfo.getClassName();
        String mapperClassName = typeInfo.getMapperClassName();
        Set<String> dependencies = typeInfo.getDependencies();

        // Package declaration
        writer.println("package " + packageName + ";");
        writer.println();

        // Imports
        Set<String> imports = importResolver.resolveImports(typeInfo);
        importResolver.writeImports(writer, imports);
        writer.println();

        // Class declaration with documentation and CDI annotation
        generateClassDeclaration(writer, className, mapperClassName);

        // Generate dependency injection (fields and constructor)
        dependencyInjectionGenerator.generateConstructorAndFields(writer, typeInfo, dependencies);

        // Generate core mapping methods
        generateToAttributeValueMethod(writer, typeInfo);
        generateFromAttributeValueMethod(writer, typeInfo);

        // Generate convenience methods
        convenienceMethodGenerator.generateConvenienceMethods(writer, typeInfo);

        // Close class
        writer.println("}");
    }

    private void generateClassDeclaration(PrintWriter writer, String className, String mapperClassName) {
        writer.println("/**");
        writer.println(" * Generated DynamoDB mapper for " + className + ".");
        writer.println(" * Provides bidirectional conversion between " + className + " and DynamoDB AttributeValue.");
        writer.println(" * Generated at: " + Instant.now());
        writer.println(" */");
        writer.println("@ApplicationScoped");
        writer.println("public class " + mapperClassName + " {");
        writer.println();
    }

    private void generateToAttributeValueMethod(PrintWriter writer, TypeInfo typeInfo) {
        String className = typeInfo.getClassName();
        String paramName = typeExtractor.getParameterName(className);

        writer.println("    /**");
        writer.println("     * Converts a " + className + " object to DynamoDB AttributeValue format.");
        writer.println("     *");
        writer.println("     * @param " + paramName + " The " + className + " object to convert");
        writer.println("     * @return AttributeValue in Map format, or null if input is null");
        writer.println("     */");
        writer.println("    public AttributeValue toDynamoDbAttributeValue(" + className + " " + paramName + ") {");
        writer.println("        if (" + paramName + " == null) {");
        writer.println("            return null;");
        writer.println("        }");
        writer.println();
        writer.println("        Map<String, AttributeValue> attributes = new HashMap<>();");
        writer.println();

        // Generate field mappings
        for (FieldInfo field : typeInfo.getFields()) {
            fieldMappingCodeGenerator.generateToAttributeValueMapping(writer, field, paramName);
            writer.println();
        }

        writer.println("        return AttributeValue.builder().m(attributes).build();");
        writer.println("    }");
        writer.println();
    }

    private void generateFromAttributeValueMethod(PrintWriter writer, TypeInfo typeInfo) {
        String className = typeInfo.getClassName();

        writer.println("    /**");
        writer.println("     * Converts a DynamoDB AttributeValue to a " + className + " object.");
        writer.println("     *");
        writer.println("     * @param attributeValue The DynamoDB AttributeValue to convert (must be in Map format)");
        writer.println("     * @return " + className + " object, or null if input is null or invalid");
        writer.println("     */");
        writer.println("    public " + className + " fromDynamoDbAttributeValue(AttributeValue attributeValue) {");
        writer.println("        if (attributeValue == null || attributeValue.m() == null) {");
        writer.println("            return null;");
        writer.println("        }");
        writer.println();
        writer.println("        Map<String, AttributeValue> item = attributeValue.m();");
        writer.println("        var builder = " + className + ".builder();");
        writer.println();

        // Generate field mappings
        for (FieldInfo field : typeInfo.getFields()) {
            fieldMappingCodeGenerator.generateFromAttributeValueMapping(writer, field);
        }

        writer.println("        return builder.build();");
        writer.println("    }");
        writer.println();
    }
}