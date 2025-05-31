package com.canvamedium.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canvamedium.R;
import com.canvamedium.model.Article;
import com.canvamedium.model.Category;
import com.canvamedium.util.BindingAdapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Adapter for displaying recommended articles in a RecyclerView.
 */
public class RecommendationsAdapter extends ListAdapter<Article, RecommendationsAdapter.RecommendationViewHolder> {

    private final ArticleClickListener articleClickListener;
    private static final String[] SAMPLE_CATEGORIES = {"Technology", "Design", "Productivity", "Data Science", "AI", "UX Research"};
    private static final String[] SAMPLE_AUTHORS = {"John Doe", "Jane Smith", "Alex Johnson", "Tejas Khare", "Maria Garcia", "James Wilson"};
    
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
            return (oldItem.getTitle() == null ? newItem.getTitle() == null : oldItem.getTitle().equals(newItem.getTitle()))
                    && (oldItem.getPreviewText() == null ? newItem.getPreviewText() == null : oldItem.getPreviewText().equals(newItem.getPreviewText()))
                    && oldItem.isBookmarked() == newItem.isBookmarked()
                    && (oldItem.getUpdatedAt() == null ? newItem.getUpdatedAt() == null : oldItem.getUpdatedAt().equals(newItem.getUpdatedAt()));
        }
    };

    /**
     * Interface for handling article click events.
     */
    public interface ArticleClickListener {
        void onArticleClick(Article article);
        void onBookmarkClick(Article article, boolean isCurrentlyBookmarked);
    }

    /**
     * Constructor for RecommendationsAdapter.
     *
     * @param clickListener The listener for article click events
     */
    public RecommendationsAdapter(ArticleClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.articleClickListener = clickListener;
    }
    
    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation_article, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Article article = getItem(position);
        
        // Hide the top divider for the first item
        if (position == 0) {
            holder.topDivider.setVisibility(View.GONE);
        } else {
            holder.topDivider.setVisibility(View.VISIBLE);
        }
        
        // Populate the views with real data when available, or sample data
        populateArticleData(holder, article, position);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (articleClickListener != null) {
                articleClickListener.onArticleClick(article);
            }
        });
        
        holder.bookmarkIcon.setOnClickListener(v -> {
            if (articleClickListener != null) {
                articleClickListener.onBookmarkClick(article, article.isBookmarked());
                
                // Update the bookmark icon immediately for better UX
                updateBookmarkIcon(holder.bookmarkIcon, !article.isBookmarked());
            }
        });
        
        holder.moreOptionsIcon.setOnClickListener(v -> {
            showArticleOptionsMenu(v, article);
        });
    }
    
    /**
     * Shows a popup menu with article options when more options icon is clicked.
     * 
     * @param view The view that was clicked (anchor for the popup)
     * @param article The article for which options are shown
     */
    private void showArticleOptionsMenu(View view, Article article) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.menu_article_options);
        
        // Configure menu items based on article state
        MenuItem saveItem = popup.getMenu().findItem(R.id.action_save_for_later);
        if (saveItem != null) {
            saveItem.setTitle(article.isBookmarked() ? 
                    view.getContext().getString(R.string.action_remove_saved) : 
                    view.getContext().getString(R.string.action_save_for_later));
            saveItem.setIcon(article.isBookmarked() ? 
                    R.drawable.ic_bookmark_filled_24dp : 
                    R.drawable.ic_bookmark_border_24dp);
        }
        
        // Set click listener for menu items
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.action_share) {
                // Share article
                Toast.makeText(view.getContext(), 
                        view.getContext().getString(R.string.share_article) + ": " + article.getTitle(), 
                        Toast.LENGTH_SHORT).show();
                return true;
                
            } else if (itemId == R.id.action_follow_author) {
                // Follow author
                String authorName = article.getAuthorName() != null ? 
                        article.getAuthorName() : 
                        view.getContext().getString(R.string.action_follow_author);
                Toast.makeText(view.getContext(), 
                        view.getContext().getString(R.string.following_author, authorName), 
                        Toast.LENGTH_SHORT).show();
                return true;
                
            } else if (itemId == R.id.action_save_for_later) {
                // Toggle bookmark state
                if (articleClickListener != null) {
                    articleClickListener.onBookmarkClick(article, article.isBookmarked());
                    
                    // Show feedback
                    Toast.makeText(view.getContext(), 
                            article.isBookmarked() ? 
                                    view.getContext().getString(R.string.removed_from_saved) : 
                                    view.getContext().getString(R.string.saved_for_later), 
                            Toast.LENGTH_SHORT).show();
                }
                return true;
                
            } else if (itemId == R.id.action_mute) {
                // Mute topic
                String topic = article.getCategory() != null && article.getCategory().getName() != null ? 
                        article.getCategory().getName() : 
                        view.getContext().getString(R.string.action_mute_topic);
                Toast.makeText(view.getContext(), 
                        view.getContext().getString(R.string.muted_topic, topic), 
                        Toast.LENGTH_SHORT).show();
                return true;
                
            } else if (itemId == R.id.action_report) {
                // Report article
                Toast.makeText(view.getContext(), 
                        view.getContext().getString(R.string.reporting_article), 
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            
            return false;
        });
        
        // Apply app styling via theme
        popup.setForceShowIcon(true);
        
        // Show the popup menu
        popup.show();
    }
    
    /**
     * Updates the bookmark icon based on the bookmarked state.
     * 
     * @param bookmarkIcon The bookmark ImageView
     * @param isBookmarked Whether the article is bookmarked
     */
    private void updateBookmarkIcon(ImageView bookmarkIcon, boolean isBookmarked) {
        bookmarkIcon.setImageResource(isBookmarked ? 
                R.drawable.ic_bookmark_filled_24dp : 
                R.drawable.ic_bookmark_border_24dp);
    }
    
    /**
     * Populates all article data fields with real or placeholder data.
     * 
     * @param holder The ViewHolder to populate
     * @param article The article to display
     * @param position The adapter position
     */
    private void populateArticleData(RecommendationViewHolder holder, Article article, int position) {
        // Load article image using binding adapter for improved reliability
        BindingAdapters.loadImage(holder.articleImage, article.getThumbnailUrl(), null);
        
        // Set title
        holder.titleTextView.setText(article.getTitle() != null ? 
                article.getTitle() : 
                "Article " + position + ": Fascinating insights you need to know");
        
        // Set snippet
        holder.snippetTextView.setText(article.getPreviewText() != null ? 
                article.getPreviewText() : 
                "This is a placeholder preview text for this interesting article. Learn more about the latest trends and insights...");
        
        // Set category & author (use real data if available, otherwise generate sample)
        String categoryAuthorText;
        if (article.getCategory() != null && article.getCategory().getName() != null) {
            String categoryName = article.getCategory().getName();
            String authorName = article.getAuthorName() != null ? 
                    article.getAuthorName() : 
                    SAMPLE_AUTHORS[position % SAMPLE_AUTHORS.length];
            categoryAuthorText = "In " + categoryName + " by " + authorName;
        } else {
            // Generate sample category and author
            String categoryName = SAMPLE_CATEGORIES[position % SAMPLE_CATEGORIES.length];
            String authorName = SAMPLE_AUTHORS[position % SAMPLE_AUTHORS.length];
            categoryAuthorText = "In " + categoryName + " by " + authorName;
        }
        holder.categoryAuthorTextView.setText(categoryAuthorText);
        
        // Set date (use real date if available, otherwise generate sample)
        String dateStr;
        if (article.getPublishedAt() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.US);
                Date date = inputFormat.parse(article.getPublishedAt());
                dateStr = outputFormat.format(date);
            } catch (Exception e) {
                dateStr = getRandomDate();
            }
        } else {
            dateStr = getRandomDate();
        }
        holder.dateTextView.setText(dateStr);
        
        // Set likes and comments (sample data as these aren't in model)
        Random random = new Random(position + 1); // Consistent random values based on position
        int likes = random.nextInt(200) + 5;
        int comments = random.nextInt(20);
        holder.likesTextView.setText(String.valueOf(likes));
        holder.commentsTextView.setText(String.valueOf(comments));
        
        // Set bookmark icon based on article state
        updateBookmarkIcon(holder.bookmarkIcon, article.isBookmarked());
    }
    
    /**
     * Generates a random date string for placeholder data.
     * 
     * @return A random month and day string like "May 12"
     */
    private String getRandomDate() {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Random random = new Random();
        int month = random.nextInt(12);
        int day = random.nextInt(28) + 1;
        
        return months[month] + " " + day;
    }

    /**
     * Sets a fixed number of recommended articles, useful for home page sections
     * that should show a limited number of items.
     *
     * @param articles The full list of articles
     * @param limit Maximum number of articles to display
     */
    public void setRecommendedArticles(List<Article> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            super.submitList(new ArrayList<>());
            return;
        }
        
        List<Article> limitedList = new ArrayList<>(
            articles.size() > limit ? 
            articles.subList(0, limit) : 
            articles
        );
        
        super.submitList(limitedList);
    }
    
    /**
     * Sets the full list of recommended articles.
     *
     * @param articles The list of articles
     */
    public void setRecommendedArticles(List<Article> articles) {
        super.submitList(articles);
    }
    
    /**
     * ViewHolder for recommendation article items.
     */
    public static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        final View topDivider;
        final ImageView articleImage;
        final TextView categoryAuthorTextView;
        final TextView titleTextView;
        final TextView snippetTextView;
        final TextView dateTextView;
        final TextView likesTextView;
        final TextView commentsTextView;
        final ImageView bookmarkIcon;
        final ImageView moreOptionsIcon;
        
        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            topDivider = itemView.findViewById(R.id.top_divider);
            articleImage = itemView.findViewById(R.id.recommendation_article_image);
            categoryAuthorTextView = itemView.findViewById(R.id.recommendation_article_category_author);
            titleTextView = itemView.findViewById(R.id.recommendation_article_title);
            snippetTextView = itemView.findViewById(R.id.recommendation_article_snippet);
            dateTextView = itemView.findViewById(R.id.recommendation_article_date);
            likesTextView = itemView.findViewById(R.id.recommendation_article_likes);
            commentsTextView = itemView.findViewById(R.id.recommendation_article_comments);
            bookmarkIcon = itemView.findViewById(R.id.recommendation_article_bookmark_icon);
            moreOptionsIcon = itemView.findViewById(R.id.recommendation_article_more_options_icon);
        }
    }
} 