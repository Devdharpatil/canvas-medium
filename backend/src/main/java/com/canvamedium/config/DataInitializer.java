package com.canvamedium.config;

import com.canvamedium.model.Template;
import com.canvamedium.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data initializer for creating default templates when the application starts.
 * These templates will be created only if no templates exist in the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DataInitializer(TemplateRepository templateRepository, ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void run(String... args) {
        // Check for templates named "Test Template" and delete them
        List<Template> testTemplates = templateRepository.findByName("Test Template");
        if (!testTemplates.isEmpty()) {
            logger.info("Found {} 'Test Template' templates. Deleting them...", testTemplates.size());
            templateRepository.deleteAll(testTemplates);
            logger.info("Deleted test templates successfully");
        }
        
        // Check if our named templates already exist and update them or create new ones
        updateOrCreateTemplate("Blog Post", 
            "A simple blog post template with header, text blocks, and image",
            "https://images.unsplash.com/photo-1471107340929-a87cd0f5b5f3?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8M3x8YmxvZ3x8MHx8fHwxNjgwMjAyMDEw",
            this::createBlogPostTemplate);
            
        updateOrCreateTemplate("Photo Gallery", 
            "A template for showcasing multiple photos with captions",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8Mnx8bGFuZHNjYXBlfHwwfHx8fDE2ODAyMDIwMTA",
            this::createPhotoGalleryTemplate);
            
        updateOrCreateTemplate("Tutorial", 
            "A step-by-step tutorial template with illustrations",
            "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8MXx8bGVhcm5pbmd8fDB8fHx8MTY4MDIwMjAxMA",
            this::createTutorialTemplate);
            
        updateOrCreateTemplate("Quote", 
            "A simple template for sharing a meaningful quote",
            "https://images.unsplash.com/photo-1527345931282-a268e9d4d007?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8MXx8cXVvdGV8fDB8fHx8MTY4MDIwMjAxMA",
            this::createQuoteTemplate);

        logger.info("Template initialization completed successfully");
    }
    
    /**
     * Helper method to update an existing template or create a new one if it doesn't exist.
     * 
     * @param name Template name
     * @param description Template description
     * @param thumbnailUrl Template thumbnail URL
     * @param creationMethod Method to call to create the template if it doesn't exist
     */
    private void updateOrCreateTemplate(String name, String description, String thumbnailUrl, Runnable creationMethod) {
        List<Template> existingTemplates = templateRepository.findByName(name);
        
        if (existingTemplates.isEmpty()) {
            logger.info("Creating new template: {}", name);
            creationMethod.run();
        } else {
            logger.info("Updating existing template: {}", name);
            Template template = existingTemplates.get(0);
            template.setDescription(description);
            template.setThumbnailUrl(thumbnailUrl);
            template.setUpdatedAt(LocalDateTime.now());
            templateRepository.save(template);
            logger.info("Template '{}' updated successfully", name);
        }
    }
    
    /**
     * Create a basic blog post template with header, text blocks, and image.
     */
    private void createBlogPostTemplate() {
        ObjectNode layout = objectMapper.createObjectNode();
        layout.put("type", "container");
        
        ArrayNode elements = objectMapper.createArrayNode();
        
        // Add a header element
        ObjectNode headerElement = objectMapper.createObjectNode();
        headerElement.put("type", Template.ELEMENT_TYPE_HEADER);
        headerElement.put("id", "header-1");
        headerElement.put("text", "Your Blog Post Title");
        headerElement.put("size", "h1");
        headerElement.put("align", "center");
        elements.add(headerElement);
        
        // Add a text element for introduction
        ObjectNode introElement = objectMapper.createObjectNode();
        introElement.put("type", Template.ELEMENT_TYPE_TEXT);
        introElement.put("id", "intro-1");
        introElement.put("text", "Write your introduction here. This is a place to hook your readers and introduce the topic of your blog post.");
        introElement.put("align", "left");
        elements.add(introElement);
        
        // Add an image element
        ObjectNode imageElement = objectMapper.createObjectNode();
        imageElement.put("type", Template.ELEMENT_TYPE_IMAGE);
        imageElement.put("id", "image-1");
        imageElement.put("alt", "Featured image");
        imageElement.put("align", "center");
        elements.add(imageElement);
        
        // Add text elements for main content
        ObjectNode contentElement1 = objectMapper.createObjectNode();
        contentElement1.put("type", Template.ELEMENT_TYPE_TEXT);
        contentElement1.put("id", "content-1");
        contentElement1.put("text", "This is the main content of your blog post. You can expand on your ideas, provide evidence, and engage your readers.");
        contentElement1.put("align", "left");
        elements.add(contentElement1);
        
        // Add a divider
        ObjectNode dividerElement = objectMapper.createObjectNode();
        dividerElement.put("type", Template.ELEMENT_TYPE_DIVIDER);
        dividerElement.put("id", "divider-1");
        elements.add(dividerElement);
        
        // Add conclusion
        ObjectNode conclusionElement = objectMapper.createObjectNode();
        conclusionElement.put("type", Template.ELEMENT_TYPE_TEXT);
        conclusionElement.put("id", "conclusion-1");
        conclusionElement.put("text", "Write your conclusion here. Summarize your main points and leave your readers with something to think about.");
        conclusionElement.put("align", "left");
        elements.add(conclusionElement);
        
        layout.set("elements", elements);
        
        Template template = new Template();
        template.setName("Blog Post");
        template.setDescription("A simple blog post template with header, text blocks, and image");
        template.setLayout(layout);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setVersion(1);
        template.setThumbnailUrl("https://images.unsplash.com/photo-1471107340929-a87cd0f5b5f3?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8M3x8YmxvZ3x8MHx8fHwxNjgwMjAyMDEw");
        
        templateRepository.save(template);
    }
    
    /**
     * Create a photo gallery template with multiple image blocks.
     */
    private void createPhotoGalleryTemplate() {
        ObjectNode layout = objectMapper.createObjectNode();
        layout.put("type", "container");
        
        ArrayNode elements = objectMapper.createArrayNode();
        
        // Add a header element
        ObjectNode headerElement = objectMapper.createObjectNode();
        headerElement.put("type", Template.ELEMENT_TYPE_HEADER);
        headerElement.put("id", "header-1");
        headerElement.put("text", "Photo Gallery");
        headerElement.put("size", "h1");
        headerElement.put("align", "center");
        elements.add(headerElement);
        
        // Add introduction text
        ObjectNode introElement = objectMapper.createObjectNode();
        introElement.put("type", Template.ELEMENT_TYPE_TEXT);
        introElement.put("id", "intro-1");
        introElement.put("text", "A collection of beautiful photos. Write a brief description here.");
        introElement.put("align", "center");
        elements.add(introElement);
        
        // Add multiple image elements for the gallery
        for (int i = 1; i <= 4; i++) {
            ObjectNode imageElement = objectMapper.createObjectNode();
            imageElement.put("type", Template.ELEMENT_TYPE_IMAGE);
            imageElement.put("id", "image-" + i);
            imageElement.put("alt", "Gallery image " + i);
            imageElement.put("align", "center");
            elements.add(imageElement);
            
            // Add caption for each image
            ObjectNode captionElement = objectMapper.createObjectNode();
            captionElement.put("type", Template.ELEMENT_TYPE_TEXT);
            captionElement.put("id", "caption-" + i);
            captionElement.put("text", "Caption for image " + i + ". Describe what's in this photo.");
            captionElement.put("align", "center");
            captionElement.put("style", "italic");
            elements.add(captionElement);
        }
        
        layout.set("elements", elements);
        
        Template template = new Template();
        template.setName("Photo Gallery");
        template.setDescription("A template for showcasing multiple photos with captions");
        template.setLayout(layout);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setVersion(1);
        template.setThumbnailUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8Mnx8bGFuZHNjYXBlfHwwfHx8fDE2ODAyMDIwMTA");
        
        templateRepository.save(template);
    }
    
    /**
     * Create a tutorial template with step-by-step instructions.
     */
    private void createTutorialTemplate() {
        ObjectNode layout = objectMapper.createObjectNode();
        layout.put("type", "container");
        
        ArrayNode elements = objectMapper.createArrayNode();
        
        // Add a header element
        ObjectNode headerElement = objectMapper.createObjectNode();
        headerElement.put("type", Template.ELEMENT_TYPE_HEADER);
        headerElement.put("id", "header-1");
        headerElement.put("text", "Step-by-Step Tutorial");
        headerElement.put("size", "h1");
        headerElement.put("align", "center");
        elements.add(headerElement);
        
        // Add introduction
        ObjectNode introElement = objectMapper.createObjectNode();
        introElement.put("type", Template.ELEMENT_TYPE_TEXT);
        introElement.put("id", "intro-1");
        introElement.put("text", "In this tutorial, you'll learn how to accomplish a specific task. This introduction should explain what readers will learn and why it's valuable.");
        introElement.put("align", "left");
        elements.add(introElement);
        
        // Add materials/prerequisites
        ObjectNode materialsHeader = objectMapper.createObjectNode();
        materialsHeader.put("type", Template.ELEMENT_TYPE_HEADER);
        materialsHeader.put("id", "materials-header");
        materialsHeader.put("text", "What You'll Need");
        materialsHeader.put("size", "h2");
        materialsHeader.put("align", "left");
        elements.add(materialsHeader);
        
        ObjectNode materialsElement = objectMapper.createObjectNode();
        materialsElement.put("type", Template.ELEMENT_TYPE_TEXT);
        materialsElement.put("id", "materials-1");
        materialsElement.put("text", "List the materials, tools, or prerequisites needed for this tutorial.");
        materialsElement.put("align", "left");
        elements.add(materialsElement);
        
        // Add multiple steps
        for (int i = 1; i <= 3; i++) {
            ObjectNode stepHeader = objectMapper.createObjectNode();
            stepHeader.put("type", Template.ELEMENT_TYPE_HEADER);
            stepHeader.put("id", "step-header-" + i);
            stepHeader.put("text", "Step " + i + ": Step Title");
            stepHeader.put("size", "h2");
            stepHeader.put("align", "left");
            elements.add(stepHeader);
            
            ObjectNode stepImage = objectMapper.createObjectNode();
            stepImage.put("type", Template.ELEMENT_TYPE_IMAGE);
            stepImage.put("id", "step-image-" + i);
            stepImage.put("alt", "Step " + i + " illustration");
            stepImage.put("align", "center");
            elements.add(stepImage);
            
            ObjectNode stepText = objectMapper.createObjectNode();
            stepText.put("type", Template.ELEMENT_TYPE_TEXT);
            stepText.put("id", "step-text-" + i);
            stepText.put("text", "Detailed instructions for step " + i + ". Explain what to do and why it's important.");
            stepText.put("align", "left");
            elements.add(stepText);
        }
        
        // Add conclusion
        ObjectNode conclusionHeader = objectMapper.createObjectNode();
        conclusionHeader.put("type", Template.ELEMENT_TYPE_HEADER);
        conclusionHeader.put("id", "conclusion-header");
        conclusionHeader.put("text", "Conclusion");
        conclusionHeader.put("size", "h2");
        conclusionHeader.put("align", "left");
        elements.add(conclusionHeader);
        
        ObjectNode conclusionElement = objectMapper.createObjectNode();
        conclusionElement.put("type", Template.ELEMENT_TYPE_TEXT);
        conclusionElement.put("id", "conclusion-1");
        conclusionElement.put("text", "Summarize what the reader has learned and suggest next steps or variations they might try.");
        conclusionElement.put("align", "left");
        elements.add(conclusionElement);
        
        layout.set("elements", elements);
        
        Template template = new Template();
        template.setName("Tutorial");
        template.setDescription("A step-by-step tutorial template with illustrations");
        template.setLayout(layout);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setVersion(1);
        template.setThumbnailUrl("https://images.unsplash.com/photo-1434030216411-0b793f4b4173?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8MXx8bGVhcm5pbmd8fDB8fHx8MTY4MDIwMjAxMA");
        
        templateRepository.save(template);
    }
    
    /**
     * Create a simple quote template.
     */
    private void createQuoteTemplate() {
        ObjectNode layout = objectMapper.createObjectNode();
        layout.put("type", "container");
        
        ArrayNode elements = objectMapper.createArrayNode();
        
        // Add a quote element
        ObjectNode quoteElement = objectMapper.createObjectNode();
        quoteElement.put("type", Template.ELEMENT_TYPE_QUOTE);
        quoteElement.put("id", "quote-1");
        quoteElement.put("text", "The greatest glory in living lies not in never falling, but in rising every time we fall.");
        quoteElement.put("align", "center");
        elements.add(quoteElement);
        
        // Add attribution
        ObjectNode attributionElement = objectMapper.createObjectNode();
        attributionElement.put("type", Template.ELEMENT_TYPE_TEXT);
        attributionElement.put("id", "attribution-1");
        attributionElement.put("text", "- Nelson Mandela");
        attributionElement.put("align", "center");
        attributionElement.put("style", "italic");
        elements.add(attributionElement);
        
        // Add image element
        ObjectNode imageElement = objectMapper.createObjectNode();
        imageElement.put("type", Template.ELEMENT_TYPE_IMAGE);
        imageElement.put("id", "image-1");
        imageElement.put("alt", "Quote background");
        imageElement.put("align", "center");
        elements.add(imageElement);
        
        // Add context
        ObjectNode contextElement = objectMapper.createObjectNode();
        contextElement.put("type", Template.ELEMENT_TYPE_TEXT);
        contextElement.put("id", "context-1");
        contextElement.put("text", "Add some context to this quote. Why is it meaningful? How does it relate to your topic?");
        contextElement.put("align", "left");
        elements.add(contextElement);
        
        layout.set("elements", elements);
        
        Template template = new Template();
        template.setName("Quote");
        template.setDescription("A simple template for sharing a meaningful quote");
        template.setLayout(layout);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setVersion(1);
        template.setThumbnailUrl("https://images.unsplash.com/photo-1527345931282-a268e9d4d007?ixlib=rb-4.0.3&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=2MXwxMjA3fDB8MXxzZWFyY2h8MXx8cXVvdGV8fDB8fHx8MTY4MDIwMjAxMA");
        
        templateRepository.save(template);
    }
} 