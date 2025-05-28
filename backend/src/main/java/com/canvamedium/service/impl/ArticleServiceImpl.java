package com.canvamedium.service.impl;

import com.canvamedium.model.Article;
import com.canvamedium.model.Article.Status;
import com.canvamedium.model.Template;
import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.repository.TemplateRepository;
import com.canvamedium.service.ArticleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the Article service.
 */
@Service
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleRepository articleRepository;
    private final TemplateRepository templateRepository;
    
    /**
     * Constructor with repository dependencies injection.
     *
     * @param articleRepository  The article repository
     * @param templateRepository The template repository
     */
    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, TemplateRepository templateRepository) {
        this.articleRepository = articleRepository;
        this.templateRepository = templateRepository;
    }
    
    @Override
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }
    
    @Override
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }
    
    @Override
    @Transactional
    public Article createArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        
        // Set status to PUBLISHED if not already set
        if (article.getStatus() == null) {
            article.setStatus(Status.PUBLISHED);
        }
        
        return articleRepository.save(article);
    }
    
    @Override
    @Transactional
    public Article createDraftArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setStatus(Status.DRAFT);
        return articleRepository.save(article);
    }
    
    @Override
    @Transactional
    public Article updateArticle(Long id, Article articleDetails) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        
        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setPreviewText(articleDetails.getPreviewText());
        article.setThumbnailUrl(articleDetails.getThumbnailUrl());
        
        if (articleDetails.getTemplate() != null) {
            Template template = templateRepository.findById(articleDetails.getTemplate().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " 
                            + articleDetails.getTemplate().getId()));
            article.setTemplate(template);
        }
        
        // Update status if changed
        if (articleDetails.getStatus() != null && articleDetails.getStatus() != article.getStatus()) {
            article.setStatus(articleDetails.getStatus());
            
            // Set publishedAt if transitioning to PUBLISHED
            if (articleDetails.getStatus() == Status.PUBLISHED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        
        article.setUpdatedAt(LocalDateTime.now());
        
        return articleRepository.save(article);
    }
    
    @Override
    @Transactional
    public Article publishArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        
        if (article.getStatus() != Status.DRAFT) {
            throw new IllegalStateException("Only draft articles can be published. Current status: " + article.getStatus());
        }
        
        article.publish();
        return articleRepository.save(article);
    }
    
    @Override
    @Transactional
    public Article archiveArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        
        article.archive();
        return articleRepository.save(article);
    }
    
    @Override
    @Transactional
    public Article createDraftCopy(Long id) {
        Article originalArticle = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        
        Article draftCopy = originalArticle.createDraftCopy();
        return articleRepository.save(draftCopy);
    }
    
    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        
        articleRepository.delete(article);
    }
    
    @Override
    public Page<Article> searchArticlesByTitle(String title, Pageable pageable) {
        return articleRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    @Override
    public Page<Article> getArticlesByTemplateId(Long templateId, Pageable pageable) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));
        
        return articleRepository.findByTemplate(template, pageable);
    }
    
    @Override
    public Page<Article> getArticlesByStatus(Status status, Pageable pageable) {
        return articleRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Page<Article> getPublishedArticles(Pageable pageable) {
        return articleRepository.findByStatusOrderByPublishedAtDesc(Status.PUBLISHED, pageable);
    }
    
    @Override
    public Page<Article> getDraftArticles(Pageable pageable) {
        return articleRepository.findByStatus(Status.DRAFT, pageable);
    }
    
    @Override
    public Page<Article> searchArticlesByStatusAndTitle(Status status, String title, Pageable pageable) {
        return articleRepository.findByStatusAndTitleContainingIgnoreCase(status, title, pageable);
    }
    
    @Override
    public Page<Article> getArticlesByStatusAndTemplateId(Status status, Long templateId, Pageable pageable) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));
        
        return articleRepository.findByStatusAndTemplate(status, template, pageable);
    }
    
    @Override
    public Page<Article> getFeaturedArticles(Pageable pageable) {
        // Return articles marked as featured and published
        return articleRepository.findByFeaturedAndStatus(true, Status.PUBLISHED, pageable);
    }
    
    @Override
    public Page<Article> searchArticles(String query, Status status, Long templateId, Boolean featured, Pageable pageable) {
        Template template = null;
        if (templateId != null) {
            template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));
        }
        
        return articleRepository.searchArticles(query, status, template, featured, pageable);
    }

    @Override
    public Page<Article> searchArticles(String query, Pageable pageable) {
        // Implement search logic based on query (e.g., title or content)
        return articleRepository.findByTitleContainingIgnoreCase(query, pageable);
    }

    @Override
    public Page<Article> getArticlesByTemplate(Long templateId, Pageable pageable) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));
        return articleRepository.findByTemplate(template, pageable);
    }

    @Override
    public Page<Article> getArticlesByCategory(Long categoryId, Pageable pageable) {
        // This method requires CategoryRepository and logic to find articles by category
        // For now, returning an empty page or throwing an exception as Category is not yet implemented
        // You would typically inject CategoryRepository and use it here.
        throw new UnsupportedOperationException("getArticlesByCategory not yet implemented as Category entity is not fully integrated.");
    }

    @Override
    public Page<Article> getArticlesByTag(Long tagId, Pageable pageable) {
        // This method requires TagRepository and logic to find articles by tag
        // For now, returning an empty page or throwing an exception as Tag is not yet implemented
        // You would typically inject TagRepository and use it here.
        throw new UnsupportedOperationException("getArticlesByTag not yet implemented as Tag entity is not fully integrated.");
    }

    @Override
    @Transactional
    public Article setFeaturedStatus(Long id, boolean featured) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        
        article.setFeatured(featured);
        article.setUpdatedAt(LocalDateTime.now()); // Update timestamp when featured status changes
        
        return articleRepository.save(article);
    }
}
