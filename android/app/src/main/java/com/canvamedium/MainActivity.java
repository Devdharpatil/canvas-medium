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

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that displays a list of articles.
 */
public class MainActivity extends AppCompatActivity implements ArticleAdapter.OnArticleClickListener {
    
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
        articleAdapter = new ArticleAdapter(articleList, this);
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
        
        // Update the article list
        articleList.clear();
        articleList.addAll(articles);
        articleAdapter.notifyDataSetChanged();
        
        showContent();
    }
    
    /**
     * Converts an ArticleEntity to an Article.
     */
    private Article convertEntityToArticle(ArticleEntity entity) {
        Article article = new Article();
        article.setId(entity.getId());
        article.setTitle(entity.getTitle());
        article.setPreviewText(entity.getPreviewText());
        article.setContent(entity.getContent());
        article.setThumbnailUrl(entity.getThumbnailUrl());
        article.setCreatedAt(entity.getCreatedAt());
        article.setUpdatedAt(entity.getUpdatedAt());
        article.setPublishedAt(entity.getPublishedAt());
        article.setStatus(entity.getStatus());
        article.setTemplateId(entity.getTemplateId());
        article.setBookmarked(entity.isBookmarked());
        
        // TODO: Set author and category objects if needed
        
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
    
    @Override
    public void onArticleClick(Article article) {
        Intent intent = ArticleDetailActivity.newIntent(this, article);
        startActivity(intent);
    }
} 