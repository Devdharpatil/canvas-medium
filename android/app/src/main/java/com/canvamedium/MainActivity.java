package com.canvamedium;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.activity.ArticleEditorActivity;
import com.canvamedium.activity.BookmarkedArticlesActivity;
import com.canvamedium.activity.CategoryBrowseActivity;
import com.canvamedium.activity.SearchActivity;
import com.canvamedium.activity.SettingsActivity;
import com.canvamedium.activity.TemplateListActivity;
import com.canvamedium.activity.UserProfileActivity;
import com.canvamedium.adapter.ArticleAdapter;
import com.canvamedium.db.entity.ArticleEntity;
import com.canvamedium.model.Article;
import com.canvamedium.sync.SyncManager;
import com.canvamedium.util.NetworkUtils;
import com.canvamedium.viewmodel.ArticleViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main activity that displays a list of articles.
 */
public class MainActivity extends AppCompatActivity implements ArticleAdapter.ArticleClickListener {
    
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArticleAdapter articleAdapter;
    private ArticleViewModel articleViewModel;
    private List<Article> articleList = new ArrayList<>();
    private TextView emptyView;
    private View errorView;
    private TextView errorText;
    private View loadingView;
    private View offlineIndicator;
    private NetworkUtils networkUtils;
    private SyncManager syncManager;
    private long categoryId = -1;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        initializeViews();
        
        // Set up RecyclerView
        setupRecyclerView();
        
        // Initialize ViewModel
        articleViewModel = new ViewModelProvider(this).get(ArticleViewModel.class);
        
        // Initialize utilities
        networkUtils = new NetworkUtils(this);
        syncManager = ((CanvaMediumApplication) getApplication()).getSyncManager();
        
