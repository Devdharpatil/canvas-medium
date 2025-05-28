package com.canvamedium;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.canvamedium.model.Article;
import com.canvamedium.model.Template;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * UI tests for {@link ArticleDetailActivity} that verify the article detail functionality.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ArticleDetailActivityTest {

    private static final String TEST_ARTICLE_TITLE = "Test Article";
    private static final String TEST_PREVIEW_TEXT = "This is a test preview text for the article";
    private static final String TEST_TEMPLATE_NAME = "Test Template";
    
    private Article testArticle;
    
    @Before
    public void setup() {
        Intents.init();
        
        // Create test data
        createTestArticle();
    }
    
    @After
    public void cleanup() {
        Intents.release();
    }
    
    /**
     * Creates a test article with sample data.
     */
    private void createTestArticle() {
        // Create a test template
        Template template = new Template();
        template.setId(1L);
        template.setName(TEST_TEMPLATE_NAME);
        
        // Create test content
        JsonObject content = new JsonObject();
        content.addProperty("text", "This is the full content of the test article.");
        
        // Create a test article
        testArticle = new Article(TEST_ARTICLE_TITLE, content, TEST_PREVIEW_TEXT, null, 1L);
        testArticle.setId(1L);
        testArticle.setTemplate(template);
    }
    
    /**
     * Tests that the article title is displayed correctly.
     */
    @Test
    public void articleTitle_isDisplayedCorrectly() {
        // Launch the activity with test article
        launchActivityWithTestArticle();
        
        // Check if article title is displayed correctly
        onView(withId(R.id.textArticleTitle)).check(matches(withText(TEST_ARTICLE_TITLE)));
    }
    
    /**
     * Tests that the article content is displayed correctly.
     */
    @Test
    public void articleContent_isDisplayedCorrectly() {
        // Launch the activity with test article
        launchActivityWithTestArticle();
        
        // Check if article preview text is part of the displayed content
        onView(withId(R.id.textArticleContent)).check(matches(isDisplayed()));
        onView(withId(R.id.textArticleContent)).check(
                matches(withText(org.hamcrest.Matchers.containsString(TEST_PREVIEW_TEXT))));
    }
    
    /**
     * Tests that the template name is displayed correctly.
     */
    @Test
    public void templateName_isDisplayedCorrectly() {
        // Launch the activity with test article
        launchActivityWithTestArticle();
        
        // Check if template name is displayed correctly
        onView(withId(R.id.textTemplateName)).check(
                matches(withText(org.hamcrest.Matchers.containsString(TEST_TEMPLATE_NAME))));
    }
    
    /**
     * Tests that the share button works correctly.
     */
    @Test
    public void shareButton_opensShareIntent() {
        // Launch the activity with test article
        launchActivityWithTestArticle();
        
        // Click share button
        onView(withId(R.id.fabShare)).perform(click());
        
        // In a more complete test, we would verify the share intent is created
        // This requires additional setup with intent verification
    }
    
    /**
     * Launches the ArticleDetailActivity with the test article.
     */
    private void launchActivityWithTestArticle() {
        // Create intent with test article
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ArticleDetailActivity.class);
        intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE, new Gson().toJson(testArticle));
        
        // Launch activity with intent
        ActivityScenario.launch(intent);
    }
} 