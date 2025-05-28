package com.canvamedium;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.canvamedium.model.Article;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying the details of an article.
 */
public class ArticleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICLE = "extra_article";
    
    private Article article;
    private TextView textArticleTitle;
    private TextView textTemplateName;
    private TextView textDate;
    private TextView textArticleContent;
    private ImageView imageArticleHeader;
    private CollapsingToolbarLayout collapsingToolbar;
    
    /**
     * Creates an intent to launch this activity with the specified article.
     *
     * @param context The context to create the intent from
     * @param article The article to display
     * @return An intent to launch this activity
     */
    public static Intent newIntent(Context context, Article article) {
        Intent intent = new Intent(context, ArticleDetailActivity.class);
        intent.putExtra(EXTRA_ARTICLE, new Gson().toJson(article));
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        
        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Get article from intent
        getArticleFromIntent();
        
        // Populate UI with article data
        if (article != null) {
            displayArticle();
        } else {
            showError();
        }
        
        // Setup share button
        setupShareButton();
    }
    
    /**
     * Initializes the views.
     */
    private void initializeViews() {
        textArticleTitle = findViewById(R.id.text_content);
        textTemplateName = findViewById(R.id.text_template_info);
        textDate = findViewById(R.id.text_date);
        textArticleContent = findViewById(R.id.text_content);
        imageArticleHeader = findViewById(R.id.image_header);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
    }
    
    /**
     * Sets up the toolbar.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }
    
    /**
     * Gets the article from the intent.
     */
    private void getArticleFromIntent() {
        String articleJson = getIntent().getStringExtra(EXTRA_ARTICLE);
        if (articleJson != null) {
            article = new Gson().fromJson(articleJson, Article.class);
        }
    }
    
    /**
     * Displays the article.
     */
    private void displayArticle() {
        // Set collapsing toolbar title
        collapsingToolbar.setTitle(article.getTitle());
        
        // Set article title
        textArticleTitle.setText(article.getTitle());
        
        // Set template name
        if (article.getTemplate() != null) {
            textTemplateName.setText(getString(R.string.template_name_format, article.getTemplate().getName()));
        } else {
            textTemplateName.setText(getString(R.string.template_unknown));
        }
        
        // Format and set date
        String formattedDate = formatDate(article.getCreatedAt());
        textDate.setText(formattedDate);
        
        // Set article content
        displayArticleContent();
        
        // Load header image
        loadHeaderImage();
    }
    
    /**
     * Displays the article content.
     */
    private void displayArticleContent() {
        // In a real app, we would render the article content based on the template
        // For now, we'll just display the preview text
        StringBuilder contentBuilder = new StringBuilder();
        
        if (article.getPreviewText() != null && !article.getPreviewText().isEmpty()) {
            contentBuilder.append(article.getPreviewText());
            contentBuilder.append("\n\n");
        }
        
        // If we have a content JSON object, try to extract some text from it
        if (article.getContent() != null) {
            JsonObject content = article.getContent();
            // For demonstration purposes, we'll assume content has a "text" field
            // In a real app, we would parse and render the content according to the template structure
            if (content.has("text")) {
                contentBuilder.append(content.get("text").getAsString());
            } else {
                // If no text field, just append a placeholder
                contentBuilder.append(getString(R.string.placeholder_content));
            }
        } else {
            contentBuilder.append(getString(R.string.placeholder_content));
        }
        
        textArticleContent.setText(contentBuilder.toString());
    }
    
    /**
     * Loads the header image.
     */
    private void loadHeaderImage() {
        if (article.getThumbnailUrl() != null && !article.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(article.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageArticleHeader);
        } else {
            imageArticleHeader.setImageResource(R.drawable.placeholder_image);
        }
    }
    
    /**
     * Shows an error message.
     */
    private void showError() {
        Toast.makeText(this, R.string.error_loading_article, Toast.LENGTH_SHORT).show();
        finish();
    }
    
    /**
     * Sets up the share button.
     */
    private void setupShareButton() {
        FloatingActionButton fabShare = findViewById(R.id.fab_share);
        fabShare.setOnClickListener(v -> shareArticle());
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
     * Formats the date string from ISO format to a more readable format.
     *
     * @param dateString ISO format date string
     * @return Formatted date string
     */
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return getString(R.string.unknown_date);
        }
        
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            
            Date date = inputDateFormat.parse(dateString);
            return date != null ? outputDateFormat.format(date) : getString(R.string.unknown_date);
        } catch (ParseException e) {
            return getString(R.string.unknown_date);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 