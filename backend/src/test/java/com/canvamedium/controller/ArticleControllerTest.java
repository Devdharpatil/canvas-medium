package com.canvamedium.controller;

import com.canvamedium.model.Article;
import com.canvamedium.model.Template;
import com.canvamedium.service.ArticleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    private Template template;
    private Article article1;
    private Article article2;
    private JsonNode contentJson;
    private JsonNode layoutJson;

    @BeforeEach
    void setUp() throws Exception {
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

        article2 = new Article("Second Article", contentJson, "Preview text for second article", "thumbnail2.jpg", template);
        article2.setId(2L);
        article2.setCreatedAt(LocalDateTime.now());
        article2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/articles - success")
    void getAllArticles_shouldReturnAllArticles() throws Exception {
        // Arrange
        List<Article> articles = Arrays.asList(article1, article2);
        Page<Article> page = new PageImpl<>(articles);
        when(articleService.getAllArticles(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(2)))
                .andExpect(jsonPath("$.articles[0].title", is("First Article")))
                .andExpect(jsonPath("$.articles[1].title", is("Second Article")))
                .andExpect(jsonPath("$.totalItems", is(2)));
    }

    @Test
    @DisplayName("GET /api/articles with title parameter - success")
    void getAllArticles_withTitleParam_shouldReturnFilteredArticles() throws Exception {
        // Arrange
        List<Article> articles = Arrays.asList(article1);
        Page<Article> page = new PageImpl<>(articles);
        when(articleService.searchArticlesByTitle(anyString(), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/articles")
                        .param("title", "First"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(1)))
                .andExpect(jsonPath("$.articles[0].title", is("First Article")))
                .andExpect(jsonPath("$.totalItems", is(1)));
    }

    @Test
    @DisplayName("GET /api/articles with templateId parameter - success")
    void getAllArticles_withTemplateIdParam_shouldReturnFilteredArticles() throws Exception {
        // Arrange
        List<Article> articles = Arrays.asList(article1, article2);
        Page<Article> page = new PageImpl<>(articles);
        when(articleService.getArticlesByTemplateId(anyLong(), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/articles")
                        .param("templateId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(2)))
                .andExpect(jsonPath("$.totalItems", is(2)));
    }

    @Test
    @DisplayName("GET /api/articles/{id} - success")
    void getArticleById_withValidId_shouldReturnArticle() throws Exception {
        // Arrange
        when(articleService.getArticleById(1L)).thenReturn(Optional.of(article1));

        // Act & Assert
        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("First Article")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/articles/{id} - not found")
    void getArticleById_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        when(articleService.getArticleById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/articles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/articles - success")
    void createArticle_withValidData_shouldReturnCreatedArticle() throws Exception {
        // Arrange
        Article newArticle = new Article("New Article", contentJson, "Preview text", "thumbnail.jpg", template);
        when(articleService.createArticle(any(Article.class))).thenReturn(newArticle);

        // Act & Assert
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newArticle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Article")));
    }

    @Test
    @DisplayName("PUT /api/articles/{id} - success")
    void updateArticle_withValidData_shouldReturnUpdatedArticle() throws Exception {
        // Arrange
        Article updatedArticle = new Article("Updated Article", contentJson, "Updated preview", "updated.jpg", template);
        when(articleService.updateArticle(eq(1L), any(Article.class))).thenReturn(updatedArticle);

        // Act & Assert
        mockMvc.perform(put("/api/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedArticle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Article")));
    }

    @Test
    @DisplayName("PUT /api/articles/{id} - not found")
    void updateArticle_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        Article updatedArticle = new Article("Updated Article", contentJson, "Updated preview", "updated.jpg", template);
        when(articleService.updateArticle(eq(999L), any(Article.class)))
                .thenThrow(new EntityNotFoundException("Article not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/articles/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedArticle)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/articles/{id} - success")
    void deleteArticle_withValidId_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/articles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/articles/{id} - not found")
    void deleteArticle_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Article not found with id: 999"))
                .when(articleService).deleteArticle(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/articles/999"))
                .andExpect(status().isNotFound());
    }
} 