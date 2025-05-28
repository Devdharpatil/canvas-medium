package com.canvamedium;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Content;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentEditorActivity extends AppCompatActivity {
    
    private TextInputEditText editTitle, editDescription, editContent;
    private Button buttonSave;
    private ApiService apiService;
    private Content existingContent;
    private boolean isEditMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_editor);
        
        // Initialize views
        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editContent = findViewById(R.id.editContent);
        buttonSave = findViewById(R.id.buttonSave);
        
        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);
        
        // Check if we're in edit mode
        if (getIntent().hasExtra("content_id")) {
            isEditMode = true;
            Long contentId = getIntent().getLongExtra("content_id", -1);
            loadContent(contentId);
            
            // Set title
            setTitle("Edit Content");
        } else {
            setTitle("Create Content");
        }
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Set up save button
        buttonSave.setOnClickListener(v -> saveContent());
    }
    
    private void loadContent(Long contentId) {
        apiService.getContentById(contentId).enqueue(new Callback<Content>() {
            @Override
            public void onResponse(@NonNull Call<Content> call, @NonNull Response<Content> response) {
                if (response.isSuccessful() && response.body() != null) {
                    existingContent = response.body();
                    populateFields(existingContent);
                } else {
                    Toast.makeText(ContentEditorActivity.this, "Failed to load content", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Content> call, @NonNull Throwable t) {
                Toast.makeText(ContentEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateFields(Content content) {
        editTitle.setText(content.getTitle());
        editDescription.setText(content.getDescription());
        editContent.setText(content.getContent());
    }
    
    private void saveContent() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";
        String contentText = editContent.getText() != null ? editContent.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(title)) {
            editTitle.setError("Title is required");
            return;
        }
        
        if (TextUtils.isEmpty(contentText)) {
            editContent.setError("Content is required");
            return;
        }
        
        Content content = new Content(title, description, contentText);
        
        if (isEditMode && existingContent != null) {
            content.setId(existingContent.getId());
            updateContent(content);
        } else {
            createContent(content);
        }
    }
    
    private void createContent(Content content) {
        apiService.createContent(content).enqueue(new Callback<Content>() {
            @Override
            public void onResponse(@NonNull Call<Content> call, @NonNull Response<Content> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ContentEditorActivity.this, "Content created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ContentEditorActivity.this, "Failed to create content", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Content> call, @NonNull Throwable t) {
                Toast.makeText(ContentEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateContent(Content content) {
        apiService.updateContent(content.getId(), content).enqueue(new Callback<Content>() {
            @Override
            public void onResponse(@NonNull Call<Content> call, @NonNull Response<Content> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ContentEditorActivity.this, "Content updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ContentEditorActivity.this, "Failed to update content", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Content> call, @NonNull Throwable t) {
                Toast.makeText(ContentEditorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 