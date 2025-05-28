package com.canvamedium.activity;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.canvamedium.R;
import com.canvamedium.model.Template;
import com.canvamedium.util.EspressoTestUtil;
import com.canvamedium.view.DraggableElementView;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * UI tests for {@link TemplateBuilderActivity} that verify the drag-and-drop functionality.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TemplateBuilderActivityTest {

    private static final String TEST_TEMPLATE_NAME = "Test Template";
    
    /**
     * Use ActivityScenarioRule to create and launch the activity under test before each test,
     * and close it after each test.
     */
    @Rule
    public ActivityScenarioRule<TemplateBuilderActivity> activityRule =
            new ActivityScenarioRule<>(createTemplateBuilderIntent());
    
    /**
     * Creates an intent for launching the TemplateBuilderActivity with a new template.
     */
    private static Intent createTemplateBuilderIntent() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                TemplateBuilderActivity.class);
        // We don't pass a template ID so it creates a new template
        return intent;
    }
    
    @Before
    public void setUp() {
        // Register any idling resources if needed
    }
    
    @After
    public void tearDown() {
        // Unregister any idling resources if needed
    }
    
    /**
     * Test that the template builder activity launches correctly.
     */
    @Test
    public void testTemplateBuilderLaunch() {
        // Check that the canvas view is displayed
        onView(withId(R.id.canvas)).check(matches(isDisplayed()));
        
        // Check that the element palette is displayed
        onView(withId(R.id.element_palette)).check(matches(isDisplayed()));
        
        // Check that the save button is displayed
        onView(withId(R.id.fab_save)).check(matches(isDisplayed()));
    }
    
    /**
     * Test adding a text element to the canvas.
     */
    @Test
    public void testAddTextElement() {
        // Click on the text element in the palette
        onView(withId(R.id.text_element)).perform(click());
        
        // Verify that a text element was added to the canvas
        // This is challenging to test directly with Espresso since the elements are added dynamically
        // We can use a custom matcher or verify indirectly through other UI elements
        
        // For this test, we'll verify the save dialog shows when we try to save
        onView(withId(R.id.fab_save)).perform(click());
        onView(withId(R.id.template_name)).check(matches(isDisplayed()));
        
        // Cancel the save dialog
        onView(withText("Cancel")).perform(click());
    }
    
    /**
     * Test adding a header element to the canvas.
     */
    @Test
    public void testAddHeaderElement() {
        // Click on the header element in the palette
        onView(withId(R.id.header_element)).perform(click());
        
        // Verify that a header element was added to the canvas (indirectly)
        onView(withId(R.id.fab_save)).perform(click());
        onView(withId(R.id.template_name)).check(matches(isDisplayed()));
        
        // Cancel the save dialog
        onView(withText("Cancel")).perform(click());
    }
    
    /**
     * Test adding multiple elements to the canvas.
     */
    @Test
    public void testAddMultipleElements() {
        // Add a text element
        onView(withId(R.id.text_element)).perform(click());
        
        // Add a header element
        onView(withId(R.id.header_element)).perform(click());
        
        // Add a divider element
        onView(withId(R.id.divider_element)).perform(click());
        
        // Verify elements were added (indirectly)
        onView(withId(R.id.fab_save)).perform(click());
        onView(withId(R.id.template_name)).check(matches(isDisplayed()));
        
        // Fill in template name
        onView(withId(R.id.template_name)).perform(ViewActions.typeText(TEST_TEMPLATE_NAME));
        
        // Cancel the save dialog
        onView(withText("Cancel")).perform(click());
    }
    
    /**
     * Test drag-and-drop functionality by adding an element and dragging it.
     */
    @Test
    public void testDragAndDropElement() {
        // Add a text element
        onView(withId(R.id.text_element)).perform(click());
        
        // Wait for the element to be added
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Find the canvas view and perform a drag operation
        // Note: This is a simplified test as we can't easily target the specific element
        // In a real test, we would need a custom matcher to find the specific DraggableElementView
        onView(withId(R.id.canvas)).perform(EspressoTestUtil.dragFrom(0.2f, 0.2f, 0.8f, 0.8f));
        
        // Verify the element was moved (indirectly)
        // We can't easily verify the position, but we can verify the app didn't crash
        onView(withId(R.id.fab_save)).check(matches(isDisplayed()));
    }
} 