package io.github.wassertim.dynamodb.toolkit.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import io.github.wassertim.dynamodb.toolkit.integration.entities.TestUser;
import io.github.wassertim.dynamodb.toolkit.integration.entities.TestProfile;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 * Integration tests to verify that the DynamoDB Toolkit annotation processor
 * correctly generates mapper classes, field constants, and other artifacts
 * for annotated entities.
 */
public class CodeGenerationIntegrationTest {

    private TestUser testUser;
    private TestProfile testProfile;

    @BeforeEach
    void setUp() {
        testProfile = new TestProfile(
            "Software Engineer passionate about distributed systems",
            "San Francisco, CA",
            "https://example.com",
            150,
            75
        );

        testUser = new TestUser(
            "user123",
            "test@example.com",
            "John Doe",
            30,
            true,
            Instant.parse("2024-01-15T10:30:00Z"),
            Arrays.asList("developer", "java", "aws"),
            testProfile
        );
    }

    @Test
    @DisplayName("Generated mapper classes work correctly for bidirectional conversion")
    void testGeneratedMappersWork() {
        // Test that the generated mappers can actually convert objects
        var profileMapper = new io.github.wassertim.dynamodb.toolkit.mappers.TestProfileMapper();
        var userMapper = new io.github.wassertim.dynamodb.toolkit.mappers.TestUserMapper(profileMapper);

        // Test profile conversion
        AttributeValue profileAttributeValue = profileMapper.toDynamoDbAttributeValue(testProfile);
        assertThat(profileAttributeValue).isNotNull();
        assertThat(profileAttributeValue.hasM()).isTrue();

        Map<String, AttributeValue> profileMap = profileAttributeValue.m();
        assertThat(profileMap).containsKey("bio");
        assertThat(profileMap).containsKey("location");
        assertThat(profileMap).containsKey("website");
        assertThat(profileMap).containsKey("followers");
        assertThat(profileMap).containsKey("following");

        // Test profile round-trip conversion
        TestProfile convertedProfile = profileMapper.fromDynamoDbAttributeValue(profileAttributeValue);
        assertThat(convertedProfile).isNotNull();
        assertThat(convertedProfile.getBio()).isEqualTo(testProfile.getBio());
        assertThat(convertedProfile.getLocation()).isEqualTo(testProfile.getLocation());
        assertThat(convertedProfile.getWebsite()).isEqualTo(testProfile.getWebsite());
        assertThat(convertedProfile.getFollowers()).isEqualTo(testProfile.getFollowers());
        assertThat(convertedProfile.getFollowing()).isEqualTo(testProfile.getFollowing());

        // Test user conversion (includes complex object mapping)
        AttributeValue userAttributeValue = userMapper.toDynamoDbAttributeValue(testUser);
        assertThat(userAttributeValue).isNotNull();
        assertThat(userAttributeValue.hasM()).isTrue();

        Map<String, AttributeValue> userMap = userAttributeValue.m();
        assertThat(userMap).containsKey("userId");
        assertThat(userMap).containsKey("email");
        assertThat(userMap).containsKey("name");
        assertThat(userMap).containsKey("age");
        assertThat(userMap).containsKey("active");
        assertThat(userMap).containsKey("createdAt");
        assertThat(userMap).containsKey("tags");
        assertThat(userMap).containsKey("profile");

        // Test user round-trip conversion
        TestUser convertedUser = userMapper.fromDynamoDbAttributeValue(userAttributeValue);
        assertThat(convertedUser).isNotNull();
        assertThat(convertedUser.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(convertedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(convertedUser.getName()).isEqualTo(testUser.getName());
        assertThat(convertedUser.getAge()).isEqualTo(testUser.getAge());
        assertThat(convertedUser.getActive()).isEqualTo(testUser.getActive());
        assertThat(convertedUser.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        assertThat(convertedUser.getTags()).isEqualTo(testUser.getTags());

        // Test nested object conversion
        assertThat(convertedUser.getProfile()).isNotNull();
        assertThat(convertedUser.getProfile().getBio()).isEqualTo(testProfile.getBio());
        assertThat(convertedUser.getProfile().getLocation()).isEqualTo(testProfile.getLocation());
    }

    @Test
    @DisplayName("Test entity structure is correct for annotation processing")
    void testEntityStructure() {
        // Verify that our test entities have the required structure
        assertThat(testUser.getUserId()).isEqualTo("user123");
        assertThat(testUser.getEmail()).isEqualTo("test@example.com");
        assertThat(testUser.getName()).isEqualTo("John Doe");
        assertThat(testUser.getAge()).isEqualTo(30);
        assertThat(testUser.getActive()).isTrue();
        assertThat(testUser.getCreatedAt()).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
        assertThat(testUser.getTags()).containsExactly("developer", "java", "aws");
        assertThat(testUser.getProfile()).isEqualTo(testProfile);

        assertThat(testProfile.getBio()).isEqualTo("Software Engineer passionate about distributed systems");
        assertThat(testProfile.getLocation()).isEqualTo("San Francisco, CA");
        assertThat(testProfile.getWebsite()).isEqualTo("https://example.com");
        assertThat(testProfile.getFollowers()).isEqualTo(150);
        assertThat(testProfile.getFollowing()).isEqualTo(75);
    }

    @Test
    @DisplayName("Verify annotation processing will handle dependencies correctly")
    void testDependencyStructure() {
        // TestUser depends on TestProfile
        // The annotation processor should:
        // 1. Detect that TestUser has a field of type TestProfile
        // 2. Ensure TestProfile mapper is generated first
        // 3. Inject TestProfileMapper into TestUserMapper constructor

        // For now, just verify the dependency relationship exists
        assertThat(testUser.getProfile()).isInstanceOf(TestProfile.class);
        assertThat(testUser.getProfile()).isNotNull();
    }

    @Test
    @DisplayName("Generated field constants should be accessible and correct")
    void testGeneratedFieldConstants() {
        // Test that field constants are generated and accessible

        // TestUserFields should contain all field names
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.userId).isEqualTo("userId");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.email).isEqualTo("email");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.name).isEqualTo("name");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.age).isEqualTo("age");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.active).isEqualTo("active");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.createdAt).isEqualTo("createdAt");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.tags).isEqualTo("tags");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestUserFields.profile).isEqualTo("profile");

        // TestProfileFields should contain all field names
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestProfileFields.bio).isEqualTo("bio");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestProfileFields.location).isEqualTo("location");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestProfileFields.website).isEqualTo("website");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestProfileFields.followers).isEqualTo("followers");
        assertThat(io.github.wassertim.dynamodb.toolkit.fields.TestProfileFields.following).isEqualTo("following");
    }

    @Test
    @DisplayName("Generated TableNameResolver should work correctly")
    void testGeneratedTableNameResolver() {
        // Test that TableNameResolver is generated and works correctly
        // TestUser has @Table(name = "test-users")
        String tableName = io.github.wassertim.infrastructure.TableNameResolver.resolveTableName(TestUser.class);
        assertThat(tableName).isEqualTo("test-users");

        // Test error case for unknown table
        assertThatThrownBy(() -> {
            io.github.wassertim.infrastructure.TableNameResolver.resolveTableName(String.class);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unknown @Table annotated class");
    }
}