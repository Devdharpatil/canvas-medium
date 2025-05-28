package com.canvamedium.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Template;
import com.canvamedium.model.TemplateElement;
import com.canvamedium.util.ImagePickerUtil;
import com.canvamedium.util.ImageResizeUtil;
import com.canvamedium.util.MediaUploadUtil;
import com.canvamedium.util.TemplateUtil;
import com.canvamedium.view.CropOverlayView;
import com.canvamedium.view.DraggableElementView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for creating and editing templates with a drag-and-drop interface.
 */
public class TemplateBuilderActivity extends AppCompatActivity implements ImagePickerUtil.ImagePickedListener {

    private static final int REQUEST_PERMISSIONS = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private FrameLayout canvasView;
    private Template currentTemplate;
    private ApiService apiService;
    private Long templateId;
    private Map<String, DraggableElementView> elementViews = new HashMap<>();
    private ImagePickerUtil imagePickerUtil;
    private String currentImageElementId;
    private boolean isUploadingImage = false;
    private Bitmap currentImageBitmap;

    private ActivityResultLauncher<String[]> permissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_builder);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        apiService = ApiClient.getClient().create(ApiService.class);
        
        // Initialize views
        canvasView = findViewById(R.id.canvas);
        FloatingActionButton fabSave = findViewById(R.id.fab_save);
        
        // Setup image picker
        imagePickerUtil = new ImagePickerUtil(this, this);
        
        // Setup permissions launcher
        setupPermissionsLauncher();
        
        // Setup element palette
        setupElementPalette();
        
        // Setup save button
        fabSave.setOnClickListener(v -> showSaveDialog());
        
        // Check if we're editing an existing template
        templateId = getIntent().getLongExtra("template_id", -1);
        if (templateId != -1) {
            loadTemplate(templateId);
            setTitle("Edit Template");
        } else {
            // Create a new template
            currentTemplate = new Template("New Template", TemplateUtil.createEmptyLayout());
            setTitle("New Template");
        }
    }

    /**
     * Sets up the permission launcher for camera and storage permissions.
     */
    private void setupPermissionsLauncher() {
        permissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (String permission : REQUIRED_PERMISSIONS) {
                        if (result.get(permission) == null || !result.get(permission)) {
                            allGranted = false;
                            break;
                        }
                    }
                    
                    if (allGranted) {
                        showImagePickerDialog();
                    } else {
                        Toast.makeText(this, "Permissions are required to use this feature", 
                                Toast.LENGTH_SHORT).show();
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
    
    /**
     * Sets up the element palette with drag-and-drop functionality.
     */
    private void setupElementPalette() {
        LinearLayout textElement = findViewById(R.id.text_element);
        LinearLayout imageElement = findViewById(R.id.image_element);
        LinearLayout headerElement = findViewById(R.id.header_element);
        LinearLayout dividerElement = findViewById(R.id.divider_element);
        LinearLayout quoteElement = findViewById(R.id.quote_element);
        
        // Text element
        textElement.setOnClickListener(v -> addElementToCanvas(Template.ELEMENT_TYPE_TEXT));
        
        // Image element
        imageElement.setOnClickListener(v -> {
            if (hasPermissions()) {
                addElementToCanvas(Template.ELEMENT_TYPE_IMAGE);
            } else {
                requestPermissions();
            }
        });
        
        // Header element
        headerElement.setOnClickListener(v -> addElementToCanvas(Template.ELEMENT_TYPE_HEADER));
        
        // Divider element
        dividerElement.setOnClickListener(v -> addElementToCanvas(Template.ELEMENT_TYPE_DIVIDER));
        
        // Quote element
        quoteElement.setOnClickListener(v -> addElementToCanvas(Template.ELEMENT_TYPE_QUOTE));
    }

    /**
     * Checks if we have the required permissions.
     *
     * @return true if all permissions are granted, false otherwise
     */
    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests the required permissions.
     */
    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    /**
     * Shows the image picker dialog.
     */
    private void showImagePickerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_picker, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.layout_camera).setOnClickListener(v -> {
            imagePickerUtil.pickFromCamera();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.layout_gallery).setOnClickListener(v -> {
            imagePickerUtil.pickFromGallery();
            dialog.dismiss();
        });

        dialog.show();
    }
    
    /**
     * Adds a new element to the canvas.
     *
     * @param elementType The type of element to add
     */
    private void addElementToCanvas(String elementType) {
        // Calculate default size based on element type
        int width = 300;
        int height = 150;
        
        if (elementType.equals(Template.ELEMENT_TYPE_IMAGE)) {
            width = 400;
            height = 300;
        } else if (elementType.equals(Template.ELEMENT_TYPE_DIVIDER)) {
            width = 400;
            height = 50;
        } else if (elementType.equals(Template.ELEMENT_TYPE_HEADER)) {
            width = 400;
            height = 100;
        }
        
        // Create element
        TemplateElement element = new TemplateElement(elementType, 50, 50, width, height);
        
        // Add default properties based on type
        if (elementType.equals(Template.ELEMENT_TYPE_TEXT)) {
            element.addProperty("text", "Double tap to edit text");
        } else if (elementType.equals(Template.ELEMENT_TYPE_HEADER)) {
            element.addProperty("text", "Header Title");
        } else if (elementType.equals(Template.ELEMENT_TYPE_QUOTE)) {
            element.addProperty("text", "Double tap to edit quote");
        } else if (elementType.equals(Template.ELEMENT_TYPE_IMAGE)) {
            currentImageElementId = element.getId();
            element.addProperty("placeholder", true);
            showImagePickerDialog();
        }
        
        // Add element to template
        currentTemplate = TemplateUtil.addElement(currentTemplate, element);
        
        // Add element view to canvas
        addElementViewToCanvas(element);
    }
    
    /**
     * Creates and adds a draggable element view to the canvas.
     *
     * @param element The TemplateElement to add
     */
    private void addElementViewToCanvas(TemplateElement element) {
        DraggableElementView elementView = new DraggableElementView(this);
        elementView.setElement(element);
        
        // Set a position change listener
        elementView.setOnPositionChangedListener((view, updatedElement) -> {
            // Update element in template
            currentTemplate = TemplateUtil.updateElement(currentTemplate, updatedElement.getId(), updatedElement);
        });
        
        // Add to canvas
        canvasView.addView(elementView);
        
        // Store view reference
        elementViews.put(element.getId(), elementView);
    }

    /**
     * Handles the result of image picking.
     *
     * @param imageUri The URI of the picked image
     * @param bitmap   The bitmap of the picked image
     */
    @Override
    public void onImagePicked(Uri imageUri, Bitmap bitmap) {
        // Store the bitmap for cropping
        currentImageBitmap = bitmap;
        
        // Show the image crop dialog
        showImageCropDialog();
    }
    
    /**
     * Shows the image crop dialog.
     */
    private void showImageCropDialog() {
        if (currentImageBitmap == null) return;
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_crop, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        ImageView imagePreview = dialogView.findViewById(R.id.image_preview);
        CropOverlayView cropOverlay = dialogView.findViewById(R.id.crop_overlay);
        SeekBar resizeSeekbar = dialogView.findViewById(R.id.resize_seekbar);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
        Button buttonCrop = dialogView.findViewById(R.id.button_crop);
        
        // Set the image preview
        imagePreview.setImageBitmap(currentImageBitmap);
        
        // Set resize seekbar listener
        final Bitmap[] resizedBitmap = {currentImageBitmap};
        resizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress > 0) {
                    float scale = progress / 100f;
                    int width = Math.round(currentImageBitmap.getWidth() * scale);
                    int height = Math.round(currentImageBitmap.getHeight() * scale);
                    
                    resizedBitmap[0] = ImageResizeUtil.resizeBitmap(currentImageBitmap, width, height);
                    imagePreview.setImageBitmap(resizedBitmap[0]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Set button listeners
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        
        buttonCrop.setOnClickListener(v -> {
            // Get the crop rectangle
            float[] cropRect = cropOverlay.getCropRect();
            
            // Apply the crop
            int cropX = Math.round(cropRect[0] * resizedBitmap[0].getWidth());
            int cropY = Math.round(cropRect[1] * resizedBitmap[0].getHeight());
            int cropWidth = Math.round((cropRect[2] - cropRect[0]) * resizedBitmap[0].getWidth());
            int cropHeight = Math.round((cropRect[3] - cropRect[1]) * resizedBitmap[0].getHeight());
            
            Bitmap croppedBitmap = ImageResizeUtil.cropBitmap(resizedBitmap[0], cropX, cropY, cropWidth, cropHeight);
            
            // Also generate a thumbnail
            Bitmap thumbnailBitmap = ImageResizeUtil.createThumbnail(croppedBitmap);
            
            // Upload the cropped image
            uploadImage(croppedBitmap);
            
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    /**
     * Uploads an image to the server with thumbnail generation.
     * 
     * @param bitmap The bitmap to upload
     */
    private void uploadImage(Bitmap bitmap) {
        // Mark that we're uploading an image
        isUploadingImage = true;
        
        // Show a loading toast
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        
        // Upload the image to the server with thumbnail generation
        MediaUploadUtil.uploadBitmapWithThumbnail(this, bitmap, new MediaUploadUtil.UploadWithThumbnailCallback() {
            @Override
            public void onSuccess(String fileUrl, String thumbnailUrl) {
                // Process the uploaded image
                processUploadedImageWithThumbnail(fileUrl, thumbnailUrl);
                isUploadingImage = false;
            }

            @Override
            public void onFailure(String errorMessage) {
                // Show error message
                Toast.makeText(TemplateBuilderActivity.this, 
                        "Failed to upload image: " + errorMessage, Toast.LENGTH_SHORT).show();
                isUploadingImage = false;
            }
        });
    }
    
    /**
     * Processes an uploaded image by updating the element with the file URL.
     *
     * @param fileUrl The URL of the uploaded image
     */
    private void processUploadedImage(String fileUrl) {
        // Find the current image element
        if (currentImageElementId != null) {
            TemplateElement element = null;
            for (TemplateElement e : TemplateUtil.extractElements(currentTemplate)) {
                if (e.getId().equals(currentImageElementId)) {
                    element = e;
                    break;
                }
            }

            if (element != null) {
                // Update element with image URL
                element.addProperty("imageUri", fileUrl);
                element.addProperty("placeholder", false);
                
                // Update the template
                currentTemplate = TemplateUtil.updateElement(currentTemplate, element.getId(), element);
                
                // Update the view
                DraggableElementView view = elementViews.get(element.getId());
                if (view != null) {
                    view.setElement(element);
                }
                
                // Show success message
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Reset current image element ID
        currentImageElementId = null;
    }
    
    /**
     * Processes an uploaded image with thumbnail by updating the element with the file URL and thumbnail.
     *
     * @param fileUrl The URL of the uploaded image
     * @param thumbnailUrl The URL of the thumbnail image
     */
    private void processUploadedImageWithThumbnail(String fileUrl, String thumbnailUrl) {
        // Find the current image element
        if (currentImageElementId != null) {
            TemplateElement element = null;
            for (TemplateElement e : TemplateUtil.extractElements(currentTemplate)) {
                if (e.getId().equals(currentImageElementId)) {
                    element = e;
                    break;
                }
            }

            if (element != null) {
                // Update element with image URL and thumbnail URL
                element.addProperty("imageUri", fileUrl);
                element.addProperty("thumbnailUri", thumbnailUrl);
                element.addProperty("placeholder", false);
                
                // Update the template
                currentTemplate = TemplateUtil.updateElement(currentTemplate, element.getId(), element);
                
                // Update the view
                DraggableElementView view = elementViews.get(element.getId());
                if (view != null) {
                    view.setElement(element);
                }
                
                // Also update the template thumbnail if it doesn't have one yet
                if (currentTemplate.getThumbnailUrl() == null || currentTemplate.getThumbnailUrl().isEmpty()) {
                    currentTemplate.setThumbnailUrl(thumbnailUrl);
                }
                
                // Show success message
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Reset current image element ID
        currentImageElementId = null;
    }
    
    /**
     * Loads an existing template from the API.
     *
     * @param templateId The ID of the template to load
     */
    private void loadTemplate(Long templateId) {
        apiService.getTemplateById(templateId).enqueue(new Callback<Template>() {
            @Override
            public void onResponse(Call<Template> call, Response<Template> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentTemplate = response.body();
                    displayTemplate();
                } else {
                    Toast.makeText(TemplateBuilderActivity.this, 
                            "Failed to load template", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Template> call, Throwable t) {
                Toast.makeText(TemplateBuilderActivity.this,
                        "Error loading template: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Displays the current template on the canvas.
     */
    private void displayTemplate() {
        // Clear current elements
        canvasView.removeAllViews();
        elementViews.clear();
        
        // Extract elements from template
        List<TemplateElement> elements = TemplateUtil.extractElements(currentTemplate);
        
        // Add each element to canvas
        for (TemplateElement element : elements) {
            addElementViewToCanvas(element);
        }
    }
    
    /**
     * Shows a dialog to save the template.
     */
    private void showSaveDialog() {
        // Don't allow saving while uploading images
        if (isUploadingImage) {
            Toast.makeText(this, "Please wait for image uploads to complete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_template, null);
        EditText nameInput = dialogView.findViewById(R.id.template_name);
        EditText descriptionInput = dialogView.findViewById(R.id.template_description);
        
        // Pre-fill with existing data if editing
        if (currentTemplate.getName() != null) {
            nameInput.setText(currentTemplate.getName());
        }
        if (currentTemplate.getDescription() != null) {
            descriptionInput.setText(currentTemplate.getDescription());
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Save Template")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter a template name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    currentTemplate.setName(name);
                    currentTemplate.setDescription(description);
                    saveTemplate();
                })
                .setNegativeButton("Cancel", null);
        
        builder.create().show();
    }
    
    /**
     * Saves the current template to the API.
     */
    private void saveTemplate() {
        Call<Template> call;
        
        if (templateId != -1) {
            // Update existing template
            call = apiService.updateTemplate(templateId, currentTemplate);
        } else {
            // Create new template
            call = apiService.createTemplate(currentTemplate);
        }
        
        call.enqueue(new Callback<Template>() {
            @Override
            public void onResponse(Call<Template> call, Response<Template> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(TemplateBuilderActivity.this, 
                            "Template saved successfully", Toast.LENGTH_SHORT).show();
                    // Update current template with saved version
                    currentTemplate = response.body();
                    templateId = currentTemplate.getId();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(TemplateBuilderActivity.this,
                            "Failed to save template", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Template> call, Throwable t) {
                Toast.makeText(TemplateBuilderActivity.this,
                        "Error saving template: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 