package com.canvamedium.service;

import com.canvamedium.model.Article;
import com.canvamedium.model.Article.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Article operations.
 */
public interface ArticleService {
    
    /**
     * Get all articles.
     *
     * @return List of all articles
     */
    List<Article> getAllArticles();
    
    /**
     * Get all articles with pagination.
     *
     * @param pageable Pagination information
     * @return A page of articles
     */
    Page<Article> getAllArticles(Pageable pageable);
    
    /**
     * Get article by ID.
     *
     * @param id The article ID
     * @return Optional containing the article if found
     */
    Optional<Article> getArticleById(Long id);
    
    /**
     * Create a new article.
     *
     * @param article The article to create
     * @return The created article
     */
    Article createArticle(Article article);
    
    /**
     * Create a new draft article.
     *
     * @param article The draft article to create
     * @return The created draft article
     */
    Article createDraftArticle(Article article);
    
    /**
     * Update an existing article.
     *
     * @param id      The ID of the article to update
     * @param article The updated article data
     * @return The updated article
     * @throws RuntimeException if the article with the given ID is not found
     */
    Article updateArticle(Long id, Article article);
    
    /**
     * Publish a draft article.
     *
     * @param id The ID of the draft article to publish
     * @return The published article
     * @throws RuntimeException if the article with the given ID is not found or is not in DRAFT status
     */
    Article publishArticle(Long id);
    
    /**
     * Archive an article.
     *
     * @param id The ID of the article to archive
     * @return The archived article
     * @throws RuntimeException if the article with the given ID is not found
     */
    Article archiveArticle(Long id);
    
    /**
     * Create a draft copy of an existing article.
     *
     * @param id The ID of the article to copy
     * @return The new draft copy
     * @throws RuntimeException if the article with the given ID is not found
     */
    Article createDraftCopy(Long id);
    
    /**
     * Delete an article by ID.
     *
     * @param id The ID of the article to delete
     * @throws RuntimeException if the article with the given ID is not found
     */
    void deleteArticle(Long id);
    
    /**
     * Search articles by title.
     *
     * @param title    The title to search for
     * @param pageable Pagination information
     * @return A page of articles matching the search criteria
     */
    Page<Article> searchArticlesByTitle(String title, Pageable pageable);
    
    /**
     * Get articles by template ID.
     *
     * @param templateId The template ID
     * @param pageable   Pagination information
     * @return A page of articles using the specified template
     */
    Page<Article> getArticlesByTemplateId(Long templateId, Pageable pageable);
    
    /**
     * Get articles by status.
     *
     * @param status   The article status
     * @param pageable Pagination information
     * @return A page of articles with the specified status
     */
    Page<Article> getArticlesByStatus(Status status, Pageable pageable);
    
    /**
     * Get published articles ordered by publication date.
     *
     * @param pageable Pagination information
     * @return A page of published articles
     */
    Page<Article> getPublishedArticles(Pageable pageable);
    
    /**
     * Get draft articles.
     *
     * @param pageable Pagination information
     * @return A page of draft articles
     */
    Page<Article> getDraftArticles(Pageable pageable);
    
    /**
     * Search articles by status and title.
     *
     * @param status   The article status
     * @param title    The title to search for
     * @param pageable Pagination information
     * @return A page of articles matching the criteria
     */
    Page<Article> searchArticlesByStatusAndTitle(Status status, String title, Pageable pageable);
    
    /**
     * Get articles by status and template ID.
     *
     * @param status     The article status
     * @param templateId The template ID
     * @param pageable   Pagination information
     * @return A page of articles matching the criteria
     */
    Page<Article> getArticlesByStatusAndTemplateId(Status status, Long templateId, Pageable pageable);
    
    /**
     * Get featured articles.
     * 
     * @param pageable Pagination information
     * @return A page of featured articles
     */
    Page<Article> getFeaturedArticles(Pageable pageable);
    
    /**
     * Comprehensive search for articles based on multiple criteria.
     *
     * @param query       The search query to look for in title and content
     * @param status      Optional filter by article status
     * @param templateId  Optional filter by template ID
     * @param featured    Optional filter for featured articles
     * @param pageable    Pagination information
     * @return A page of articles matching all provided criteria
     */
    Page<Article> searchArticles(String query, Status status, Long templateId, Boolean featured, Pageable pageable);
    
    /**
     * Search for articles.
     *
     * @param query    The search query
     * @param pageable Pagination information
     * @return Page of matching articles
     */
    Page<Article> searchArticles(String query, Pageable pageable);
    
    /**
     * Get articles by template.
     *
     * @param templateId The template ID
     * @param pageable   Pagination information
     * @return Page of articles using the given template
     */
    Page<Article> getArticlesByTemplate(Long templateId, Pageable pageable);
    
    /**
     * Get articles by category.
     *
     * @param categoryId The category ID
     * @param pageable   Pagination information
     * @return Page of articles in the given category
     */
    Page<Article> getArticlesByCategory(Long categoryId, Pageable pageable);
    
    /**
     * Get articles by tag.
     *
     * @param tagId    The tag ID
     * @param pageable Pagination information
     * @return Page of articles with the given tag
     */
    Page<Article> getArticlesByTag(Long tagId, Pageable pageable);
    
    /**
     * Set featured status for an article.
     *
     * @param id       The article ID
     * @param featured The featured status to set
     * @return The updated article
     */
    Article setFeaturedStatus(Long id, boolean featured);
} 