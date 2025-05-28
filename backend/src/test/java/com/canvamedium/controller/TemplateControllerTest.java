package com.canvamedium.controller;

import com.canvamedium.model.Template;
import com.canvamedium.service.TemplateService;
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

@WebMvcTest(TemplateController.class)
public class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    private Template template1;
    private Template template2;
    private JsonNode layoutJson;

    @BeforeEach
    void setUp() throws Exception {
        String layoutStr = "{\"type\":\"container\",\"children\":[{\"type\":\"text\",\"content\":\"Sample Text\"}]}";
        layoutJson = objectMapper.readTree(layoutStr);

        template1 = new Template("Basic Template", layoutJson);
        template1.setId(1L);
        template1.setCreatedAt(LocalDateTime.now());
        template1.setUpdatedAt(LocalDateTime.now());

        template2 = new Template("Advanced Template", layoutJson);
        template2.setId(2L);
        template2.setCreatedAt(LocalDateTime.now());
        template2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/templates - success")
    void getAllTemplates_shouldReturnAllTemplates() throws Exception {
        // Arrange
        List<Template> templates = Arrays.asList(template1, template2);
        Page<Template> page = new PageImpl<>(templates);
        when(templateService.getAllTemplates(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templates", hasSize(2)))
                .andExpect(jsonPath("$.templates[0].name", is("Basic Template")))
                .andExpect(jsonPath("$.templates[1].name", is("Advanced Template")))
                .andExpect(jsonPath("$.totalItems", is(2)));
    }

    @Test
    @DisplayName("GET /api/templates with name parameter - success")
    void getAllTemplates_withNameParam_shouldReturnFilteredTemplates() throws Exception {
        // Arrange
        List<Template> templates = Arrays.asList(template1);
        Page<Template> page = new PageImpl<>(templates);
        when(templateService.searchTemplatesByName(anyString(), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/templates")
                        .param("name", "Basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templates", hasSize(1)))
                .andExpect(jsonPath("$.templates[0].name", is("Basic Template")))
                .andExpect(jsonPath("$.totalItems", is(1)));
    }

    @Test
    @DisplayName("GET /api/templates/{id} - success")
    void getTemplateById_withValidId_shouldReturnTemplate() throws Exception {
        // Arrange
        when(templateService.getTemplateById(1L)).thenReturn(Optional.of(template1));

        // Act & Assert
        mockMvc.perform(get("/api/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Basic Template")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/templates/{id} - not found")
    void getTemplateById_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/templates/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/templates - success")
    void createTemplate_withValidData_shouldReturnCreatedTemplate() throws Exception {
        // Arrange
        Template newTemplate = new Template("New Template", layoutJson);
        when(templateService.createTemplate(any(Template.class))).thenReturn(newTemplate);

        // Act & Assert
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTemplate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Template")));
    }

    @Test
    @DisplayName("PUT /api/templates/{id} - success")
    void updateTemplate_withValidData_shouldReturnUpdatedTemplate() throws Exception {
        // Arrange
        Template updatedTemplate = new Template("Updated Template", layoutJson);
        when(templateService.updateTemplate(eq(1L), any(Template.class))).thenReturn(updatedTemplate);

        // Act & Assert
        mockMvc.perform(put("/api/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTemplate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Template")));
    }

    @Test
    @DisplayName("PUT /api/templates/{id} - not found")
    void updateTemplate_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        Template updatedTemplate = new Template("Updated Template", layoutJson);
        when(templateService.updateTemplate(eq(999L), any(Template.class)))
                .thenThrow(new EntityNotFoundException("Template not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/templates/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTemplate)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/templates/{id} - success")
    void deleteTemplate_withValidId_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/templates/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/templates/{id} - not found")
    void deleteTemplate_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Template not found with id: 999"))
                .when(templateService).deleteTemplate(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/templates/999"))
                .andExpect(status().isNotFound());
    }
} 