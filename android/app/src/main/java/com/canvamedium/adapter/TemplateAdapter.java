package com.canvamedium.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.model.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying a list of templates in a RecyclerView.
 */
public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    private List<Template> templates;
    private final Context context;
    private final OnTemplateClickListener listener;
    
    // Cache for template type colors for consistent coloring
    private final Map<String, Integer> templateColorMap = new HashMap<>();
    
    // Default colors for different template types
    private static final int COLOR_BLOG = Color.parseColor("#4CAF50");       // Green
    private static final int COLOR_ARTICLE = Color.parseColor("#2196F3");    // Blue
    private static final int COLOR_GALLERY = Color.parseColor("#FF9800");    // Orange
    private static final int COLOR_TUTORIAL = Color.parseColor("#673AB7");   // Purple
    private static final int COLOR_QUOTE = Color.parseColor("#E91E63");      // Pink
    private static final int COLOR_DEFAULT = Color.parseColor("#607D8B");    // Blue Gray

    /**
     * Interface for handling template click events.
     */
    public interface OnTemplateClickListener {
        void onTemplateClick(Template template);
    }

    /**
     * Constructor for the adapter.
     *
     * @param context  The context
     * @param listener The click listener
     */
    public TemplateAdapter(Context context, OnTemplateClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.templates = new ArrayList<>();
        initTemplatePlaceholderColors();
    }
    
    /**
     * Initialize the color mapping for template types
     */
    private void initTemplatePlaceholderColors() {
        templateColorMap.put(Template.TEMPLATE_TYPE_BLOG, COLOR_BLOG);
        templateColorMap.put(Template.TEMPLATE_TYPE_ARTICLE, COLOR_ARTICLE);
        templateColorMap.put(Template.TEMPLATE_TYPE_PHOTO_GALLERY, COLOR_GALLERY);
        templateColorMap.put(Template.TEMPLATE_TYPE_TUTORIAL, COLOR_TUTORIAL);
        templateColorMap.put(Template.TEMPLATE_TYPE_QUOTE, COLOR_QUOTE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Template template = templates.get(position);
        holder.bind(template, listener, templateColorMap);
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    /**
     * Updates the adapter's data.
     *
     * @param templates The new list of templates
     */
    public void setTemplates(List<Template> templates) {
        this.templates = templates;
        notifyDataSetChanged();
    }

    /**
     * Adds a new template to the adapter.
     *
     * @param template The template to add
     */
    public void addTemplate(Template template) {
        templates.add(template);
        notifyItemInserted(templates.size() - 1);
    }

    /**
     * ViewHolder class for template items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView nameView;
        private final TextView descriptionView;
        private final TextView placeholderText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.template_thumbnail);
            nameView = itemView.findViewById(R.id.template_name);
            descriptionView = itemView.findViewById(R.id.template_description);
            
            // Add a TextView for displaying template type on placeholder
            placeholderText = new TextView(itemView.getContext());
            placeholderText.setTextColor(Color.WHITE);
            placeholderText.setTextSize(16);
            placeholderText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
            // Add the placeholder text to the thumbnail as an overlay
            if (thumbnailView.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) thumbnailView.getParent();
                placeholderText.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ));
                parent.addView(placeholderText);
                
                // Center the text over the thumbnail
                placeholderText.setX(thumbnailView.getX());
                placeholderText.setY(thumbnailView.getY());
                placeholderText.setPadding(0, thumbnailView.getHeight()/2 - 50, 0, 0);
            }
        }

        void bind(final Template template, final OnTemplateClickListener listener, 
                 final Map<String, Integer> colorMap) {
            nameView.setText(template.getName());
            
            if (template.getDescription() != null && !template.getDescription().isEmpty()) {
                descriptionView.setText(template.getDescription());
                descriptionView.setVisibility(View.VISIBLE);
            } else {
                descriptionView.setVisibility(View.GONE);
            }
            
            // Load thumbnail if available, otherwise show a nice placeholder
            if (template.getThumbnailUrl() != null && !template.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(template.getThumbnailUrl())
                        .centerCrop()
                        .placeholder(generatePlaceholderForTemplate(template, colorMap))
                        .into(thumbnailView);
                placeholderText.setVisibility(View.GONE);
            } else {
                // Create a colored placeholder based on template type
                thumbnailView.setImageDrawable(generatePlaceholderForTemplate(template, colorMap));
                placeholderText.setText(getTemplateTypeDisplayName(template.getName()));
                placeholderText.setVisibility(View.VISIBLE);
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemplateClick(template);
                }
            });
        }
        
        /**
         * Generates a placeholder drawable for a template based on its type
         * 
         * @param template The template
         * @param colorMap The map of template types to colors
         * @return A drawable to use as placeholder
         */
        private GradientDrawable generatePlaceholderForTemplate(Template template, Map<String, Integer> colorMap) {
            // Determine color based on template name (since we might not have type directly)
            int color = COLOR_DEFAULT;
            String templateName = template.getName().toLowerCase();
            
            // Try to match template name with known types
            for (Map.Entry<String, Integer> entry : colorMap.entrySet()) {
                if (templateName.contains(entry.getKey().toLowerCase())) {
                    color = entry.getValue();
                    break;
                }
            }
            
            // Create gradient drawable
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(color);
            
            return drawable;
        }
        
        /**
         * Gets a display name for the template type from the template name
         * 
         * @param templateName The template name
         * @return A display name for the template type
         */
        private String getTemplateTypeDisplayName(String templateName) {
            if (templateName == null) {
                return "Template";
            }
            
            templateName = templateName.toLowerCase();
            
            if (templateName.contains("blog")) {
                return "Blog Post";
            } else if (templateName.contains("article")) {
                return "Article";
            } else if (templateName.contains("gallery") || templateName.contains("photo")) {
                return "Photo Gallery";
            } else if (templateName.contains("tutorial") || templateName.contains("how")) {
                return "Tutorial";
            } else if (templateName.contains("quote")) {
                return "Quote";
            } else {
                return "Template";
            }
        }
    }
} 