package io.github.wassertim.dynamodb.toolkit.injection;

import java.io.PrintWriter;
import java.util.Set;

import io.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import io.github.wassertim.dynamodb.toolkit.analysis.TypeExtractor;

/**
 * Generates CDI dependency injection code for mapper classes.
 * Handles constructor-based dependency injection for mapper dependencies.
 */
public class DependencyInjectionGenerator {

    private final TypeExtractor typeExtractor;

    public DependencyInjectionGenerator(TypeExtractor typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    /**
     * Generates dependency injection fields and constructor for a mapper class.
     */
    public void generateConstructorAndFields(PrintWriter writer, TypeInfo typeInfo, Set<String> dependencies) {
        if (dependencies.isEmpty()) {
            writer.println("    // No dependencies required");
            writer.println();
            return;
        }

        // Generate dependency fields
        generateDependencyFields(writer, dependencies);
        writer.println();

        // Generate constructor
        generateConstructor(writer, typeInfo, dependencies);
        writer.println();
    }

    private void generateDependencyFields(PrintWriter writer, Set<String> dependencies) {
        for (String dependency : dependencies) {
            String simpleClassName = typeExtractor.extractSimpleTypeName(dependency);
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);
            writer.println("    private final " + simpleClassName + " " + fieldName + ";");
        }
    }

    private void generateConstructor(PrintWriter writer, TypeInfo typeInfo, Set<String> dependencies) {
        String mapperClassName = typeInfo.getMapperClassName();

        // Constructor signature
        writer.print("    public " + mapperClassName + "(");
        String[] dependencyArray = dependencies.toArray(new String[0]);
        for (int i = 0; i < dependencyArray.length; i++) {
            if (i > 0) writer.print(", ");
            String dependency = dependencyArray[i];
            String simpleClassName = typeExtractor.extractSimpleTypeName(dependency);
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);
            writer.print(simpleClassName + " " + fieldName);
        }
        writer.println(") {");

        // Constructor body - field assignments
        for (String dependency : dependencies) {
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);
            writer.println("        this." + fieldName + " = " + fieldName + ";");
        }
        writer.println("    }");
    }
}