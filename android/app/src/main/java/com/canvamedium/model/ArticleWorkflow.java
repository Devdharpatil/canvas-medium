package com.canvamedium.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class responsible for managing article workflow state transitions.
 * Enforces valid state transitions and provides validation rules.
 */
public class ArticleWorkflow {

    // Map defining valid state transitions
    private static final Map<ArticleState, Set<ArticleState>> VALID_TRANSITIONS;
    
    static {
        Map<ArticleState, Set<ArticleState>> transitions = new HashMap<>();
        
        // From DRAFT state
        transitions.put(ArticleState.DRAFT, new HashSet<>(Arrays.asList(
            ArticleState.SAVED,
            ArticleState.DELETED
        )));
        
        // From SAVED state
        transitions.put(ArticleState.SAVED, new HashSet<>(Arrays.asList(
            ArticleState.DRAFT,
            ArticleState.PENDING_REVIEW,
            ArticleState.DELETED
        )));
        
        // From PENDING_REVIEW state
        transitions.put(ArticleState.PENDING_REVIEW, new HashSet<>(Arrays.asList(
            ArticleState.SAVED,
            ArticleState.PUBLISHED,
            ArticleState.DELETED
        )));
        
        // From PUBLISHED state
        transitions.put(ArticleState.PUBLISHED, new HashSet<>(Arrays.asList(
            ArticleState.ARCHIVED,
            ArticleState.DELETED
        )));
        
        // From ARCHIVED state
        transitions.put(ArticleState.ARCHIVED, new HashSet<>(Arrays.asList(
            ArticleState.PUBLISHED,
            ArticleState.DELETED
        )));
        
        // From DELETED state (can be restored to draft only)
        transitions.put(ArticleState.DELETED, new HashSet<>(Collections.singletonList(
            ArticleState.DRAFT
        )));
        
        VALID_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }
    
    /**
     * Check if a state transition is valid.
     *
     * @param currentState The current state of the article
     * @param newState The proposed new state
     * @return true if the transition is valid, false otherwise
     */
    public static boolean isValidTransition(ArticleState currentState, ArticleState newState) {
        if (currentState == newState) {
            return true; // Same state is always valid
        }
        
        Set<ArticleState> validNextStates = VALID_TRANSITIONS.get(currentState);
        return validNextStates != null && validNextStates.contains(newState);
    }
    
    /**
     * Get all valid next states from the current state.
     *
     * @param currentState The current state of the article
     * @return A set of valid next states
     */
    public static Set<ArticleState> getValidNextStates(ArticleState currentState) {
        Set<ArticleState> validNextStates = VALID_TRANSITIONS.get(currentState);
        return validNextStates != null ? Collections.unmodifiableSet(validNextStates) 
                                       : Collections.emptySet();
    }
    
    /**
     * Validate an article state transition.
     *
     * @param currentState The current state of the article
     * @param newState The proposed new state
     * @throws IllegalStateException if the transition is invalid
     */
    public static void validateTransition(ArticleState currentState, ArticleState newState) 
            throws IllegalStateException {
        if (!isValidTransition(currentState, newState)) {
            throw new IllegalStateException(
                "Invalid state transition from " + currentState + " to " + newState);
        }
    }
    
    /**
     * Check if an article in the given state can be edited.
     *
     * @param state The current state of the article
     * @return true if the article can be edited, false otherwise
     */
    public static boolean canEdit(ArticleState state) {
        return state == ArticleState.DRAFT || state == ArticleState.SAVED;
    }
    
    /**
     * Check if an article in the given state can be published.
     *
     * @param state The current state of the article
     * @return true if the article can be published, false otherwise
     */
    public static boolean canPublish(ArticleState state) {
        return state == ArticleState.PENDING_REVIEW;
    }
    
    /**
     * Check if an article in the given state can be submitted for review.
     *
     * @param state The current state of the article
     * @return true if the article can be submitted for review, false otherwise
     */
    public static boolean canSubmitForReview(ArticleState state) {
        return state == ArticleState.SAVED;
    }
} 