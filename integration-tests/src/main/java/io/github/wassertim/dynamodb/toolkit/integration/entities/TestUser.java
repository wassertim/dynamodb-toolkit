package io.github.wassertim.dynamodb.toolkit.integration.entities;

import io.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import io.github.wassertim.dynamodb.toolkit.api.annotations.Table;
import io.github.wassertim.dynamodb.toolkit.api.annotations.PartitionKey;
import io.github.wassertim.dynamodb.toolkit.api.annotations.SortKey;

import java.time.Instant;
import java.util.List;

/**
 * Test entity to verify annotation processing and code generation.
 * This class will be used to test that the DynamoDB Toolkit correctly
 * generates mappers, field constants, and other artifacts.
 */
@DynamoMappable
@Table(name = "test-users")
public class TestUser {

    @PartitionKey
    private String userId;

    @SortKey
    private String email;

    private String name;
    private Integer age;
    private Boolean active;
    private Instant createdAt;
    private List<String> tags;
    private TestProfile profile;  // Will require TestProfile to also be @DynamoMappable

    // Default constructor for DynamoDB
    public TestUser() {}

    // Builder constructor
    public TestUser(String userId, String email, String name, Integer age, Boolean active,
                   Instant createdAt, List<String> tags, TestProfile profile) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.age = age;
        this.active = active;
        this.createdAt = createdAt;
        this.tags = tags;
        this.profile = profile;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public TestProfile getProfile() { return profile; }
    public void setProfile(TestProfile profile) { this.profile = profile; }

    // Builder pattern methods
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String email;
        private String name;
        private Integer age;
        private Boolean active;
        private Instant createdAt;
        private List<String> tags;
        private TestProfile profile;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder profile(TestProfile profile) {
            this.profile = profile;
            return this;
        }

        public TestUser build() {
            return new TestUser(userId, email, name, age, active, createdAt, tags, profile);
        }
    }

    @Override
    public String toString() {
        return "TestUser{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", tags=" + tags +
                ", profile=" + profile +
                '}';
    }
}