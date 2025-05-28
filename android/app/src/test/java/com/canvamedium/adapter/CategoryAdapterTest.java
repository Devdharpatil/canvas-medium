package com.canvamedium.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.canvamedium.R;
import com.canvamedium.model.Category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class CategoryAdapterTest {

    @Mock
    private Context mockContext;

    @Mock
    private CategoryAdapter.OnCategoryClickListener mockListener;

    @Mock
    private LayoutInflater mockInflater;

    @Mock
    private View mockItemView;

    @Mock
    private ViewGroup mockParent;

    private CategoryAdapter adapter;
    private List<Category> testCategories;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Setup mock inflater
        when(mockContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).thenReturn(mockInflater);
        when(mockInflater.inflate(anyInt(), any(ViewGroup.class), eq(false))).thenReturn(mockItemView);
        
        // Create test data
        Category category1 = new Category("Technology", "technology");
        category1.setId(1L);
        category1.setDescription("Tech news and articles");
        category1.setArticleCount(10);
        
        Category category2 = new Category("Science", "science");
        category2.setId(2L);
        category2.setDescription("Scientific discoveries");
        category2.setArticleCount(5);
        category2.setFeatured(true);
        
        testCategories = Arrays.asList(category1, category2);
        
        // Create adapter
        adapter = new CategoryAdapter(mockContext, mockListener);
    }

    @Test
    public void testAdapterInitialization() {
        assertNotNull(adapter);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testSetCategories() {
        adapter.setCategories(testCategories);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testAddCategories() {
        adapter.addCategories(testCategories);
        assertEquals(2, adapter.getItemCount());
        
        // Add more categories
        Category category3 = new Category("Arts", "arts");
        category3.setId(3L);
        adapter.addCategories(Arrays.asList(category3));
        
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testClearCategories() {
        adapter.setCategories(testCategories);
        assertEquals(2, adapter.getItemCount());
        
        adapter.clearCategories();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testSetHorizontalLayout() {
        // Default should be grid layout (false)
        adapter.setHorizontalLayout(true);
        // We can't directly test the layout change, but we can verify it doesn't crash
        assertNotNull(adapter);
    }
} 