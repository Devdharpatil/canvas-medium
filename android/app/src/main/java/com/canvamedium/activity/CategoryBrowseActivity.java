package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.canvamedium.R;
import com.canvamedium.model.Category;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity for browsing categories.
 */
public class CategoryBrowseActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private NestedScrollView emptyView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_browse);

        // Initialize views
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup ViewPager with adapter
        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.all_categories);
                    break;
                case 1:
                    tab.setText(R.string.featured_categories);
                    break;
                case 2:
                    tab.setText(R.string.popular_categories);
                    break;
            }
        }).attach();

        // Hide empty view initially
        showEmptyState(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows or hides the empty state view.
     *
     * @param show True to show the empty state, false to hide it
     */
    public void showEmptyState(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        viewPager.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Shows or hides the progress indicator.
     *
     * @param show True to show progress, false to hide
     */
    public void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Handles category selection.
     *
     * @param category The selected category
     */
    public void onCategorySelected(Category category) {
        // Navigate to articles filtered by the selected category
        // TODO: Implement navigation to articles filtered by category
        // For now, just show a toast message
        // Toast.makeText(this, "Selected category: " + category.getName(), Toast.LENGTH_SHORT).show();
        
        // This will be implemented in the next task when we add category filtering to the article list
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("CATEGORY_ID", category.getId());
        intent.putExtra("CATEGORY_NAME", category.getName());
        startActivity(intent);
    }

    /**
     * Adapter for the category ViewPager.
     */
    private static class CategoryPagerAdapter extends FragmentStateAdapter {

        public CategoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return CategoryListFragment.newInstance(CategoryListFragment.TYPE_FEATURED);
                case 2:
                    return CategoryListFragment.newInstance(CategoryListFragment.TYPE_POPULAR);
                case 0:
                default:
                    return CategoryListFragment.newInstance(CategoryListFragment.TYPE_ALL);
            }
        }

        @Override
        public int getItemCount() {
            return 3; // All, Featured, Popular
        }
    }
} 