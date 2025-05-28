package com.canvamedium.service.impl;

import com.canvamedium.model.Tag;
import com.canvamedium.repository.TagRepository;
import com.canvamedium.service.TagService;
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
 * Implementation of the TagService interface.
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    /**
     * Constructor with dependencies injection.
     *
     * @param tagRepository The tag repository
     */
    @Autowired
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Page<Tag> getAllTags(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    @Override
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    @Override
    public Optional<Tag> getTagBySlug(String slug) {
        return tagRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public Tag createTag(Tag tag) {
        // Validate name and slug uniqueness
        validateTagNameAndSlug(tag);
        
        // Set timestamps
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public Tag updateTag(Long id, Tag tagDetails) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        
        // Only validate name and slug if they are being changed
        if (!tag.getName().equals(tagDetails.getName())) {
            if (!isTagNameAvailable(tagDetails.getName())) {
                throw new IllegalArgumentException("Tag name already exists: " + tagDetails.getName());
            }
            tag.setName(tagDetails.getName());
        }
        
        if (!tag.getSlug().equals(tagDetails.getSlug())) {
            if (!isTagSlugAvailable(tagDetails.getSlug())) {
                throw new IllegalArgumentException("Tag slug already exists: " + tagDetails.getSlug());
            }
            tag.setSlug(tagDetails.getSlug());
        }
        
        tag.setUpdatedAt(LocalDateTime.now());
        
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        
        tagRepository.delete(tag);
    }

    @Override
    public Page<Tag> searchTags(String query, Pageable pageable) {
        return tagRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    @Override
    public Page<Tag> getPopularTags(Pageable pageable) {
        return tagRepository.findTagsByPopularity(pageable);
    }

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        return tagRepository.findByArticleId(articleId);
    }

    @Override
    public boolean isTagNameAvailable(String name) {
        return !tagRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean isTagSlugAvailable(String slug) {
        return !tagRepository.existsBySlug(slug);
    }

    /**
     * Validates the uniqueness of the tag name and slug.
     *
     * @param tag The tag to validate
     * @throws IllegalArgumentException if the name or slug is already in use
     */
    private void validateTagNameAndSlug(Tag tag) {
        if (!isTagNameAvailable(tag.getName())) {
            throw new IllegalArgumentException("Tag name already exists: " + tag.getName());
        }
        
        if (!isTagSlugAvailable(tag.getSlug())) {
            throw new IllegalArgumentException("Tag slug already exists: " + tag.getSlug());
        }
    }
} 