package com.canvamedium.api;

import com.canvamedium.model.Tag;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API service interface for tag-related operations.
 */
public interface TagService {

    /**
     * Get all tags with pagination.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of tags
     */
    @GET("api/tags")
    Call<ApiResponse<List<Tag>>> getTags(
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Get popular tags.
     *
     * @param limit The maximum number of tags to return
     * @return A call that returns a list of popular tags
     */
    @GET("api/tags/popular")
    Call<List<Tag>> getPopularTags(
            @Query("limit") int limit);

    /**
     * Get tags for an article.
     *
     * @param articleId The article ID
     * @return A call that returns a list of tags for the article
     */
    @GET("api/articles/{articleId}/tags")
    Call<List<Tag>> getArticleTags(
            @Path("articleId") Long articleId);

    /**
     * Add a tag to an article.
     *
     * @param articleId The article ID
     * @param tag The tag to add
     * @return A call that returns the added tag
     */
    @POST("api/articles/{articleId}/tags")
    Call<Tag> addArticleTag(
            @Path("articleId") Long articleId,
            @Body Tag tag);

    /**
     * Remove a tag from an article.
     *
     * @param articleId The article ID
     * @param tagId The tag ID
     * @return A call with no content
     */
    @DELETE("api/articles/{articleId}/tags/{tagId}")
    Call<Void> removeArticleTag(
            @Path("articleId") Long articleId,
            @Path("tagId") Long tagId);

    /**
     * Search tags by name.
     *
     * @param query The search query
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of tags matching the search
     */
    @GET("api/tags/search")
    Call<ApiResponse<List<Tag>>> searchTags(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Create a new tag.
     *
     * @param tag The tag to create
     * @return A call that returns the created tag
     */
    @POST("api/tags")
    Call<Tag> createTag(@Body Tag tag);

    /**
     * Update an existing tag.
     *
     * @param id The tag ID
     * @param tag The updated tag
     * @return A call that returns the updated tag
     */
    @PUT("api/tags/{id}")
    Call<Tag> updateTag(
            @Path("id") Long id,
            @Body Tag tag);

    /**
     * Delete a tag.
     *
     * @param id The tag ID
     * @return A call with no content
     */
    @DELETE("api/tags/{id}")
    Call<Void> deleteTag(@Path("id") Long id);
} 