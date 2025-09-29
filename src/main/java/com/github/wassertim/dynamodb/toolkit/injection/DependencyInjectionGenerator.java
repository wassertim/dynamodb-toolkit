package com.github.wassertim.dynamodb.toolkit.injection;

import com.palantir.javapoet.*;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeExtractor;
import javax.lang.model.element.Modifier;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * JavaPoet-based CDI dependency injection code generator for mapper classes.
 * Generates CDI dependency injection code for mapper classes.
 * Handles constructor-based dependency injection for mapper dependencies.
 */
public class DependencyInjectionGenerator {

    private final TypeExtractor typeExtractor;

    public DependencyInjectionGenerator(TypeExtractor typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    /**
     * Generates dependency injection fields for a mapper class using JavaPoet.
     */
    public List<FieldSpec> generateDependencyFields(Set<String> dependencies) {
        List<FieldSpec> fields = new ArrayList<>();

        for (String dependency : dependencies) {
            String simpleClassName = typeExtractor.extractSimpleTypeName(dependency);
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);

            FieldSpec dependencyField = FieldSpec.builder(
                    ClassName.bestGuess(simpleClassName),
                    fieldName,
                    Modifier.PRIVATE, Modifier.FINAL)
                    .build();
            fields.add(dependencyField);
        }

        return fields;
    }

    /**
     * Generates dependency injection constructor for a mapper class using JavaPoet.
     */
    public MethodSpec generateConstructor(TypeInfo typeInfo, Set<String> dependencies) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (String dependency : dependencies) {
            String simpleClassName = typeExtractor.extractSimpleTypeName(dependency);
            String fieldName = typeExtractor.getFieldNameForDependency(dependency);

            constructorBuilder.addParameter(ClassName.bestGuess(simpleClassName), fieldName);
            constructorBuilder.addStatement("this.$L = $L", fieldName, fieldName);
        }

        return constructorBuilder.build();
    }


}