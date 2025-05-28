package com.canvamedium.controller;

import com.canvamedium.model.Article;
import com.canvamedium.model.Article.Status;
import com.canvamedium.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing Article entities.
 */
@RestController
@RequestMapping("/api/articles")
@Tag(name = "Article", description = "Article management API")
public class ArticleController {
    
    private final ArticleService articleService;
    
    /**
     * Constructor with service dependency injection.
     *
     * @param articleService The article service
     */
    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }
    
    /**
     * Get all articles.
     *
     * @param page       Page number (optional, default 0)
     * @param size       Page size (optional, default 10)
     * @param sortBy     Field to sort by (optional, default "createdAt")
     * @param sortDir    Sort direction (optional, default "desc")
     * @param title      Filter by article title (optional)
     * @param templateId Filter by template ID (optional)
     * @param status     Filter by article status (optional)
     * @return ResponseEntity containing the list of articles
     */
    @GetMapping
    @Operation(summary = "Get all articles", description = "Get a list of all articles with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved articles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getAllArticles(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by article title") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by template ID") @RequestParam(required = false) Long templateId,
            @Parameter(description = "Filter by article status (DRAFT, PUBLISHED, ARCHIVED)") 
                @RequestParam(required = false) Status status) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Article> articlePage;
        
        if (status != null) {
            if (templateId != null) {
                articlePage = articleService.getArticlesByStatusAndTemplateId(status, templateId, pageRequest);
            } else if (title != null && !title.isEmpty()) {
                articlePage = articleService.searchArticlesByStatusAndTitle(status, title, pageRequest);
            } else {
                articlePage = articleService.getArticlesByStatus(status, pageRequest);
            }
        } else if (templateId != null) {
            articlePage = articleService.getArticlesByTemplateId(templateId, pageRequest);
        } else if (title != null && !title.isEmpty()) {
            articlePage = articleService.searchArticlesByTitle(title, pageRequest);
        } else {
            articlePage = articleService.getAllArticles(pageRequest);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());
        response.put("currentPage", articlePage.getNumber());
        response.put("totalItems", articlePage.getTotalElements());
        response.put("totalPages", articlePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all published articles.
     *
     * @param page    Page number (optional, default 0)
     * @param size    Page size (optional, default 10)
     * @param sortBy  Field to sort by (optional, default "publishedAt")
     * @param sortDir Sort direction (optional, default "desc")
     * @return ResponseEntity containing the list of published articles
     */
    @GetMapping("/published")
    @Operation(summary = "Get all published articles", description = "Get a list of all published articles with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved published articles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPublishedArticles(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "publishedAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Article> articlePage = articleService.getPublishedArticles(pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());
        response.put("currentPage", articlePage.getNumber());
        response.put("totalItems", articlePage.getTotalElements());
        response.put("totalPages", articlePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all draft articles.
     *
     * @param page    Page number (optional, default 0)
     * @param size    Page size (optional, default 10)
     * @param sortBy  Field to sort by (optional, default "updatedAt")
     * @param sortDir Sort direction (optional, default "desc")
     * @return ResponseEntity containing the list of draft articles
     */
    @GetMapping("/drafts")
    @Operation(summary = "Get all draft articles", description = "Get a list of all draft articles with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved draft articles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getDraftArticles(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Article> articlePage = articleService.getDraftArticles(pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());
        response.put("currentPage", articlePage.getNumber());
        response.put("totalItems", articlePage.getTotalElements());
        response.put("totalPages", articlePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get an article by ID.
     *
     * @param id The article ID
     * @return ResponseEntity containing the article
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID", description = "Get a specific article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved article",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Article> getArticleById(
            @Parameter(description = "Article ID", required = true) @PathVariable Long id) {
        
        return articleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new article.
     *
     * @param article The article to create
     * @return ResponseEntity containing the created article
     */
    @PostMapping
    @Operation(summary = "Create a new article", description = "Create a new article with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Article successfully created",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Article> createArticle(
            @Parameter(description = "Article data", required = true) @Valid @RequestBody Article article) {
        
        Article createdArticle = articleService.createArticle(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
    }
    
    /**
     * Create a new draft article.
     *
     * @param article The draft article to create
     * @return ResponseEntity containing the created draft article
     */
    @PostMapping("/drafts")
    @Operation(summary = "Create a new draft article", description = "Create a new draft article with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Draft article successfully created",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Article> createDraftArticle(
            @Parameter(description = "Draft article data", required = true) @Valid @RequestBody Article article) {
        
        Article createdDraftArticle = articleService.createDraftArticle(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDraftArticle);
    }
    
    /**
     * Create a draft copy of an existing article.
     *
     * @param id The ID of the article to copy
     * @return ResponseEntity containing the created draft copy
     */
    @PostMapping("/{id}/draft-copy")
    @Operation(summary = "Create a draft copy of an article", description = "Create a new draft based on an existing article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Draft copy successfully created",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "404", description = "Source article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Article> createDraftCopy(
            @Parameter(description = "Source article ID", required = true) @PathVariable Long id) {
        
        try {
            Article draftCopy = articleService.createDraftCopy(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(draftCopy);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Publish a draft article.
     *
     * @param id The ID of the draft article to publish
     * @return ResponseEntity containing the published article
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish a draft article", description = "Change the status of a draft article to published")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Article successfully published",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "400", description = "Article is not in draft status"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> publishArticle(
            @Parameter(description = "Draft article ID", required = true) @PathVariable Long id) {
        
        try {
            Article publishedArticle = articleService.publishArticle(id);
            return ResponseEntity.ok(publishedArticle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Archive an article.
     *
     * @param id The ID of the article to archive
     * @return ResponseEntity containing the archived article
     */
    @PutMapping("/{id}/archive")
    @Operation(summary = "Archive an article", description = "Change the status of an article to archived")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Article successfully archived",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> archiveArticle(
            @Parameter(description = "Article ID", required = true) @PathVariable Long id) {
        
        try {
            Article archivedArticle = articleService.archiveArticle(id);
            return ResponseEntity.ok(archivedArticle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update an existing article.
     *
     * @param id      The ID of the article to update
     * @param article The updated article data
     * @return ResponseEntity containing the updated article
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing article", description = "Update an article with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Article successfully updated",
                    content = @Content(schema = @Schema(implementation = Article.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Article> updateArticle(
            @Parameter(description = "Article ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated article data", required = true) @Valid @RequestBody Article article) {
        
        try {
            Article updatedArticle = articleService.updateArticle(id, article);
            return ResponseEntity.ok(updatedArticle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete an article by ID.
     *
     * @param id The ID of the article to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an article", description = "Delete an article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Article successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "Article ID", required = true) @PathVariable Long id) {
        
        try {
            articleService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get featured articles.
     *
     * @param page    Page number (optional, default 0)
     * @param size    Page size (optional, default 10)
     * @param sortDir Sort direction (optional, default "desc")
     * @return ResponseEntity containing the list of featured articles
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured articles", description = "Get a list of featured articles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved featured articles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getFeaturedArticles(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, "publishedAt"));
        
        Page<Article> articlePage = articleService.getFeaturedArticles(pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());
        response.put("currentPage", articlePage.getNumber());
        response.put("totalItems", articlePage.getTotalElements());
        response.put("totalPages", articlePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search articles with multiple criteria.
     *
     * @param query      Search query for title and content (optional)
     * @param status     Article status filter (optional)
     * @param templateId Template ID filter (optional)
     * @param featured   Featured article filter (optional)
     * @param page       Page number (optional, default 0)
     * @param size       Page size (optional, default 10)
     * @param sortBy     Field to sort by (optional, default "updatedAt")
     * @param sortDir    Sort direction (optional, default "desc")
     * @return ResponseEntity containing the search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search articles", description = "Search articles with multiple criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> searchArticles(
            @Parameter(description = "Search query for title and content") @RequestParam(required = false) String query,
            @Parameter(description = "Article status filter") @RequestParam(required = false) Status status,
            @Parameter(description = "Template ID filter") @RequestParam(required = false) Long templateId,
            @Parameter(description = "Featured article filter") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Article> articlePage = articleService.searchArticles(query, status, templateId, featured, pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());
        response.put("currentPage", articlePage.getNumber());
        response.put("totalItems", articlePage.getTotalElements());
        response.put("totalPages", articlePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get articles by category.
     *
     * @param categoryId The category ID
     * @param page       The page number (0-based)
     * @param size       The page size
     * @param sortBy     The field to sort by
     * @param sortDir    The sort direction
     * @return Response entity containing the page of articles
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get articles by category", description = "Returns articles in a specific category")
    public ResponseEntity<Page<Article>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Article> articles = articleService.getArticlesByCategory(categoryId, pageable);
        
        return ResponseEntity.ok(articles);
    }

    /**
     * Get articles by tag.
     *
     * @param tagId    The tag ID
     * @param page     The page number (0-based)
     * @param size     The page size
     * @param sortBy   The field to sort by
     * @param sortDir  The sort direction
     * @return Response entity containing the page of articles
     */
    @GetMapping("/tag/{tagId}")
    @Operation(summary = "Get articles by tag", description = "Returns articles with a specific tag")
    public ResponseEntity<Page<Article>> getArticlesByTag(
            @PathVariable Long tagId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Article> articles = articleService.getArticlesByTag(tagId, pageable);
        
        return ResponseEntity.ok(articles);
    }

    /**
     * Set featured status for an article.
     *
     * @param id       The article ID
     * @param featured The featured status to set
     * @return Response entity containing the updated article
     */
    @PutMapping("/{id}/featured")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Set featured status", description = "Updates the featured status of an article")
    public ResponseEntity<Article> setFeaturedStatus(
            @PathVariable Long id,
            @RequestParam boolean featured) {
        
        Article article = articleService.setFeaturedStatus(id, featured);
        return ResponseEntity.ok(article);
    }
} 