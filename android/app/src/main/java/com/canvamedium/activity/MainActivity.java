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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.canvamedium.R;
import com.canvamedium.adapter.ArticleAdapter;
import com.canvamedium.adapter.CategoryAdapter;
import com.canvamedium.adapter.FeaturedArticleAdapter;
import com.canvamedium.model.Article;
import com.canvamedium.model.Category;
import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.repository.CategoryRepository;
import com.canvamedium.sync.SyncManager;
import com.canvamedium.util.AuthManager;
import com.canvamedium.util.CarouselPageTransformer;
import com.canvamedium.util.DemoDataGenerator;
import com.canvamedium.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main activity that shows the article feed and provides navigation to other features.
 */
public class MainActivity extends AppCompatActivity implements 
        ArticleAdapter.ArticleClickListener,
        CategoryAdapter.OnCategoryClickListener,
        FeaturedArticleAdapter.OnFeaturedArticleClickListener {

    private static final String TAG = "MainActivity";
    
    private RecyclerView recyclerView;
    private RecyclerView categoryRecyclerView;
    private ArticleAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private FeaturedArticleAdapter featuredAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private View offlineIndicator;
    private View errorView;
    private View loadingView;
    private ImageView appLogo;
    private ImageButton searchButton;
    private ImageButton menuButton;
    private BottomNavigationView bottomNavigationView;
    
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
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyView = findViewById(R.id.emptyView);
        offlineIndicator = findViewById(R.id.offlineIndicator);
        errorView = findViewById(R.id.errorView);
        loadingView = findViewById(R.id.loadingView);
        
        // Initialize toolbar components
        appLogo = findViewById(R.id.toolbar_app_logo);
        searchButton = findViewById(R.id.toolbar_search_icon);
        menuButton = findViewById(R.id.toolbar_menu_icon);
        
        // Set up menu button
        menuButton.setOnClickListener(this::showPopupMenu);
        
        // Set up search button
        searchButton.setOnClickListener(v -> {
            // Handle search click
            searchArticles("");
        });

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

        // Set up RecyclerView for articles
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleAdapter(this);
        recyclerView.setAdapter(adapter);
        
        // Set up category RecyclerView
        categoryAdapter = new CategoryAdapter(this, this);
        categoryAdapter.setHorizontalLayout(true);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Set up featured carousel
        setupFeaturedCarousel();

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
        
        // Set up bottom navigation
        setupBottomNavigation();
        
        // Load initial data
        loadCategories();
        loadArticles();
    }

    /**
     * Sets up the bottom navigation view with its listener
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom navigation item selected: " + item.getTitle());
            
            if (itemId == R.id.nav_home) {
                // Already on home, just refresh
                refreshArticles();
                return true;
            } else if (itemId == R.id.nav_discover) {
                // Navigate to discover/community
                // TODO: Refactor to use Fragments for smoother in-app navigation instead of starting new Activities for tabs.
                safeNavigateTo(() -> {
                    startActivity(new Intent(this, CategoryBrowseActivity.class));
                });
                return true;
            } else if (itemId == R.id.nav_create) {
                // Navigate to create
                startCreateArticle();
                return true;
            } else if (itemId == R.id.nav_templates) {
                // Navigate to templates
                // TODO: Refactor to use Fragments for smoother in-app navigation instead of starting new Activities for tabs.
                safeNavigateTo(() -> {
                    startActivity(new Intent(this, TemplateListActivity.class));
                });
                return true;
            } else if (itemId == R.id.nav_profile_bottom) {
                // Navigate to profile
                // TODO: Refactor to use Fragments for smoother in-app navigation instead of starting new Activities for tabs.
                safeNavigateTo(() -> {
                    startActivity(new Intent(this, UserProfileActivity.class));
                });
                return true;
            }
            
            return false;
        });
        
        // Set the initial selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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
                    searchArticles("");
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

    /**
     * Loads articles with the specified filters
     */
    private void loadArticles() {
        showLoading(true);
        showEmptyState(false);
        
        articleRepository.getAllArticles((articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error loading articles: " + error);
                    showError(getString(R.string.error_loading_articles));
                    return;
                }
                
                if (articles == null || articles.isEmpty()) {
                    showEmptyState(true);
                    adapter.setArticles(new ArrayList<>());
                    featuredAdapter.setFeaturedArticles(new ArrayList<>());
                } else {
                    showEmptyState(false);
                    
                    // Filter for featured articles for the carousel
                    List<Article> featuredArticles = articles.stream()
                            .filter(Article::isFeatured)
                            .limit(5)
                            .collect(Collectors.toList());
                    
                    // If there are no featured articles, use the first 5 regular articles
                    if (featuredArticles.isEmpty() && !articles.isEmpty()) {
                        featuredArticles = articles.stream()
                                .limit(5)
                                .collect(Collectors.toList());
                    }
                    
                    featuredAdapter.setFeaturedArticles(featuredArticles);
                    
                    // Filter articles by selected category if needed
                    if (selectedCategoryId != -1) {
                        List<Article> filteredArticles = articles.stream()
                                .filter(article -> article.getCategory() != null 
                                        && article.getCategory().getId() == selectedCategoryId)
                                .collect(Collectors.toList());
                        adapter.setArticles(filteredArticles);
                    } else {
                        adapter.setArticles(articles);
                    }
                    
                    // Update category counts
                    updateCategoryCounts(articles);
                }
            });
        });
    }
    
    /**
     * Loads articles by category
     */
    private void loadArticlesByCategory(long categoryId) {
        swipeRefreshLayout.setRefreshing(true);
        showLoading(true);
        
        articleRepository.getArticlesByCategory(categoryId, (articles, error) -> {
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                if (error != null) {
                    Log.e(TAG, "Error loading articles by category: " + error);
                    showError(getString(R.string.error_loading_articles));
                    return;
                }
                
                if (articles != null && !articles.isEmpty()) {
                    adapter.setArticles(articles);
                    showEmptyState(false);
                } else {
                    adapter.clearArticles();
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
        // Navigate to article creation
        Intent intent = new Intent(MainActivity.this, ArticleEditorActivity.class);
        startActivity(intent);
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

    /**
     * Loads categories for the horizontal category selector
     */
    private void loadCategories() {
        showLoading(true);
        
        categoryRepository.getAllCategories((categories, error) -> {
            showLoading(false);
            
            if (error != null) {
                Toast.makeText(this, getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading categories: " + error);
                
                // At least show "All" category
                Category allCategory = new Category("All", "all");
                allCategory.setId(-1L);
                categoryAdapter.setCategories(List.of(allCategory));
                return;
            }
            
            if (categories != null && !categories.isEmpty()) {
                // Create "All" category at the beginning
                Category allCategory = new Category("All", "all");
                allCategory.setId(-1L);
                allCategory.setDescription("All articles");
                allCategory.setArticleCount(adapter.getItemCount());
                
                List<Category> allCategories = new ArrayList<>();
                allCategories.add(allCategory);
                allCategories.addAll(categories);
                
                categoryAdapter.setCategories(allCategories);
            } else {
                // Handle empty categories
                Category allCategory = new Category("All", "all");
                allCategory.setId(-1L);
                categoryAdapter.setCategories(List.of(allCategory));
            }
        });
    }
    
    /**
     * Updates the article counts for each category
     */
    private void updateCategoryCounts(List<Article> articles) {
        if (categoryAdapter.getItemCount() <= 1) {
            return;
        }
        
        List<Category> currentCategories = new ArrayList<>();
        for (int i = 0; i < categoryAdapter.getItemCount(); i++) {
            Category category = categoryAdapter.getCategory(i);
            if (category != null) {
                if (category.getId() == -1) {
                    // Update "All" category count
                    category.setArticleCount(articles.size());
                } else {
                    // Count articles for this category
                    long count = articles.stream()
                            .filter(article -> article.getCategory() != null 
                                    && article.getCategory().getId() == category.getId())
                            .count();
                    category.setArticleCount((int) count);
                }
                currentCategories.add(category);
            }
        }
        
        categoryAdapter.setCategories(currentCategories);
    }

    /**
     * Callback when a category is clicked
     */
    @Override
    public void onCategoryClick(Category category) {
        selectedCategoryId = category.getId();
        if (selectedCategoryId == -1) {
            // "All" category selected
            loadArticles();
        } else {
            loadArticlesByCategory(selectedCategoryId);
        }
    }
    
    /**
     * Callback when a featured article is clicked
     */
    @Override
    public void onFeaturedArticleClick(Article article) {
        // Open article detail screen
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }

    /**
     * Sets up the trending article carousel with appropriate transformations and animations
     */
    private void setupFeaturedCarousel() {
        featuredAdapter = new FeaturedArticleAdapter(this, this);
        
        // Use trending_carousel instead of featuredCarousel
        ViewPager2 trendingCarousel = findViewById(R.id.trending_carousel);
        trendingCarousel.setAdapter(featuredAdapter);
        
        // Set offscreen page limit to ensure adjacent pages are available
        trendingCarousel.setOffscreenPageLimit(3);
        
        // Apply the new CarouselPageTransformer
        trendingCarousel.setPageTransformer(new CarouselPageTransformer());
        
        // Set up custom indicator
        LinearLayout indicatorsContainer = findViewById(R.id.custom_page_indicator_container);
        
        // Auto-cycle through the carousel
        Handler sliderHandler = new Handler();
        Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (trendingCarousel.getCurrentItem() < featuredAdapter.getItemCount() - 1) {
                    trendingCarousel.setCurrentItem(trendingCarousel.getCurrentItem() + 1);
                } else {
                    trendingCarousel.setCurrentItem(0);
                }
                sliderHandler.postDelayed(this, 3000);
            }
        };

        trendingCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(indicatorsContainer, position, featuredAdapter.getItemCount());
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        
        // Set initial position to show peeking on both sides if possible
        if (featuredAdapter.getItemCount() >= 3) { // Need at least 3 to reliably start at index 1 and show both peeks
            int initialPosition = 1; // Start at the second item (index 1)
            trendingCarousel.setCurrentItem(initialPosition, false);
            // Manually update indicators for this initial, non-scrolled position
            updateIndicators(indicatorsContainer, initialPosition, featuredAdapter.getItemCount());
        } else if (featuredAdapter.getItemCount() > 0) { // If less than 3 but more than 0
            updateIndicators(indicatorsContainer, 0, featuredAdapter.getItemCount());
        }
        
        // View All button click listener
        TextView viewAllButton = findViewById(R.id.trending_view_all_button);
        viewAllButton.setOnClickListener(v -> {
            Toast.makeText(this, "View All Trending Articles", Toast.LENGTH_SHORT).show();
            // Future implementation to show all trending articles
        });
    }
    
    /**
     * Creates and updates custom indicators for ViewPager2
     */
    private void updateIndicators(LinearLayout container, int currentPosition, int count) {
        // If container is empty, create indicators
        if (container.getChildCount() == 0) {
            createIndicators(container, count, currentPosition);
        } else {
            // Update indicators to reflect current position
            for (int i = 0; i < container.getChildCount(); i++) {
                ImageView indicator = (ImageView) container.getChildAt(i);
                if (i == currentPosition) {
                    indicator.setImageResource(R.drawable.indicator_dot_active_rect);
                } else {
                    indicator.setImageResource(R.drawable.indicator_dot_inactive);
                }
            }
        }
    }
    
    /**
     * Creates indicators for ViewPager2
     */
    private void createIndicators(LinearLayout container, int count, int currentPosition) {
        container.removeAllViews();
        
        if (count <= 0) return;
        
        // Create indicator views
        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            
            // Set indicator appearance based on position
            if (i == currentPosition) {
                indicator.setImageResource(R.drawable.indicator_dot_active_rect);
            } else {
                indicator.setImageResource(R.drawable.indicator_dot_inactive);
            }
            
            // Set margins between indicators
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);
            
            // Add to container
            container.addView(indicator);
        }
    }
} 