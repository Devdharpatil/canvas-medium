package com.canvamedium.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.model.TemplateElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for building article content from templates and populating the editor UI.
 */
public class ContentBuilder {

    /**
     * Interface to handle image element clicks for content selection.
     */
    public interface OnImageClickListener {
        void onImageClick(ImageView imageView);
    }

    /**
     * Builds the editor UI from a template layout.
     *
     * @param layout The template layout as a JsonObject
     * @param container The container to add the editor elements to
     * @param context The context for inflating views
     * @param imageClickListener The listener for image element clicks
     * @return A map of element IDs to their corresponding views
     */
    public Map<String, View> buildEditorFromTemplate(JsonObject layout, 
                                                   ViewGroup container, 
                                                   Context context,
                                                   OnImageClickListener imageClickListener) {
        Map<String, View> elementViews = new HashMap<>();
        
        if (layout.has("elements") && layout.get("elements").isJsonArray()) {
            JsonArray elements = layout.getAsJsonArray("elements");
            
            for (JsonElement elementJson : elements) {
                if (elementJson.isJsonObject()) {
                    JsonObject elementObj = elementJson.getAsJsonObject();
                    String elementType = elementObj.has("type") ? 
                            elementObj.get("type").getAsString() : "";
                    String elementId = elementObj.has("id") ? 
                            elementObj.get("id").getAsString() : UUID.randomUUID().toString();
                    
                    View elementView = createEditorElementByType(elementType, elementObj, context, imageClickListener);
                    
                    if (elementView != null) {
                        // Add tag to store the element ID
                        elementView.setTag(R.id.element_id_tag, elementId);
                        
                        // Add to container
                        container.addView(elementView);
                        
                        // Store in map for easy access
                        elementViews.put(elementId, elementView);
                    }
                }
            }
        }
        
        return elementViews;
    }
    
    /**
     * Creates an editor element based on the element type.
     *
     * @param elementType The type of the element
     * @param elementObj The element data as a JsonObject
     * @param context The context for inflating views
     * @param imageClickListener The listener for image element clicks
     * @return The created editor element view
     */
    private View createEditorElementByType(String elementType, 
                                         JsonObject elementObj, 
                                         Context context,
                                         OnImageClickListener imageClickListener) {
        switch (elementType.toUpperCase()) {
            case "TEXT":
                return createTextEditor(elementObj, context);
                
            case "IMAGE":
                return createImageEditor(elementObj, context, imageClickListener);
                
            case "HEADER":
                return createHeaderEditor(elementObj, context);
                
            case "DIVIDER":
                return createDivider(context);
                
            case "QUOTE":
                return createQuoteEditor(elementObj, context);
                
            default:
                return null;
        }
    }
    
    /**
     * Creates a text editor element.
     *
     * @param elementObj The element data as a JsonObject
     * @param context The context for inflating views
     * @return The created text editor view
     */
    private View createTextEditor(JsonObject elementObj, Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        TextView labelView = new TextView(context);
        labelView.setText("Text Block");
        labelView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        labelView.setPadding(0, 8, 0, 8);
        container.addView(labelView);
        
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editText.setHint("Enter text content here");
        editText.setBackgroundResource(android.R.drawable.edit_text);
        editText.setMinLines(3);
        editText.setGravity(Gravity.TOP | Gravity.START);
        editText.setPadding(16, 16, 16, 16);
        
        // Set initial content if available
        if (elementObj.has("content") && !elementObj.get("content").isJsonNull()) {
            editText.setText(elementObj.get("content").getAsString());
        }
        
        // Store the edit text in the container tag for content collection
        container.setTag(R.id.content_editor_tag, editText);
        
        container.addView(editText);
        container.setPadding(0, 8, 0, 16);
        
        return container;
    }
    
