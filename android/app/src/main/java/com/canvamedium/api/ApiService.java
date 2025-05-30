package com.canvamedium.api;

import com.canvamedium.model.Article;
import com.canvamedium.model.Content;
import com.canvamedium.model.Template;
import com.canvamedium.model.Tag;
import com.canvamedium.model.Category;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Query;

/**
 * Retrofit API service interface for communicating with the CanvaMedium backend.
 */
public interface ApiService {
    
    // Template endpoints
    
    /**
     * Get all templates with optional filtering and pagination.
     *
     * @param options Query parameters for filtering and pagination (page, size, sortBy, sortDir, name)
     * @return Call object containing the response
     */
    @GET("api/templates")
    Call<Map<String, Object>> getTemplates(@QueryMap Map<String, Object> options);
    
    /**
     * Get a template by ID.
     *
     * @param id The template ID
     * @return Call object containing the template
     */
    @GET("api/templates/{id}")
    Call<Template> getTemplateById(@Path("id") Long id);
    
    /**
     * Create a new template.
     *
     * @param template The template to create
     * @return Call object containing the created template
     */
    @POST("api/templates")
    Call<Template> createTemplate(@Body Template template);
    
    /**
     * Update an existing template.
     *
     * @param id       The template ID
     * @param template The updated template data
     * @return Call object containing the updated template
     */
    @PUT("api/templates/{id}")
    Call<Template> updateTemplate(@Path("id") Long id, @Body Template template);
    
    /**
     * Delete a template by ID.
     *
     * @param id The template ID
     * @return Call object containing the response
     */
    @DELETE("api/templates/{id}")
    Call<Void> deleteTemplate(@Path("id") Long id);
    
    // Article endpoints
    
    /**
     * Get all articles without pagination for the feed view.
     *
     * @return Call object containing the list of articles
     */
    @GET("api/articles/all")
    Call<List<Article>> getAllArticles();
    
    /**
     * Get all articles with optional filtering and pagination.
     *
     * @param options Query parameters for filtering and pagination (page, size, sortBy, sortDir, title, templateId)
     * @return Call object containing the response
     */
    @GET("api/articles")
    Call<Map<String, Object>> getArticles(@QueryMap Map<String, Object> options);
    
    /**
     * Get all published articles.
     *
     * @param options Query parameters for filtering and pagination
     * @return Call object containing the response
     */
    @GET("api/articles/published")
    Call<Map<String, Object>> getPublishedArticles(@QueryMap Map<String, Object> options);
    
    /**
     * Get all draft articles.
     *
     * @param options Query parameters for filtering and pagination
     * @return Call object containing the response
     */
    @GET("api/articles/drafts")
    Call<Map<String, Object>> getDraftArticles(@QueryMap Map<String, Object> options);
    
    /**
     * Get all archived articles.
     *
     * @param options Query parameters for filtering and pagination
     * @return Call object containing the response
     */
    @GET("api/articles/archived")
    Call<Map<String, Object>> getArchivedArticles(@QueryMap Map<String, Object> options);
    
    /**
     * Search for articles with multiple criteria.
     *
     * @param options Query parameters for search (query, status, templateId, featured, page, size, sortBy, sortDir)
     * @return Call object containing the search results
     */
    @GET("api/articles/search")
    Call<Map<String, Object>> searchArticles(@QueryMap Map<String, Object> options);
    
    /**
     * Search for articles by query string.
     *
     * @param query Search query string
     * @return Call object containing the list of matching articles
     */
    @GET("api/articles/search")
    Call<List<Article>> searchArticles(@Query("query") String query);
    
    /**
     * Get articles by category ID.
     *
     * @param categoryId The category ID
     * @return Call object containing the list of articles in the category
     */
    @GET("api/articles/category/{categoryId}")
    Call<List<Article>> getArticlesByCategory(@Path("categoryId") long categoryId);
    
    /**
     * Bookmark an article.
     *
     * @param articleId The ID of the article to bookmark
     * @return Call object containing the response
     */
    @POST("api/articles/{id}/bookmark")
    Call<ResponseBody> bookmarkArticle(@Path("id") long articleId);
    
    /**
     * Remove bookmark from an article.
     *
     * @param articleId The ID of the article to unbookmark
     * @return Call object containing the response
     */
    @DELETE("api/articles/{id}/bookmark")
    Call<ResponseBody> unbookmarkArticle(@Path("id") long articleId);
    
    /**
     * Get featured articles.
     *
     * @param options Query parameters for pagination
     * @return Call object containing the featured articles
     */
    @GET("api/articles/featured")
    Call<Map<String, Object>> getFeaturedArticles(@QueryMap Map<String, Object> options);
    
    /**
     * Get articles sorted by date (newest first).
     *
     * @return Call object containing the list of articles sorted by date
     */
    @GET("api/articles?sort=publishedAt,desc")
    Call<List<Article>> getArticlesSortedByDate();
    
    /**
     * Get articles sorted by popularity.
     *
     * @return Call object containing the list of articles sorted by views or popularity
     */
    @GET("api/articles?sort=popularity,desc")
    Call<List<Article>> getArticlesSortedByPopularity();
    
