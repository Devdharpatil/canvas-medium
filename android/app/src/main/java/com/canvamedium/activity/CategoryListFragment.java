package com.canvamedium.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.R;
import com.canvamedium.adapter.CategoryAdapter;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiResponse;
import com.canvamedium.api.CategoryService;
import com.canvamedium.model.Category;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for displaying a list of categories.
 */
public class CategoryListFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_FEATURED = 1;
    public static final int TYPE_POPULAR = 2;

    private static final String ARG_TYPE = "type";
    private static final int PAGE_SIZE = 20;

    private int type;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CategoryAdapter adapter;
    private CategoryService categoryService;

    /**
     * Creates a new instance of the fragment.
     *
     * @param type The category list type (ALL, FEATURED, POPULAR)
     * @return A new instance of the fragment
     */
    public static CategoryListFragment newInstance(int type) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE, TYPE_ALL);
        }
        categoryService = ApiClient.getClient().create(CategoryService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        // Setup adapter
        adapter = new CategoryAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Setup layout manager with span count 2 for grid layout
        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        recyclerView.setLayoutManager(layoutManager);

        // Setup pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshCategories);

        // Setup scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && !isLoading && hasMoreData) {
                    loadMoreCategories();
                }
            }
        });

        // Load initial data
        loadCategories();
    }

    /**
     * Refreshes the categories list.
     */
    private void refreshCategories() {
        currentPage = 0;
        hasMoreData = true;
        adapter.clearCategories();
        loadCategories();
    }

    /**
     * Loads more categories (pagination).
     */
    private void loadMoreCategories() {
        currentPage++;
        loadCategories();
    }

    /**
     * Loads categories based on the type and current page.
     */
    private void loadCategories() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            swipeRefreshLayout.setRefreshing(true);
        }

        switch (type) {
            case TYPE_FEATURED:
                Call<ApiResponse<List<Category>>> featuredCall = categoryService.getFeaturedCategories(currentPage, PAGE_SIZE);
                featuredCall.enqueue(new Callback<ApiResponse<List<Category>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                        handleApiResponse(response);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                        handleCallFailure(t);
                    }
                });
                break;
            case TYPE_POPULAR:
                Call<ApiResponse<List<Category>>> popularCall = categoryService.getPopularCategories(currentPage, PAGE_SIZE);
                popularCall.enqueue(new Callback<ApiResponse<List<Category>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                        handleApiResponse(response);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                        handleCallFailure(t);
                    }
                });
                break;
            case TYPE_ALL:
            default:
                if (currentPage == 0) {
                    Call<List<Category>> topLevelCall = categoryService.getTopLevelCategories();
                    topLevelCall.enqueue(new Callback<List<Category>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                            handleListResponse(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                            handleCallFailure(t);
                        }
                    });
                } else {
                    Call<ApiResponse<List<Category>>> paginatedCall = categoryService.getCategories(currentPage, PAGE_SIZE);
                    paginatedCall.enqueue(new Callback<ApiResponse<List<Category>>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                            handleApiResponse(response);
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                            handleCallFailure(t);
                        }
                    });
                }
                break;
        }
    }

    /**
     * Handles API response for paginated data.
     */
    private <T> void handleApiResponse(Response<ApiResponse<List<Category>>> response) {
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);

        if (response.isSuccessful() && response.body() != null) {
            handlePaginatedResponse(response.body());
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles list response for non-paginated data.
     */
    private void handleListResponse(Response<List<Category>> response) {
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);

        if (response.isSuccessful() && response.body() != null) {
            List<Category> categories = response.body();
            if (categories != null && !categories.isEmpty()) {
                adapter.addCategories(categories);
                hasMoreData = categories.size() >= PAGE_SIZE;
            } else {
                hasMoreData = false;
                if (currentPage == 0) {
                    showEmptyState();
                }
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles call failure.
     */
    private void handleCallFailure(Throwable t) {
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(requireContext(), getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles paginated response from the API.
     *
     * @param response The API response
     */
    private void handlePaginatedResponse(ApiResponse<List<Category>> response) {
        List<Category> categories = response.getContent();
        if (categories != null && !categories.isEmpty()) {
            adapter.addCategories(categories);
            hasMoreData = !response.isLast();
        } else {
            hasMoreData = false;
            if (currentPage == 0) {
                showEmptyState();
            }
        }
    }

    /**
     * Shows empty state UI when no categories are available.
     */
    private void showEmptyState() {
        // The parent activity will handle empty state visibility
        if (getActivity() instanceof CategoryBrowseActivity) {
            ((CategoryBrowseActivity) getActivity()).showEmptyState(true);
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        // Handle category click - Navigate to articles in this category
        if (getActivity() instanceof CategoryBrowseActivity) {
            ((CategoryBrowseActivity) getActivity()).onCategorySelected(category);
        }
    }
} 