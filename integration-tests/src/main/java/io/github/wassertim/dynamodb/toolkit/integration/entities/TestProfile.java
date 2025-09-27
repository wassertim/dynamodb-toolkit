package io.github.wassertim.dynamodb.toolkit.integration.entities;

import io.github.wassertim.dynamodb.toolkit.api.annotations.DynamoMappable;

/**
 * Nested entity to test complex object mapping and dependency resolution.
 * This class tests that the annotation processor correctly handles dependencies
 * between @DynamoMappable classes.
 */
@DynamoMappable
public class TestProfile {

    private String bio;
    private String location;
    private String website;
    private Integer followers;
    private Integer following;

    // Default constructor
    public TestProfile() {}

    // Builder constructor
    public TestProfile(String bio, String location, String website, Integer followers, Integer following) {
        this.bio = bio;
        this.location = location;
        this.website = website;
        this.followers = followers;
        this.following = following;
    }

    // Getters and setters
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

    // Builder pattern methods
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String bio;
        private String location;
        private String website;
        private Integer followers;
        private Integer following;

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder followers(Integer followers) {
            this.followers = followers;
            return this;
        }

        public Builder following(Integer following) {
            this.following = following;
            return this;
        }

        public TestProfile build() {
            return new TestProfile(bio, location, website, followers, following);
        }
    }

    @Override
    public String toString() {
        return "TestProfile{" +
                "bio='" + bio + '\'' +
                ", location='" + location + '\'' +
                ", website='" + website + '\'' +
                ", followers=" + followers +
                ", following=" + following +
                '}';
    }
}