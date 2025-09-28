package com.github.wassertim.dynamodb.toolkit.generation;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import com.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.Instant;

/**
 * Abstract base class for JavaPoet-based code generators.
 * Provides common functionality for generating type-safe Java code
 * with automatic import management and consistent formatting.
 */
public abstract class AbstractJavaPoetGenerator {

    protected final Filer filer;
    protected final Messager messager;

    protected AbstractJavaPoetGenerator(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }

    /**
     * Generates a Java file using JavaPoet with consistent formatting.
     */
    protected void writeJavaFile(String packageName, TypeSpec typeSpec) throws IOException {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .indent("    ") // 4-space indentation to match existing code style
                .skipJavaLangImports(true)
                .build();

        javaFile.writeTo(filer);

        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated class: " + packageName + "." + typeSpec.name());
    }

    /**
     * Creates a standard Javadoc header with generation timestamp.
     */
    protected String createGeneratedJavadoc(String description) {
        return description + "\n" +
               "Generated at: " + Instant.now() + "\n";
    }

    /**
     * Determines the target package name for generated classes.
     * Subclasses can override this for specific package strategies.
     */
    protected abstract String getTargetPackage(TypeInfo typeInfo);
}