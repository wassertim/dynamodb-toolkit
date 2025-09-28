package com.github.wassertim.dynamodb.toolkit.generation;

import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;
import com.github.wassertim.dynamodb.toolkit.analysis.FieldInfo;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * JavaPoet-based implementation of field constants generator.
 * Generates field constant classes containing type-safe field name constants
 * for DynamoDB operations. These constants eliminate hardcoded strings in
 * queries and provide compile-time safety for field references.
 */
public class FieldConstantsGenerator extends AbstractJavaPoetGenerator {

    public FieldConstantsGenerator(Filer filer, Messager messager) {
        super(filer, messager);
    }

    /**
     * Generates a field constants class for the given type information.
     */
    public void generateFieldConstants(TypeInfo typeInfo) throws IOException {
        String packageName = getTargetPackage(typeInfo);
        String constantsClassName = typeInfo.getClassName() + "Fields";

        TypeSpec constantsClass = buildFieldConstantsClass(typeInfo, constantsClassName);
        writeJavaFile(packageName, constantsClass);
    }

    private TypeSpec buildFieldConstantsClass(TypeInfo typeInfo, String constantsClassName) {
        String className = typeInfo.getClassName();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(constantsClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc(createGeneratedJavadoc(
                        "Generated field constants for " + className + ".\n" +
                        "Provides type-safe field name constants for DynamoDB operations,\n" +
                        "eliminating hardcoded strings and enabling compile-time validation."
                ));

        // Add private constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addComment("Utility class - prevent instantiation")
                .build();
        classBuilder.addMethod(constructor);

        // Generate field constants
        for (FieldInfo field : typeInfo.getFields()) {
            FieldSpec fieldConstant = createFieldConstant(field);
            classBuilder.addField(fieldConstant);
        }

        return classBuilder.build();
    }

    private FieldSpec createFieldConstant(FieldInfo field) {
        String fieldName = field.getFieldName();

        return FieldSpec.builder(String.class, fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", fieldName)
                .addJavadoc("Field name constant for '$L' field.\n", fieldName)
                .build();
    }

    @Override
    protected String getTargetPackage(TypeInfo typeInfo) {
        return "com.github.wassertim.dynamodb.toolkit.fields";
    }
}