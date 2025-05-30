package com.canvamedium.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.canvamedium.databinding.ItemArticleBinding;
import com.canvamedium.model.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying articles in a RecyclerView.
 */
public class ArticleAdapter extends ListAdapter<Article, ArticleAdapter.ArticleViewHolder> {

    private final ArticleClickListener articleClickListener;

    /**
     * Interface for handling article click events.
     */
    public interface ArticleClickListener {
        void onArticleClick(Article article);
        void onBookmarkClick(Article article, boolean isCurrentlyBookmarked);
    }

    /**
     * Constructor for ArticleAdapter.
     *
     * @param clickListener The listener for article click events
     */
    public ArticleAdapter(ArticleClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.articleClickListener = clickListener;
    }

    /**
     * DiffUtil callback for efficiently updating the RecyclerView.
     */
    private static final DiffUtil.ItemCallback<Article> DIFF_CALLBACK = new DiffUtil.ItemCallback<Article>() {
        @Override
        public boolean areItemsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            // Compare the relevant fields to determine if the content is the same
            // Added null checks to prevent NullPointerException
            return (oldItem.getTitle() == null ? newItem.getTitle() == null : oldItem.getTitle().equals(newItem.getTitle()))
                    && (oldItem.getPreviewText() == null ? newItem.getPreviewText() == null : oldItem.getPreviewText().equals(newItem.getPreviewText()))
                    && oldItem.isBookmarked() == newItem.isBookmarked()
                    && (oldItem.getStatus() == null ? newItem.getStatus() == null : oldItem.getStatus().equals(newItem.getStatus()))
                    && (oldItem.getUpdatedAt() == null ? newItem.getUpdatedAt() == null : oldItem.getUpdatedAt().equals(newItem.getUpdatedAt()));
        }
    };

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArticleBinding binding = ItemArticleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ArticleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = getItem(position);
        holder.bind(article);
    }

    /**
     * Updates the adapter with a new list of articles.
     *
     * @param articles The new list of articles
     */
    public void setArticles(List<Article> articles) {
        submitList(articles);
    }
    
    /**
     * Adds articles to the current list for pagination.
     *
     * @param articles The articles to add
     */
    public void addArticles(List<Article> articles) {
        List<Article> currentList = new ArrayList<>(getCurrentList());
        currentList.addAll(articles);
        submitList(currentList);
    }
    
    /**
     * Clears all articles from the list.
     */
    public void clearArticles() {
        submitList(null);
    }

    /**
     * ViewHolder for article items.
     */
    public class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final ItemArticleBinding binding;

        public ArticleViewHolder(ItemArticleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Bind article data to the view.
         *
         * @param article The article to bind
         */
        public void bind(Article article) {
            binding.setArticle(article);
            
            // Set click listener for the whole article
            binding.setClickListener(v -> articleClickListener.onArticleClick(article));
            
            // Set click listener for the bookmark icon
            binding.imageBookmark.setOnClickListener(v -> 
                articleClickListener.onBookmarkClick(article, article.isBookmarked())
            );
            
            // Ensure the binding is executed immediately
            binding.executePendingBindings();
        }
    }
} 