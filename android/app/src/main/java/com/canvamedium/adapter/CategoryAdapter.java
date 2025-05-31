package com.canvamedium.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canvamedium.R;
import com.canvamedium.model.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying categories in a RecyclerView.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;
    private final Context context;
    private final OnCategoryClickListener listener;
    private boolean isHorizontalLayout = false;
    private long selectedCategoryId = -1; // Default to "All" category

    /**
     * Interface for category click events.
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    /**
     * Constructor for the category adapter.
     *
     * @param context  The context
     * @param listener The listener for category click events
     */
    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Sets the layout mode for the adapter.
     *
     * @param isHorizontal True for horizontal layout, false for grid/vertical layout
     */
    public void setHorizontalLayout(boolean isHorizontal) {
        this.isHorizontalLayout = isHorizontal;
    }

    /**
     * Sets the selected category ID.
     *
     * @param categoryId The ID of the selected category
     */
    public void setSelectedCategoryId(long categoryId) {
        if (this.selectedCategoryId != categoryId) {
            this.selectedCategoryId = categoryId;
            notifyDataSetChanged();
        }
    }

    /**
     * Gets the currently selected category ID.
     *
     * @return The selected category ID
     */
    public long getSelectedCategoryId() {
        return selectedCategoryId;
    }

    /**
     * Updates the adapter with a new list of categories.
     *
     * @param categories The new list of categories
     */
    public void setCategories(List<Category> categories) {
        this.categories.clear();
        if (categories != null) {
            this.categories.addAll(categories);
        }
        notifyDataSetChanged();
    }

    /**
     * Adds categories to the existing list.
     *
     * @param categories The categories to add
     */
    public void addCategories(List<Category> categories) {
        if (categories != null && !categories.isEmpty()) {
            int startPosition = this.categories.size();
            this.categories.addAll(categories);
            notifyItemRangeInserted(startPosition, categories.size());
        }
    }

    /**
     * Clears all categories from the adapter.
     */
    public void clearCategories() {
        this.categories.clear();
        notifyDataSetChanged();
    }

    /**
     * Gets a category at the specified position.
     *
     * @param position The position to get the category from
     * @return The category at the specified position, or null if the position is invalid
     */
    public Category getCategory(int position) {
        if (position >= 0 && position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutResource = isHorizontalLayout ? 
                R.layout.item_category_horizontal : R.layout.item_category;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        
        // Determine if this category is selected
        boolean isSelected = category.getId() == selectedCategoryId;
        
        holder.nameTextView.setText(category.getName());
        
        // Set article count if available
        if (isHorizontalLayout) {
            holder.countTextView.setText(String.valueOf(category.getArticleCount()));
        } else {
            holder.countTextView.setText(context.getString(R.string.articles_count, category.getArticleCount()));
        }
        
        // Set description if available and not in horizontal layout
        if (!isHorizontalLayout && holder.descriptionTextView != null) {
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                holder.descriptionTextView.setText(category.getDescription());
                holder.descriptionTextView.setVisibility(View.VISIBLE);
            } else {
                holder.descriptionTextView.setVisibility(View.GONE);
            }
        }
        
        // Special handling for "All" category
        boolean isAllCategory = category.getId() == -1;
        
        // Set custom background color if available, otherwise use default
        int cardColor;
        if (isAllCategory) {
            // Special styling for "All" category
            cardColor = ContextCompat.getColor(context, R.color.colorPrimary);
        } else if (category.getColor() != null && !category.getColor().isEmpty()) {
            try {
                cardColor = Color.parseColor(category.getColor());
            } catch (IllegalArgumentException e) {
                cardColor = ContextCompat.getColor(context, category.isFeatured() ? 
                    R.color.colorAccent : R.color.colorPrimary);
            }
        } else {
            cardColor = ContextCompat.getColor(context, category.isFeatured() ? 
                R.color.colorAccent : R.color.colorPrimary);
        }
        
        // Apply card styling
        if (holder.cardView instanceof MaterialCardView) {
            MaterialCardView materialCardView = (MaterialCardView) holder.cardView;
            materialCardView.setCardBackgroundColor(cardColor);
            
            // Apply selected state styling
            if (isSelected) {
                materialCardView.setStrokeWidth(4);
                materialCardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorAccent));
                materialCardView.setCardElevation(6);
            } else {
                materialCardView.setStrokeWidth(0);
                materialCardView.setCardElevation(3);
            }
        } else {
            holder.cardView.setCardBackgroundColor(cardColor);
        }
        
        // Set text color based on background brightness
        boolean isDarkBackground = isDarkColor(cardColor);
        int textColor = isDarkBackground ? Color.WHITE : Color.BLACK;
        holder.nameTextView.setTextColor(textColor);
        holder.countTextView.setTextColor(textColor);
        if (!isHorizontalLayout && holder.descriptionTextView != null) {
            holder.descriptionTextView.setTextColor(textColor);
        }
        
        // Load icon with appropriate tint based on background
        if (category.getIcon() != null && !category.getIcon().isEmpty()) {
            Glide.with(context)
                    .load(category.getIcon())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_category_placeholder)
                    .error(R.drawable.ic_category_placeholder)
                    .into(holder.iconImageView);
            holder.iconImageView.setColorFilter(textColor);
        } else if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getIconUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_category_placeholder)
                    .error(R.drawable.ic_category_placeholder)
                    .into(holder.iconImageView);
            holder.iconImageView.setColorFilter(textColor);
        } else {
            holder.iconImageView.setImageResource(R.drawable.ic_category_placeholder);
            holder.iconImageView.setColorFilter(textColor);
        }
        
        // Set featured badge visibility and tint
        if (holder.featuredBadge != null) {
            if (category.isFeatured() && !isAllCategory) {
                holder.featuredBadge.setVisibility(View.VISIBLE);
                ((ImageView) holder.featuredBadge).setColorFilter(textColor);
            } else {
                holder.featuredBadge.setVisibility(View.GONE);
            }
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedCategoryId(category.getId());
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * ViewHolder class for category items.
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final TextView nameTextView;
        final TextView countTextView;
        final TextView descriptionTextView;
        final ImageView iconImageView;
        final View featuredBadge;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_category);
            nameTextView = itemView.findViewById(R.id.text_category_name);
            countTextView = itemView.findViewById(R.id.text_article_count);
            descriptionTextView = itemView.findViewById(R.id.text_category_description);
            iconImageView = itemView.findViewById(R.id.image_category_icon);
            featuredBadge = itemView.findViewById(R.id.featured_badge);
        }
    }

    /**
     * Determines if a color is dark enough to warrant light text.
     *
     * @param color The color to check
     * @return True if the color is dark, false otherwise
     */
    private boolean isDarkColor(int color) {
        // Calculate perceived brightness using the formula:
        // (0.299*R + 0.587*G + 0.114*B)
        double brightness = Color.red(color) * 0.299 + 
                            Color.green(color) * 0.587 + 
                            Color.blue(color) * 0.114;
        
        // Threshold for determining dark colors
        return brightness < 160;
    }
} 