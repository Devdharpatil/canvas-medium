package com.canvamedium.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Model class representing a Template in the CanvaMedium Android app.
 * Templates are reusable layout structures that can be used to create articles.
 */
public class Template implements Serializable {
    
    /**
     * Element types supported in templates
     */
    public static final String ELEMENT_TYPE_TEXT = "TEXT";
    public static final String ELEMENT_TYPE_IMAGE = "IMAGE";
    public static final String ELEMENT_TYPE_HEADER = "HEADER";
    public static final String ELEMENT_TYPE_DIVIDER = "DIVIDER";
    public static final String ELEMENT_TYPE_QUOTE = "QUOTE";
    
    // Template types for predefined templates
    public static final String TEMPLATE_TYPE_BLOG = "Blog Post";
    public static final String TEMPLATE_TYPE_PHOTO_GALLERY = "Photo Gallery";
    public static final String TEMPLATE_TYPE_ARTICLE = "Article";
    public static final String TEMPLATE_TYPE_TUTORIAL = "Tutorial";
    public static final String TEMPLATE_TYPE_QUOTE = "Quote";
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private JsonObject layout;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    private Integer version;
    
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    
    /**
     * Default constructor for Retrofit serialization.
     */
    public Template() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param name   The name of the template
     * @param layout The layout of the template as a JSON structure
     */
    public Template(String name, JsonObject layout) {
        this.name = name;
        this.layout = layout;
        this.version = 1;
    }
    
    /**
     * Constructor with all fields.
     *
     * @param name        The name of the template
     * @param description The description of the template
     * @param layout      The layout of the template as a JSON structure
     * @param thumbnailUrl The URL of the template thumbnail image
     */
    public Template(String name, String description, JsonObject layout, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.layout = layout;
        this.thumbnailUrl = thumbnailUrl;
        this.version = 1;
    }
    
    /**
     * Creates a Template instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing template data
     * @return A new Template instance
     */
    public static Template fromMap(Map<?, ?> map) {
        Template template = new Template();
        
        // Extract and set the template properties from the map
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                template.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("name")) {
            Object nameObj = map.get("name");
            if (nameObj != null) {
                template.setName(nameObj.toString());
            }
        }
        
        if (map.containsKey("description")) {
            Object descObj = map.get("description");
            if (descObj != null) {
                template.setDescription(descObj.toString());
            }
        }
        
        if (map.containsKey("layout")) {
            Object layoutObj = map.get("layout");
            if (layoutObj != null) {
                Gson gson = new Gson();
                template.setLayout(gson.toJsonTree(layoutObj).getAsJsonObject());
            }
        }
        
        if (map.containsKey("created_at")) {
            Object createdAtObj = map.get("created_at");
            if (createdAtObj != null) {
                template.setCreatedAt(createdAtObj.toString());
            }
        }
        
        if (map.containsKey("updated_at")) {
            Object updatedAtObj = map.get("updated_at");
            if (updatedAtObj != null) {
                template.setUpdatedAt(updatedAtObj.toString());
            }
        }
        
        if (map.containsKey("version")) {
            Object versionObj = map.get("version");
            if (versionObj instanceof Number) {
                template.setVersion(((Number) versionObj).intValue());
            }
        }
        
        if (map.containsKey("thumbnail_url")) {
            Object thumbnailObj = map.get("thumbnail_url");
            if (thumbnailObj != null) {
                template.setThumbnailUrl(thumbnailObj.toString());
            }
        }
        
