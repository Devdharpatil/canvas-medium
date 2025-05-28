package com.canvamedium.model;

/**
 * Enum representing the possible states of an article in the publishing workflow.
 */
public enum ArticleState {
    /**
     * The article is being created or edited and has not been saved yet.
     */
    DRAFT("draft"),

    /**
     * The article has been saved but is not published yet.
     */
    SAVED("saved"),
    
    /**
     * The article is awaiting review before publishing.
     */
    PENDING_REVIEW("pending_review"),

    /**
     * The article has been published and is visible to all users.
     */
    PUBLISHED("published"),

    /**
     * The article has been archived and is no longer visible in regular feeds.
     */
    ARCHIVED("archived"),
    
    /**
     * The article has been deleted but is still in the system (soft delete).
     */
    DELETED("deleted");

    private final String value;

    /**
     * Constructor.
     *
     * @param value The string value of the state
     */
    ArticleState(String value) {
        this.value = value;
    }

    /**
     * Get the string value of the state.
     *
     * @return The string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Convert a string value to the corresponding ArticleState.
     *
     * @param value The string value
     * @return The ArticleState, or DRAFT if not found
     */
    public static ArticleState fromString(String value) {
        for (ArticleState state : ArticleState.values()) {
            if (state.getValue().equalsIgnoreCase(value)) {
                return state;
            }
        }
        return DRAFT;
    }
} 