package com.canvamedium.service.impl;

import com.canvamedium.model.Template;
import com.canvamedium.repository.TemplateRepository;
import com.canvamedium.service.TemplateService;
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
 * Implementation of the Template service.
 */
@Service
public class TemplateServiceImpl implements TemplateService {
    
    private final TemplateRepository templateRepository;
    
    /**
     * Constructor with repository dependency injection.
     *
     * @param templateRepository The template repository
     */
    @Autowired
    public TemplateServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
    
    @Override
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }
    
    @Override
    public Page<Template> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }
    
    @Override
    @Transactional
    public Template createTemplate(Template template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public Template updateTemplate(Long id, Template templateDetails) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
        
        template.setName(templateDetails.getName());
        template.setLayout(templateDetails.getLayout());
        template.setUpdatedAt(LocalDateTime.now());
        
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
        
        templateRepository.delete(template);
    }
    
    @Override
    public Page<Template> searchTemplatesByName(String name, Pageable pageable) {
        return templateRepository.findByNameContainingIgnoreCase(name, pageable);
    }
} 