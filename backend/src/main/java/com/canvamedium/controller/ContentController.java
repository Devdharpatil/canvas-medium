package com.canvamedium.controller;

import com.canvamedium.model.Content;
import com.canvamedium.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contents")
public class ContentController {
    
    private final ContentService contentService;
    
    @Autowired
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }
    
    @GetMapping
    public ResponseEntity<List<Content>> getAllContents() {
        return ResponseEntity.ok(contentService.getAllContents());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentById(@PathVariable Long id) {
        Optional<Content> content = contentService.getContentById(id);
        return content.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Content>> searchContentsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(contentService.searchContentsByTitle(title));
    }
    
    @PostMapping
    public ResponseEntity<Content> createContent(@Valid @RequestBody Content content) {
        Content savedContent = contentService.saveContent(content);
        return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Content> updateContent(@PathVariable Long id, @Valid @RequestBody Content content) {
        Optional<Content> existingContent = contentService.getContentById(id);
        if (existingContent.isPresent()) {
            content.setId(id);
            return ResponseEntity.ok(contentService.saveContent(content));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        Optional<Content> existingContent = contentService.getContentById(id);
        if (existingContent.isPresent()) {
            contentService.deleteContent(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 