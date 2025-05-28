package com.canvamedium.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Abstract scroll listener for handling pagination in RecyclerViews.
 * This listener detects when the user is nearing the end of the list
 * and triggers a load more data request when needed.
 */
public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

    private final LinearLayoutManager layoutManager;
    private static final int VISIBLE_THRESHOLD = 5; // Load more when this many items remain

    /**
     * Constructor for the PaginationScrollListener
     *
     * @param layoutManager the LinearLayoutManager associated with the RecyclerView
     */
    public PaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    /**
     * Called when the RecyclerView is scrolled
     *
     * @param recyclerView the RecyclerView being scrolled
     * @param dx horizontal scroll amount
     * @param dy vertical scroll amount
     */
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        // Only proceed if scrolling down
        if (dy <= 0) {
            return;
        }

        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        // If loading or at the last page, don't do anything
        if (isLoading() || isLastPage()) {
            return;
        }

        // If we're near the end of the list and there are more pages, load more
        if (lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            loadMoreItems();
        }
    }

    /**
     * Load more items from the data source
     */
    protected abstract void loadMoreItems();

    /**
     * Check if data is currently being loaded
     *
     * @return true if loading, false otherwise
     */
    public abstract boolean isLoading();

    /**
     * Check if the last page has been reached
     *
     * @return true if on the last page, false otherwise
     */
    public abstract boolean isLastPage();
} 