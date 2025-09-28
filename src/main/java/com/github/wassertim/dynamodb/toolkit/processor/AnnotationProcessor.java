package com.github.wassertim.dynamodb.toolkit.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import com.github.wassertim.dynamodb.toolkit.api.annotations.Table;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeAnalyzer;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import com.github.wassertim.dynamodb.toolkit.generation.MapperGenerator;
import com.github.wassertim.dynamodb.toolkit.generation.FieldConstantsGenerator;
import com.github.wassertim.dynamodb.toolkit.generation.TableNameResolverGenerator;

/**
 * Annotation processor for generating DynamoDB toolkit artifacts from
 * @DynamoMappable and @Table annotated classes.
 *
 * This processor:
 * 1. Discovers all @DynamoMappable annotated types during compilation
 * 2. Analyzes type dependencies and builds a dependency graph
 * 3. Generates three types of artifacts for each type:
 *    - Mapper classes: Bidirectional DynamoDB AttributeValue conversion with CDI support
 *    - Field constants: Type-safe field name constants for DynamoDB operations
 *    - TableNameResolver: Centralized table name resolution for @Table entities
 * 4. Processes dependencies in correct order (dependencies first)
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable",
    "com.github.wassertim.dynamodb.toolkit.api.annotations.Table"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AnnotationProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private TypeAnalyzer typeAnalyzer;
    private DependencyResolver dependencyResolver;
    private MapperGenerator mapperGenerator;
    private FieldConstantsGenerator fieldConstantsGenerator;
    private TableNameResolverGenerator tableNameResolverGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();

        // Initialize analysis and generation components
        this.typeAnalyzer = new TypeAnalyzer(elementUtils, messager);
        this.dependencyResolver = new DependencyResolver(typeAnalyzer);
        this.mapperGenerator = new MapperGenerator(filer, messager);
        this.fieldConstantsGenerator = new FieldConstantsGenerator(filer, messager);
        this.tableNameResolverGenerator = new TableNameResolverGenerator(filer, messager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            // Collect all @DynamoMappable annotated elements
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(DynamoMappable.class);

            // Collect all @Table annotated elements
            Set<? extends Element> tableElements = roundEnv.getElementsAnnotatedWith(Table.class);

            // If no annotations to process, return false
            if (annotatedElements.isEmpty() && tableElements.isEmpty()) {
                return false;
            }

            // Process @DynamoMappable types (mappers and field constants)
            if (!annotatedElements.isEmpty()) {
                processDynamoMappableTypes(annotatedElements);
            }

            // Process @Table types (TableNameResolver)
            if (!tableElements.isEmpty()) {
                processTableTypes(tableElements);
            }

            messager.printMessage(Diagnostic.Kind.NOTE, "DynamoDB mapper generation completed successfully");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Error during mapper generation: " + e.getMessage() + "\n" + sw.toString());
        }

        return true;
    }


    private void processDynamoMappableTypes(Set<? extends Element> annotatedElements) {
        messager.printMessage(Diagnostic.Kind.NOTE,
                String.format("Processing %d @DynamoMappable types...", annotatedElements.size()));

        // Analyze all types and build dependency graph
        dependencyResolver.analyzeDependencies(annotatedElements);

        // Generate all artifacts in dependency order (dependencies first)
        for (Element element : dependencyResolver.getProcessingOrder()) {
            if (element instanceof TypeElement typeElement) {
                try {
                    messager.printMessage(Diagnostic.Kind.NOTE,
                            "Generating artifacts for: " + typeElement.getQualifiedName());

                    TypeInfo typeInfo = typeAnalyzer.analyzeType(typeElement);

                    // Generate all artifacts for this type
                    mapperGenerator.generateMapper(typeInfo);
                    fieldConstantsGenerator.generateFieldConstants(typeInfo);

                } catch (IOException e) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            "Failed to generate artifacts for " + typeElement.getQualifiedName() + ": " + e.getMessage());
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            "Unexpected error generating artifacts for " + typeElement.getQualifiedName() +
                                    ": " + e.getMessage() + "\n" + sw.toString());
                }
            }
        }
    }

    private void processTableTypes(Set<? extends Element> tableElements) {
        messager.printMessage(Diagnostic.Kind.NOTE,
                String.format("Found %d @Table annotated types...", tableElements.size()));

        List<TypeInfo> tableTypeInfos = new ArrayList<>();

        for (Element element : tableElements) {
            if (element instanceof TypeElement typeElement) {
                try {
                    TypeInfo typeInfo = typeAnalyzer.analyzeType(typeElement);
                    tableTypeInfos.add(typeInfo);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            "Failed to analyze @Table type " + typeElement.getQualifiedName() + ": " + e.getMessage());
                }
            }
        }

        // Generate the TableNameResolver with all @Table types
        if (!tableTypeInfos.isEmpty()) {
            generateTableNameResolver(tableTypeInfos);
        }
    }

    private void generateTableNameResolver(List<TypeInfo> allTypeInfos) {
        try {
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "Generating TableNameResolver for " + allTypeInfos.size() + " table types...");

            tableNameResolverGenerator.generateTableNameResolver(allTypeInfos);

        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate TableNameResolver: " + e.getMessage());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Unexpected error generating TableNameResolver: " + e.getMessage() + "\n" + sw.toString());
        }
    }
}