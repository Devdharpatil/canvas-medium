package com.canvamedium.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.model.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of templates in a RecyclerView.
 */
public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    private List<Template> templates;
    private final Context context;
    private final OnTemplateClickListener listener;

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
        holder.bind(template, listener);
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.template_thumbnail);
            nameView = itemView.findViewById(R.id.template_name);
            descriptionView = itemView.findViewById(R.id.template_description);
        }

        void bind(final Template template, final OnTemplateClickListener listener) {
            nameView.setText(template.getName());
            
            if (template.getDescription() != null && !template.getDescription().isEmpty()) {
                descriptionView.setText(template.getDescription());
                descriptionView.setVisibility(View.VISIBLE);
            } else {
                descriptionView.setVisibility(View.GONE);
            }
            
            // Load thumbnail if available
            if (template.getThumbnailUrl() != null && !template.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(template.getThumbnailUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(thumbnailView);
            } else {
                thumbnailView.setImageResource(R.drawable.ic_launcher_background);
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemplateClick(template);
                }
            });
        }
    }
} 