    /**
     * Get an article by ID.
     *
     * @param id The article ID
     * @return Call object containing the article
     */
    @GET("api/articles/{id}")
    Call<Article> getArticleById(@Path("id") Long id);
    
    /**
     * Create a new article.
     *
     * @param article The article to create
     * @return Call object containing the created article
     */
    @POST("api/articles")
    Call<Article> createArticle(@Body Article article);
    
    /**
     * Create a new draft article.
     *
     * @param article The draft article to create
     * @return Call object containing the created draft article
     */
    @POST("api/articles/drafts")
    Call<Article> createDraftArticle(@Body Article article);
    
    /**
     * Create a draft copy of an existing article.
     *
     * @param id The ID of the article to copy
     * @return Call object containing the created draft copy
     */
    @POST("api/articles/{id}/draft-copy")
    Call<Article> createDraftCopy(@Path("id") Long id);
    
    /**
     * Publish a draft article.
     *
     * @param id The ID of the draft article to publish
     * @return Call object containing the published article
     */
    @PUT("api/articles/{id}/publish")
    Call<Article> publishArticle(@Path("id") Long id);
    
    /**
     * Archive a published article.
     *
     * @param id The ID of the published article to archive
     * @return Call object containing the archived article
     */
    @PUT("api/articles/{id}/archive")
    Call<Article> archiveArticle(@Path("id") Long id);
    
    /**
     * Update an existing article.
     *
     * @param id      The article ID
     * @param article The updated article data
     * @return Call object containing the updated article
     */
    @PUT("api/articles/{id}")
    Call<Article> updateArticle(@Path("id") Long id, @Body Article article);
    
    /**
     * Delete an article by ID.
     *
     * @param id The article ID
     * @return Call object containing the response
     */
    @DELETE("api/articles/{id}")
    Call<Void> deleteArticle(@Path("id") Long id);
    
    // Media endpoints
    
    /**
     * Upload a file to the server.
     *
     * @param file The file part to upload
     * @return Call object containing the response with the file URL
     */
    @Multipart
    @POST("api/media/upload")
    Call<Map<String, String>> uploadFile(@Part MultipartBody.Part file);
    
    /**
     * Upload a file to the server with thumbnail generation.
     *
     * @param file The file part to upload
     * @return Call object containing the response with the file URL and thumbnail URL
     */
    @Multipart
    @POST("api/media/upload-with-thumbnail")
    Call<Map<String, String>> uploadFileWithThumbnail(@Part MultipartBody.Part file);
    
    /**
     * Generate a thumbnail for an existing image.
     *
     * @param params The parameters map containing the imageUrl key
     * @return Call object containing the response with the thumbnail URL
     */
    @POST("api/media/thumbnail")
    Call<Map<String, String>> generateThumbnail(@Body Map<String, String> params);
    
    /**
     * Delete a file from the server.
     *
     * @param filename The name of the file to delete
     * @return Call object containing the response
     */
    @DELETE("api/media/{filename}")
    Call<Map<String, Boolean>> deleteFile(@Path("filename") String filename);
    
    // Content endpoints
    
    /**
     * Get content by ID.
     *
     * @param id The content ID
     * @return Call object containing the content
     */
    @GET("api/contents/{id}")
    Call<Content> getContentById(@Path("id") Long id);
    
    /**
     * Create a new content.
     *
     * @param content The content to create
     * @return Call object containing the created content
     */
    @POST("api/contents")
    Call<Content> createContent(@Body Content content);
    
    /**
     * Update existing content.
     *
     * @param id      The content ID
     * @param content The updated content data
     * @return Call object containing the updated content
     */
    @PUT("api/contents/{id}")
    Call<Content> updateContent(@Path("id") Long id, @Body Content content);
    
    // Tag endpoints
    
    /**
     * Get all tags.
     *
     * @return Call object containing the list of tags
     */
    @GET("api/tags/all")
    Call<List<Tag>> getAllTags();
    
    /**
     * Get a tag by name.
     *
     * @param name The tag name
     * @return Call object containing the tag
     */
    @GET("api/tags/name/{name}")
    Call<Tag> getTagByName(@Path("name") String name);
    
    /**
     * Search tags by query string.
     *
     * @param query Search query string
     * @return Call object containing the list of matching tags
     */
    @GET("api/tags/search")
    Call<List<Tag>> searchTags(@Query("query") String query);
    
    // Category endpoints
    
    /**
     * Get all categories.
     *
     * @return Call object containing the list of categories
     */
    @GET("api/categories/all")
    Call<List<Category>> getAllCategories();
    
    /**
     * Get a category by ID.
     *
     * @param id The category ID
     * @return Call object containing the category
     */
    @GET("api/categories/{id}")
    Call<Category> getCategoryById(@Path("id") Long id);
    
    /**
     * Search categories by query string.
     *
     * @param query Search query string
     * @return Call object containing the list of matching categories
     */
    @GET("api/categories/search")
    Call<List<Category>> searchCategories(@Query("query") String query);
} 