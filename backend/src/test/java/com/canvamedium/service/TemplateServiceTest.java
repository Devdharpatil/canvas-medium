package com.canvamedium.service;

import com.canvamedium.model.Template;
import com.canvamedium.repository.TemplateRepository;
import com.canvamedium.service.impl.TemplateServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateServiceImpl templateService;

    private Template template1;
    private Template template2;
    private JsonNode layoutJson;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
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
    @DisplayName("Get all templates - success")
    void getAllTemplates_shouldReturnAllTemplates() {
        // Arrange
        List<Template> templates = Arrays.asList(template1, template2);
        when(templateRepository.findAll()).thenReturn(templates);

        // Act
        List<Template> result = templateService.getAllTemplates();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Basic Template", result.get(0).getName());
        assertEquals("Advanced Template", result.get(1).getName());
    }

    @Test
    @DisplayName("Get all templates with pagination - success")
    void getAllTemplatesWithPagination_shouldReturnTemplatesPage() {
        // Arrange
        List<Template> templates = Arrays.asList(template1, template2);
        Page<Template> page = new PageImpl<>(templates);
        Pageable pageable = PageRequest.of(0, 10);
        when(templateRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Template> result = templateService.getAllTemplates(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Get template by ID - success")
    void getTemplateById_withValidId_shouldReturnTemplate() {
        // Arrange
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template1));

        // Act
        Optional<Template> result = templateService.getTemplateById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Basic Template", result.get().getName());
    }

    @Test
    @DisplayName("Get template by ID - not found")
    void getTemplateById_withInvalidId_shouldReturnEmptyOptional() {
        // Arrange
        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<Template> result = templateService.getTemplateById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Create template - success")
    void createTemplate_shouldSetTimestampsAndSave() {
        // Arrange
        Template newTemplate = new Template("New Template", layoutJson);
        when(templateRepository.save(any(Template.class))).thenReturn(newTemplate);

        // Act
        Template result = templateService.createTemplate(newTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("New Template", result.getName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(templateRepository).save(newTemplate);
    }

    @Test
    @DisplayName("Update template - success")
    void updateTemplate_withValidId_shouldUpdateFields() {
        // Arrange
        Template updatedTemplate = new Template("Updated Template", layoutJson);
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template1));
        when(templateRepository.save(any(Template.class))).thenReturn(updatedTemplate);

        // Act
        Template result = templateService.updateTemplate(1L, updatedTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Template", result.getName());
    }

    @Test
    @DisplayName("Update template - not found")
    void updateTemplate_withInvalidId_shouldThrowException() {
        // Arrange
        Template updatedTemplate = new Template("Updated Template", layoutJson);
        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            templateService.updateTemplate(999L, updatedTemplate);
        });
    }

    @Test
    @DisplayName("Delete template - success")
    void deleteTemplate_withValidId_shouldDeleteTemplate() {
        // Arrange
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template1));

        // Act & Assert
        assertDoesNotThrow(() -> templateService.deleteTemplate(1L));
        verify(templateRepository).delete(template1);
    }

    @Test
    @DisplayName("Delete template - not found")
    void deleteTemplate_withInvalidId_shouldThrowException() {
        // Arrange
        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            templateService.deleteTemplate(999L);
        });
    }

    @Test
    @DisplayName("Search templates by name - success")
    void searchTemplatesByName_shouldReturnMatchingTemplates() {
        // Arrange
        List<Template> templates = Arrays.asList(template1);
        Page<Template> page = new PageImpl<>(templates);
        Pageable pageable = PageRequest.of(0, 10);
        when(templateRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Template> result = templateService.searchTemplatesByName("Basic", pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Basic Template", result.getContent().get(0).getName());
    }
} 