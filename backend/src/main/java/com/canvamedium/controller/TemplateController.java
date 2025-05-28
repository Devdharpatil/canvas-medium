package com.canvamedium.controller;

import com.canvamedium.model.Template;
import com.canvamedium.service.TemplateService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Template entities.
 */
@RestController
@RequestMapping("/api/templates")
@Tag(name = "Template", description = "Template management API")
public class TemplateController {
    
    private final TemplateService templateService;
    
    /**
     * Constructor with service dependency injection.
     *
     * @param templateService The template service
     */
    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }
    
    /**
     * Get all templates.
     *
     * @param page     Page number (optional, default 0)
     * @param size     Page size (optional, default 10)
     * @param sortBy   Field to sort by (optional, default "createdAt")
     * @param sortDir  Sort direction (optional, default "desc")
     * @param name     Filter by template name (optional)
     * @return ResponseEntity containing the list of templates
     */
    @GetMapping
    @Operation(summary = "Get all templates", description = "Get a list of all templates with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved templates"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getAllTemplates(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by template name") @RequestParam(required = false) String name) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Template> templatePage;
        if (name != null && !name.isEmpty()) {
            templatePage = templateService.searchTemplatesByName(name, pageRequest);
        } else {
            templatePage = templateService.getAllTemplates(pageRequest);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("templates", templatePage.getContent());
        response.put("currentPage", templatePage.getNumber());
        response.put("totalItems", templatePage.getTotalElements());
        response.put("totalPages", templatePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a template by ID.
     *
     * @param id The template ID
     * @return ResponseEntity containing the template
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID", description = "Get a specific template by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved template",
                    content = @Content(schema = @Schema(implementation = Template.class))),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Template> getTemplateById(
            @Parameter(description = "Template ID", required = true) @PathVariable Long id) {
        
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new template.
     *
     * @param template The template to create
     * @return ResponseEntity containing the created template
     */
    @PostMapping
    @Operation(summary = "Create a new template", description = "Create a new template with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Template successfully created",
                    content = @Content(schema = @Schema(implementation = Template.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Template> createTemplate(
            @Parameter(description = "Template data", required = true) @Valid @RequestBody Template template) {
        
        Template createdTemplate = templateService.createTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }
    
    /**
     * Update an existing template.
     *
     * @param id       The ID of the template to update
     * @param template The updated template data
     * @return ResponseEntity containing the updated template
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing template", description = "Update a template with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template successfully updated",
                    content = @Content(schema = @Schema(implementation = Template.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Template> updateTemplate(
            @Parameter(description = "Template ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated template data", required = true) @Valid @RequestBody Template template) {
        
        try {
            Template updatedTemplate = templateService.updateTemplate(id, template);
            return ResponseEntity.ok(updatedTemplate);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a template by ID.
     *
     * @param id The ID of the template to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a template", description = "Delete a template by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Template successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "Template ID", required = true) @PathVariable Long id) {
        
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 