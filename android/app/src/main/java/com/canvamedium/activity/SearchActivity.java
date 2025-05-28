package com.canvamedium.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.adapter.ArticleAdapter;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Article;
import com.canvamedium.model.Template;
import com.canvamedium.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for searching and filtering articles.
 */
public class SearchActivity extends AppCompatActivity implements ArticleAdapter.ArticleClickListener {
    
    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private List<Article> articleList;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner statusSpinner;
    private Spinner templateSpinner;
    private Spinner featuredSpinner;
    
    private ApiService apiService;
    private List<Template> templateList;
    
    private String currentQuery = "";
    private String currentStatus = "";
    private Long currentTemplateId = null;
    private Boolean currentFeatured = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);
        
        // Initialize views
        recyclerView = findViewById(R.id.search_recycler_view);
        progressBar = findViewById(R.id.search_progress_bar);
        emptyStateTextView = findViewById(R.id.search_empty_state);
        swipeRefreshLayout = findViewById(R.id.search_swipe_refresh);
        statusSpinner = findViewById(R.id.status_spinner);
        templateSpinner = findViewById(R.id.template_spinner);
        featuredSpinner = findViewById(R.id.featured_spinner);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        articleList = new ArrayList<>();
        adapter = new ArticleAdapter(this);
        recyclerView.setAdapter(adapter);
        
        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::performSearch);
        
        // Set up spinners
        setupStatusSpinner();
        setupFeaturedSpinner();
        loadTemplates(); // Will set up template spinner after loading templates
        
        // Handle search intent
        if (getIntent() != null && getIntent().hasExtra(SearchManager.QUERY)) {
            currentQuery = getIntent().getStringExtra(SearchManager.QUERY);
            performSearch();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        
        // Set up search view
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        
        if (currentQuery != null && !currentQuery.isEmpty()) {
            searchView.setQuery(currentQuery, false);
            searchView.clearFocus();
        }
        
        // Set up search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                performSearch();
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && !currentQuery.isEmpty()) {
                    currentQuery = "";
                    performSearch();
                }
                return false;
            }
        });
        
        return true;
    }
    
    /**
     * Perform search with current filters.
     */
    private void performSearch() {
        showLoading();
        
        Map<String, Object> options = new HashMap<>();
        
        if (currentQuery != null && !currentQuery.isEmpty()) {
            options.put("query", currentQuery);
        }
        
        if (currentStatus != null && !currentStatus.isEmpty() && !currentStatus.equals("ALL")) {
            options.put("status", currentStatus);
        }
        
        if (currentTemplateId != null) {
            options.put("templateId", currentTemplateId);
        }
        
        if (currentFeatured != null) {
            options.put("featured", currentFeatured);
        }
        
        options.put("page", 0);
        options.put("size", 20);
        options.put("sortBy", "updatedAt");
        options.put("sortDir", "desc");
        
        apiService.searchArticles(options).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<?> articles = (List<?>) response.body().get("articles");
                        
                        if (articles != null && !articles.isEmpty()) {
                            articleList.clear();
                            
                            for (Object article : articles) {
                                if (article instanceof Map) {
                                    Map<?, ?> articleMap = (Map<?, ?>) article;
                                    Article newArticle = Article.fromMap(articleMap);
                                    articleList.add(newArticle);
                                }
                            }
                            
                            adapter.notifyDataSetChanged();
                            showContent();
                        } else {
                            showEmptyState();
                        }
                    } catch (Exception e) {
                        showError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    showError("Error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Load templates for the template spinner.
     */
    private void loadTemplates() {
        Map<String, Object> options = new HashMap<>();
        options.put("page", 0);
        options.put("size", 100);
        
        apiService.getTemplates(options).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<?> templates = (List<?>) response.body().get("templates");
                        
                        if (templates != null && !templates.isEmpty()) {
                            templateList = new ArrayList<>();
                            templateList.add(new Template()); // Add empty template for "All"
                            
                            for (Object template : templates) {
                                if (template instanceof Map) {
                                    Map<?, ?> templateMap = (Map<?, ?>) template;
                                    Template newTemplate = Template.fromMap(templateMap);
                                    templateList.add(newTemplate);
                                }
                            }
                            
                            setupTemplateSpinner();
                        }
                    } catch (Exception e) {
                        Toast.makeText(SearchActivity.this, 
                                "Error loading templates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, 
                        "Failed to load templates: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Set up the status filter spinner.
     */
    private void setupStatusSpinner() {
        String[] statusArray = {"All", "PUBLISHED", "DRAFT", "ARCHIVED"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
        
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentStatus = "";
                } else {
                    currentStatus = statusArray[position];
                }
                performSearch();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentStatus = "";
            }
        });
    }
    
    /**
     * Set up the template filter spinner.
     */
    private void setupTemplateSpinner() {
        if (templateList == null || templateList.isEmpty()) {
            return;
        }
        
        List<String> templateNames = new ArrayList<>();
        templateNames.add("All Templates");
        
        for (int i = 1; i < templateList.size(); i++) {
            templateNames.add(templateList.get(i).getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, templateNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        templateSpinner.setAdapter(adapter);
        
        templateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentTemplateId = null;
                } else {
                    currentTemplateId = templateList.get(position).getId();
                }
                performSearch();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentTemplateId = null;
            }
        });
    }
    
    /**
     * Set up the featured filter spinner.
     */
    private void setupFeaturedSpinner() {
        String[] featuredArray = {"All Articles", "Featured Only", "Non-Featured Only"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, featuredArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        featuredSpinner.setAdapter(adapter);
        
        featuredSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentFeatured = null;
                } else if (position == 1) {
                    currentFeatured = true;
                } else {
                    currentFeatured = false;
                }
                performSearch();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentFeatured = null;
            }
        });
    }
    
    /**
     * Show loading state.
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);
    }
    
    /**
     * Show content state.
     */
    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
    }
    
    /**
     * Show empty state.
     */
    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(R.string.no_search_results);
    }
    
    /**
     * Show error state.
     *
     * @param message Error message
     */
    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(getString(R.string.error_message, message));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE_ID, article.getId());
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(Article article, boolean isCurrentlyBookmarked) {
        Toast.makeText(this, 
                isCurrentlyBookmarked ? "Bookmarked" : "Unbookmarked", 
                Toast.LENGTH_SHORT).show();
    }
} 