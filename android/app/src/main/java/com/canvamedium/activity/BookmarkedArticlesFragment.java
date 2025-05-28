package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.R;
import com.canvamedium.adapter.ArticleAdapter;
import com.canvamedium.api.ApiResponse;
import com.canvamedium.api.BookmarkService;
import com.canvamedium.api.RetrofitClient;
import com.canvamedium.model.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for displaying bookmarked articles.
 */
public class BookmarkedArticlesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArticleAdapter articleAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 20;

    public BookmarkedArticlesFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of the fragment.
     *
     * @return A new instance of BookmarkedArticlesFragment
     */
    public static BookmarkedArticlesFragment newInstance() {
        return new BookmarkedArticlesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarked_articles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_bookmarked_articles);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty_bookmarks);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        articleAdapter = new ArticleAdapter(new ArticleAdapter.ArticleClickListener() {
            @Override
            public void onArticleClick(Article article) {
                Intent intent = new Intent(requireActivity(), ArticleDetailActivity.class);
                intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE_ID, article.getId());
                startActivity(intent);
            }
            
            @Override
            public void onBookmarkClick(Article article, boolean isCurrentlyBookmarked) {
                // Handle bookmark click
                // Toggle the bookmark status
                BookmarkService bookmarkService = RetrofitClient.createService(BookmarkService.class);
                if (isCurrentlyBookmarked) {
                    bookmarkService.removeBookmark(article.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                // Refresh the list
                                swipeRefreshLayout.setRefreshing(true);
                                loadBookmarkedArticles();
                            } else {
                                showErrorMessage("Failed to remove bookmark");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showErrorMessage("Error: " + t.getMessage());
                        }
                    });
                }
            }
        });
        recyclerView.setAdapter(articleAdapter);

        // Add pagination scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreBookmarks();
                    }
                }
            }
        });

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            articleAdapter.clearArticles();
            loadBookmarkedArticles();
        });

        // Load initial data
        loadBookmarkedArticles();
    }

    /**
     * Loads bookmarked articles from the API.
     */
    private void loadBookmarkedArticles() {
        isLoading = true;
        showLoadingIndicator();
        
        BookmarkService bookmarkService = RetrofitClient.createService(BookmarkService.class);
        bookmarkService.getBookmarks(currentPage, PAGE_SIZE).enqueue(new Callback<ApiResponse<List<Article>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Article>>> call, @NonNull Response<ApiResponse<List<Article>>> response) {
                hideLoadingIndicator();
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Article>> apiResponse = response.body();
                    List<Article> articles = apiResponse.getContent();
                    
                    if (articles.isEmpty() && currentPage == 0) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                        articleAdapter.setArticles(articles);
                        
                        // Check if last page
                        isLastPage = currentPage >= apiResponse.getTotalPages() - 1;
                        if (!isLastPage) {
                            currentPage++;
                        }
                    }
                } else {
                    showErrorMessage("Failed to load bookmarks");
                    if (currentPage == 0) {
                        showEmptyView();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Article>>> call, @NonNull Throwable t) {
                hideLoadingIndicator();
                isLoading = false;
                showErrorMessage("Error: " + t.getMessage());
                if (currentPage == 0) {
                    showEmptyView();
                }
            }
        });
    }

    /**
     * Loads more bookmarked articles for pagination.
     */
    private void loadMoreBookmarks() {
        isLoading = true;
        
        BookmarkService bookmarkService = RetrofitClient.createService(BookmarkService.class);
        bookmarkService.getBookmarks(currentPage, PAGE_SIZE).enqueue(new Callback<ApiResponse<List<Article>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Article>>> call, @NonNull Response<ApiResponse<List<Article>>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Article>> apiResponse = response.body();
                    List<Article> articles = apiResponse.getContent();
                    
                    articleAdapter.addArticles(articles);
                    
                    // Check if last page
                    isLastPage = currentPage >= apiResponse.getTotalPages() - 1;
                    if (!isLastPage) {
                        currentPage++;
                    }
                } else {
                    showErrorMessage("Failed to load more bookmarks");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Article>>> call, @NonNull Throwable t) {
                isLoading = false;
                showErrorMessage("Error loading more: " + t.getMessage());
            }
        });
    }

    /**
     * Shows the loading indicator.
     */
    private void showLoadingIndicator() {
        if (currentPage == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(true);
    }

    /**
     * Hides the loading indicator.
     */
    private void hideLoadingIndicator() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Shows the empty view when no bookmarks are available.
     */
    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the empty view.
     */
    private void hideEmptyView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    /**
     * Shows an error message.
     *
     * @param message The error message
     */
    private void showErrorMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
} 