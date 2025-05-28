package com.canvamedium.model;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the ArticleWorkflow class.
 */
public class ArticleWorkflowTest {

    @Test
    public void isValidTransition_ValidTransitions_ReturnsTrue() {
        // Test valid transitions from DRAFT
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.DRAFT, ArticleState.SAVED));
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.DRAFT, ArticleState.DELETED));
        
        // Test valid transitions from SAVED
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.SAVED, ArticleState.DRAFT));
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.SAVED, ArticleState.PENDING_REVIEW));
        
        // Test valid transitions from PENDING_REVIEW
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.PENDING_REVIEW, ArticleState.SAVED));
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.PENDING_REVIEW, ArticleState.PUBLISHED));
        
        // Test valid transitions from PUBLISHED
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.PUBLISHED, ArticleState.ARCHIVED));
        
        // Test same state transition (always valid)
        assertTrue(ArticleWorkflow.isValidTransition(ArticleState.DRAFT, ArticleState.DRAFT));
    }
    
    @Test
    public void isValidTransition_InvalidTransitions_ReturnsFalse() {
        // Test invalid transitions from DRAFT
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.DRAFT, ArticleState.PENDING_REVIEW));
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.DRAFT, ArticleState.PUBLISHED));
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.DRAFT, ArticleState.ARCHIVED));
        
        // Test invalid transitions from PUBLISHED
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.PUBLISHED, ArticleState.DRAFT));
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.PUBLISHED, ArticleState.SAVED));
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.PUBLISHED, ArticleState.PENDING_REVIEW));
        
        // Test invalid transitions from ARCHIVED
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.ARCHIVED, ArticleState.DRAFT));
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.ARCHIVED, ArticleState.SAVED));
        assertFalse(ArticleWorkflow.isValidTransition(ArticleState.ARCHIVED, ArticleState.PENDING_REVIEW));
    }
    
    @Test
    public void getValidNextStates_ReturnsCorrectStates() {
        // Check DRAFT valid next states
        Set<ArticleState> draftNextStates = ArticleWorkflow.getValidNextStates(ArticleState.DRAFT);
        assertEquals(2, draftNextStates.size());
        assertTrue(draftNextStates.contains(ArticleState.SAVED));
        assertTrue(draftNextStates.contains(ArticleState.DELETED));
        
        // Check SAVED valid next states
        Set<ArticleState> savedNextStates = ArticleWorkflow.getValidNextStates(ArticleState.SAVED);
        assertEquals(3, savedNextStates.size());
        assertTrue(savedNextStates.contains(ArticleState.DRAFT));
        assertTrue(savedNextStates.contains(ArticleState.PENDING_REVIEW));
        assertTrue(savedNextStates.contains(ArticleState.DELETED));
    }
    
    @Test(expected = IllegalStateException.class)
    public void validateTransition_InvalidTransition_ThrowsException() {
        // This transition is invalid and should throw an exception
        ArticleWorkflow.validateTransition(ArticleState.DRAFT, ArticleState.PUBLISHED);
    }
    
    @Test
    public void validateTransition_ValidTransition_NoException() {
        // This should not throw an exception
        ArticleWorkflow.validateTransition(ArticleState.DRAFT, ArticleState.SAVED);
    }
    
    @Test
    public void canEdit_EditableStates_ReturnsTrue() {
        assertTrue(ArticleWorkflow.canEdit(ArticleState.DRAFT));
        assertTrue(ArticleWorkflow.canEdit(ArticleState.SAVED));
    }
    
    @Test
    public void canEdit_NonEditableStates_ReturnsFalse() {
        assertFalse(ArticleWorkflow.canEdit(ArticleState.PENDING_REVIEW));
        assertFalse(ArticleWorkflow.canEdit(ArticleState.PUBLISHED));
        assertFalse(ArticleWorkflow.canEdit(ArticleState.ARCHIVED));
        assertFalse(ArticleWorkflow.canEdit(ArticleState.DELETED));
    }
    
    @Test
    public void canPublish_PublishableStates_ReturnsTrue() {
        assertTrue(ArticleWorkflow.canPublish(ArticleState.PENDING_REVIEW));
    }
    
    @Test
    public void canPublish_NonPublishableStates_ReturnsFalse() {
        assertFalse(ArticleWorkflow.canPublish(ArticleState.DRAFT));
        assertFalse(ArticleWorkflow.canPublish(ArticleState.SAVED));
        assertFalse(ArticleWorkflow.canPublish(ArticleState.PUBLISHED));
        assertFalse(ArticleWorkflow.canPublish(ArticleState.ARCHIVED));
        assertFalse(ArticleWorkflow.canPublish(ArticleState.DELETED));
    }
    
    @Test
    public void canSubmitForReview_SubmittableStates_ReturnsTrue() {
        assertTrue(ArticleWorkflow.canSubmitForReview(ArticleState.SAVED));
    }
    
    @Test
    public void canSubmitForReview_NonSubmittableStates_ReturnsFalse() {
        assertFalse(ArticleWorkflow.canSubmitForReview(ArticleState.DRAFT));
        assertFalse(ArticleWorkflow.canSubmitForReview(ArticleState.PENDING_REVIEW));
        assertFalse(ArticleWorkflow.canSubmitForReview(ArticleState.PUBLISHED));
        assertFalse(ArticleWorkflow.canSubmitForReview(ArticleState.ARCHIVED));
        assertFalse(ArticleWorkflow.canSubmitForReview(ArticleState.DELETED));
    }
} 