package com.canvamedium.api;

import com.canvamedium.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API service interface for category-related operations.
 */
public interface CategoryService {

    /**
     * Get all categories with pagination.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of categories
     */
    @GET("api/categories")
    Call<ApiResponse<List<Category>>> getCategories(
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Get top-level categories.
     *
     * @return A call that returns a list of top-level categories
     */
    @GET("api/categories/top-level")
    Call<List<Category>> getTopLevelCategories();

    /**
     * Get child categories for a parent category.
     *
     * @param parentId The parent category ID
     * @return A call that returns a list of child categories
     */
    @GET("api/categories/children/{parentId}")
    Call<List<Category>> getChildCategories(
            @Path("parentId") Long parentId);

    /**
     * Get featured categories.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of featured categories
     */
    @GET("api/categories/featured")
    Call<ApiResponse<List<Category>>> getFeaturedCategories(
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Get popular categories.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of popular categories
     */
    @GET("api/categories/popular")
    Call<ApiResponse<List<Category>>> getPopularCategories(
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Get category by ID.
     *
     * @param id The category ID
     * @return A call that returns the category
     */
    @GET("api/categories/{id}")
    Call<Category> getCategoryById(
            @Path("id") Long id);

    /**
     * Get category by slug.
     *
     * @param slug The category slug
     * @return A call that returns the category
     */
    @GET("api/categories/slug/{slug}")
    Call<Category> getCategoryBySlug(
            @Path("slug") String slug);

    /**
     * Search categories by name.
     *
     * @param query The search query
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of categories matching the search
     */
    @GET("api/categories/search")
    Call<ApiResponse<List<Category>>> searchCategories(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size);
} 