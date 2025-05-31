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
import com.canvamedium.model.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying featured articles in a ViewPager2.
 */
public class FeaturedArticleAdapter extends RecyclerView.Adapter<FeaturedArticleAdapter.FeaturedArticleViewHolder> {

    private final List<Article> featuredArticles;
    private final Context context;
    private final OnFeaturedArticleClickListener listener;

    /**
     * Interface for handling featured article click events.
     */
    public interface OnFeaturedArticleClickListener {
        void onFeaturedArticleClick(Article article);
    }

    /**
     * Constructor for the featured article adapter.
     *
     * @param context  The context
     * @param listener The listener for featured article click events
     */
    public FeaturedArticleAdapter(Context context, OnFeaturedArticleClickListener listener) {
        this.context = context;
        this.featuredArticles = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the adapter with a new list of featured articles.
     *
     * @param articles The new list of featured articles
     */
    public void setFeaturedArticles(List<Article> articles) {
        this.featuredArticles.clear();
        if (articles != null) {
            this.featuredArticles.addAll(articles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_article, parent, false);
        return new FeaturedArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedArticleViewHolder holder, int position) {
        Article article = featuredArticles.get(position);
        
        // Set article title
        holder.titleTextView.setText(article.getTitle());
        
        // Set category text for badge (since we removed the separate category TextView)
        if (article.getCategory() != null) {
            holder.badgeTextView.setText(article.getCategory().getName());
        } else {
            holder.badgeTextView.setText(R.string.featured);
        }
        
        // Load image if available
        if (article.getThumbnailUrl() != null && !article.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                .load(article.getThumbnailUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_placeholder);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFeaturedArticleClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return featuredArticles.size();
    }

    /**
     * ViewHolder class for featured article items.
     */
    static class FeaturedArticleViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView titleTextView;
        final TextView badgeTextView;

        FeaturedArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.featured_article_image);
            titleTextView = itemView.findViewById(R.id.featured_article_title);
            badgeTextView = itemView.findViewById(R.id.featured_badge);
        }
    }
} 