package com.tourino.domain;

/**
 * Enumeration of route difficulty levels for user classification.
 */
public enum Difficulty {
    EASY("Easy - suitable for beginners"),
    MODERATE("Moderate - some experience required"),
    HARD("Hard - experienced users only"),
    EXPERT("Expert - professional level");

    private final String description;

    Difficulty(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of this difficulty level.
     * @return the difficulty description
     */
    public String getDescription() {
        return description;
    }
}