package io.github.wassertim.dynamodb.toolkit.integration.entities;

import io.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested entity to test complex object mapping and dependency resolution.
 * This class tests that the annotation processor correctly handles dependencies
 * between @DynamoMappable classes.
 */
@DynamoMappable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestProfile {

    private String bio;
    private String location;
    private String website;
    private Integer followers;
    private Integer following;

    // Manual getters/setters for now (Lombok should generate these)
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Integer getFollowers() { return followers; }
    public void setFollowers(Integer followers) { this.followers = followers; }

    public Integer getFollowing() { return following; }
    public void setFollowing(Integer following) { this.following = following; }

    // Manual builder for now (Lombok should generate this)
    public static TestProfileBuilder builder() { return new TestProfileBuilder(); }

    public static class TestProfileBuilder {
        private String bio, location, website;
        private Integer followers, following;

        public TestProfileBuilder bio(String bio) { this.bio = bio; return this; }
        public TestProfileBuilder location(String location) { this.location = location; return this; }
        public TestProfileBuilder website(String website) { this.website = website; return this; }
        public TestProfileBuilder followers(Integer followers) { this.followers = followers; return this; }
        public TestProfileBuilder following(Integer following) { this.following = following; return this; }

        public TestProfile build() {
            TestProfile profile = new TestProfile();
            profile.setBio(bio);
            profile.setLocation(location);
            profile.setWebsite(website);
            profile.setFollowers(followers);
            profile.setFollowing(following);
            return profile;
        }
    }
}