        // Check if coming from category selection
        if (getIntent().hasExtra("CATEGORY_ID")) {
            categoryId = getIntent().getLongExtra("CATEGORY_ID", -1);
            String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
            if (categoryName != null) {
                setTitle(categoryName);
            }
        }
        
        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshArticles);
        
        // Set up FAB click listener
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ArticleEditorActivity.class);
            startActivity(intent);
        });
        
        // Set up error view retry button
        findViewById(R.id.buttonRetry).setOnClickListener(v -> refreshArticles());
        
        // Load data
        loadArticles();
        
        // Show offline indicator if needed
        updateOfflineIndicator();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Set up search view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Launch search activity with query
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra(android.app.SearchManager.QUERY, query);
                startActivity(intent);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_templates) {
            Intent intent = new Intent(this, TemplateListActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_categories) {
            Intent intent = new Intent(this, CategoryBrowseActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_bookmarks) {
            Intent intent = new Intent(this, BookmarkedArticlesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_sync) {
            syncNow();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Initializes the views.
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyView = findViewById(R.id.textEmpty);
        errorView = findViewById(R.id.errorView);
        errorText = findViewById(R.id.textError);
        loadingView = findViewById(R.id.loadingView);
        offlineIndicator = findViewById(R.id.offlineIndicator);
    }
    
    /**
     * Sets up the RecyclerView.
     */
    private void setupRecyclerView() {
        articleAdapter = new ArticleAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(articleAdapter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateOfflineIndicator();
    }
    
    /**
     * Loads articles from the repository.
     */
    private void loadArticles() {
        showLoading(true);
        
        if (categoryId > 0) {
            // Load articles for a specific category
            articleViewModel.getArticlesByCategory(categoryId).observe(this, this::handleArticlesResult);
        } else {
            // Load all articles
            articleViewModel.getAllArticles().observe(this, this::handleArticlesResult);
        }
    }
    
    /**
     * Refreshes the articles data.
     */
    private void refreshArticles() {
        if (!networkUtils.isOnline()) {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(swipeRefreshLayout, R.string.no_internet_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        // Force a refresh from the server
        articleViewModel.refreshArticles();
        swipeRefreshLayout.setRefreshing(false);
    }
    
    /**
     * Handles the result of loading articles.
     */
    private void handleArticlesResult(List<ArticleEntity> articleEntities) {
        showLoading(false);
        
        if (articleEntities == null || articleEntities.isEmpty()) {
            showEmptyView();
            return;
        }
        
        // Convert ArticleEntity objects to Article objects
        List<Article> articles = new ArrayList<>();
        for (ArticleEntity entity : articleEntities) {
            Article article = convertEntityToArticle(entity);
            articles.add(article);
        }
        
        // Update the adapter with the new list
        articleAdapter.submitList(articles);
        
        // Show the content
        showContent();
    }
    
    /**
     * Converts an ArticleEntity to an Article model.
     */
    private Article convertEntityToArticle(ArticleEntity entity) {
        Gson gson = new Gson();
        JsonObject contentJson;
        try {
            contentJson = gson.fromJson(entity.getContent(), JsonObject.class);
        } catch (Exception e) {
            contentJson = new JsonObject();
        }
        
        Article article = new Article();
        article.setId(entity.getId());
        article.setTitle(entity.getTitle());
        article.setContent(contentJson);
        article.setPreviewText(entity.getPreviewText());
        article.setThumbnailUrl(entity.getThumbnailUrl());
        article.setTemplateId(entity.getTemplateId());
        article.setStatus(entity.getStatus());
        
        // Convert dates from Date to String format
        if (entity.getPublishedAt() != null) {
            article.setPublishedAt(DATE_FORMAT.format(entity.getPublishedAt()));
        }
        
        if (entity.getCreatedAt() != null) {
            article.setCreatedAt(DATE_FORMAT.format(entity.getCreatedAt()));
        }
        
        if (entity.getUpdatedAt() != null) {
            article.setUpdatedAt(DATE_FORMAT.format(entity.getUpdatedAt()));
        }
        
        article.setBookmarked(entity.isBookmarked());
        article.setAuthorName(entity.getAuthorName());
        
        // Set category if available
        if (entity.getCategoryId() != null && entity.getCategoryName() != null) {
            com.canvamedium.model.Category category = new com.canvamedium.model.Category();
            category.setId(entity.getCategoryId());
            category.setName(entity.getCategoryName());
            article.setCategory(category);
        }
        
        // Set tags if available
        List<String> tagNames = entity.getTags();
        if (tagNames != null && !tagNames.isEmpty()) {
            List<com.canvamedium.model.Tag> tags = new ArrayList<>();
            for (String tagName : tagNames) {
                com.canvamedium.model.Tag tag = new com.canvamedium.model.Tag();
                tag.setName(tagName);
                tags.add(tag);
            }
            article.setTags(tags);
        }
        
        return article;
    }
    
    /**
     * Shows or hides the loading view.
     */
    private void showLoading(boolean show) {
        if (show) {
            loadingView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    
    /**
     * Shows the content view.
     */
    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
    
    /**
     * Shows the empty view.
     */
    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }
    
    /**
     * Shows an error.
     *
     * @param message The error message
     */
    private void showError(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }
    
    /**
     * Updates the offline indicator.
     */
    private void updateOfflineIndicator() {
        if (networkUtils.isOnline()) {
            offlineIndicator.setVisibility(View.GONE);
        } else {
            offlineIndicator.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Triggers a manual sync with the server.
     */
    private void syncNow() {
        if (!networkUtils.isOnline()) {
            Snackbar.make(swipeRefreshLayout, R.string.no_internet_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        Snackbar.make(swipeRefreshLayout, R.string.syncing_data, Snackbar.LENGTH_SHORT).show();
        syncManager.syncNow();
    }
    
    /**
     * Handle article click event.
     *
     * @param article The clicked article
     */
    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(this, com.canvamedium.activity.ArticleDetailActivity.class);
        intent.putExtra("EXTRA_ARTICLE_ID", article.getId());
        startActivity(intent);
    }
    
    /**
     * Handle bookmark click event.
     *
     * @param article The article to bookmark/unbookmark
     * @param isCurrentlyBookmarked Whether the article is currently bookmarked
     */
    @Override
    public void onBookmarkClick(Article article, boolean isCurrentlyBookmarked) {
        // Toggle bookmark state
        article.setBookmarked(!isCurrentlyBookmarked);
        
        // Update article in repository
        articleViewModel.toggleBookmark(article.getId(), !isCurrentlyBookmarked)
                .observe(this, success -> {
                    if (success) {
                        articleAdapter.notifyDataSetChanged();
                        
                        String message = isCurrentlyBookmarked 
                                ? "Article removed from bookmarks" 
                                : "Article added to bookmarks";
                        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show();
                    } else {
                        // Revert state on failure
                        article.setBookmarked(isCurrentlyBookmarked);
                        articleAdapter.notifyDataSetChanged();
                        
                        Snackbar.make(recyclerView, "Failed to update bookmark", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
} 