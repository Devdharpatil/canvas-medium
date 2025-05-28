package com.canvamedium.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.model.Article;
import com.canvamedium.viewmodel.ArticleViewModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

/**
 * Activity for displaying the details of an article.
 */
public class ArticleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    
    private ArticleViewModel articleViewModel;
    private Article article;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView imageHeader;
    private TextView textContent;
    private TextView textAuthor;
    private TextView textDate;
    private TextView textTemplateInfo;
    private View loadingView;
    private View errorView;
    private TextView textError;
    private MenuItem bookmarkMenuItem;
    
    /**
     * Create an intent to start this activity.
     *
     * @param context The context
     * @param article The article to display
     * @return The intent
     */
    public static Intent newIntent(Context context, Article article) {
        Intent intent = new Intent(context, ArticleDetailActivity.class);
        intent.putExtra(EXTRA_ARTICLE_ID, article.getId());
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        
        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        imageHeader = findViewById(R.id.image_header);
        textContent = findViewById(R.id.text_content);
        textAuthor = findViewById(R.id.text_author);
        textDate = findViewById(R.id.text_date);
        textTemplateInfo = findViewById(R.id.text_template_info);
        loadingView = findViewById(R.id.loading_view);
        errorView = findViewById(R.id.error_view);
        textError = findViewById(R.id.text_error);
        
        // Setup retry button
        findViewById(R.id.button_retry).setOnClickListener(v -> loadArticle());
        
        // Initialize ViewModel
        articleViewModel = new ViewModelProvider(this).get(ArticleViewModel.class);
        
        // Check if we have a valid article ID
        if (getIntent().hasExtra(EXTRA_ARTICLE_ID)) {
            long articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1);
            if (articleId != -1) {
                loadArticle(articleId);
            } else {
                showError(getString(R.string.error_loading_article));
            }
        } else {
            showError(getString(R.string.error_loading_article));
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article_detail, menu);
        bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
        updateBookmarkIcon();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_bookmark) {
            toggleBookmark();
            return true;
        } else if (id == R.id.action_share) {
            shareArticle();
            return true;
        } else if (id == R.id.action_edit) {
            editArticle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Loads the article by ID.
     */
    private void loadArticle() {
        if (getIntent().hasExtra(EXTRA_ARTICLE_ID)) {
            long articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1);
            if (articleId != -1) {
                loadArticle(articleId);
            } else {
                showError(getString(R.string.error_loading_article));
            }
        }
    }
    
    /**
     * Loads the article by ID.
     *
     * @param articleId The article ID
     */
    private void loadArticle(long articleId) {
        showLoading(true);
        
        articleViewModel.getArticleById(articleId).observe(this, result -> {
            showLoading(false);
            
            if (result != null) {
                article = result;
                displayArticle();
                updateBookmarkIcon();
            } else {
                showError(getString(R.string.error_loading_article));
            }
        });
    }
    
    /**
     * Displays the article content.
     */
    private void displayArticle() {
        collapsingToolbarLayout.setTitle(article.getTitle());
        
        // Set header image
        if (article.getThumbnailUrl() != null && !article.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(article.getThumbnailUrl())
                    .into(imageHeader);
        }
        
        // Set content
        textContent.setText(article.getPreviewText());
        
        // Set author
        if (article.getAuthorName() != null) {
            textAuthor.setText(article.getAuthorName());
            textAuthor.setVisibility(View.VISIBLE);
        } else {
            textAuthor.setVisibility(View.GONE);
        }
        
        // Set date
        if (article.getPublishedAt() != null) {
            textDate.setText(article.getPublishedAt());
            textDate.setVisibility(View.VISIBLE);
        } else {
            textDate.setVisibility(View.GONE);
        }
        
        // Set template info
        if (article.getTemplate() != null) {
            textTemplateInfo.setText(getString(R.string.template_name_format, article.getTemplate().getName()));
            textTemplateInfo.setVisibility(View.VISIBLE);
        } else {
            textTemplateInfo.setText(R.string.template_unknown);
            textTemplateInfo.setVisibility(View.VISIBLE);
        }
        
        errorView.setVisibility(View.GONE);
    }
    
    /**
     * Shows or hides the loading indicator.
     *
     * @param show Whether to show the loading indicator
     */
    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Shows an error message.
     *
     * @param message The error message
     */
    private void showError(String message) {
        errorView.setVisibility(View.VISIBLE);
        textError.setText(message);
        loadingView.setVisibility(View.GONE);
    }
    
    /**
     * Updates the bookmark icon based on the article's bookmarked status.
     */
    private void updateBookmarkIcon() {
        if (bookmarkMenuItem != null && article != null) {
            bookmarkMenuItem.setIcon(article.isBookmarked() ? 
                    R.drawable.ic_bookmark : 
                    R.drawable.ic_bookmark_border);
        }
    }
    
    /**
     * Toggles the bookmark status of the article.
     */
    private void toggleBookmark() {
        if (article != null) {
            boolean newBookmarkState = !article.isBookmarked();
            
            articleViewModel.updateBookmark(article.getId(), newBookmarkState)
                    .observe(this, success -> {
                        if (success) {
                            article.setBookmarked(newBookmarkState);
                            updateBookmarkIcon();
                            
                            String message = newBookmarkState ? 
                                    "Article added to bookmarks" : 
                                    "Article removed from bookmarks";
                            Snackbar.make(collapsingToolbarLayout, message, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(collapsingToolbarLayout, "Failed to update bookmark", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    
    /**
     * Shares the article.
     */
    private void shareArticle() {
        if (article != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    getString(R.string.share_article_text, article.getTitle(), article.getPreviewText()));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)));
        }
    }
    
    /**
     * Opens the article editor for this article.
     */
    private void editArticle() {
        if (article != null) {
            Intent intent = new Intent(this, ArticleEditorActivity.class);
            intent.putExtra(ArticleEditorActivity.EXTRA_ARTICLE_ID, article.getId());
            startActivity(intent);
        }
    }
} 