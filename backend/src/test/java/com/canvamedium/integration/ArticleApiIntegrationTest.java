package com.canvamedium.integration;

import com.canvamedium.model.Article;
import com.canvamedium.model.Template;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Article API endpoints.
 */
@SqlGroup({
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/sql/clear_tables.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/sql/insert_test_templates.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/sql/insert_test_articles.sql")
})
public class ArticleApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode contentJson;
    private HttpHeaders headers;
    private Template testTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // Create sample content JSON
        String contentStr = "{\"title\":\"Article Title\",\"body\":\"Article content goes here\"}";
        contentJson = objectMapper.readTree(contentStr);
        
        // Create test template
        testTemplate = new Template();
        testTemplate.setId(1L);
        testTemplate.setName("Test Template");
        
        // Setup headers
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("GET /api/articles - should return list of articles")
    void getAllArticles_shouldReturnArticles() {
        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                createURL("/api/articles"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertTrue(responseBody.containsKey("articles"));
        assertTrue(responseBody.containsKey("totalItems"));
    }

    @Test
    @DisplayName("GET /api/articles/{id} - should return article by id")
    void getArticleById_withValidId_shouldReturnArticle() {
        // Act
        ResponseEntity<Article> response = restTemplate.getForEntity(
                createURL("/api/articles/1"),
                Article.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Article 1", response.getBody().getTitle());
    }

    @Test
    @DisplayName("GET /api/articles/{id} - should return 404 for non-existent article")
    void getArticleById_withInvalidId_shouldReturn404() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURL("/api/articles/999"),
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/articles with title parameter - should return filtered articles")
    void getArticles_withTitleParameter_shouldReturnFilteredArticles() {
        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                createURL("/api/articles?title=Test Article 1"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/articles with templateId parameter - should return filtered articles")
    void getArticles_withTemplateIdParameter_shouldReturnFilteredArticles() {
        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                createURL("/api/articles?templateId=1"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("POST /api/articles - should create new article")
    void createArticle_shouldReturnCreatedArticle() throws Exception {
        // Arrange
        Article newArticle = new Article(
            "Integration Test Article", 
            contentJson, 
            "This is a preview text for integration test", 
            "thumbnail.jpg",
            testTemplate
        );
        
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(newArticle), headers);
        
        // Act
        ResponseEntity<Article> response = restTemplate.postForEntity(
                createURL("/api/articles"),
                request,
                Article.class
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Article", response.getBody().getTitle());
        assertNotNull(response.getBody().getId());
    }

    @Test
    @DisplayName("PUT /api/articles/{id} - should update existing article")
    void updateArticle_withValidId_shouldReturnUpdatedArticle() throws Exception {
        // Arrange
        Article updatedArticle = new Article(
            "Updated Test Article", 
            contentJson, 
            "This is an updated preview text", 
            "updated.jpg",
            testTemplate
        );
        
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(updatedArticle), headers);
        
        // Act
        ResponseEntity<Article> response = restTemplate.exchange(
                createURL("/api/articles/1"),
                HttpMethod.PUT,
                request,
                Article.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Test Article", response.getBody().getTitle());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    @DisplayName("PUT /api/articles/{id} - should return 404 for non-existent article")
    void updateArticle_withInvalidId_shouldReturn404() throws Exception {
        // Arrange
        Article updatedArticle = new Article(
            "Updated Test Article", 
            contentJson, 
            "This is an updated preview text", 
            "updated.jpg",
            testTemplate
        );
        
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(updatedArticle), headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                createURL("/api/articles/999"),
                HttpMethod.PUT,
                request,
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/articles/{id} - should delete article")
    void deleteArticle_withValidId_shouldReturnNoContent() {
        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                createURL("/api/articles/1"),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify article is deleted
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                createURL("/api/articles/1"),
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/articles/all - should return all articles without pagination")
    void getAllArticlesWithoutPagination_shouldReturnAllArticles() {
        // Act
        ResponseEntity<Article[]> response = restTemplate.getForEntity(
                createURL("/api/articles/all"),
                Article[].class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }
} 