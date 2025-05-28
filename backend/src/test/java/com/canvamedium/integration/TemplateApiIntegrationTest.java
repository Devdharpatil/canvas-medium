package com.canvamedium.integration;

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
 * Integration tests for Template API endpoints.
 */
@SqlGroup({
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/sql/clear_tables.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/sql/insert_test_templates.sql")
})
public class TemplateApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode layoutJson;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() throws Exception {
        // Create sample layout JSON
        String layoutStr = "{\"type\":\"container\",\"children\":[{\"type\":\"text\",\"content\":\"Sample Text\"}]}";
        layoutJson = objectMapper.readTree(layoutStr);
        
        // Setup headers
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("GET /api/templates - should return list of templates")
    void getAllTemplates_shouldReturnTemplates() {
        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                createURL("/api/templates"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertTrue(responseBody.containsKey("templates"));
        assertTrue(responseBody.containsKey("totalItems"));
    }

    @Test
    @DisplayName("GET /api/templates/{id} - should return template by id")
    void getTemplateById_withValidId_shouldReturnTemplate() {
        // Act
        ResponseEntity<Template> response = restTemplate.getForEntity(
                createURL("/api/templates/1"),
                Template.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Template 1", response.getBody().getName());
    }

    @Test
    @DisplayName("GET /api/templates/{id} - should return 404 for non-existent template")
    void getTemplateById_withInvalidId_shouldReturn404() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                createURL("/api/templates/999"),
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/templates - should create new template")
    void createTemplate_shouldReturnCreatedTemplate() throws Exception {
        // Arrange
        Template newTemplate = new Template("Integration Test Template", layoutJson);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(newTemplate), headers);
        
        // Act
        ResponseEntity<Template> response = restTemplate.postForEntity(
                createURL("/api/templates"),
                request,
                Template.class
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Template", response.getBody().getName());
        assertNotNull(response.getBody().getId());
    }

    @Test
    @DisplayName("PUT /api/templates/{id} - should update existing template")
    void updateTemplate_withValidId_shouldReturnUpdatedTemplate() throws Exception {
        // Arrange
        Template updatedTemplate = new Template("Updated Test Template", layoutJson);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(updatedTemplate), headers);
        
        // Act
        ResponseEntity<Template> response = restTemplate.exchange(
                createURL("/api/templates/1"),
                HttpMethod.PUT,
                request,
                Template.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Test Template", response.getBody().getName());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    @DisplayName("PUT /api/templates/{id} - should return 404 for non-existent template")
    void updateTemplate_withInvalidId_shouldReturn404() throws Exception {
        // Arrange
        Template updatedTemplate = new Template("Updated Test Template", layoutJson);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(updatedTemplate), headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                createURL("/api/templates/999"),
                HttpMethod.PUT,
                request,
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/templates/{id} - should delete template")
    void deleteTemplate_withValidId_shouldReturnNoContent() {
        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                createURL("/api/templates/1"),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify template is deleted
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                createURL("/api/templates/1"),
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
} 