package com.canvamedium.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Article;
import com.canvamedium.model.Category;
import com.canvamedium.model.Template;
import com.canvamedium.util.NetworkUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class to generate demo data for the app.
 */
public class DemoDataGenerator {
    private static final String TAG = "DemoDataGenerator";
    
    /**
     * Creates sample articles for demonstration purposes.
     *
     * @param context The application context
     * @param callback Callback to notify when all articles are created
     */
    public static void createSampleArticles(Context context, final DemoDataCallback callback) {
        ApiService apiService = ApiClient.createAuthenticatedService(ApiService.class, context);
        
        // First, get some templates to use for our articles
        Map<String, Object> options = new HashMap<>();
        apiService.getTemplates(options).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<Article> sampleArticles = new ArrayList<>();
                        
                        // Get the templates from the response
                        Object templatesObj = response.body().get("templates");
                        if (templatesObj instanceof List) {
                            List<?> templates = (List<?>) templatesObj;
                            if (!templates.isEmpty()) {
                                // Create sample articles using the first template
                                createArticleBatch(context, apiService, templates, sampleArticles, callback);
                            } else {
                                // No templates found, create a default template first
                                createDefaultTemplate(context, apiService, callback);
                            }
                        } else {
                            // Couldn't get templates from response, use local fallback
                            createSampleArticlesLocally(context, callback);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating sample articles", e);
                        // Fall back to local mode if API approach fails
                        createSampleArticlesLocally(context, callback);
                    }
                } else {
                    // Non-successful response, check if it's a 403 error
                    if (response.code() == 403) {
                        Log.w(TAG, "Authentication required (403 Forbidden). Creating local demo content instead.");
                        createSampleArticlesLocally(context, callback);
                    } else {
                        callback.onError("Failed to fetch templates: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error while fetching templates: " + t.getMessage(), t);
                
                // If there's a network error, create a default template locally and continue
                Toast.makeText(context, "Network error, creating demo content locally", Toast.LENGTH_SHORT).show();
                
                // Create local articles directly instead of trying to use the API
                createSampleArticlesLocally(context, callback);
            }
        });
    }
    
    private static void createDefaultTemplate(Context context, ApiService apiService, final DemoDataCallback callback) {
        // Create blog template
        Template blogTemplate = Template.createPredefinedTemplate(Template.TEMPLATE_TYPE_BLOG);
        
        apiService.createTemplate(blogTemplate).enqueue(new Callback<Template>() {
            @Override
            public void onResponse(Call<Template> call, Response<Template> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Object> templates = new ArrayList<>();
                    templates.add(response.body());
                    createArticleBatch(context, apiService, templates, new ArrayList<>(), callback);
                } else {
                    callback.onError("Failed to create template");
                }
            }

            @Override
            public void onFailure(Call<Template> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    private static void createArticleBatch(Context context, ApiService apiService, 
                                          List<?> templates, List<Article> createdArticles, 
                                          final DemoDataCallback callback) {
        // Check for 403 errors from previous API calls
        if (!NetworkUtils.isNetworkConnected(context)) {
            Log.w(TAG, "No network connection, creating local articles");
            createLocalArticlesFromTemplates(context, templates, callback);
            return;
        }
        
        // Try to create tech article
        createTechArticle(context, apiService, templates, new Callback<Article>() {
            @Override
            public void onResponse(Call<Article> call, Response<Article> response) {
                if (response.isSuccessful() && response.body() != null) {
                    createdArticles.add(response.body());
                    
                    // Continue with other articles if first one succeeds
                    createNextArticles(context, apiService, templates, createdArticles, callback);
                } else {
                    // Check for HTTP 403 Forbidden
                    if (response.code() == 403) {
                        Log.w(TAG, "Authorization error (403 Forbidden) while creating articles. Falling back to local articles.");
                        // Fall back to local articles immediately
                        createLocalArticlesFromTemplates(context, templates, callback);
                    } else {
                        Log.e(TAG, "Error creating article: " + response.code() + " " + response.message());
                        // Try to continue with any created articles, or create local ones if none
                        if (!createdArticles.isEmpty()) {
                            callback.onComplete(createdArticles);
                        } else {
                            createLocalArticlesFromTemplates(context, templates, callback);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Article> call, Throwable t) {
                Log.e(TAG, "Network error while creating articles", t);
                // Fall back to local articles
                createLocalArticlesFromTemplates(context, templates, callback);
            }
        });
    }
    
    /**
     * Helper method to create local articles from templates
     */
    private static void createLocalArticlesFromTemplates(Context context, List<?> templates, DemoDataCallback callback) {
        List<Template> localTemplates = new ArrayList<>();
        for (Object templateObj : templates) {
            Template template;
            if (templateObj instanceof Template) {
                template = (Template) templateObj;
            } else if (templateObj instanceof java.util.Map) {
                template = Template.fromMap((java.util.Map<?, ?>) templateObj);
            } else {
                continue;
            }
            localTemplates.add(template);
        }
        
        // If we couldn't convert any templates, create predefined ones
        if (localTemplates.isEmpty()) {
            createSampleArticlesLocally(context, callback);
        } else {
            createLocalArticles(context, localTemplates, callback);
        }
    }
    
    /**
     * Helper method to continue creating articles after the first one succeeds
     */
    private static void createNextArticles(Context context, ApiService apiService, 
                                          List<?> templates, List<Article> createdArticles, 
                                          final DemoDataCallback callback) {
        // Create travel article
        createTravelArticle(context, apiService, templates, new Callback<Article>() {
            @Override
            public void onResponse(Call<Article> call, Response<Article> response) {
                if (response.isSuccessful() && response.body() != null) {
                    createdArticles.add(response.body());
                    
                    // Create recipe article
                    createRecipeArticle(context, apiService, templates, new Callback<Article>() {
                        @Override
                        public void onResponse(Call<Article> call, Response<Article> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                createdArticles.add(response.body());
                                
                                // Create health article
                                createHealthArticle(context, apiService, templates, new Callback<Article>() {
                                    @Override
                                    public void onResponse(Call<Article> call, Response<Article> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            createdArticles.add(response.body());
                                            
                                            // Create quote article
                                            createQuoteArticle(context, apiService, templates, new Callback<Article>() {
                                                @Override
                                                public void onResponse(Call<Article> call, Response<Article> response) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        createdArticles.add(response.body());
                                                    }
                                                    // Return all articles we managed to create
                                                    callback.onComplete(createdArticles);
                                                }

                                                @Override
                                                public void onFailure(Call<Article> call, Throwable t) {
                                                    // Return the ones we have
                                                    callback.onComplete(createdArticles);
                                                }
                                            });
                                        } else {
                                            // Return what we have so far
                                            callback.onComplete(createdArticles);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Article> call, Throwable t) {
                                        // Return the ones we have
                                        callback.onComplete(createdArticles);
                                    }
                                });
                            } else {
                                // Return what we have so far
                                callback.onComplete(createdArticles);
                            }
                        }

                        @Override
                        public void onFailure(Call<Article> call, Throwable t) {
                            // Return the ones we have
                            callback.onComplete(createdArticles);
                        }
                    });
                } else {
                    // Return what we have so far
                    callback.onComplete(createdArticles);
                }
            }

            @Override
            public void onFailure(Call<Article> call, Throwable t) {
                // Return the ones we have
                callback.onComplete(createdArticles);
            }
        });
    }

    private static void createTechArticle(Context context, ApiService apiService, List<?> templates, Callback<Article> callback) {
        Object templateObj = templates.get(0);
        Template template;
        
        if (templateObj instanceof Template) {
            template = (Template) templateObj;
        } else if (templateObj instanceof java.util.Map) {
            template = Template.fromMap((java.util.Map<?, ?>) templateObj);
        } else {
            callback.onFailure(null, new IllegalArgumentException("Invalid template type"));
            return;
        }
        
        JsonObject content = new JsonObject();
        content.addProperty("title", "The Future of AI Development in 2024");
        
        JsonArray blocks = new JsonArray();
        JsonObject introBlock = new JsonObject();
        introBlock.addProperty("type", "paragraph");
        introBlock.addProperty("text", "Artificial Intelligence has transformed rapidly over the past year, with developments in machine learning, neural networks, and generative AI that once seemed years away. Here's what we can expect in 2024.");
        blocks.add(introBlock);
        
        content.add("blocks", blocks);
        
        Article article = new Article(
                "The Future of AI Development in 2024",
                content,
                "A deep dive into upcoming AI trends that will shape technology in 2024.",
                "https://images.unsplash.com/photo-1677442135416-d57ead66c6a8?q=80&w=1932&auto=format&fit=crop",
                template.getId(),
                Article.STATUS_PUBLISHED
        );
        
        // Set categories and tags
        Category techCategory = new Category();
        techCategory.setId(1L);
        techCategory.setName("Technology");
        article.setCategory(techCategory);
        
        // Set publish date
        article.setPublishedAt("2023-12-15T10:30:00Z");
        
        // Set as featured
        article.setFeatured(true);
        
        apiService.createArticle(article).enqueue(callback);
    }
    
    private static void createTravelArticle(Context context, ApiService apiService, List<?> templates, Callback<Article> callback) {
        Object templateObj = templates.get(0);
        Template template;
        
        if (templateObj instanceof Template) {
            template = (Template) templateObj;
        } else if (templateObj instanceof java.util.Map) {
            template = Template.fromMap((java.util.Map<?, ?>) templateObj);
        } else {
            callback.onFailure(null, new IllegalArgumentException("Invalid template type"));
            return;
        }
        
        JsonObject content = new JsonObject();
        content.addProperty("title", "Hidden Gems: Unexplored Destinations in Southeast Asia");
        
        JsonArray blocks = new JsonArray();
        JsonObject introBlock = new JsonObject();
        introBlock.addProperty("type", "paragraph");
        introBlock.addProperty("text", "Beyond the well-traveled paths of Bangkok and Bali lie countless hidden gems waiting to be discovered. These off-the-beaten-path destinations offer authentic cultural experiences without the tourist crowds.");
        blocks.add(introBlock);
        
        content.add("blocks", blocks);
        
        Article article = new Article(
                "Hidden Gems: Unexplored Destinations in Southeast Asia",
                content,
                "Discover secret paradises and authentic cultural experiences away from tourist crowds.",
                "https://images.unsplash.com/photo-1528127269322-539801943592?q=80&w=2070&auto=format&fit=crop",
                template.getId(),
                Article.STATUS_PUBLISHED
        );
        
        // Set categories and tags
        Category travelCategory = new Category();
        travelCategory.setId(2L);
        travelCategory.setName("Travel");
        article.setCategory(travelCategory);
        
        // Set publish date
        article.setPublishedAt("2023-12-20T14:45:00Z");
        
        apiService.createArticle(article).enqueue(callback);
    }
    
    private static void createRecipeArticle(Context context, ApiService apiService, List<?> templates, Callback<Article> callback) {
        Object templateObj = templates.get(0);
        Template template;
        
        if (templateObj instanceof Template) {
            template = (Template) templateObj;
        } else if (templateObj instanceof java.util.Map) {
            template = Template.fromMap((java.util.Map<?, ?>) templateObj);
        } else {
            callback.onFailure(null, new IllegalArgumentException("Invalid template type"));
            return;
        }
        
        JsonObject content = new JsonObject();
        content.addProperty("title", "Traditional Italian Pasta: Secret Family Recipes");
        
        JsonArray blocks = new JsonArray();
        JsonObject introBlock = new JsonObject();
        introBlock.addProperty("type", "paragraph");
        introBlock.addProperty("text", "Passed down through generations, these authentic pasta recipes bring the true taste of Italy to your home kitchen. Learn the techniques that Italian grandmothers have perfected over centuries.");
        blocks.add(introBlock);
        
        content.add("blocks", blocks);
        
        Article article = new Article(
                "Traditional Italian Pasta: Secret Family Recipes",
                content,
                "Master the art of homemade pasta with authentic techniques passed down through generations.",
                "https://images.unsplash.com/photo-1556761223-4c4282c73f77?q=80&w=2065&auto=format&fit=crop",
                template.getId(),
                Article.STATUS_PUBLISHED
        );
        
        // Set categories and tags
        Category foodCategory = new Category();
        foodCategory.setId(3L);
        foodCategory.setName("Food & Cooking");
        article.setCategory(foodCategory);
        
        // Set publish date
        article.setPublishedAt("2023-12-22T09:15:00Z");
        
        apiService.createArticle(article).enqueue(callback);
    }
    
    private static void createHealthArticle(Context context, ApiService apiService, List<?> templates, Callback<Article> callback) {
        Object templateObj = templates.get(0);
        Template template;
        
        if (templateObj instanceof Template) {
            template = (Template) templateObj;
        } else if (templateObj instanceof java.util.Map) {
            template = Template.fromMap((java.util.Map<?, ?>) templateObj);
        } else {
            callback.onFailure(null, new IllegalArgumentException("Invalid template type"));
            return;
        }
        
        JsonObject content = new JsonObject();
        content.addProperty("title", "Mindfulness Practices for Daily Wellness");
        
        JsonArray blocks = new JsonArray();
        JsonObject introBlock = new JsonObject();
        introBlock.addProperty("type", "paragraph");
        introBlock.addProperty("text", "Incorporating simple mindfulness techniques into your daily routine can significantly improve mental clarity, reduce stress, and enhance overall wellbeing, even with just a few minutes of practice each day.");
        blocks.add(introBlock);
        
        content.add("blocks", blocks);
        
        Article article = new Article(
                "Mindfulness Practices for Daily Wellness",
                content,
                "Simple yet powerful mindfulness techniques to enhance mental clarity and reduce stress in just minutes a day.",
                "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=1998&auto=format&fit=crop",
                template.getId(),
                Article.STATUS_PUBLISHED
        );
        
        // Set categories and tags
        Category healthCategory = new Category();
        healthCategory.setId(4L);
        healthCategory.setName("Health & Wellness");
        article.setCategory(healthCategory);
        
        // Set publish date
        article.setPublishedAt("2023-12-27T16:30:00Z");
        
        // Set as featured
        article.setFeatured(true);
        
        apiService.createArticle(article).enqueue(callback);
    }
    
    private static void createQuoteArticle(Context context, ApiService apiService, List<?> templates, Callback<Article> callback) {
        // Try to get a quote template if available (usually template index 4)
        Template template;
        
        if (templates.size() > 4) {
            Object templateObj = templates.get(4); // Quote template
            if (templateObj instanceof Template) {
                template = (Template) templateObj;
            } else if (templateObj instanceof java.util.Map) {
                template = Template.fromMap((java.util.Map<?, ?>) templateObj);
            } else {
                Object defaultTemplateObj = templates.get(0);
                if (defaultTemplateObj instanceof Template) {
                    template = (Template) defaultTemplateObj;
                } else {
                    template = Template.fromMap((java.util.Map<?, ?>) defaultTemplateObj);
                }
            }
        } else {
            Object templateObj = templates.get(0);
            if (templateObj instanceof Template) {
                template = (Template) templateObj;
            } else {
                template = Template.fromMap((java.util.Map<?, ?>) templateObj);
            }
        }
        
        JsonObject content = new JsonObject();
        content.addProperty("title", "Words of Wisdom: Transformative Quotes");
        
        JsonArray blocks = new JsonArray();
        JsonObject quoteBlock = new JsonObject();
        quoteBlock.addProperty("type", "quote");
        quoteBlock.addProperty("text", "\"The only way to do great work is to love what you do. If you haven't found it yet, keep looking. Don't settle.\"");
        quoteBlock.addProperty("author", "Steve Jobs");
        blocks.add(quoteBlock);
        
        content.add("blocks", blocks);
        
        Article article = new Article(
                "Words of Wisdom: Transformative Quotes",
                content,
                "Powerful quotes that inspire change, creativity, and personal growth.",
                "https://images.unsplash.com/photo-1580894732930-0babd100d356?q=80&w=2070&auto=format&fit=crop",
                template.getId(),
                Article.STATUS_PUBLISHED
        );
        
        // Set categories and tags
        Category inspirationCategory = new Category();
        inspirationCategory.setId(5L);
        inspirationCategory.setName("Inspiration");
        article.setCategory(inspirationCategory);
        
        // Set publish date
        article.setPublishedAt("2023-12-30T11:00:00Z");
        
        apiService.createArticle(article).enqueue(callback);
    }
    
    /**
     * Creates sample articles locally without trying to fetch templates from the server.
     * Useful when there are authentication or network issues.
     *
     * @param context The application context
     * @param callback Callback to notify when all articles are created
     */
    public static void createSampleArticlesLocally(Context context, final DemoDataCallback callback) {
        // Create a simple template for offline mode
        List<Template> templates = new ArrayList<>();
        
        Template basicTemplate = new Template();
        basicTemplate.setId(1L);
        basicTemplate.setName("Basic Article");
        basicTemplate.setDescription("A simple article template with header and content");
        
        templates.add(basicTemplate);
        
        // Create articles using the template
        createLocalArticles(context, templates, callback);
    }
    
    /**
     * Creates sample articles locally when offline.
     * 
     * @param context The application context
     * @param templates The list of templates
     * @param callback Callback to notify when articles are created
     */
    private static void createLocalArticles(Context context, List<Template> templates, final DemoDataCallback callback) {
        // Create all sample articles in one go for local mode
        List<Article> localArticles = new ArrayList<>();
        
        // Tech article
        JsonObject techContent = new JsonObject();
        techContent.addProperty("title", "The Future of AI Development in 2024");
        JsonArray techBlocks = new JsonArray();
        JsonObject techIntro = new JsonObject();
        techIntro.addProperty("type", "paragraph");
        techIntro.addProperty("text", "Artificial Intelligence has transformed rapidly over the past year, with developments in machine learning, neural networks, and generative AI that once seemed years away. Here's what we can expect in 2024.");
        techBlocks.add(techIntro);
        techContent.add("blocks", techBlocks);
        
        Article techArticle = new Article(
                "The Future of AI Development in 2024",
                techContent,
                "A deep dive into upcoming AI trends that will shape technology in 2024.",
                SampleImageProvider.getImageUrl(0),
                templates.get(0).getId(),
                Article.STATUS_PUBLISHED
        );
        techArticle.setId(1L);
        techArticle.setCreatedAt("2024-05-15T10:30:00Z");
        techArticle.setUpdatedAt("2024-05-15T10:30:00Z");
        
        // Set categories and featured status
        Category techCategory = new Category();
        techCategory.setId(1L);
        techCategory.setName("Technology");
        techArticle.setCategory(techCategory);
        techArticle.setFeatured(true);
        
        // Add to local articles
        localArticles.add(techArticle);
        
        // Travel article
        JsonObject travelContent = new JsonObject();
        travelContent.addProperty("title", "Hidden Gems: Unexplored Destinations in Southeast Asia");
        JsonArray travelBlocks = new JsonArray();
        JsonObject travelIntro = new JsonObject();
        travelIntro.addProperty("type", "paragraph");
        travelIntro.addProperty("text", "Beyond the well-traveled paths of Bangkok and Bali lie countless hidden gems waiting to be discovered. These off-the-beaten-path destinations offer authentic cultural experiences without the tourist crowds.");
        travelBlocks.add(travelIntro);
        travelContent.add("blocks", travelBlocks);
        
        Article travelArticle = new Article(
                "Hidden Gems: Unexplored Destinations in Southeast Asia",
                travelContent,
                "Discover secret paradises and authentic cultural experiences away from tourist crowds.",
                SampleImageProvider.getImageUrl(4),
                templates.get(Math.min(1, templates.size() - 1)).getId(),
                Article.STATUS_PUBLISHED
        );
        travelArticle.setId(2L);
        travelArticle.setCreatedAt("2024-05-18T14:45:00Z");
        travelArticle.setUpdatedAt("2024-05-18T14:45:00Z");
        
        // Set category
        Category travelCategory = new Category();
        travelCategory.setId(2L);
        travelCategory.setName("Travel");
        travelArticle.setCategory(travelCategory);
        
        // Add to local articles
        localArticles.add(travelArticle);
        
        // Recipe article
        JsonObject recipeContent = new JsonObject();
        recipeContent.addProperty("title", "Traditional Italian Pasta: Secret Family Recipes");
        JsonArray recipeBlocks = new JsonArray();
        JsonObject recipeIntro = new JsonObject();
        recipeIntro.addProperty("type", "paragraph");
        recipeIntro.addProperty("text", "Passed down through generations, these authentic pasta recipes bring the true taste of Italy to your home kitchen. Learn the techniques that Italian grandmothers have perfected over centuries.");
        recipeBlocks.add(recipeIntro);
        recipeContent.add("blocks", recipeBlocks);
        
        Article recipeArticle = new Article(
                "Traditional Italian Pasta: Secret Family Recipes",
                recipeContent,
                "Master the art of homemade pasta with authentic techniques passed down through generations.",
                SampleImageProvider.getImageUrl(7),
                templates.get(Math.min(2, templates.size() - 1)).getId(),
                Article.STATUS_PUBLISHED
        );
        recipeArticle.setId(3L);
        recipeArticle.setCreatedAt("2024-05-22T09:15:00Z");
        recipeArticle.setUpdatedAt("2024-05-22T09:15:00Z");
        
        // Set category
        Category foodCategory = new Category();
        foodCategory.setId(3L);
        foodCategory.setName("Food & Cooking");
        recipeArticle.setCategory(foodCategory);
        
        // Add to local articles
        localArticles.add(recipeArticle);
        
        // Health article
        JsonObject healthContent = new JsonObject();
        healthContent.addProperty("title", "Mindfulness Practices for Daily Wellness");
        JsonArray healthBlocks = new JsonArray();
        JsonObject healthIntro = new JsonObject();
        healthIntro.addProperty("type", "paragraph");
        healthIntro.addProperty("text", "Incorporating simple mindfulness techniques into your daily routine can significantly improve mental clarity, reduce stress, and enhance overall wellbeing, even with just a few minutes of practice each day.");
        healthBlocks.add(healthIntro);
        healthContent.add("blocks", healthBlocks);
        
        Article healthArticle = new Article(
                "Mindfulness Practices for Daily Wellness",
                healthContent,
                "Simple yet powerful mindfulness techniques to enhance mental clarity and reduce stress in just minutes a day.",
                SampleImageProvider.getImageUrl(8),
                templates.get(Math.min(3, templates.size() - 1)).getId(),
                Article.STATUS_PUBLISHED
        );
        healthArticle.setId(4L);
        healthArticle.setCreatedAt("2024-05-27T16:30:00Z");
        healthArticle.setUpdatedAt("2024-05-27T16:30:00Z");
        
        // Set category
        Category healthCategory = new Category();
        healthCategory.setId(4L);
        healthCategory.setName("Health & Wellness");
        healthArticle.setCategory(healthCategory);
        healthArticle.setFeatured(true);
        
        // Add to local articles
        localArticles.add(healthArticle);
        
        // Quote article
        JsonObject quoteContent = new JsonObject();
        quoteContent.addProperty("title", "Words of Wisdom: Transformative Quotes");
        JsonArray quoteBlocks = new JsonArray();
        JsonObject quoteBlock = new JsonObject();
        quoteBlock.addProperty("type", "quote");
        quoteBlock.addProperty("text", "\"The only way to do great work is to love what you do. If you haven't found it yet, keep looking. Don't settle.\"");
        quoteBlock.addProperty("author", "Steve Jobs");
        quoteBlocks.add(quoteBlock);
        quoteContent.add("blocks", quoteBlocks);
        
        Article quoteArticle = new Article(
                "Words of Wisdom: Transformative Quotes",
                quoteContent,
                "Powerful quotes that inspire change, creativity, and personal growth.",
                SampleImageProvider.getImageUrl(11),
                templates.get(Math.min(4, templates.size() - 1)).getId(),
                Article.STATUS_PUBLISHED
        );
        quoteArticle.setId(5L);
        quoteArticle.setCreatedAt("2024-05-30T11:00:00Z");
        quoteArticle.setUpdatedAt("2024-05-30T11:00:00Z");
        
        // Set category
        Category inspirationCategory = new Category();
        inspirationCategory.setId(5L);
        inspirationCategory.setName("Inspiration");
        quoteArticle.setCategory(inspirationCategory);
        
        // Add to local articles
        localArticles.add(quoteArticle);
        
        // Log success message
        Log.i(TAG, "Successfully created " + localArticles.size() + " local demo articles");
        
        // Add a slight delay to make the loading indicator visible
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Return the created articles through the callback
            callback.onComplete(localArticles);
        }, 800);
    }
    
    /**
     * Callback interface for demo data generation.
     */
    public interface DemoDataCallback {
        /**
         * Called when all sample articles are created successfully.
         * 
         * @param articles The list of created articles
         */
        void onComplete(List<Article> articles);
        
        /**
         * Called when an error occurs during demo data generation.
         * 
         * @param errorMessage The error message
         */
        void onError(String errorMessage);
    }
} 