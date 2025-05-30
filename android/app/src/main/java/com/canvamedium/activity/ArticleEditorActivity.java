package com.canvamedium.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.adapter.TemplateSelectionAdapter;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Article;
import com.canvamedium.model.Template;
import com.canvamedium.model.TemplateElement;
import com.canvamedium.util.ContentBuilder;
import com.canvamedium.util.ImageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for creating and editing articles based on templates.
 */
public class ArticleEditorActivity extends AppCompatActivity implements TemplateSelectionAdapter.OnTemplateSelectedListener {

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_CONTENT_IMAGE = 101;
    
    // Intent extra keys
    public static final String EXTRA_ARTICLE_ID = "EXTRA_ARTICLE_ID";
    
    private TextInputEditText titleEditText;
    private TextInputEditText previewTextEditText;
    private ImageView thumbnailImageView;
    private Button selectThumbnailButton;
    private RecyclerView templateRecyclerView;
    private TextView noTemplateText;
    private LinearLayout contentContainer;
    private Button saveDraftButton;
    private Button previewButton;
    private Button publishButton;
    
    private TemplateSelectionAdapter templateAdapter;
    private ApiService apiService;
    private String thumbnailUrl;
    private Template selectedTemplate;
    private ContentBuilder contentBuilder;
    private Article editingArticle;
    private boolean isEditMode = false;
    
    // Map to store content element views by their ID for easy access
    private Map<String, View> contentElementViews = new HashMap<>();
    
