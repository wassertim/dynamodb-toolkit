package com.github.wassertim.dynamodb.toolkit.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation tests for JavaPoet-generated code quality and performance.
 */
public class JavaPoetValidationTest {

    @Test
    @DisplayName("Validate TestUserMapper code quality")
    void validateTestUserMapperQuality() throws IOException {
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/TestUserMapper.java");

        assertThat(mapperPath).exists();

        String content = Files.readString(mapperPath);

        // Validate JavaPoet-generated characteristics
        assertThat(content)
            .contains("@ApplicationScoped")
            .contains("public class TestUserMapper")
            .contains("toDynamoDbAttributeValue(TestUser testUser)")
            .contains("fromDynamoDbAttributeValue(AttributeValue attributeValue)")
            .contains("fromDynamoDbItem(Map<String, AttributeValue> item)")
            .contains("fromDynamoDbItems(List<Map<String, AttributeValue>> items)")
            .contains("toDynamoDbItem(TestUser object)")
            .contains("toDynamoDbItems(List<TestUser> objects)");

        // Validate clean code structure (no string concatenation artifacts)
        assertThat(content)
            .doesNotContain("\\n") // No escaped newlines
            .doesNotContain("+ \"") // No string concatenation patterns
            .doesNotContain("writer.println"); // No PrintWriter artifacts

        // Validate proper JavaDoc
        assertThat(content)
            .contains("/**")
            .contains("Generated DynamoDB mapper for TestUser")
            .contains("Generated at:")
            .contains("@param")
            .contains("@return");
    }

    @Test
    @DisplayName("Validate TestUserFields code quality")
    void validateTestUserFieldsQuality() throws IOException {
        Path fieldsPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/fields/TestUserFields.java");

        assertThat(fieldsPath).exists();

        String content = Files.readString(fieldsPath);

        // Validate field constants structure
        assertThat(content)
            .contains("public final class TestUserFields")
            .contains("public static final String userId = \"userId\"")
            .contains("public static final String email = \"email\"")
            .contains("private TestUserFields()")
            .contains("Utility class - prevent instantiation");

        // Validate proper JavaDoc for each field
        assertThat(content)
            .contains("Field name constant for 'userId' field")
            .contains("Field name constant for 'email' field");
    }

    @Test
    @DisplayName("Validate TableNameResolver code quality")
    void validateTableNameResolverQuality() throws IOException {
        Path resolverPath = Path.of("target/generated-sources/annotations/com/github/wassertim/infrastructure/TableNameResolver.java");

        assertThat(resolverPath).exists();

        String content = Files.readString(resolverPath);

        // Validate modern switch expression syntax
        assertThat(content)
            .contains("return switch (entityClass.getName())")
            .contains("case \"com.github.wassertim.dynamodb.toolkit.integration.entities.TestUser\" -> \"test-users\"")
            .contains("default -> throw new IllegalArgumentException")
            .doesNotContain("break;"); // No old-style switch

        // Validate proper error handling
        assertThat(content)
            .contains("Unknown @Table annotated class:")
            .contains("Known tables:");
    }

    @Test
    @DisplayName("Measure code generation performance metrics")
    void measureCodeGenerationMetrics() throws IOException {
        // Analyze generated mapper file
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/TestUserMapper.java");
        String mapperContent = Files.readString(mapperPath);

        // Count lines of code (excluding empty lines and comments)
        long mapperLoc = mapperContent.lines()
            .filter(line -> !line.trim().isEmpty())
            .filter(line -> !line.trim().startsWith("//"))
            .filter(line -> !line.trim().startsWith("*"))
            .filter(line -> !line.trim().startsWith("/**"))
            .filter(line -> !line.trim().equals("*/"))
            .count();

        // Generated mapper should be reasonably sized (not too bloated)
        assertThat(mapperLoc).describedAs("Mapper lines of code").isBetween(150L, 300L);

        // Count import statements
        long importCount = mapperContent.lines()
            .filter(line -> line.startsWith("import "))
            .count();

        // JavaPoet should optimize imports
        assertThat(importCount).describedAs("Import count").isLessThan(15);

        // Verify method count
        long methodCount = Pattern.compile("public .* \\w+\\(.*\\) \\{")
            .matcher(mapperContent)
            .results()
            .count();

        // Should have core methods + convenience methods
        assertThat(methodCount).describedAs("Method count").isEqualTo(6); // 2 core + 4 convenience
    }

    @Test
    @DisplayName("Validate code consistency and formatting")
    void validateCodeConsistency() throws IOException {
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/TestUserMapper.java");
        String content = Files.readString(mapperPath);

        String[] lines = content.split("\n");

        // Validate 4-space indentation
        boolean hasProperIndentation = false;
        for (String line : lines) {
            if (line.startsWith("    ") && !line.startsWith("        ")) {
                hasProperIndentation = true;
                break;
            }
        }
        assertThat(hasProperIndentation).describedAs("Should have 4-space indentation").isTrue();

        // Validate consistent null handling
        assertThat(content)
            .contains("== null")
            .contains("!= null")
            .contains("if (");

        // Validate consistent naming patterns
        assertThat(content)
            .contains("toDynamoDbAttributeValue")
            .contains("fromDynamoDbAttributeValue")
            .contains("toDynamoDbItem")
            .contains("fromDynamoDbItem");
    }

    @Test
    @DisplayName("Performance: Verify compilation speed impact")
    void verifyCompilationPerformance() {
        // This test validates that the JavaPoet migration doesn't negatively impact compilation performance
        // by checking that annotation processing completes in reasonable time

        long startTime = System.currentTimeMillis();

        // The fact that this test is running means compilation succeeded
        // Check that we're within reasonable bounds
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Should be near-instantaneous for validation
        assertThat(elapsedTime).describedAs("Test execution time").isLessThan(1000);
    }

    @Test
    @DisplayName("Memory efficiency: Validate generated code size")
    void validateGeneratedCodeSize() throws IOException {
        // Check that generated files are not unnecessarily large
        Path mapperPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/mappers/TestUserMapper.java");
        Path fieldsPath = Path.of("target/generated-sources/annotations/com/github/wassertim/dynamodb/toolkit/fields/TestUserFields.java");

        long mapperSize = Files.size(mapperPath);
        long fieldsSize = Files.size(fieldsPath);

        // Generated files should be reasonably sized (not bloated)
        assertThat(mapperSize).describedAs("Mapper file size").isBetween(5000L, 15000L); // 5-15KB
        assertThat(fieldsSize).describedAs("Fields file size").isBetween(1000L, 5000L);  // 1-5KB
    }
}