package com.canvamedium.service;

import com.canvamedium.model.Content;
import com.canvamedium.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContentService {
    
    private final ContentRepository contentRepository;
    
    @Autowired
    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
    
    public List<Content> getAllContents() {
        return contentRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Optional<Content> getContentById(Long id) {
        return contentRepository.findById(id);
    }
    
    public List<Content> searchContentsByTitle(String title) {
        return contentRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public Content saveContent(Content content) {
        if (content.getId() == null) {
            content.setCreatedAt(LocalDateTime.now());
        }
        content.setUpdatedAt(LocalDateTime.now());
        return contentRepository.save(content);
    }
    
    public void deleteContent(Long id) {
        contentRepository.deleteById(id);
    }
} 