package com.canvamedium.service;

import com.canvamedium.model.Article;
import com.canvamedium.model.Article.Status;
import com.canvamedium.model.Template;
import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.repository.TemplateRepository;
import com.canvamedium.service.impl.ArticleServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Template template;
    private Article article1;
    private Article article2;
    private Article draftArticle;
    private JsonNode contentJson;
    private JsonNode layoutJson;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        String layoutStr = "{\"type\":\"container\",\"children\":[{\"type\":\"text\",\"content\":\"Sample Text\"}]}";
        layoutJson = objectMapper.readTree(layoutStr);
        
        String contentStr = "{\"title\":\"Article Title\",\"body\":\"Article content goes here\"}";
        contentJson = objectMapper.readTree(contentStr);

        template = new Template("Blog Template", layoutJson);
        template.setId(1L);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        article1 = new Article("First Article", contentJson, "Preview text for first article", "thumbnail1.jpg", template);
        article1.setId(1L);
        article1.setCreatedAt(LocalDateTime.now());
        article1.setUpdatedAt(LocalDateTime.now());
        article1.setStatus(Status.PUBLISHED);
        article1.setPublishedAt(LocalDateTime.now());

        article2 = new Article("Second Article", contentJson, "Preview text for second article", "thumbnail2.jpg", template);
        article2.setId(2L);
        article2.setCreatedAt(LocalDateTime.now());
        article2.setUpdatedAt(LocalDateTime.now());
        article2.setStatus(Status.PUBLISHED);
        article2.setPublishedAt(LocalDateTime.now());
        
        draftArticle = new Article("Draft Article", contentJson, "Preview text for draft article", "draft-thumbnail.jpg", template);
        draftArticle.setId(3L);
        draftArticle.setCreatedAt(LocalDateTime.now());
        draftArticle.setUpdatedAt(LocalDateTime.now());
        draftArticle.setStatus(Status.DRAFT);
    }

    @Test
    @DisplayName("Get all articles - success")
    void getAllArticles_shouldReturnAllArticles() {
        // Arrange
        List<Article> articles = Arrays.asList(article1, article2);
        when(articleRepository.findAll()).thenReturn(articles);

        // Act
        List<Article> result = articleService.getAllArticles();

        // Assert
        assertEquals(2, result.size());
        assertEquals("First Article", result.get(0).getTitle());
        assertEquals("Second Article", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Get all articles with pagination - success")
    void getAllArticlesWithPagination_shouldReturnArticlesPage() {
        // Arrange
        List<Article> articles = Arrays.asList(article1, article2);
        Page<Article> page = new PageImpl<>(articles);
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.getAllArticles(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Get article by ID - success")
    void getArticleById_withValidId_shouldReturnArticle() {
        // Arrange
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1));

        // Act
        Optional<Article> result = articleService.getArticleById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("First Article", result.get().getTitle());
    }

    @Test
    @DisplayName("Get article by ID - not found")
    void getArticleById_withInvalidId_shouldReturnEmptyOptional() {
        // Arrange
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<Article> result = articleService.getArticleById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Create article - success")
    void createArticle_shouldSetTimestampsAndSave() {
        // Arrange
        Article newArticle = new Article("New Article", contentJson, "Preview text", "thumbnail.jpg", template);
        when(articleRepository.save(any(Article.class))).thenReturn(newArticle);

        // Act
        Article result = articleService.createArticle(newArticle);

        // Assert
        assertNotNull(result);
        assertEquals("New Article", result.getTitle());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(articleRepository).save(newArticle);
    }
    
    @Test
    @DisplayName("Create draft article - success")
    void createDraftArticle_shouldSetDraftStatusAndSave() {
        // Arrange
        Article newDraftArticle = new Article("New Draft", contentJson, "Draft preview text", "draft.jpg", template);
        when(articleRepository.save(any(Article.class))).thenReturn(newDraftArticle);

        // Act
        Article result = articleService.createDraftArticle(newDraftArticle);

        // Assert
        assertNotNull(result);
        assertEquals("New Draft", result.getTitle());
        assertEquals(Status.DRAFT, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getPublishedAt());
        verify(articleRepository).save(newDraftArticle);
    }

    @Test
    @DisplayName("Update article - success")
    void updateArticle_withValidId_shouldUpdateFields() {
        // Arrange
        Article updatedArticle = new Article("Updated Article", contentJson, "Updated preview", "updated.jpg", template);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(articleRepository.save(any(Article.class))).thenReturn(updatedArticle);

        // Act
        Article result = articleService.updateArticle(1L, updatedArticle);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Article", result.getTitle());
        assertEquals("Updated preview", result.getPreviewText());
    }

    @Test
    @DisplayName("Update article - article not found")
    void updateArticle_withInvalidArticleId_shouldThrowException() {
        // Arrange
        Article updatedArticle = new Article("Updated Article", contentJson, "Updated preview", "updated.jpg", template);
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            articleService.updateArticle(999L, updatedArticle);
        });
    }

    @Test
    @DisplayName("Update article - template not found")
    void updateArticle_withInvalidTemplateId_shouldThrowException() {
        // Arrange
        Template invalidTemplate = new Template("Invalid Template", layoutJson);
        invalidTemplate.setId(999L);
        
        Article updatedArticle = new Article("Updated Article", contentJson, "Updated preview", "updated.jpg", invalidTemplate);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1));
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            articleService.updateArticle(1L, updatedArticle);
        });
    }
    
    @Test
    @DisplayName("Update article status - success")
    void updateArticle_withStatusChange_shouldUpdateStatusAndDates() {
        // Arrange
        Article draftToPublish = new Article("Draft To Publish", contentJson, "Preview text", "thumbnail.jpg", template);
        draftToPublish.setStatus(Status.DRAFT);
        
        Article updatedArticle = new Article("Draft To Publish", contentJson, "Preview text", "thumbnail.jpg", template);
        updatedArticle.setStatus(Status.PUBLISHED);
        
        when(articleRepository.findById(3L)).thenReturn(Optional.of(draftToPublish));
        when(articleRepository.save(any(Article.class))).thenReturn(updatedArticle);

        // Act
        Article result = articleService.updateArticle(3L, updatedArticle);

        // Assert
        assertNotNull(result);
        assertEquals(Status.PUBLISHED, result.getStatus());
    }

    @Test
    @DisplayName("Delete article - success")
    void deleteArticle_withValidId_shouldDeleteArticle() {
        // Arrange
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1));

        // Act & Assert
        assertDoesNotThrow(() -> articleService.deleteArticle(1L));
        verify(articleRepository).delete(article1);
    }

    @Test
    @DisplayName("Delete article - not found")
    void deleteArticle_withInvalidId_shouldThrowException() {
        // Arrange
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            articleService.deleteArticle(999L);
        });
    }

    @Test
    @DisplayName("Search articles by title - success")
    void searchArticlesByTitle_shouldReturnMatchingArticles() {
        // Arrange
        List<Article> articles = Arrays.asList(article1);
        Page<Article> page = new PageImpl<>(articles);
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Article> result = articleService.searchArticlesByTitle("First", pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("First Article", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Get articles by template ID - success")
    void getArticlesByTemplateId_shouldReturnMatchingArticles() {
        // Arrange
        List<Article> articles = Arrays.asList(article1, article2);
        Page<Article> page = new PageImpl<>(articles);
        Pageable pageable = PageRequest.of(0, 10);
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(articleRepository.findByTemplate(template, pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.getArticlesByTemplateId(1L, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("First Article", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Get articles by template ID - template not found")
    void getArticlesByTemplateId_withInvalidTemplateId_shouldThrowException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            articleService.getArticlesByTemplateId(999L, pageable);
        });
    }
    
    @Test
    @DisplayName("Publish article - success")
    void publishArticle_withDraftArticle_shouldChangeStatusAndSetPublishedAt() {
        // Arrange
        when(articleRepository.findById(3L)).thenReturn(Optional.of(draftArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Article result = articleService.publishArticle(3L);

        // Assert
        assertEquals(Status.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublishedAt());
    }
    
    @Test
    @DisplayName("Publish article - not found")
    void publishArticle_withInvalidId_shouldThrowException() {
        // Arrange
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            articleService.publishArticle(999L);
        });
    }
    
    @Test
    @DisplayName("Publish article - already published")
    void publishArticle_withAlreadyPublishedArticle_shouldThrowException() {
        // Arrange
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1)); // article1 is already published

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            articleService.publishArticle(1L);
        });
    }
    
    @Test
    @DisplayName("Archive article - success")
    void archiveArticle_shouldChangeStatusToArchived() {
        // Arrange
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Article result = articleService.archiveArticle(1L);

        // Assert
        assertEquals(Status.ARCHIVED, result.getStatus());
    }
    
    @Test
    @DisplayName("Create draft copy - success")
    void createDraftCopy_shouldCreateNewDraftArticle() {
        // Arrange
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article1));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article savedArticle = invocation.getArgument(0);
            savedArticle.setId(4L); // Simulate auto-generated ID
            return savedArticle;
        });

        // Act
        Article result = articleService.createDraftCopy(1L);

        // Assert
        assertEquals(4L, result.getId());
        assertEquals(article1.getTitle() + " (Draft)", result.getTitle());
        assertEquals(Status.DRAFT, result.getStatus());
        assertNull(result.getPublishedAt());
    }
    
    @Test
    @DisplayName("Get articles by status - success")
    void getArticlesByStatus_shouldReturnMatchingArticles() {
        // Arrange
        List<Article> draftArticles = Arrays.asList(draftArticle);
        Page<Article> page = new PageImpl<>(draftArticles);
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findByStatus(Status.DRAFT, pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.getArticlesByStatus(Status.DRAFT, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Draft Article", result.getContent().get(0).getTitle());
        assertEquals(Status.DRAFT, result.getContent().get(0).getStatus());
    }
    
    @Test
    @DisplayName("Get published articles - success")
    void getPublishedArticles_shouldReturnPublishedArticlesOrderedByPublishedAt() {
        // Arrange
        List<Article> publishedArticles = Arrays.asList(article1, article2);
        Page<Article> page = new PageImpl<>(publishedArticles);
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findByStatusOrderByPublishedAtDesc(Status.PUBLISHED, pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.getPublishedArticles(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(Status.PUBLISHED, result.getContent().get(0).getStatus());
        assertEquals(Status.PUBLISHED, result.getContent().get(1).getStatus());
    }
    
    @Test
    @DisplayName("Get draft articles - success")
    void getDraftArticles_shouldReturnDraftArticles() {
        // Arrange
        List<Article> draftArticles = Arrays.asList(draftArticle);
        Page<Article> page = new PageImpl<>(draftArticles);
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findByStatus(Status.DRAFT, pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.getDraftArticles(pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Draft Article", result.getContent().get(0).getTitle());
        assertEquals(Status.DRAFT, result.getContent().get(0).getStatus());
    }
    
    @Test
    @DisplayName("Search articles by status and title - success")
    void searchArticlesByStatusAndTitle_shouldReturnMatchingArticles() {
        // Arrange
        List<Article> publishedArticles = Arrays.asList(article1);
        Page<Article> page = new PageImpl<>(publishedArticles);
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findByStatusAndTitleContainingIgnoreCase(Status.PUBLISHED, "First", pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.searchArticlesByStatusAndTitle(Status.PUBLISHED, "First", pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("First Article", result.getContent().get(0).getTitle());
        assertEquals(Status.PUBLISHED, result.getContent().get(0).getStatus());
    }
    
    @Test
    @DisplayName("Get articles by status and template ID - success")
    void getArticlesByStatusAndTemplateId_shouldReturnMatchingArticles() {
        // Arrange
        List<Article> articles = Arrays.asList(article1);
        Page<Article> page = new PageImpl<>(articles);
        Pageable pageable = PageRequest.of(0, 10);
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(articleRepository.findByStatusAndTemplate(Status.PUBLISHED, template, pageable)).thenReturn(page);

        // Act
        Page<Article> result = articleService.getArticlesByStatusAndTemplateId(Status.PUBLISHED, 1L, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("First Article", result.getContent().get(0).getTitle());
        assertEquals(Status.PUBLISHED, result.getContent().get(0).getStatus());
    }

    @Test
    void searchArticles_withAllParameters_shouldReturnMatchingArticles() {
        // Arrange
        String query = "Test";
        Status status = Status.PUBLISHED;
        Long templateId = 1L;
        Boolean featured = true;
        PageRequest pageable = PageRequest.of(0, 10);
        
        Template template = new Template();
        template.setId(templateId);
        
        Article article = new Article();
        article.setTitle("Test Article");
        
        Page<Article> expectedPage = new PageImpl<>(List.of(article));
        
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(articleRepository.searchArticles(query, status, template, featured, pageable))
                .thenReturn(expectedPage);
        
        // Act
        Page<Article> result = articleService.searchArticles(query, status, templateId, featured, pageable);
        
        // Assert
        assertEquals(expectedPage, result);
        verify(templateRepository).findById(templateId);
        verify(articleRepository).searchArticles(query, status, template, featured, pageable);
    }
    
    @Test
    void searchArticles_withNullParameters_shouldReturnMatchingArticles() {
        // Arrange
        String query = "Test";
        PageRequest pageable = PageRequest.of(0, 10);
        
        Article article = new Article();
        article.setTitle("Test Article");
        
        Page<Article> expectedPage = new PageImpl<>(List.of(article));
        
        when(articleRepository.searchArticles(query, null, null, null, pageable))
                .thenReturn(expectedPage);
        
        // Act
        Page<Article> result = articleService.searchArticles(query, null, null, null, pageable);
        
        // Assert
        assertEquals(expectedPage, result);
        verify(articleRepository).searchArticles(query, null, null, null, pageable);
        verifyNoInteractions(templateRepository);
    }
    
    @Test
    void searchArticles_withInvalidTemplateId_shouldThrowEntityNotFoundException() {
        // Arrange
        String query = "Test";
        Long nonExistentTemplateId = 999L;
        PageRequest pageable = PageRequest.of(0, 10);
        
        when(templateRepository.findById(nonExistentTemplateId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
                articleService.searchArticles(query, null, nonExistentTemplateId, null, pageable));
        
        assertTrue(exception.getMessage().contains("Template not found"));
        verify(templateRepository).findById(nonExistentTemplateId);
        verifyNoInteractions(articleRepository);
    }
} 