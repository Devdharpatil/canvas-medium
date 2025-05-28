package com.canvamedium.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.canvamedium.R;
import com.canvamedium.model.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying tags in a RecyclerView.
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private final List<Tag> tags;
    private final Context context;
    private final OnTagClickListener listener;
    private boolean selectionMode = false;
    private final List<Tag> selectedTags = new ArrayList<>();

    /**
     * Interface for tag click events.
     */
    public interface OnTagClickListener {
        void onTagClick(Tag tag, boolean isSelected);
    }

    /**
     * Constructor for the tag adapter.
     *
     * @param context  The context
     * @param listener The listener for tag click events
     */
    public TagAdapter(Context context, OnTagClickListener listener) {
        this.context = context;
        this.tags = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Sets whether the adapter is in selection mode.
     *
     * @param selectionMode True for selection mode, false for normal mode
     */
    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }

    /**
     * Sets selected tags.
     *
     * @param selectedTags The list of selected tags
     */
    public void setSelectedTags(List<Tag> selectedTags) {
        this.selectedTags.clear();
        if (selectedTags != null) {
            this.selectedTags.addAll(selectedTags);
        }
        notifyDataSetChanged();
    }

    /**
     * Gets the list of selected tags.
     *
     * @return The list of selected tags
     */
    public List<Tag> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }

    /**
     * Updates the adapter with a new list of tags.
     *
     * @param tags The new list of tags
     */
    public void setTags(List<Tag> tags) {
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
        notifyDataSetChanged();
    }

    /**
     * Adds tags to the existing list.
     *
     * @param tags The tags to add
     */
    public void addTags(List<Tag> tags) {
        if (tags != null && !tags.isEmpty()) {
            int startPosition = this.tags.size();
            this.tags.addAll(tags);
            notifyItemRangeInserted(startPosition, tags.size());
        }
    }

    /**
     * Clears all tags from the adapter.
     */
    public void clearTags() {
        this.tags.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        
        holder.nameTextView.setText(tag.getName());
        
        boolean isSelected = selectedTags.contains(tag);
        
        // Update UI based on selection state
        if (selectionMode) {
            holder.itemView.setActivated(isSelected);
        } else {
            holder.itemView.setActivated(false);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                // Toggle selection state
                boolean newSelectionState = !isSelected;
                if (newSelectionState) {
                    if (!selectedTags.contains(tag)) {
                        selectedTags.add(tag);
                    }
                } else {
                    selectedTags.remove(tag);
                }
                notifyItemChanged(position);
            }
            
            // Notify listener
            if (listener != null) {
                listener.onTagClick(tag, !isSelected);
            }
        });
        
        // Show article count if available and not in selection mode
        if (!selectionMode && holder.countTextView != null) {
            if (tag.getArticleCount() > 0) {
                holder.countTextView.setText(String.valueOf(tag.getArticleCount()));
                holder.countTextView.setVisibility(View.VISIBLE);
            } else {
                holder.countTextView.setVisibility(View.GONE);
            }
        } else if (holder.countTextView != null) {
            holder.countTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    /**
     * ViewHolder class for tag items.
     */
    static class TagViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView countTextView;

        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_tag_name);
            countTextView = itemView.findViewById(R.id.text_article_count);
        }
    }
} 