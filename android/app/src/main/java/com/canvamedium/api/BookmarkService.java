package com.canvamedium.api;

import com.canvamedium.model.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API service interface for bookmark-related operations.
 */
public interface BookmarkService {

    /**
     * Get all bookmarked articles for the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return A call that returns a paginated list of bookmarked articles
     */
    @GET("api/bookmarks")
    Call<ApiResponse<List<Article>>> getBookmarks(
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Bookmark an article.
     *
     * @param articleId The ID of the article to bookmark
     * @return A call with no content
     */
    @POST("api/bookmarks/{articleId}")
    Call<Void> bookmarkArticle(
            @Path("articleId") Long articleId);

    /**
     * Remove a bookmark.
     *
     * @param articleId The ID of the article to remove from bookmarks
     * @return A call with no content
     */
    @DELETE("api/bookmarks/{articleId}")
    Call<Void> removeBookmark(
            @Path("articleId") Long articleId);

    /**
     * Check if an article is bookmarked by the current user.
     *
     * @param articleId The article ID
     * @return A call that returns a boolean indicating if the article is bookmarked
     */
    @GET("api/bookmarks/check/{articleId}")
    Call<Boolean> isArticleBookmarked(
            @Path("articleId") Long articleId);
} 