    // Current image element being edited
    private ImageView currentImageElement;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_editor);
        
        // Initialize API service
        apiService = ApiClient.createAuthenticatedService(ApiService.class, this);
        
        // Initialize content builder
        contentBuilder = new ContentBuilder();
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize views
        titleEditText = findViewById(R.id.title_edit_text);
        previewTextEditText = findViewById(R.id.preview_text_edit_text);
        thumbnailImageView = findViewById(R.id.thumbnail_image_view);
        selectThumbnailButton = findViewById(R.id.select_thumbnail_button);
        templateRecyclerView = findViewById(R.id.template_recycler_view);
        noTemplateText = findViewById(R.id.no_template_text);
        contentContainer = findViewById(R.id.content_container);
        saveDraftButton = findViewById(R.id.save_draft_button);
        previewButton = findViewById(R.id.preview_button);
        publishButton = findViewById(R.id.publish_button);
        
        // Set up template adapter
        templateAdapter = new TemplateSelectionAdapter(this, this);
        templateRecyclerView.setAdapter(templateAdapter);
        
        // Check mode and source of navigation
        String mode = getIntent().getStringExtra("mode");
        String source = getIntent().getStringExtra("source");
        
        // Check if we're editing an existing article
        if (getIntent().hasExtra(EXTRA_ARTICLE_ID)) {
            // Load the article with the given ID
            long articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1);
            if (articleId != -1) {
                isEditMode = true;
                setTitle("Edit Article");
                loadArticleById(articleId);
            }
        } else if (getIntent().hasExtra("article")) {
            editingArticle = (Article) getIntent().getSerializableExtra("article");
            isEditMode = true;
            setTitle("Edit Article");
            loadArticleData(editingArticle);
        } else if ("create_new".equals(mode) && "fab_home".equals(source)) {
            // Coming from FAB, focus on title and show special welcome message
            setTitle("Create Article");
            titleEditText.requestFocus();
            Toast.makeText(this, "Create your article by selecting a template first", Toast.LENGTH_LONG).show();
            // Pre-populate with timestamp to help user
            String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            titleEditText.setText("Draft Article - " + timestamp);
        } else {
            // Default create mode
            setTitle("Create Article");
        }
        
        // Initialize click listeners
        selectThumbnailButton.setOnClickListener(v -> selectThumbnailImage());
        
        saveDraftButton.setOnClickListener(v -> saveArticleAsDraft());
        
        previewButton.setOnClickListener(v -> previewArticle());
        
        publishButton.setOnClickListener(v -> publishArticle());
        
        // Load templates
        loadTemplates();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article_editor, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showDiscardChangesDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        showDiscardChangesDialog();
    }
    
    /**
     * Shows a dialog confirming whether to discard changes.
     */
    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard your changes?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Loads the list of templates from the API.
     */
    private void loadTemplates() {
        Map<String, Object> options = new HashMap<>();
        Call<Map<String, Object>> call = apiService.getTemplates(options);
        
        call.enqueue(new Callback<Map<String, Object>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    List<Template> templates = (List<Template>) data.get("content");
                    templateAdapter.setTemplates(templates);
                    
                    // If editing an article and it has a template, select it
                    if (isEditMode && editingArticle.getTemplateId() != null) {
                        for (int i = 0; i < templates.size(); i++) {
                            if (templates.get(i).getId().equals(editingArticle.getTemplateId())) {
                                templateAdapter.setSelectedPosition(i);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(ArticleEditorActivity.this, "Failed to load templates", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ArticleEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Called when a template is selected from the RecyclerView.
     *
     * @param template The selected template
     */
    @Override
    public void onTemplateSelected(Template template) {
        selectedTemplate = template;
        publishButton.setEnabled(true);
        
        // Clear previous content
        contentContainer.removeAllViews();
        contentElementViews.clear();
        
        // Show content container and hide no template message
        noTemplateText.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        
        // Create content elements based on template layout
        if (template.getLayout() != null) {
            createContentElements(template.getLayout());
        }
    }
    
    /**
     * Creates content editing elements based on the template layout.
     *
     * @param layout The template layout as a JsonObject
     */
    private void createContentElements(JsonObject layout) {
        // Implementation depends on your template structure
        // This is a simplified example that assumes layout has elements array
        if (layout.has("elements") && layout.get("elements").isJsonArray()) {
            contentBuilder.buildEditorFromTemplate(layout, contentContainer, this, this::handleContentImageClick);
            
            // If editing, populate content from article
            if (isEditMode && editingArticle.getContent() != null) {
                contentBuilder.populateEditorFromContent(editingArticle.getContent(), contentContainer);
            }
        }
    }
    
    /**
     * Handles clicks on image elements in the content.
     *
     * @param imageView The clicked ImageView
     */
    private void handleContentImageClick(ImageView imageView) {
        currentImageElement = imageView;
        
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CONTENT_IMAGE);
    }
    
    /**
     * Opens the image picker to select a thumbnail image.
     */
    private void selectThumbnailImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                // Handle thumbnail image selection
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Show loading state
                    selectThumbnailButton.setEnabled(false);
                    selectThumbnailButton.setText("Uploading...");
                    
                    // Upload thumbnail image
                    uploadImage(imageUri, true);
                }
            } else if (requestCode == REQUEST_CONTENT_IMAGE && currentImageElement != null) {
                // Handle content image selection
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Show loading state in the image
                    currentImageElement.setImageResource(R.drawable.ic_image_loading);
                    
                    // Upload content image
                    uploadImage(imageUri, false);
                }
            }
        }
    }
    
    /**
     * Uploads an image to the server.
     *
     * @param imageUri The URI of the image to upload
     * @param isThumbnail Whether this is a thumbnail image
     */
    private void uploadImage(Uri imageUri, boolean isThumbnail) {
        try {
            // Convert URI to File
            File imageFile = ImageUtils.getFileFromUri(this, imageUri);
            
            // Create request body for the file
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
            
            // Call the appropriate upload method
            Call<Map<String, String>> call;
            if (isThumbnail) {
                call = apiService.uploadFileWithThumbnail(filePart);
            } else {
                call = apiService.uploadFile(filePart);
            }
            
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body().get("url");
                        
                        if (isThumbnail) {
                            // Store thumbnail URL and update UI
                            thumbnailUrl = imageUrl;
                            Glide.with(ArticleEditorActivity.this)
                                    .load(imageUrl)
                                    .centerCrop()
                                    .into(thumbnailImageView);
                            
                            // Reset button state
                            selectThumbnailButton.setEnabled(true);
                            selectThumbnailButton.setText("Change Image");
                        } else if (currentImageElement != null) {
                            // Update content image and store URL in tag
                            currentImageElement.setTag(imageUrl);
                            Glide.with(ArticleEditorActivity.this)
                                    .load(imageUrl)
                                    .centerCrop()
                                    .into(currentImageElement);
                        }
                    } else {
                        Toast.makeText(ArticleEditorActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        resetUploadState(isThumbnail);
                    }
                }
                
                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(ArticleEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    resetUploadState(isThumbnail);
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            resetUploadState(isThumbnail);
        }
    }
    
    /**
     * Resets the upload state UI elements.
     *
     * @param isThumbnail Whether this is a thumbnail image
     */
    private void resetUploadState(boolean isThumbnail) {
        if (isThumbnail) {
            selectThumbnailButton.setEnabled(true);
            selectThumbnailButton.setText("Select Image");
        } else if (currentImageElement != null) {
            currentImageElement.setImageResource(R.drawable.ic_image_placeholder);
        }
    }
    
    /**
     * Loads article data into the editor when editing an existing article.
     *
     * @param article The article to edit
     */
    private void loadArticleData(Article article) {
        titleEditText.setText(article.getTitle());
        previewTextEditText.setText(article.getPreviewText());
        
        // Load thumbnail if available
        if (article.getThumbnailUrl() != null && !article.getThumbnailUrl().isEmpty()) {
            thumbnailUrl = article.getThumbnailUrl();
            Glide.with(this)
                    .load(thumbnailUrl)
                    .centerCrop()
                    .into(thumbnailImageView);
            selectThumbnailButton.setText("Change Image");
        }
        
        // Enable publish button if published or has template
        if (article.isPublished() || article.getTemplateId() != null) {
            publishButton.setEnabled(true);
        }
    }
    
    /**
     * Collects the article data from the UI elements.
     *
     * @return A new Article object with the entered data
     */
    private Article collectArticleData() {
        String title = titleEditText.getText().toString().trim();
        String previewText = previewTextEditText.getText().toString().trim();
        
        // Collect content from the content elements
        JsonObject content = contentBuilder.buildContentFromEditor(contentContainer);
        
        // Create new article or update existing one
        Article article;
        if (isEditMode) {
            article = editingArticle;
            article.setTitle(title);
            article.setPreviewText(previewText);
            article.setContent(content);
            if (thumbnailUrl != null) {
                article.setThumbnailUrl(thumbnailUrl);
            }
        } else {
            article = new Article(title, content, previewText, thumbnailUrl, 
                    selectedTemplate != null ? selectedTemplate.getId() : null);
        }
        
        return article;
    }
    
    /**
     * Validates the article data before saving or publishing.
     *
     * @return True if the data is valid, false otherwise
     */
    private boolean validateArticleData() {
        String title = titleEditText.getText().toString().trim();
        String previewText = previewTextEditText.getText().toString().trim();
        
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            titleEditText.requestFocus();
            return false;
        }
        
        if (previewText.isEmpty()) {
            previewTextEditText.setError("Preview text is required");
            previewTextEditText.requestFocus();
            return false;
        }
        
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            Toast.makeText(this, "Please select a thumbnail image", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedTemplate == null) {
            Toast.makeText(this, "Please select a template", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Additional validation for content elements could be added here
        
        return true;
    }
    
    /**
     * Saves the article as a draft.
     */
    private void saveArticleAsDraft() {
        // Minimal validation for drafts
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            titleEditText.requestFocus();
            return;
        }
        
        // Collect data
        Article article = collectArticleData();
        article.setStatus(Article.STATUS_DRAFT);
        
        // Create or update draft article
        Call<Article> call;
        if (isEditMode) {
            call = apiService.updateArticle(article.getId(), article);
        } else {
            call = apiService.createDraftArticle(article);
        }
        
        call.enqueue(new Callback<Article>() {
            @Override
            public void onResponse(Call<Article> call, Response<Article> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ArticleEditorActivity.this, "Draft saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ArticleEditorActivity.this, "Failed to save draft", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Article> call, Throwable t) {
                Toast.makeText(ArticleEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Shows a preview of the article.
     */
    private void previewArticle() {
        if (!validateArticleData()) {
            return;
        }
        
        // Create a temporary article for preview
        Article previewArticle = collectArticleData();
        
        // Open ArticleDetailActivity in preview mode
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("article", previewArticle);
        intent.putExtra("preview_mode", true);
        startActivity(intent);
    }
    
    /**
     * Publishes the article.
     */
    private void publishArticle() {
        if (!validateArticleData()) {
            return;
        }
        
        // Collect data
        Article article = collectArticleData();
        
        // If it's a new article, create it first, then publish
        if (!isEditMode) {
            Call<Article> createCall = apiService.createDraftArticle(article);
            
            createCall.enqueue(new Callback<Article>() {
                @Override
                public void onResponse(Call<Article> call, Response<Article> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Article createdArticle = response.body();
                        publishCreatedArticle(createdArticle.getId());
                    } else {
                        Toast.makeText(ArticleEditorActivity.this, "Failed to create article", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<Article> call, Throwable t) {
                    Toast.makeText(ArticleEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Update the article first, then publish
            Call<Article> updateCall = apiService.updateArticle(article.getId(), article);
            
            updateCall.enqueue(new Callback<Article>() {
                @Override
                public void onResponse(Call<Article> call, Response<Article> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Article updatedArticle = response.body();
                        publishCreatedArticle(updatedArticle.getId());
                    } else {
                        Toast.makeText(ArticleEditorActivity.this, "Failed to update article", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<Article> call, Throwable t) {
                    Toast.makeText(ArticleEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Publishes a created or updated article.
     *
     * @param articleId The ID of the article to publish
     */
    private void publishCreatedArticle(Long articleId) {
        Call<Article> publishCall = apiService.publishArticle(articleId);
        
        publishCall.enqueue(new Callback<Article>() {
            @Override
            public void onResponse(Call<Article> call, Response<Article> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ArticleEditorActivity.this, "Article published successfully", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ArticleEditorActivity.this, "Failed to publish article", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Article> call, Throwable t) {
                Toast.makeText(ArticleEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Gets the thumbnail URL.
     *
     * @return The thumbnail URL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    /**
     * Sets the thumbnail URL.
     *
     * @param thumbnailUrl The thumbnail URL to set
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    /**
     * Gets the selected template.
     *
     * @return The selected template
     */
    public Template getSelectedTemplate() {
        return selectedTemplate;
    }
    
    /**
     * Sets the selected template.
     *
     * @param selectedTemplate The template to set as selected
     */
    public void setSelectedTemplate(Template selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }
    
    /**
     * Checks if the activity is in edit mode.
     *
     * @return True if editing an existing article, false if creating a new one
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    /**
     * Loads an article by its ID from the API.
     * 
     * @param articleId The ID of the article to load
     */
    private void loadArticleById(long articleId) {
        // Show loading indicator
        View loadingIndicator = findViewById(R.id.loading_indicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }
        
        // Disable UI while loading
        setUiEnabled(false);
        
        // Call API to get article details
        apiService.getArticleById(articleId).enqueue(new Callback<Article>() {
            @Override
            public void onResponse(Call<Article> call, Response<Article> response) {
                // Hide loading indicator
                if (loadingIndicator != null) {
                    loadingIndicator.setVisibility(View.GONE);
                }
                
                // Re-enable UI
                setUiEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    editingArticle = response.body();
                    loadArticleData(editingArticle);
                } else {
                    Toast.makeText(ArticleEditorActivity.this, 
                        "Failed to load article", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<Article> call, Throwable t) {
                // Hide loading indicator
                if (loadingIndicator != null) {
                    loadingIndicator.setVisibility(View.GONE);
                }
                
                // Re-enable UI
                setUiEnabled(true);
                
                Toast.makeText(ArticleEditorActivity.this, 
                    "Error loading article: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Helper method to enable/disable all input UI elements
     */
    private void setUiEnabled(boolean enabled) {
        titleEditText.setEnabled(enabled);
        previewTextEditText.setEnabled(enabled);
        selectThumbnailButton.setEnabled(enabled);
        saveDraftButton.setEnabled(enabled);
        previewButton.setEnabled(enabled);
        publishButton.setEnabled(enabled);
        // Disable/enable template selection only if we're not in edit mode
        if (!isEditMode) {
            templateRecyclerView.setEnabled(enabled);
        }
    }
} 