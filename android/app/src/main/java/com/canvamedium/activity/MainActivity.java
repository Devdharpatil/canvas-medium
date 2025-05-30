package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.R;
import com.canvamedium.adapter.ArticleAdapter;
import com.canvamedium.model.Article;
import com.canvamedium.model.Category;
import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.repository.CategoryRepository;
import com.canvamedium.sync.SyncManager;
import com.canvamedium.util.AuthManager;
import com.canvamedium.util.DemoDataGenerator;
import com.canvamedium.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that shows the article feed and provides navigation to other features.
 */
public class MainActivity extends AppCompatActivity implements ArticleAdapter.ArticleClickListener {

    private static final String TAG = "MainActivity";
    
    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExtendedFloatingActionButton fabAdd;
    private View emptyView;
    private View offlineIndicator;
    private View errorView;
    private View loadingView;
    private TabLayout tabLayout;
    private SearchView searchView;
    
    private ArticleRepository articleRepository;
    private CategoryRepository categoryRepository;
    private AuthManager authManager;
    
    // Track whether we're in demo mode
    private boolean isDemoMode = false;
    private long selectedCategoryId = -1; // -1 means "All"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the default ActionBar since we're using a custom toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAdd = findViewById(R.id.fabAdd);
        emptyView = findViewById(R.id.emptyView);
        offlineIndicator = findViewById(R.id.offlineIndicator);
        errorView = findViewById(R.id.errorView);
        loadingView = findViewById(R.id.loadingView);
        tabLayout = findViewById(R.id.tabLayout);
        searchView = findViewById(R.id.searchView);
        
