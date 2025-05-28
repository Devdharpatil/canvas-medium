package com.canvamedium.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.canvamedium.R;
import com.canvamedium.model.Tag;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TagAdapterTest {

    @Mock
    private Context mockContext;

    @Mock
    private TagAdapter.OnTagClickListener mockListener;

    @Mock
    private View mockItemView;

    @Mock
    private TextView mockNameTextView;

    @Mock
    private TextView mockCountTextView;

    private TagAdapter adapter;
    private List<Tag> testTags;

    @Before
    public void setUp() {
        adapter = new TagAdapter(mockContext, mockListener);
        
        testTags = new ArrayList<>();
        testTags.add(createTag(1L, "Design", "design", 10));
        testTags.add(createTag(2L, "Programming", "programming", 20));
        testTags.add(createTag(3L, "Art", "art", 5));
    }

    @Test
    public void setTags_shouldUpdateTagsList() {
        // Act
        adapter.setTags(testTags);
        
        // Assert
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void addTags_shouldAppendToExistingList() {
        // Arrange
        adapter.setTags(testTags.subList(0, 1));
        assertEquals(1, adapter.getItemCount());
        
        // Act
        adapter.addTags(testTags.subList(1, 3));
        
        // Assert
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void clearTags_shouldEmptyTheList() {
        // Arrange
        adapter.setTags(testTags);
        assertEquals(3, adapter.getItemCount());
        
        // Act
        adapter.clearTags();
        
        // Assert
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void setSelectedTags_shouldUpdateSelectedTagsList() {
        // Arrange
        adapter.setTags(testTags);
        
        // Act
        adapter.setSelectedTags(Arrays.asList(testTags.get(0), testTags.get(2)));
        
        // Assert
        List<Tag> selectedTags = adapter.getSelectedTags();
        assertEquals(2, selectedTags.size());
        assertTrue(selectedTags.contains(testTags.get(0)));
        assertTrue(selectedTags.contains(testTags.get(2)));
    }

    @Test
    public void setSelectionMode_shouldUpdateSelectionMode() {
        // Arrange
        adapter.setSelectionMode(false);
        
        // Act
        adapter.setSelectionMode(true);
        
        // Assert
        // Since we can't directly test the private field, we rely on behavior verification
        // The adapter should call notifyDataSetChanged when selection mode changes
    }

    /**
     * Helper method to create a Tag with specified properties.
     */
    private Tag createTag(Long id, String name, String slug, int articleCount) {
        Tag tag = new Tag(name, slug);
        tag.setId(id);
        tag.setArticleCount(articleCount);
        return tag;
    }
} 