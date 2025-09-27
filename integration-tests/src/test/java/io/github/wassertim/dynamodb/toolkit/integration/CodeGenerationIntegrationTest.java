package io.github.wassertim.dynamodb.toolkit.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import io.github.wassertim.dynamodb.toolkit.integration.entities.TestUser;
import io.github.wassertim.dynamodb.toolkit.integration.entities.TestProfile;
import io.github.wassertim.dynamodb.toolkit.mappers.TestUserMapper;
import io.github.wassertim.dynamodb.toolkit.mappers.TestProfileMapper;
import io.github.wassertim.dynamodb.toolkit.fields.TestUserFields;
import io.github.wassertim.dynamodb.toolkit.fields.TestProfileFields;
import io.github.wassertim.infrastructure.TableNameResolver;

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
    private TestUserMapper testUserMapper;
    private TestProfileMapper testProfileMapper;

    @BeforeEach
    void setUp() {
        // Create test entities using builders
        testProfile = TestProfile.builder()
            .bio("Software Engineer passionate about distributed systems")
            .location("San Francisco, CA")
            .website("https://example.com")
            .followers(150)
            .following(75)
            .build();

        testUser = TestUser.builder()
            .userId("user123")
            .email("test@example.com")
            .name("John Doe")
            .age(30)
            .active(true)
            .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
            .tags(Arrays.asList("developer", "java", "aws"))
            .profile(testProfile)
            .build();

        // Create mappers
        testProfileMapper = new TestProfileMapper();
        testUserMapper = new TestUserMapper(testProfileMapper);
    }

    @Test
    @DisplayName("Generated mapper classes work correctly for bidirectional conversion")
    void testGeneratedMappersWork() {
        // Test profile conversion
        AttributeValue profileAttributeValue = testProfileMapper.toDynamoDbAttributeValue(testProfile);
        assertThat(profileAttributeValue).isNotNull();
        assertThat(profileAttributeValue.hasM()).isTrue();

        Map<String, AttributeValue> profileMap = profileAttributeValue.m();
        assertThat(profileMap).containsKey("bio");
        assertThat(profileMap).containsKey("location");
        assertThat(profileMap).containsKey("website");
        assertThat(profileMap).containsKey("followers");
        assertThat(profileMap).containsKey("following");

        // Test profile round-trip conversion
        TestProfile convertedProfile = testProfileMapper.fromDynamoDbAttributeValue(profileAttributeValue);
        assertThat(convertedProfile).isNotNull();
        assertThat(convertedProfile.getBio()).isEqualTo(testProfile.getBio());
        assertThat(convertedProfile.getLocation()).isEqualTo(testProfile.getLocation());
        assertThat(convertedProfile.getWebsite()).isEqualTo(testProfile.getWebsite());
        assertThat(convertedProfile.getFollowers()).isEqualTo(testProfile.getFollowers());
        assertThat(convertedProfile.getFollowing()).isEqualTo(testProfile.getFollowing());

        // Test user conversion (includes complex object mapping)
        AttributeValue userAttributeValue = testUserMapper.toDynamoDbAttributeValue(testUser);
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
        TestUser convertedUser = testUserMapper.fromDynamoDbAttributeValue(userAttributeValue);
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
    @DisplayName("Generated field constants are accessible and correct")
    void testGeneratedFieldConstants() {
        // TestUserFields should contain all field names
        assertThat(TestUserFields.userId).isEqualTo("userId");
        assertThat(TestUserFields.email).isEqualTo("email");
        assertThat(TestUserFields.name).isEqualTo("name");
        assertThat(TestUserFields.age).isEqualTo("age");
        assertThat(TestUserFields.active).isEqualTo("active");
        assertThat(TestUserFields.createdAt).isEqualTo("createdAt");
        assertThat(TestUserFields.tags).isEqualTo("tags");
        assertThat(TestUserFields.profile).isEqualTo("profile");

        // TestProfileFields should contain all field names
        assertThat(TestProfileFields.bio).isEqualTo("bio");
        assertThat(TestProfileFields.location).isEqualTo("location");
        assertThat(TestProfileFields.website).isEqualTo("website");
        assertThat(TestProfileFields.followers).isEqualTo("followers");
        assertThat(TestProfileFields.following).isEqualTo("following");
    }

    @Test
    @DisplayName("Generated TableNameResolver works correctly")
    void testGeneratedTableNameResolver() {
        // TestUser has @Table(name = "test-users")
        String tableName = TableNameResolver.resolveTableName(TestUser.class);
        assertThat(tableName).isEqualTo("test-users");

        // Test error case for unknown table
        assertThatThrownBy(() -> {
            TableNameResolver.resolveTableName(String.class);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unknown @Table annotated class");
    }

    @Test
    @DisplayName("Test entity structure and builders work correctly")
    void testEntityStructure() {
        // Verify that our test entities have the expected data
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
}