        // Set up menu button
        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showPopupMenu);

        // Set up demo button in empty view
        Button buttonCreateDemo = findViewById(R.id.buttonCreateDemo);
        if (buttonCreateDemo != null) {
            buttonCreateDemo.setOnClickListener(v -> generateDemoData());
        }
        
        // Set up retry button
        Button buttonRetry = findViewById(R.id.buttonRetry);
        if (buttonRetry != null) {
            buttonRetry.setOnClickListener(v -> {
                errorView.setVisibility(View.GONE);
                loadArticles();
            });
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleAdapter(this);
        recyclerView.setAdapter(adapter);

        // Initialize repositories and managers
        articleRepository = new ArticleRepository(getApplication());
        categoryRepository = new CategoryRepository(getApplication());
        authManager = AuthManager.getInstance(this);

        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }
        
        // Show offline indicator if needed
        updateConnectivityState();

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshArticles);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        );

        // Set up FAB
        fabAdd.setOnClickListener(view -> startCreateArticle());
        
        // Set up searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchArticles(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        
        // Set up tabs
        setupCategoryTabs();

        // Load initial data
        loadArticles();
    }
    
    private void setupCategoryTabs() {
        // Add "All" tab
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        
        // Load categories and add tabs
        categoryRepository.getAllCategories((categories, error) -> {
            if (error == null && categories != null && !categories.isEmpty()) {
                runOnUiThread(() -> {
                    for (Category category : categories) {
                        tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
                    }
                });
            }
        });
        
        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // "All" selected
                    selectedCategoryId = -1;
                    loadArticles();
                } else if (position <= tabLayout.getTabCount()) {
                    // Get category ID from position (adjust for "All" tab)
                    selectedCategoryId = position; // Simplified - in production you'd map to actual category IDs
                    loadArticlesByCategory(selectedCategoryId);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Reload on reselection
                onTabSelected(tab);
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_main);
        
        popup.setOnMenuItemClickListener(item -> {
            try {
                // Check if fragment manager is in a valid state before navigation
                if (getSupportFragmentManager().isStateSaved()) {
                    Toast.makeText(this, "Please wait a moment before navigating", Toast.LENGTH_SHORT).show();
                    return false;
                }

                int id = item.getItemId();
        
                if (id == R.id.action_search) {
                    searchView.setIconified(false); // Open the search view
                    return true;
                } else if (id == R.id.action_bookmarks) {
                    safeNavigateTo(() -> {
                        startActivity(new Intent(this, BookmarkedArticlesActivity.class));
                    });
                    return true;
                } else if (id == R.id.action_categories) {
                    safeNavigateTo(() -> {
                        startActivity(new Intent(this, CategoryBrowseActivity.class));
                    });
                    return true;
                } else if (id == R.id.action_profile) {
                    safeNavigateTo(() -> {
                        startActivity(new Intent(this, UserProfileActivity.class));
                    });
                    return true;
                } else if (id == R.id.action_templates) {
                    safeNavigateTo(() -> {
                        startActivity(new Intent(this, TemplateListActivity.class));
                    });
                    return true;
                } else if (id == R.id.action_settings) {
                    safeNavigateTo(() -> {
                        startActivity(new Intent(this, SettingsActivity.class));
                    });
                    return true;
                } else if (id == R.id.action_logout) {
                    logout();
                    return true;
                } else if (id == R.id.action_demo_data) {
                    generateDemoData();
                    return true;
                } else if (id == R.id.action_filter) {
                    showFilterDialog();
                    return true;
                } else if (id == R.id.action_sync) {
                    syncContent();
                    return true;
                }
                
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Error handling menu click", e);
                Toast.makeText(this, "Navigation temporarily unavailable", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        
        popup.show();
    }
    
    /**
     * Safely navigate to a destination by handling errors and providing feedback
     *
     * @param navigationAction The action to perform for navigation
     */
    private void safeNavigateTo(Runnable navigationAction) {
        try {
            // First check network connectivity for API-dependent screens
            boolean isConnected = NetworkUtils.isNetworkConnected(this);
            
            // Show loading indicator
            showLoading(true);
            
            // Create a handler for the main thread
            new Handler().postDelayed(() -> {
                try {
                    // Execute the navigation action
                    navigationAction.run();
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed", e);
                    Toast.makeText(this, "Unable to navigate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    // Hide loading indicator
                    showLoading(false);
                    
                    // Show offline warning if not connected
                    if (!isConnected) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "You're offline. Some features may be limited.",
                            Snackbar.LENGTH_LONG
                        ).show();
                    }
                }
            }, 100); // Small delay to show loading indicator
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to begin navigation", e);
            showLoading(false);
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show filter options for articles
     */
    private void showFilterDialog() {
        final String[] filterOptions = {"All", "Most Recent", "Most Popular", "Bookmarked", "By Category"};
        
        new AlertDialog.Builder(this)
            .setTitle("Filter Articles")
            .setItems(filterOptions, (dialog, which) -> {
                switch (which) {
                    case 0: // All
                        selectedCategoryId = -1;
                        loadArticles();
                        break;
                    case 1: // Most Recent
                        filterArticlesByRecent();
                        break;
                    case 2: // Most Popular
                        filterArticlesByPopularity();
                        break;
                    case 3: // Bookmarked
                        safeNavigateTo(() -> {
                            startActivity(new Intent(this, BookmarkedArticlesActivity.class));
                        });
                        break;
                    case 4: // By Category
                        safeNavigateTo(() -> {
                            startActivity(new Intent(this, CategoryBrowseActivity.class));
                        });
                        break;
                }
            })
            .show();
    }
    
    /**
     * Filter articles by recency
     */
    private void filterArticlesByRecent() {
        swipeRefreshLayout.setRefreshing(true);
        showLoading(true);
        
        articleRepository.getArticlesSortedByDate((articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error filtering articles by recent: " + error);
                    showError(error);
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Showing most recent articles",
                        Snackbar.LENGTH_SHORT
                    ).show();
                } else {
                    showEmptyState(true);
                }
            });
        });
    }
    
    /**
     * Filter articles by popularity
     */
    private void filterArticlesByPopularity() {
        swipeRefreshLayout.setRefreshing(true);
        showLoading(true);
        
        articleRepository.getArticlesSortedByPopularity((articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error filtering articles by popularity: " + error);
                    showError(error);
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Showing most popular articles",
                        Snackbar.LENGTH_SHORT
                    ).show();
                } else {
                    showEmptyState(true);
                }
            });
        });
    }
    
    /**
     * Synchronize content with the server
     */
    private void syncContent() {
        swipeRefreshLayout.setRefreshing(true);
        Snackbar.make(
            findViewById(android.R.id.content),
            "Syncing your content...",
            Snackbar.LENGTH_SHORT
        ).show();
        
        // Request a sync from the SyncManager
        SyncManager syncManager = ((com.canvamedium.CanvaMediumApplication) getApplication()).getSyncManager();
        syncManager.requestSync()
            .subscribe(
                () -> {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Sync completed successfully",
                            Snackbar.LENGTH_SHORT
                        ).show();
                        
                        // Refresh data
                        loadArticles();
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Sync failed: " + error.getMessage(),
                            Snackbar.LENGTH_LONG
                        ).setAction("RETRY", v -> syncContent()).show();
                    });
                }
            );
    }
    
    private void updateConnectivityState() {
        boolean isConnected = NetworkUtils.isNetworkConnected(this);
        offlineIndicator.setVisibility(isConnected ? View.GONE : View.VISIBLE);
    }

    private void loadArticles() {
        swipeRefreshLayout.setRefreshing(true);
        showLoading(true);
        updateConnectivityState();
        
        // Get articles from repository
        articleRepository.getAllArticles((articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error loading articles: " + error);
                    
                    // Show error view with retry option
                    showError(error);
                    
                    // Try to load from local database as fallback
                    loadLocalArticles();
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                } else {
                    // Show empty state
                    showEmptyState(true);
                    
                    // Prompt to create demo data
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "No articles found. Generate demo content?",
                            Snackbar.LENGTH_LONG)
                        .setAction("GENERATE", v -> generateDemoData())
                        .show();
                }
            });
        });
    }
    
    private void loadArticlesByCategory(long categoryId) {
        swipeRefreshLayout.setRefreshing(true);
        showLoading(true);
        
        // Get articles from repository
        articleRepository.getArticlesByCategory(categoryId, (articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error loading articles: " + error);
                    
                    // Show error with retry option
                    showError(error);
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                } else {
                    // Show empty state for category
                    showEmptyState(true);
                }
            });
        });
    }
    
    private void searchArticles(String query) {
        if (query == null || query.trim().isEmpty()) {
            // If query is empty, show all articles
            loadArticles();
            return;
        }
        
        swipeRefreshLayout.setRefreshing(true);
        showLoading(true);
        
        // Search articles from repository
        articleRepository.searchArticles(query, (articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error searching articles: " + error);
                    
                    // Show error with retry option
                    showError(error);
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                    
                    // Show search results message
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Found " + articles.size() + " results for '" + query + "'",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    // Show no results message
                    adapter.clearArticles();
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "No results found for '" + query + "'",
                            Snackbar.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            });
        });
    }
    
    private void showEmptyState(boolean show) {
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }
    
    private void showError(String errorMessage) {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
    
    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void refreshArticles() {
        if (selectedCategoryId == -1) {
            loadArticles();
        } else {
            loadArticlesByCategory(selectedCategoryId);
        }
    }
    
    private void generateDemoData() {
        swipeRefreshLayout.setRefreshing(true);
        isDemoMode = true;
        showLoading(true);
        
        // Show generating message to user
        Snackbar.make(
            findViewById(android.R.id.content),
            "Generating demo articles...",
            Snackbar.LENGTH_SHORT
        ).show();
        
        DemoDataGenerator.createSampleArticles(
            this,
            new DemoDataGenerator.DemoDataCallback() {
                @Override
                public void onComplete(List<Article> articles) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showLoading(false);
                        if (articles != null && !articles.isEmpty()) {
                            adapter.setArticles(articles);
                            showEmptyState(false);
                            
                            // Show success message with article count
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                articles.size() + " demo articles created successfully!",
                                Snackbar.LENGTH_SHORT
                            ).show();
                            
                            // Update local storage with demo articles
                            saveArticlesToLocalStorage(articles);
                            
                            // Refresh tabs with new categories
                            setupCategoryTabs();
                        } else {
                            Toast.makeText(MainActivity.this, 
                                "No articles were created", Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showLoading(false);
                        
                        // Show error and offer retry
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error: " + errorMessage,
                            Snackbar.LENGTH_LONG
                        )
                        .setAction("RETRY", v -> generateDemoData())
                        .show();
                        
                        // Also try loading any existing local articles
                        loadLocalArticles();
                    });
                }
            }
        );
    }
    
    /**
     * Save articles to local storage for offline access
     */
    private void saveArticlesToLocalStorage(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        
        // Save articles to database in background
        for (Article article : articles) {
            articleRepository.insertOrUpdateArticle(article)
                .subscribe(
                    () -> Log.d(TAG, "Article saved to local storage: " + article.getTitle()),
                    error -> Log.e(TAG, "Error saving article: " + error.getMessage())
                );
        }
    }
    
    /**
     * Load articles from local storage if available
     */
    private void loadLocalArticles() {
        articleRepository.getArticlesFromDatabase((articles, error) -> {
            runOnUiThread(() -> {
                if (error != null) {
                    Log.e(TAG, "Error loading local articles: " + error);
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Loaded " + articles.size() + " articles from local storage", 
                            Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void startCreateArticle() {
        try {
            // Show loading state
            fabAdd.setEnabled(false);
            
            Intent intent = new Intent(this, ArticleEditorActivity.class);
            // Add parameters for new article creation
            intent.putExtra("mode", "create_new");
            intent.putExtra("source", "fab_home");
            startActivity(intent);
            
            // Re-enable fab after short delay
            new Handler().postDelayed(() -> fabAdd.setEnabled(true), 500);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to article creation", e);
            Toast.makeText(this, "Unable to open article creator", Toast.LENGTH_SHORT).show();
            fabAdd.setEnabled(true);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void logout() {
        new AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes", (dialog, which) -> {
                authManager.logout();
                navigateToLogin();
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE_ID, article.getId());
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(Article article, boolean isCurrentlyBookmarked) {
        // Toggle bookmark in repository
        article.setBookmarked(!isCurrentlyBookmarked);
        articleRepository.toggleBookmark(article.getId(), !isCurrentlyBookmarked)
            .subscribe(
                () -> {
                    String message = article.isBookmarked() ? 
                        "Article bookmarked" : "Bookmark removed";
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                },
                throwable -> {
                    Log.e(TAG, "Error toggling bookmark", throwable);
                    Snackbar.make(findViewById(android.R.id.content), 
                        "Error updating bookmark", Snackbar.LENGTH_SHORT).show();
                    // Revert the bookmark state in the UI
                    article.setBookmarked(isCurrentlyBookmarked);
                    adapter.notifyDataSetChanged();
                }
            );
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateConnectivityState();
    }
} 