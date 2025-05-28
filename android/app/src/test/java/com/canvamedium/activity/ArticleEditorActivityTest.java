package com.canvamedium.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.canvamedium.R;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Article;
import com.canvamedium.model.Template;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ArticleEditorActivityTest {
    
    private ArticleEditorActivity activity;
    
    @Before
    public void setUp() {
        // Set up activity
        activity = Robolectric.buildActivity(ArticleEditorActivity.class).create().get();
    }
    
    @Test
    public void testActivityCreated() {
        assertNotNull(activity);
    }
    
    @Test
    public void testDefaultUIState() {
        // Publish button should be disabled by default
        Button publishButton = activity.findViewById(R.id.publish_button);
        assertFalse(publishButton.isEnabled());
        
        // Fields should be empty
        EditText titleEditText = activity.findViewById(R.id.title_edit_text);
        assertEquals("", titleEditText.getText().toString());
        
        EditText previewEditText = activity.findViewById(R.id.preview_text_edit_text);
        assertEquals("", previewEditText.getText().toString());
    }
    
    @Test
    public void testValidateArticleData_EmptyTitle() {
        // Set up the UI with empty title
        EditText titleEditText = activity.findViewById(R.id.title_edit_text);
        titleEditText.setText("");
        
        EditText previewEditText = activity.findViewById(R.id.preview_text_edit_text);
        previewEditText.setText("Preview text");
        
        // Test the validation method
        boolean isValid = activity.validateArticleData();
        
        // Validation should fail and show an error for title
        assertFalse(isValid);
        assertEquals("Title is required", titleEditText.getError().toString());
    }
    
    @Test
    public void testValidateArticleData_ValidInput() {
        // Set up the UI with valid inputs
        EditText titleEditText = activity.findViewById(R.id.title_edit_text);
        titleEditText.setText("Test Title");
        
        EditText previewEditText = activity.findViewById(R.id.preview_text_edit_text);
        previewEditText.setText("Preview text");
        
        // Set thumbnail URL
        activity.setThumbnailUrl("https://example.com/image.jpg");
        
        // Set selected template
        Template template = new Template();
        template.setId(1L);
        activity.setSelectedTemplate(template);
        
        // Test the validation method
        boolean isValid = activity.validateArticleData();
        
        // Validation should pass
        assertTrue(isValid);
    }
    
    @Test
    public void testCollectArticleData() {
        // Setup test data
        EditText titleEditText = activity.findViewById(R.id.title_edit_text);
        titleEditText.setText("Test Title");
        
        EditText previewEditText = activity.findViewById(R.id.preview_text_edit_text);
        previewEditText.setText("Test Preview Text");
        
        // Set thumbnail URL
        activity.setThumbnailUrl("https://example.com/image.jpg");
        
        // Set selected template
        Template template = new Template();
        template.setId(1L);
        activity.setSelectedTemplate(template);
        
        // Test the method
        Article article = activity.collectArticleData();
        
        // Verify the collected data
        assertNotNull(article);
        assertEquals("Test Title", article.getTitle());
        assertEquals("Test Preview Text", article.getPreviewText());
        assertEquals("https://example.com/image.jpg", article.getThumbnailUrl());
        assertEquals(Long.valueOf(1L), article.getTemplateId());
    }
    
    @Test
    public void testEditMode() {
        // Create test article
        Article testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("Existing Title");
        testArticle.setPreviewText("Existing Preview");
        testArticle.setThumbnailUrl("https://example.com/existing.jpg");
        testArticle.setContent(new JsonObject());
        testArticle.setTemplateId(2L);
        testArticle.setStatus(Article.STATUS_DRAFT);
        
        // Create intent with article
        Intent intent = new Intent(RuntimeEnvironment.application, ArticleEditorActivity.class);
        intent.putExtra("article", testArticle);
        
        // Start activity with intent
        activity = Robolectric.buildActivity(ArticleEditorActivity.class, intent).create().get();
        
        // Verify edit mode is active
        assertTrue(activity.isEditMode());
        
        // Verify fields are populated
        EditText titleEditText = activity.findViewById(R.id.title_edit_text);
        assertEquals("Existing Title", titleEditText.getText().toString());
        
        EditText previewEditText = activity.findViewById(R.id.preview_text_edit_text);
        assertEquals("Existing Preview", previewEditText.getText().toString());
    }
} 