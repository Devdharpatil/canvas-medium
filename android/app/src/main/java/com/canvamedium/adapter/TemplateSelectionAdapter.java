package com.canvamedium.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.model.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying templates in a horizontal RecyclerView for selection.
 */
public class TemplateSelectionAdapter extends RecyclerView.Adapter<TemplateSelectionAdapter.TemplateViewHolder> {

    private final List<Template> templates;
    private final Context context;
    private int selectedPosition = -1;
    private final OnTemplateSelectedListener listener;

    /**
     * Interface for template selection events.
     */
    public interface OnTemplateSelectedListener {
        void onTemplateSelected(Template template);
    }

    /**
     * Constructor for the template selection adapter.
     *
     * @param context  The context
     * @param listener The listener for template selection events
     */
    public TemplateSelectionAdapter(Context context, OnTemplateSelectedListener listener) {
        this.context = context;
        this.templates = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the adapter with a new list of templates.
     *
     * @param templates The new list of templates
     */
    public void setTemplates(List<Template> templates) {
        this.templates.clear();
        if (templates != null) {
            this.templates.addAll(templates);
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the selected template position.
     *
     * @param position The position to select
     */
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        
        // Notify adapter of changes to update UI
        if (previousPosition >= 0) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition >= 0) {
            notifyItemChanged(selectedPosition);
        }
    }

    /**
     * Gets the currently selected template.
     *
     * @return The selected template, or null if none selected
     */
    public Template getSelectedTemplate() {
        if (selectedPosition >= 0 && selectedPosition < templates.size()) {
            return templates.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        Template template = templates.get(position);
        
        holder.nameTextView.setText(template.getName());
        
        if (template.getDescription() != null && !template.getDescription().isEmpty()) {
            holder.descriptionTextView.setText(template.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }
        
        // Load thumbnail if available
        if (template.getThumbnailUrl() != null && !template.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                .load(template.getThumbnailUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.thumbnailImageView);
        } else {
            // Set placeholder image
            holder.thumbnailImageView.setImageResource(R.drawable.ic_image_placeholder);
        }
        
        // Highlight the selected template
        if (position == selectedPosition) {
            holder.cardView.setStrokeWidth(6); // Selected state with thicker stroke
            holder.cardView.setStrokeColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.cardView.setStrokeWidth(0); // Normal state without stroke
            holder.cardView.setStrokeColor(0);
        }
        
        // Handle click events
        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(position);
            if (listener != null) {
                listener.onTemplateSelected(template);
            }
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    /**
     * ViewHolder class for template items.
     */
    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView thumbnailImageView;
        final TextView nameTextView;
        final TextView descriptionTextView;

        TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.template_card);
            thumbnailImageView = itemView.findViewById(R.id.template_thumbnail);
            nameTextView = itemView.findViewById(R.id.template_name);
            descriptionTextView = itemView.findViewById(R.id.template_description);
        }
    }
} 