        return template;
    }
    
    /**
     * Creates a predefined template based on the specified type.
     *
     * @param templateType One of the TEMPLATE_TYPE_* constants
     * @return A new Template with predefined elements
     */
    public static Template createPredefinedTemplate(String templateType) {
        switch (templateType) {
            case TEMPLATE_TYPE_BLOG:
                return createBlogTemplate();
            case TEMPLATE_TYPE_PHOTO_GALLERY:
                return createPhotoGalleryTemplate();
            case TEMPLATE_TYPE_ARTICLE:
                return createArticleTemplate();
            case TEMPLATE_TYPE_TUTORIAL:
                return createTutorialTemplate(); 
            case TEMPLATE_TYPE_QUOTE:
                return createQuoteTemplate();
            default:
                return createEmptyTemplate();
        }
    }
    
    /**
     * Creates a basic empty template.
     *
     * @return An empty template
     */
    public static Template createEmptyTemplate() {
        JsonObject layout = new JsonObject();
        JsonArray elements = new JsonArray();
        layout.add("elements", elements);
        
        return new Template("Empty Template", "A blank template to start from scratch", layout, null);
    }
    
    /**
     * Creates a blog post template.
     *
     * @return A blog post template
     */
    public static Template createBlogTemplate() {
        JsonObject layout = new JsonObject();
        JsonArray elements = new JsonArray();
        
        // Add header
        JsonObject headerElement = new JsonObject();
        headerElement.addProperty("id", UUID.randomUUID().toString());
        headerElement.addProperty("type", ELEMENT_TYPE_HEADER);
        headerElement.addProperty("x", 50);
        headerElement.addProperty("y", 50);
        headerElement.addProperty("width", 600);
        headerElement.addProperty("height", 100);
        headerElement.addProperty("zIndex", 0);
        
        JsonObject headerProps = new JsonObject();
        headerProps.addProperty("text", "Blog Post Title");
        headerElement.add("properties", headerProps);
        
        elements.add(headerElement);
        
        // Add image
        JsonObject imageElement = new JsonObject();
        imageElement.addProperty("id", UUID.randomUUID().toString());
        imageElement.addProperty("type", ELEMENT_TYPE_IMAGE);
        imageElement.addProperty("x", 50);
        imageElement.addProperty("y", 170);
        imageElement.addProperty("width", 600);
        imageElement.addProperty("height", 350);
        imageElement.addProperty("zIndex", 1);
        
        JsonObject imageProps = new JsonObject();
        imageProps.addProperty("placeholder", true);
        imageElement.add("properties", imageProps);
        
        elements.add(imageElement);
        
        // Add text introduction
        JsonObject introElement = new JsonObject();
        introElement.addProperty("id", UUID.randomUUID().toString());
        introElement.addProperty("type", ELEMENT_TYPE_TEXT);
        introElement.addProperty("x", 50);
        introElement.addProperty("y", 540);
        introElement.addProperty("width", 600);
        introElement.addProperty("height", 150);
        introElement.addProperty("zIndex", 2);
        
        JsonObject introProps = new JsonObject();
        introProps.addProperty("text", "Start your blog post with an engaging introduction.");
        introElement.add("properties", introProps);
        
        elements.add(introElement);
        
        // Add divider
        JsonObject dividerElement = new JsonObject();
        dividerElement.addProperty("id", UUID.randomUUID().toString());
        dividerElement.addProperty("type", ELEMENT_TYPE_DIVIDER);
        dividerElement.addProperty("x", 50);
        dividerElement.addProperty("y", 710);
        dividerElement.addProperty("width", 600);
        dividerElement.addProperty("height", 20);
        dividerElement.addProperty("zIndex", 3);
        
        elements.add(dividerElement);
        
        // Add content
        JsonObject contentElement = new JsonObject();
        contentElement.addProperty("id", UUID.randomUUID().toString());
        contentElement.addProperty("type", ELEMENT_TYPE_TEXT);
        contentElement.addProperty("x", 50);
        contentElement.addProperty("y", 750);
        contentElement.addProperty("width", 600);
        contentElement.addProperty("height", 250);
        contentElement.addProperty("zIndex", 4);
        
        JsonObject contentProps = new JsonObject();
        contentProps.addProperty("text", "Write your main blog content here. Explain key points and engage with your readers through compelling stories and examples.");
        contentElement.add("properties", contentProps);
        
        elements.add(contentElement);
        
        layout.add("elements", elements);
        
        return new Template(
                "Blog Post", 
                "A template for creating blog posts with header, image, and text sections",
                layout, 
                null);
    }
    
    /**
     * Creates a photo gallery template.
     *
     * @return A photo gallery template
     */
    public static Template createPhotoGalleryTemplate() {
        JsonObject layout = new JsonObject();
        JsonArray elements = new JsonArray();
        
        // Add header
        JsonObject headerElement = new JsonObject();
        headerElement.addProperty("id", UUID.randomUUID().toString());
        headerElement.addProperty("type", ELEMENT_TYPE_HEADER);
        headerElement.addProperty("x", 50);
        headerElement.addProperty("y", 50);
        headerElement.addProperty("width", 600);
        headerElement.addProperty("height", 100);
        headerElement.addProperty("zIndex", 0);
        
        JsonObject headerProps = new JsonObject();
        headerProps.addProperty("text", "Photo Gallery");
        headerElement.add("properties", headerProps);
        
        elements.add(headerElement);
        
        // Add intro text
        JsonObject introElement = new JsonObject();
        introElement.addProperty("id", UUID.randomUUID().toString());
        introElement.addProperty("type", ELEMENT_TYPE_TEXT);
        introElement.addProperty("x", 50);
        introElement.addProperty("y", 170);
        introElement.addProperty("width", 600);
        introElement.addProperty("height", 100);
        introElement.addProperty("zIndex", 1);
        
        JsonObject introProps = new JsonObject();
        introProps.addProperty("text", "A collection of beautiful images from my travels.");
        introElement.add("properties", introProps);
        
        elements.add(introElement);
        
        // Add multiple image placeholders
        for (int i = 0; i < 3; i++) {
            JsonObject imageElement = new JsonObject();
            imageElement.addProperty("id", UUID.randomUUID().toString());
            imageElement.addProperty("type", ELEMENT_TYPE_IMAGE);
            imageElement.addProperty("x", 50);
            imageElement.addProperty("y", 290 + (i * 270));
            imageElement.addProperty("width", 600);
            imageElement.addProperty("height", 220);
            imageElement.addProperty("zIndex", i + 2);
            
            JsonObject imageProps = new JsonObject();
            imageProps.addProperty("placeholder", true);
            imageElement.add("properties", imageProps);
            
            elements.add(imageElement);
            
            // Add caption text below each image
            JsonObject captionElement = new JsonObject();
            captionElement.addProperty("id", UUID.randomUUID().toString());
            captionElement.addProperty("type", ELEMENT_TYPE_TEXT);
            captionElement.addProperty("x", 50);
            captionElement.addProperty("y", 520 + (i * 270));
            captionElement.addProperty("width", 600);
            captionElement.addProperty("height", 40);
            captionElement.addProperty("zIndex", i + 5);
            
            JsonObject captionProps = new JsonObject();
            captionProps.addProperty("text", "Image caption " + (i + 1) + " - Add description here");
            captionElement.add("properties", captionProps);
            
            elements.add(captionElement);
        }
        
        layout.add("elements", elements);
        
        return new Template(
                "Photo Gallery", 
                "A template for showcasing multiple photos with captions", 
                layout, 
                null);
    }
    
    /**
     * Creates an article template.
     *
     * @return An article template
     */
    public static Template createArticleTemplate() {
        JsonObject layout = new JsonObject();
        JsonArray elements = new JsonArray();
        
        // Add header
        JsonObject headerElement = new JsonObject();
        headerElement.addProperty("id", UUID.randomUUID().toString());
        headerElement.addProperty("type", ELEMENT_TYPE_HEADER);
        headerElement.addProperty("x", 50);
        headerElement.addProperty("y", 50);
        headerElement.addProperty("width", 600);
        headerElement.addProperty("height", 100);
        headerElement.addProperty("zIndex", 0);
        
        JsonObject headerProps = new JsonObject();
        headerProps.addProperty("text", "Article Title");
        headerElement.add("properties", headerProps);
        
        elements.add(headerElement);
        
        // Add introduction
        JsonObject introElement = new JsonObject();
        introElement.addProperty("id", UUID.randomUUID().toString());
        introElement.addProperty("type", ELEMENT_TYPE_TEXT);
        introElement.addProperty("x", 50);
        introElement.addProperty("y", 170);
        introElement.addProperty("width", 600);
        introElement.addProperty("height", 120);
        introElement.addProperty("zIndex", 1);
        
        JsonObject introProps = new JsonObject();
        introProps.addProperty("text", "Start with a compelling introduction that hooks your readers and introduces your topic.");
        introElement.add("properties", introProps);
        
        elements.add(introElement);
        
        // Add image
        JsonObject imageElement = new JsonObject();
        imageElement.addProperty("id", UUID.randomUUID().toString());
        imageElement.addProperty("type", ELEMENT_TYPE_IMAGE);
        imageElement.addProperty("x", 50);
        imageElement.addProperty("y", 310);
        imageElement.addProperty("width", 600);
        imageElement.addProperty("height", 300);
        imageElement.addProperty("zIndex", 2);
        
        JsonObject imageProps = new JsonObject();
        imageProps.addProperty("placeholder", true);
        imageElement.add("properties", imageProps);
        
        elements.add(imageElement);
        
        // Add section headers and text blocks
        String[] sectionHeadings = {"First Section", "Second Section", "Conclusion"};
        String[] sectionContents = {
                "Explain your first main point with supporting details and examples.",
                "Continue with your second main point, maintaining reader interest.",
                "Summarize your key points and leave the reader with a final thought or call to action."
        };
        
        int yOffset = 630;
        for (int i = 0; i < sectionHeadings.length; i++) {
            // Add section header
            JsonObject sectionHeaderElement = new JsonObject();
            sectionHeaderElement.addProperty("id", UUID.randomUUID().toString());
            sectionHeaderElement.addProperty("type", ELEMENT_TYPE_HEADER);
            sectionHeaderElement.addProperty("x", 50);
            sectionHeaderElement.addProperty("y", yOffset);
            sectionHeaderElement.addProperty("width", 600);
            sectionHeaderElement.addProperty("height", 60);
            sectionHeaderElement.addProperty("zIndex", i + 3);
            
            JsonObject sectionHeaderProps = new JsonObject();
            sectionHeaderProps.addProperty("text", sectionHeadings[i]);
            sectionHeaderElement.add("properties", sectionHeaderProps);
            
            elements.add(sectionHeaderElement);
            yOffset += 80;
            
            // Add section content
            JsonObject sectionContentElement = new JsonObject();
            sectionContentElement.addProperty("id", UUID.randomUUID().toString());
            sectionContentElement.addProperty("type", ELEMENT_TYPE_TEXT);
            sectionContentElement.addProperty("x", 50);
            sectionContentElement.addProperty("y", yOffset);
            sectionContentElement.addProperty("width", 600);
            sectionContentElement.addProperty("height", 150);
            sectionContentElement.addProperty("zIndex", i + 6);
            
            JsonObject sectionContentProps = new JsonObject();
            sectionContentProps.addProperty("text", sectionContents[i]);
            sectionContentElement.add("properties", sectionContentProps);
            
            elements.add(sectionContentElement);
            yOffset += 170;
        }
        
        layout.add("elements", elements);
        
        return new Template(
                "Article",
                "A comprehensive article template with sections and visuals",
                layout,
                null);
    }
    
    /**
     * Creates a tutorial template.
     *
     * @return A tutorial template
     */
    public static Template createTutorialTemplate() {
        JsonObject layout = new JsonObject();
        JsonArray elements = new JsonArray();
        
        // Add header
        JsonObject headerElement = new JsonObject();
        headerElement.addProperty("id", UUID.randomUUID().toString());
        headerElement.addProperty("type", ELEMENT_TYPE_HEADER);
        headerElement.addProperty("x", 50);
        headerElement.addProperty("y", 50);
        headerElement.addProperty("width", 600);
        headerElement.addProperty("height", 100);
        headerElement.addProperty("zIndex", 0);
        
        JsonObject headerProps = new JsonObject();
        headerProps.addProperty("text", "How-To Tutorial");
        headerElement.add("properties", headerProps);
        
        elements.add(headerElement);
        
        // Add introduction
        JsonObject introElement = new JsonObject();
        introElement.addProperty("id", UUID.randomUUID().toString());
        introElement.addProperty("type", ELEMENT_TYPE_TEXT);
        introElement.addProperty("x", 50);
        introElement.addProperty("y", 170);
        introElement.addProperty("width", 600);
        introElement.addProperty("height", 100);
        introElement.addProperty("zIndex", 1);
        
        JsonObject introProps = new JsonObject();
        introProps.addProperty("text", "In this tutorial, you'll learn how to accomplish [specific task] step by step.");
        introElement.add("properties", introProps);
        
        elements.add(introElement);
        
        // Add steps
        String[] steps = {"Step 1: Getting Started", "Step 2: Main Process", "Step 3: Finalizing"};
        String[] stepContents = {
                "Begin by gathering all necessary materials and preparing your workspace.",
                "Follow this key process carefully, paying attention to details for best results.",
                "Complete the process by reviewing your work and making any final adjustments."
        };
        
        int yOffset = 290;
        for (int i = 0; i < steps.length; i++) {
            // Add step header
            JsonObject stepHeaderElement = new JsonObject();
            stepHeaderElement.addProperty("id", UUID.randomUUID().toString());
            stepHeaderElement.addProperty("type", ELEMENT_TYPE_HEADER);
            stepHeaderElement.addProperty("x", 50);
            stepHeaderElement.addProperty("y", yOffset);
            stepHeaderElement.addProperty("width", 600);
            stepHeaderElement.addProperty("height", 60);
            stepHeaderElement.addProperty("zIndex", i*3 + 2);
            
            JsonObject stepHeaderProps = new JsonObject();
            stepHeaderProps.addProperty("text", steps[i]);
            stepHeaderElement.add("properties", stepHeaderProps);
            
            elements.add(stepHeaderElement);
            yOffset += 80;
            
            // Add step content
            JsonObject stepContentElement = new JsonObject();
            stepContentElement.addProperty("id", UUID.randomUUID().toString());
            stepContentElement.addProperty("type", ELEMENT_TYPE_TEXT);
            stepContentElement.addProperty("x", 50);
            stepContentElement.addProperty("y", yOffset);
            stepContentElement.addProperty("width", 600);
            stepContentElement.addProperty("height", 100);
            stepContentElement.addProperty("zIndex", i*3 + 3);
            
            JsonObject stepContentProps = new JsonObject();
            stepContentProps.addProperty("text", stepContents[i]);
            stepContentElement.add("properties", stepContentProps);
            
            elements.add(stepContentElement);
            yOffset += 120;
            
            // Add image for this step
            JsonObject stepImageElement = new JsonObject();
            stepImageElement.addProperty("id", UUID.randomUUID().toString());
            stepImageElement.addProperty("type", ELEMENT_TYPE_IMAGE);
            stepImageElement.addProperty("x", 50);
            stepImageElement.addProperty("y", yOffset);
            stepImageElement.addProperty("width", 600);
            stepImageElement.addProperty("height", 200);
            stepImageElement.addProperty("zIndex", i*3 + 4);
            
            JsonObject stepImageProps = new JsonObject();
            stepImageProps.addProperty("placeholder", true);
            stepImageElement.add("properties", stepImageProps);
            
            elements.add(stepImageElement);
            yOffset += 220;
        }
        
        // Add conclusion
        JsonObject conclusionElement = new JsonObject();
        conclusionElement.addProperty("id", UUID.randomUUID().toString());
        conclusionElement.addProperty("type", ELEMENT_TYPE_TEXT);
        conclusionElement.addProperty("x", 50);
        conclusionElement.addProperty("y", yOffset);
        conclusionElement.addProperty("width", 600);
        conclusionElement.addProperty("height", 100);
        conclusionElement.addProperty("zIndex", 11);
        
        JsonObject conclusionProps = new JsonObject();
        conclusionProps.addProperty("text", "Congratulations! You've successfully completed the tutorial. Practice these steps to master the technique.");
        conclusionElement.add("properties", conclusionProps);
        
        elements.add(conclusionElement);
        
        layout.add("elements", elements);
        
        return new Template(
                "Tutorial",
                "A step-by-step guide template with images for each step",
                layout,
                null);
    }
    
    /**
     * Creates a quote template.
     *
     * @return A quote template
     */
    public static Template createQuoteTemplate() {
        JsonObject layout = new JsonObject();
        JsonArray elements = new JsonArray();
        
        // Add quote
        JsonObject quoteElement = new JsonObject();
        quoteElement.addProperty("id", UUID.randomUUID().toString());
        quoteElement.addProperty("type", ELEMENT_TYPE_QUOTE);
        quoteElement.addProperty("x", 50);
        quoteElement.addProperty("y", 150);
        quoteElement.addProperty("width", 600);
        quoteElement.addProperty("height", 200);
        quoteElement.addProperty("zIndex", 0);
        
        JsonObject quoteProps = new JsonObject();
        quoteProps.addProperty("text", "The future belongs to those who believe in the beauty of their dreams.");
        quoteElement.add("properties", quoteProps);
        
        elements.add(quoteElement);
        
        // Add attribution
        JsonObject attributionElement = new JsonObject();
        attributionElement.addProperty("id", UUID.randomUUID().toString());
        attributionElement.addProperty("type", ELEMENT_TYPE_TEXT);
        attributionElement.addProperty("x", 400);
        attributionElement.addProperty("y", 370);
        attributionElement.addProperty("width", 250);
        attributionElement.addProperty("height", 50);
        attributionElement.addProperty("zIndex", 1);
        
        JsonObject attributionProps = new JsonObject();
        attributionProps.addProperty("text", "- Eleanor Roosevelt");
        attributionElement.add("properties", attributionProps);
        
        elements.add(attributionElement);
        
        // Add optional image
        JsonObject imageElement = new JsonObject();
        imageElement.addProperty("id", UUID.randomUUID().toString());
        imageElement.addProperty("type", ELEMENT_TYPE_IMAGE);
        imageElement.addProperty("x", 50);
        imageElement.addProperty("y", 450);
        imageElement.addProperty("width", 600);
        imageElement.addProperty("height", 300);
        imageElement.addProperty("zIndex", 2);
        
        JsonObject imageProps = new JsonObject();
        imageProps.addProperty("placeholder", true);
        imageElement.add("properties", imageProps);
        
        elements.add(imageElement);
        
        layout.add("elements", elements);
        
        return new Template(
                "Quote",
                "A template for highlighting inspirational quotes with attribution",
                layout,
                null);
    }
    
    /**
     * Gets the list of elements from the template layout.
     * This uses the TemplateUtil to extract elements from the layout.
     * 
     * @return List of template elements
     */
    public List<TemplateElement> getElements() {
        return com.canvamedium.util.TemplateUtil.extractElements(this);
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public JsonObject getLayout() {
        return layout;
    }
    
    public void setLayout(JsonObject layout) {
        this.layout = layout;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    @Override
    public String toString() {
        return name;
    }
}