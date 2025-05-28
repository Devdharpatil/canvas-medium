package com.canvamedium.repository;

import com.canvamedium.model.Article;
import com.canvamedium.model.Article.Status;
import com.canvamedium.model.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Article entity operations.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    /**
     * Find articles by title containing the given text (case-insensitive).
     *
     * @param title    The title to search for
     * @param pageable Pagination information
     * @return A page of articles matching the search criteria
     */
    Page<Article> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find all articles ordered by creation date (descending).
     *
     * @return List of articles ordered by creation date
     */
    List<Article> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find articles by template.
     *
     * @param template The template to search for
     * @param pageable Pagination information
     * @return A page of articles using the specified template
     */
    Page<Article> findByTemplate(Template template, Pageable pageable);
    
    /**
     * Find articles containing preview text (case-insensitive).
     *
     * @param previewText The preview text to search for
     * @param pageable    Pagination information
     * @return A page of articles with matching preview text
     */
    Page<Article> findByPreviewTextContainingIgnoreCase(String previewText, Pageable pageable);
    
    /**
     * Find articles by status.
     *
     * @param status   The article status
     * @param pageable Pagination information
     * @return A page of articles with the specified status
     */
    Page<Article> findByStatus(Status status, Pageable pageable);
    
    /**
     * Find articles by status ordered by creation date (descending).
     *
     * @param status The article status
     * @return List of articles with the specified status ordered by creation date
     */
    List<Article> findByStatusOrderByCreatedAtDesc(Status status);
    
    /**
     * Find published articles ordered by publication date (descending).
     *
     * @param pageable Pagination information
     * @return A page of published articles ordered by publication date
     */
    Page<Article> findByStatusOrderByPublishedAtDesc(Status status, Pageable pageable);
    
    /**
     * Find articles by status and title containing the given text (case-insensitive).
     *
     * @param status   The article status
     * @param title    The title to search for
     * @param pageable Pagination information
     * @return A page of articles matching the criteria
     */
    Page<Article> findByStatusAndTitleContainingIgnoreCase(Status status, String title, Pageable pageable);
    
    /**
     * Find articles by status and template.
     *
     * @param status   The article status
     * @param template The template to search for
     * @param pageable Pagination information
     * @return A page of articles matching the criteria
     */
    Page<Article> findByStatusAndTemplate(Status status, Template template, Pageable pageable);
    
    /**
     * Find featured articles.
     *
     * @param featured The featured flag
     * @param pageable Pagination information
     * @return A page of featured articles
     */
    Page<Article> findByFeatured(boolean featured, Pageable pageable);
    
    /**
     * Find featured articles with a specific status.
     *
     * @param featured The featured flag
     * @param status   The article status
     * @param pageable Pagination information
     * @return A page of featured articles with the specified status
     */
    Page<Article> findByFeaturedAndStatus(boolean featured, Status status, Pageable pageable);
    
    /**
     * Find articles by content containing the given text.
     * Note: This is a JSONB path search for Postgres.
     *
     * @param content  The content text to search for
     * @param pageable Pagination information
     * @return A page of articles with matching content
     */
    @Query(value = "SELECT a.* FROM article a WHERE a.content::text ILIKE CONCAT('%', :content, '%')", 
           nativeQuery = true)
    Page<Article> findByContentContaining(@Param("content") String content, Pageable pageable);
    
    /**
     * Find articles by title or preview text containing the given text (case-insensitive).
     *
     * @param searchTerm    The search term for title
     * @param sameSearchTerm The same search term for preview text
     * @param pageable      Pagination information
     * @return A page of articles matching the search term in title or preview text
     */
    Page<Article> findByTitleContainingIgnoreCaseOrPreviewTextContainingIgnoreCase(
            String searchTerm, String sameSearchTerm, Pageable pageable);
            
    /**
     * Find articles by template and featured flag.
     *
     * @param template The template
     * @param featured The featured flag
     * @param pageable Pagination information
     * @return A page of articles matching the criteria
     */
    Page<Article> findByTemplateAndFeatured(Template template, boolean featured, Pageable pageable);
    
    /**
     * Find articles by template, status, and featured flag.
     *
     * @param template The template
     * @param status   The article status
     * @param featured The featured flag
     * @param pageable Pagination information
     * @return A page of articles matching the criteria
     */
    Page<Article> findByTemplateAndStatusAndFeatured(Template template, Status status, boolean featured, Pageable pageable);
    
    /**
     * Comprehensive search query for articles using all possible filters.
     * Searches in title and preview text for the query term and applies status,
     * template, and featured filters if provided.
     *
     * @param query      The search query for title and preview text
     * @param status     Optional article status
     * @param template   Optional template
     * @param featured   Optional featured flag
     * @param pageable   Pagination information
     * @return A page of articles matching all criteria
     */
    @Query("SELECT a FROM Article a WHERE " +
           "(:query IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.previewText) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:template IS NULL OR a.template = :template) AND " +
           "(:featured IS NULL OR a.featured = :featured)")
    Page<Article> searchArticles(
            @Param("query") String query,
            @Param("status") Status status,
            @Param("template") Template template,
            @Param("featured") Boolean featured,
            Pageable pageable);
} 