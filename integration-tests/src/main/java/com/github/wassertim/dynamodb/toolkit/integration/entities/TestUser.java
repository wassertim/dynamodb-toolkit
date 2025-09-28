package com.github.wassertim.dynamodb.toolkit.integration.entities;

import com.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import com.github.wassertim.dynamodb.toolkit.api.annotations.Table;
import com.github.wassertim.dynamodb.toolkit.api.annotations.PartitionKey;
import com.github.wassertim.dynamodb.toolkit.api.annotations.SortKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Test entity to verify annotation processing and code generation.
 * This class will be used to test that the DynamoDB Toolkit correctly
 * generates mappers, field constants, and other artifacts.
 */
@DynamoMappable
@Table(name = "test-users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // Manual getters/setters for now (Lombok should generate these)
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

    // Manual builder for now (Lombok should generate this)
    public static TestUserBuilder builder() { return new TestUserBuilder(); }

    public static class TestUserBuilder {
        private String userId, email, name;
        private Integer age;
        private Boolean active;
        private Instant createdAt;
        private List<String> tags;
        private TestProfile profile;

        public TestUserBuilder userId(String userId) { this.userId = userId; return this; }
        public TestUserBuilder email(String email) { this.email = email; return this; }
        public TestUserBuilder name(String name) { this.name = name; return this; }
        public TestUserBuilder age(Integer age) { this.age = age; return this; }
        public TestUserBuilder active(Boolean active) { this.active = active; return this; }
        public TestUserBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public TestUserBuilder tags(List<String> tags) { this.tags = tags; return this; }
        public TestUserBuilder profile(TestProfile profile) { this.profile = profile; return this; }

        public TestUser build() {
            TestUser user = new TestUser();
            user.setUserId(userId);
            user.setEmail(email);
            user.setName(name);
            user.setAge(age);
            user.setActive(active);
            user.setCreatedAt(createdAt);
            user.setTags(tags);
            user.setProfile(profile);
            return user;
        }
    }
}