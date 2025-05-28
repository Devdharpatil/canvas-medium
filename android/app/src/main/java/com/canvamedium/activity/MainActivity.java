package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.R;
import com.canvamedium.adapter.ArticleAdapter;
import com.canvamedium.model.Article;
import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.util.AuthManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that shows the article feed and provides navigation to other features.
 */
public class MainActivity extends AppCompatActivity implements ArticleAdapter.ArticleClickListener {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;
    
    private ArticleRepository articleRepository;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fab = findViewById(R.id.fabAdd);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleAdapter(this);
        recyclerView.setAdapter(adapter);

        // Initialize repositories and managers
        articleRepository = new ArticleRepository(getApplication());
        authManager = AuthManager.getInstance(this);

        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshArticles);

        // Set up FAB
        fab.setOnClickListener(view -> startCreateArticle());

        // Load initial data
        loadArticles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_bookmarks) {
            startActivity(new Intent(this, BookmarkedArticlesActivity.class));
            return true;
        } else if (id == R.id.action_categories) {
            startActivity(new Intent(this, CategoryBrowseActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadArticles() {
        swipeRefreshLayout.setRefreshing(true);
        
        // This is a simplified implementation
        // In a real app, you would observe LiveData from the repository
        // For now, just set some sample data
        List<Article> sampleArticles = new ArrayList<>();
        // Add sample articles
        
        adapter.setArticles(sampleArticles);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshArticles() {
        loadArticles();
    }

    private void startCreateArticle() {
        Intent intent = new Intent(this, TemplateListActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void logout() {
        authManager.logout();
        navigateToLogin();
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
        Toast.makeText(this, "Bookmark toggled", Toast.LENGTH_SHORT).show();
    }
} 