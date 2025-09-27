package com.tourino.dynamodb.toolkit.mapping;

import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import com.tourino.dynamodb.toolkit.analysis.TypeInfo;
import com.tourino.dynamodb.toolkit.analysis.FieldInfo;
import com.tourino.dynamodb.toolkit.analysis.TypeExtractor;

/**
 * Resolves and generates import statements for generated mapper classes.
 * Handles standard imports, domain class imports, enum imports, and dependency mapper imports.
 */
public class ImportResolver {

    private final TypeExtractor typeExtractor;

    public ImportResolver(TypeExtractor typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    /**
     * Resolves all necessary imports for a mapper class.
     */
    public Set<String> resolveImports(TypeInfo typeInfo) {
        Set<String> imports = new LinkedHashSet<>();

        // Standard imports
        addStandardImports(imports);

        // Domain class import
        imports.add(typeInfo.getFullyQualifiedClassName());

        // Enum type imports
        addEnumImports(imports, typeInfo);

        // Dependency mapper imports
        addDependencyMapperImports(imports, typeInfo);

        // Domain class imports for complex types
        addComplexTypeImports(imports, typeInfo);

        return imports;
    }

    /**
     * Writes all imports to the PrintWriter.
     */
    public void writeImports(PrintWriter writer, Set<String> imports) {
        // Standard imports first
        writer.println("import software.amazon.awssdk.services.dynamodb.model.AttributeValue;");
        writer.println("import jakarta.enterprise.context.ApplicationScoped;");
        writer.println("import com.tourino.dynamodb.runtime.MappingUtils;");
        writer.println();
        writer.println("import java.util.*;");
        writer.println("import java.util.stream.Collectors;");
        writer.println("import java.time.Instant;");
        writer.println("import java.util.Objects;");
        writer.println();

        // Domain and custom imports
        for (String importStatement : imports) {
            if (!isStandardImport(importStatement)) {
                writer.println("import " + importStatement + ";");
            }
        }
    }

    private void addStandardImports(Set<String> imports) {
        // Standard imports are handled separately in writeImports
        // This method exists for consistency and future extension
    }

    private void addEnumImports(Set<String> imports, TypeInfo typeInfo) {
        for (FieldInfo field : typeInfo.getFields()) {
            if (field.getMappingStrategy() == FieldInfo.MappingStrategy.ENUM) {
                String enumTypeName = field.getFieldTypeName();
                if (enumTypeName.contains(".")) {
                    imports.add(enumTypeName);
                }
            }
        }
    }

    private void addDependencyMapperImports(Set<String> imports, TypeInfo typeInfo) {
        for (String dependency : typeInfo.getDependencies()) {
            imports.add(dependency);
        }
    }

    private void addComplexTypeImports(Set<String> imports, TypeInfo typeInfo) {
        for (FieldInfo field : typeInfo.getFields()) {
            if (field.getMappingStrategy() == FieldInfo.MappingStrategy.COMPLEX_OBJECT ||
                field.getMappingStrategy() == FieldInfo.MappingStrategy.COMPLEX_LIST) {

                String fieldTypeName = field.getFieldTypeName();
                if (fieldTypeName.contains(".")) {
                    if (field.getMappingStrategy() == FieldInfo.MappingStrategy.COMPLEX_LIST) {
                        // For lists, extract the fully qualified element type
                        String elementType = typeExtractor.extractFullyQualifiedListElementType(field);
                        if (elementType != null && elementType.contains(".")) {
                            imports.add(elementType);
                        }
                    } else {
                        // For single complex objects
                        imports.add(fieldTypeName);
                    }
                }
            }
        }
    }

    private boolean isStandardImport(String importStatement) {
        return importStatement.startsWith("java.") ||
               importStatement.startsWith("jakarta.") ||
               importStatement.startsWith("software.amazon.awssdk.") ||
               importStatement.equals("com.tourino.dynamodb.runtime.MappingUtils");
    }
}