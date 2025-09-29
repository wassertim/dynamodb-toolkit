package com.tourino.domain;

/**
 * Enumeration of route types for categorizing user routes.
 */
public enum RouteType {
    WALKING("walking"),
    CYCLING("cycling"),
    CYCLING_REGULAR("cycling-regular"),
    DRIVING("driving-car"),
    HIKING("foot-hiking"),
    MOUNTAIN_BIKING("cycling-mountain"),
    RUNNING("foot-running"),
    WHEELCHAIR("wheelchair");

    private final String profile;

    RouteType(String profile) {
        this.profile = profile;
    }

    /**
     * Gets the OpenRouteService profile name for this route type.
     * @return the profile name used in route calculations
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Creates RouteType from profile string.
     * @param profile the profile string
     * @return the corresponding RouteType
     * @throws IllegalArgumentException if profile doesn't match any route type
     */
    public static RouteType fromProfile(String profile) {
        for (RouteType routeType : RouteType.values()) {
            if (routeType.profile.equals(profile)) {
                return routeType;
            }
        }
        throw new IllegalArgumentException("Unknown RouteType profile: " + profile);
    }
}