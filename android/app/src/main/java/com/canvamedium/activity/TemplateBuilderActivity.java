package com.canvamedium.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.view.Gravity;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.TextView;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

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
    // Add debounce constants
    private static final int TOUCH_DEBOUNCE_DELAY = 16; // ~60fps
    private static final int MAX_BUFFER_SIZE = 5;

    private FrameLayout canvasView;
    private Template currentTemplate;
    private ApiService apiService;
    private Long templateId;
    private Map<String, DraggableElementView> elementViews = new HashMap<>();
    private ImagePickerUtil imagePickerUtil;
    private String currentImageElementId;
    private boolean isUploadingImage = false;
    private Bitmap currentImageBitmap;
    
    // Add variables for debouncing touch events
    private long lastTouchEventTime = 0;
    private final Map<String, Queue<MotionEvent>> pendingEvents = new HashMap<>();

    private ActivityResultLauncher<String[]> permissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_builder);
        
        // Enable hardware acceleration for the entire activity
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        apiService = ApiClient.createAuthenticatedService(ApiService.class, this);
        
        // Initialize views
        canvasView = findViewById(R.id.canvas);
        // Enable hardware acceleration for smoother rendering
        canvasView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // Set up touch event dispatching with debouncing
        setupTouchEventDispatcher();
        
        FloatingActionButton fabSave = findViewById(R.id.fab_save);
        
        // Setup image picker
        imagePickerUtil = new ImagePickerUtil(this, this);
        
        // Setup permissions launcher and request permissions immediately
        // to avoid asking later when user adds an image
        setupPermissionsLauncher();
        requestPermissionsIfNeeded();
        
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
            // Check if we should create a specific predefined template type
            String templateType = getIntent().getStringExtra("template_type");
            if (templateType != null) {
                // Create a template based on the specified type
                currentTemplate = Template.createPredefinedTemplate(templateType);
                setTitle("New " + templateType);
            } else {
                // Create a default template (blog template)
                currentTemplate = createDefaultTemplate();
                setTitle("New Template");
            }
            
            // Display the template
            displayTemplate();
        }
    }

    /**
     * Creates a new default template with proper styling.
     * 
     * @return A styled template
     */
    private Template createDefaultTemplate() {
        // Use the newly created Blog template as default
        return Template.createPredefinedTemplate(Template.TEMPLATE_TYPE_BLOG);
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
                        // Permissions granted, now we can use them
                    } else {
                        // Even if permissions are denied, we'll still allow adding a placeholder image box
                        // User can add actual images when they grant permissions later
                        Toast.makeText(this, "You can add image placeholders now. Grant permissions later to upload actual images.", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Requests permissions if needed, but doesn't block the app functionality.
     */
    private void requestPermissionsIfNeeded() {
        if (!hasPermissions()) {
            permissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }
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
     * Shows the image picker dialog when user wants to add an image.
     */
    private void showImagePickerDialog() {
        // Use the new simplified method instead of asking for permissions
        imagePickerUtil.showImagePicker();
    }

    /**
     * Adds an image element to the template.
     */
    private void addImageElement() {
        // Check if we have permissions, but don't block the flow
        if (!hasPermissions()) {
            // Just notify the user but continue with showing image picker
            Toast.makeText(this, "Using document picker since permissions aren't granted", Toast.LENGTH_SHORT).show();
        }
        
        // Show the image picker dialog
        showImagePickerDialog();
    }

    /**
     * Sets up the element palette with draggable elements.
     */
    private void setupElementPalette() {
        LinearLayout elementPalette = findViewById(R.id.element_palette);
        
        // Add text element
        TextView textElement = new TextView(this);
        textElement.setText("Text");
        textElement.setGravity(Gravity.CENTER);
        textElement.setBackgroundResource(R.drawable.bg_element_palette);
        textElement.setTextColor(Color.BLACK);
        textElement.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textElement.setPadding(32, 16, 32, 16);
        textElement.setOnClickListener(v -> addTextElement());
        elementPalette.addView(textElement);
        
        // Add header element
        TextView headerElement = new TextView(this);
        headerElement.setText("Header");
        headerElement.setGravity(Gravity.CENTER);
        headerElement.setBackgroundResource(R.drawable.bg_element_palette);
        headerElement.setTextColor(Color.BLACK);
        headerElement.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headerElement.setPadding(32, 16, 32, 16);
        headerElement.setOnClickListener(v -> addHeaderElement());
        elementPalette.addView(headerElement);
        
        // Add image element
        TextView imageElement = new TextView(this);
        imageElement.setText("Image");
        imageElement.setGravity(Gravity.CENTER);
        imageElement.setBackgroundResource(R.drawable.bg_element_palette);
        imageElement.setTextColor(Color.BLACK);
        imageElement.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageElement.setPadding(32, 16, 32, 16);
        imageElement.setOnClickListener(v -> addImageElement());
        elementPalette.addView(imageElement);
        
        // Add divider element
        TextView dividerElement = new TextView(this);
        dividerElement.setText("Divider");
        dividerElement.setGravity(Gravity.CENTER);
        dividerElement.setBackgroundResource(R.drawable.bg_element_palette);
        dividerElement.setTextColor(Color.BLACK);
        dividerElement.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        dividerElement.setPadding(32, 16, 32, 16);
        dividerElement.setOnClickListener(v -> addDividerElement());
        elementPalette.addView(dividerElement);
        
        // Add quote element
        TextView quoteElement = new TextView(this);
        quoteElement.setText("Quote");
        quoteElement.setGravity(Gravity.CENTER);
        quoteElement.setBackgroundResource(R.drawable.bg_element_palette);
        quoteElement.setTextColor(Color.BLACK);
        quoteElement.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        quoteElement.setPadding(32, 16, 32, 16);
        quoteElement.setOnClickListener(v -> addQuoteElement());
        elementPalette.addView(quoteElement);
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
     * Displays an existing template on the canvas.
     */
    private void displayTemplate() {
        if (currentTemplate == null) return;
        
        // Clear existing views
        canvasView.removeAllViews();
        elementViews.clear();
        
        // Add each element to the canvas
        List<TemplateElement> elements = TemplateUtil.extractElements(currentTemplate);
        
        // If no elements exist in the template, add some default elements
        if (elements.isEmpty()) {
            currentTemplate = createDefaultTemplate();
            elements = TemplateUtil.extractElements(currentTemplate);
        }
        
        // Add elements in batches to avoid UI lag
        if (elements.size() > 5) {
            // First batch immediately
            final List<TemplateElement> firstBatch = elements.subList(0, 5);
            for (TemplateElement element : firstBatch) {
                addElementViewToCanvas(element);
            }
            
            // Rest of elements after a short delay for smoother rendering
            final List<TemplateElement> remainingElements = elements.subList(5, elements.size());
            canvasView.post(() -> {
                for (TemplateElement element : remainingElements) {
                    addElementViewToCanvas(element);
                }
            });
        } else {
            // Fewer elements, add them all immediately
            for (TemplateElement element : elements) {
                addElementViewToCanvas(element);
            }
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

    /**
     * Sets up a touch event dispatcher that handles debouncing of touch events
     * to prevent excessive UI updates during drag operations
     */
    private void setupTouchEventDispatcher() {
        canvasView.setOnTouchListener((v, event) -> {
            // Find the child view that received the touch event
            float x = event.getX();
            float y = event.getY();
            
            // Let the canvas handle the touch if it's not on a child view
            for (int i = canvasView.getChildCount() - 1; i >= 0; i--) {
                View child = canvasView.getChildAt(i);
                
                // Check if the touch is within this child's bounds
                if (x >= child.getX() && x <= child.getX() + child.getWidth() &&
                    y >= child.getY() && y <= child.getY() + child.getHeight()) {
                    
                    // If this is a DraggableElementView, apply our debouncing logic
                    if (child instanceof DraggableElementView) {
                        DraggableElementView elementView = (DraggableElementView) child;
                        String elementId = elementView.getElement().getId();
                        
                        // Process the event based on its action type
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // Always pass down events immediately
                                dispatchTouchEventToChild(elementView, event);
                                
                                // Clear any pending events for this element
                                pendingEvents.put(elementId, new LinkedList<>());
                                break;
                                
                            case MotionEvent.ACTION_MOVE:
                                // Add to queue
                                Queue<MotionEvent> queue = pendingEvents.get(elementId);
                                if (queue == null) {
                                    queue = new LinkedList<>();
                                    pendingEvents.put(elementId, queue);
                                }
                                
                                // Create a copy of the event since the original will be recycled
                                queue.add(MotionEvent.obtain(event));
                                
                                // Process the queue if we've exceeded the debounce delay
                                long currentTime = SystemClock.uptimeMillis();
                                if (currentTime - lastTouchEventTime > TOUCH_DEBOUNCE_DELAY) {
                                    processEventQueue(elementId, elementView);
                                    lastTouchEventTime = currentTime;
                                }
                                break;
                                
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                // Process any remaining events in the queue
                                processEventQueue(elementId, elementView);
                                
                                // Then send the final up/cancel event
                                dispatchTouchEventToChild(elementView, event);
                                
                                // Clear the queue
                                if (pendingEvents.containsKey(elementId)) {
                                    for (MotionEvent e : pendingEvents.get(elementId)) {
                                        e.recycle();
                                    }
                                    pendingEvents.remove(elementId);
                                }
                                break;
                        }
                        
                        return true;
                    }
                }
            }
            
            // Not handled by a child view
            return false;
        });
    }
    
    /**
     * Process queued touch events for an element
     * 
     * @param elementId The ID of the element
     * @param elementView The view for the element
     */
    private void processEventQueue(String elementId, DraggableElementView elementView) {
        Queue<MotionEvent> queue = pendingEvents.get(elementId);
        if (queue != null && !queue.isEmpty()) {
            // If we have too many events, skip some to maintain responsiveness
            while (queue.size() > MAX_BUFFER_SIZE) {
                MotionEvent skippedEvent = queue.poll();
                if (skippedEvent != null) {
                    skippedEvent.recycle();
                }
            }
            
            // Process the most recent event
            MotionEvent lastEvent = queue.poll();
            if (lastEvent != null) {
                dispatchTouchEventToChild(elementView, lastEvent);
                
                // Recycle the event when done
                lastEvent.recycle();
            }
            
            // Clear the rest of the queue
            for (MotionEvent e : queue) {
                e.recycle();
            }
            queue.clear();
        }
    }
    
    /**
     * Dispatches a touch event to a child view
     * 
     * @param view The target view
     * @param event The motion event to dispatch
     */
    private void dispatchTouchEventToChild(View view, MotionEvent event) {
        if (view == null || event == null) return;
        
        // Adjust coordinates relative to the child view
        MotionEvent transformedEvent = MotionEvent.obtain(event);
        transformedEvent.offsetLocation(-view.getX(), -view.getY());
        
        // Dispatch the event
        view.dispatchTouchEvent(transformedEvent);
        
        // Recycle the transformed event
        transformedEvent.recycle();
    }

    /**
     * Adds a text element to the canvas.
     */
    private void addTextElement() {
        TemplateElement element = new TemplateElement();
        element.setId(UUID.randomUUID().toString());
        element.setType(Template.ELEMENT_TYPE_TEXT);
        element.setX(100);
        element.setY(100);
        element.setWidth(400);
        element.setHeight(100);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("text", "Sample Text");
        element.setProperties(properties);
        
        currentTemplate.getElements().add(element);
        addElementViewToCanvas(element);
    }

    /**
     * Adds a header element to the canvas.
     */
    private void addHeaderElement() {
        TemplateElement element = new TemplateElement();
        element.setId(UUID.randomUUID().toString());
        element.setType(Template.ELEMENT_TYPE_HEADER);
        element.setX(100);
        element.setY(50);
        element.setWidth(500);
        element.setHeight(80);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("text", "Header Title");
        element.setProperties(properties);
        
        currentTemplate.getElements().add(element);
        addElementViewToCanvas(element);
    }

    /**
     * Adds a divider element to the canvas.
     */
    private void addDividerElement() {
        TemplateElement element = new TemplateElement();
        element.setId(UUID.randomUUID().toString());
        element.setType(Template.ELEMENT_TYPE_DIVIDER);
        element.setX(100);
        element.setY(200);
        element.setWidth(400);
        element.setHeight(20);
        
        currentTemplate.getElements().add(element);
        addElementViewToCanvas(element);
    }

    /**
     * Adds a quote element to the canvas.
     */
    private void addQuoteElement() {
        TemplateElement element = new TemplateElement();
        element.setId(UUID.randomUUID().toString());
        element.setType(Template.ELEMENT_TYPE_QUOTE);
        element.setX(100);
        element.setY(250);
        element.setWidth(400);
        element.setHeight(120);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("text", "Sample Quote");
        element.setProperties(properties);
        
        currentTemplate.getElements().add(element);
        addElementViewToCanvas(element);
    }

    /**
     * Adds an element view to the canvas.
     * 
     * @param element The element to add
     */
    private void addElementViewToCanvas(TemplateElement element) {
        DraggableElementView elementView = new DraggableElementView(this);
        elementView.setElement(element);
        elementView.setOnPositionChangedListener((view, updatedElement) -> {
            // Update the element in the template
            for (int i = 0; i < currentTemplate.getElements().size(); i++) {
                if (currentTemplate.getElements().get(i).getId().equals(updatedElement.getId())) {
                    currentTemplate.getElements().set(i, updatedElement);
                    break;
                }
            }
        });
        
        canvasView.addView(elementView);
        elementViews.put(element.getId(), elementView);
    }
} 