    /**
     * Creates an image editor element.
     *
     * @param elementObj The element data as a JsonObject
     * @param context The context for inflating views
     * @param imageClickListener The listener for image element clicks
     * @return The created image editor view
     */
    private View createImageEditor(JsonObject elementObj, 
                                Context context,
                                OnImageClickListener imageClickListener) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        TextView labelView = new TextView(context);
        labelView.setText("Image");
        labelView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        labelView.setPadding(0, 8, 0, 8);
        container.addView(labelView);
        
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
        );
        imageParams.setMargins(0, 8, 0, 8);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundResource(android.R.drawable.picture_frame);
        imageView.setImageResource(R.drawable.ic_image_placeholder);
        imageView.setContentDescription("Content image");
        
        // Set initial image if available
        if (elementObj.has("url") && !elementObj.get("url").isJsonNull()) {
            String imageUrl = elementObj.get("url").getAsString();
            imageView.setTag(imageUrl); // Store URL in tag
            
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageView);
        }
        
        // Add click listener for image selection
        if (imageClickListener != null) {
            imageView.setOnClickListener(v -> imageClickListener.onImageClick(imageView));
        }
        
        // Store the image view in the container tag for content collection
        container.setTag(R.id.content_editor_tag, imageView);
        
        container.addView(imageView);
        container.setPadding(0, 8, 0, 16);
        
        return container;
    }
    
    /**
     * Creates a header editor element.
     *
     * @param elementObj The element data as a JsonObject
     * @param context The context for inflating views
     * @return The created header editor view
     */
    private View createHeaderEditor(JsonObject elementObj, Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        TextView labelView = new TextView(context);
        labelView.setText("Header");
        labelView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        labelView.setPadding(0, 8, 0, 8);
        container.addView(labelView);
        
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editText.setHint("Enter header text here");
        editText.setBackgroundResource(android.R.drawable.edit_text);
        editText.setTextAppearance(context, android.R.style.TextAppearance_Large);
        editText.setGravity(Gravity.CENTER_VERTICAL);
        editText.setPadding(16, 16, 16, 16);
        
        // Set initial content if available
        if (elementObj.has("content") && !elementObj.get("content").isJsonNull()) {
            editText.setText(elementObj.get("content").getAsString());
        }
        
        // Store the edit text in the container tag for content collection
        container.setTag(R.id.content_editor_tag, editText);
        
        container.addView(editText);
        container.setPadding(0, 8, 0, 16);
        
        return container;
    }
    
    /**
     * Creates a quote editor element.
     *
     * @param elementObj The element data as a JsonObject
     * @param context The context for inflating views
     * @return The created quote editor view
     */
    private View createQuoteEditor(JsonObject elementObj, Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        TextView labelView = new TextView(context);
        labelView.setText("Quote");
        labelView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        labelView.setPadding(0, 8, 0, 8);
        container.addView(labelView);
        
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editText.setHint("Enter quote text here");
        editText.setBackgroundResource(android.R.drawable.edit_text);
        editText.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        editText.setGravity(Gravity.CENTER);
        editText.setMinLines(2);
        editText.setPadding(32, 16, 32, 16);
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        
        // Set initial content if available
        if (elementObj.has("content") && !elementObj.get("content").isJsonNull()) {
            editText.setText(elementObj.get("content").getAsString());
        }
        
        // Store the edit text in the container tag for content collection
        container.setTag(R.id.content_editor_tag, editText);
        
        container.addView(editText);
        container.setPadding(0, 8, 0, 16);
        
        return container;
    }
    
    /**
     * Creates a divider element.
     *
     * @param context The context for inflating views
     * @return The created divider view
     */
    private View createDivider(Context context) {
        View divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        params.setMargins(0, 16, 0, 16);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        
        return divider;
    }
    
    /**
     * Populates the editor UI with content from an article.
     *
     * @param content The article content as a JsonObject
     * @param container The container with the editor elements
     */
    public void populateEditorFromContent(JsonObject content, ViewGroup container) {
        if (content == null || !content.has("elements") || !content.get("elements").isJsonArray()) {
            return;
        }
        
        JsonArray contentElements = content.getAsJsonArray("elements");
        
        // Match content elements with editor elements by ID
        for (int i = 0; i < contentElements.size() && i < container.getChildCount(); i++) {
            if (!contentElements.get(i).isJsonObject()) continue;
            
            JsonObject contentElement = contentElements.get(i).getAsJsonObject();
            View editorElement = container.getChildAt(i);
            
            // Get the editor input view
            View editorInput = (View) editorElement.getTag(R.id.content_editor_tag);
            if (editorInput == null) continue;
            
            // Populate based on element type
            if (editorInput instanceof EditText && contentElement.has("content")) {
                ((EditText) editorInput).setText(contentElement.get("content").getAsString());
            } else if (editorInput instanceof ImageView && contentElement.has("url")) {
                ImageView imageView = (ImageView) editorInput;
                String imageUrl = contentElement.get("url").getAsString();
                imageView.setTag(imageUrl); // Store URL in tag
                
                Glide.with(editorInput.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(imageView);
            }
        }
    }
    
    /**
     * Builds content JSON from the editor UI.
     *
     * @param container The container with the editor elements
     * @return The article content as a JsonObject
     */
    public JsonObject buildContentFromEditor(ViewGroup container) {
        JsonObject content = new JsonObject();
        JsonArray elements = new JsonArray();
        
        for (int i = 0; i < container.getChildCount(); i++) {
            View editorElement = container.getChildAt(i);
            String elementId = (String) editorElement.getTag(R.id.element_id_tag);
            
            // Skip elements without an ID
            if (elementId == null) continue;
            
            // Get the editor input view
            View editorInput = (View) editorElement.getTag(R.id.content_editor_tag);
            if (editorInput == null) continue;
            
            JsonObject elementObject = new JsonObject();
            elementObject.addProperty("id", elementId);
            
            // Build based on element type
            if (editorInput instanceof EditText) {
                EditText editText = (EditText) editorInput;
                String text = editText.getText().toString();
                
                // Determine element type based on parent container or other attributes
                String elementType = "TEXT";
                if (editorInput.getParent() != null) {
                    View parentView = (View) editorInput.getParent();
                    
                    if (parentView instanceof LinearLayout) {
                        LinearLayout parentLayout = (LinearLayout) parentView;
                        if (parentLayout.getChildCount() > 0) {
                            View firstChild = parentLayout.getChildAt(0);
                            if (firstChild instanceof TextView) {
                                String label = ((TextView) firstChild).getText().toString();
                                
                                if ("Header".equals(label)) {
                                    elementType = "HEADER";
                                } else if ("Quote".equals(label)) {
                                    elementType = "QUOTE";
                                }
                            }
                        }
                    }
                }
                
                elementObject.addProperty("type", elementType);
                elementObject.addProperty("content", text);
                
            } else if (editorInput instanceof ImageView) {
                ImageView imageView = (ImageView) editorInput;
                String imageUrl = (String) imageView.getTag();
                
                elementObject.addProperty("type", "IMAGE");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    elementObject.addProperty("url", imageUrl);
                }
            } else {
                // Skip unsupported element types
                continue;
            }
            
            elements.add(elementObject);
        }
        
        content.add("elements", elements);
        return content;
    }
} 