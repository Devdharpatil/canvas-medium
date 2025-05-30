package com.canvamedium.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canvamedium.db.entity.ArticleEntity;
import com.canvamedium.model.Article;
import com.canvamedium.repository.ArticleRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Article data that survives configuration changes.
 */
public class ArticleViewModel extends AndroidViewModel {

    private final ArticleRepository articleRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGeneratingDemo = new MutableLiveData<>(false);

    /**
     * Constructor for ArticleViewModel
     *
     * @param application the application context
     */
    public ArticleViewModel(@NonNull Application application) {
        super(application);
        articleRepository = new ArticleRepository(application);
    }

    /**
     * Get all articles
     *
     * @return LiveData emitting the list of all articles
     */
    public LiveData<List<ArticleEntity>> getAllArticles() {
        return articleRepository.getAllArticles();
    }

    /**
     * Get bookmarked articles
     *
     * @return LiveData emitting the list of bookmarked articles
     */
    public LiveData<List<ArticleEntity>> getBookmarkedArticles() {
        return articleRepository.getBookmarkedArticles();
    }

    /**
     * Get articles by category
     *
     * @param categoryId the category ID
     * @return LiveData emitting the list of articles in the category
     */
    public LiveData<List<ArticleEntity>> getArticlesByCategory(long categoryId) {
        return articleRepository.getArticlesByCategory(categoryId);
    }

    /**
     * Get an article by ID
     *
     * @param articleId the article ID
     * @return LiveData emitting the article
     */
    public LiveData<Article> getArticleById(long articleId) {
        MutableLiveData<Article> articleLiveData = new MutableLiveData<>();
        
        disposables.add(
            articleRepository.getArticleById(articleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    articleEntity -> {
                        Article article = convertEntityToArticle(articleEntity);
                        articleLiveData.setValue(article);
                    },
                    throwable -> {
                        errorMessage.setValue("Error fetching article: " + throwable.getMessage());
                        articleLiveData.setValue(null);
                    }
                )
        );
        
        return articleLiveData;
    }

    /**
     * Converts an ArticleEntity to an Article model.
     */
    private Article convertEntityToArticle(ArticleEntity entity) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject contentJson;
        try {
            contentJson = gson.fromJson(entity.getContent(), com.google.gson.JsonObject.class);
        } catch (Exception e) {
            contentJson = new com.google.gson.JsonObject();
        }
        
        Article article = new Article();
        article.setId(entity.getId());
        article.setTitle(entity.getTitle());
        article.setContent(contentJson);
        article.setPreviewText(entity.getPreviewText());
        article.setThumbnailUrl(entity.getThumbnailUrl());
        article.setTemplateId(entity.getTemplateId() != null ? entity.getTemplateId() : -1L);
        article.setStatus(entity.getStatus());
        
        // Convert dates from Date to String format
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
        
        if (entity.getPublishedAt() != null) {
            article.setPublishedAt(dateFormat.format(entity.getPublishedAt()));
        }
        
        if (entity.getCreatedAt() != null) {
            article.setCreatedAt(dateFormat.format(entity.getCreatedAt()));
        }
        
        if (entity.getUpdatedAt() != null) {
            article.setUpdatedAt(dateFormat.format(entity.getUpdatedAt()));
        }
        
        article.setBookmarked(entity.isBookmarked());
        article.setAuthorName(entity.getAuthorName());
        
        // Set category if available
        if (entity.getCategoryId() != null && entity.getCategoryName() != null) {
            com.canvamedium.model.Category category = new com.canvamedium.model.Category();
            category.setId(entity.getCategoryId());
            category.setName(entity.getCategoryName());
            article.setCategory(category);
        }
        
        // Set tags if available
        java.util.List<String> tagNames = entity.getTags();
        if (tagNames != null && !tagNames.isEmpty()) {
            java.util.List<com.canvamedium.model.Tag> tags = new java.util.ArrayList<>();
            for (String tagName : tagNames) {
                com.canvamedium.model.Tag tag = new com.canvamedium.model.Tag();
                tag.setName(tagName);
                tags.add(tag);
            }
            article.setTags(tags);
        }
        
        return article;
    }

    /**
     * Search articles by query
     *
     * @param query the search query
     * @return LiveData emitting the list of articles matching the query
     */
    public LiveData<List<ArticleEntity>> searchArticles(String query) {
        return articleRepository.searchArticles(query);
    }

    /**
     * Toggle bookmark status of an article
     *
     * @param articleId the ID of the article
     * @param isBookmarked whether to bookmark or unbookmark the article
     * @return LiveData emitting a boolean indicating success
     */
    public LiveData<Boolean> toggleBookmark(long articleId, boolean isBookmarked) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        
        disposables.add(
            articleRepository.toggleBookmark(articleId, isBookmarked)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        // Successfully bookmarked/unbookmarked
                        resultLiveData.setValue(true);
                    },
                    throwable -> {
                        errorMessage.setValue("Error updating bookmark: " + throwable.getMessage());
                        resultLiveData.setValue(false);
                    }
                )
        );
        
        return resultLiveData;
    }

    /**
     * Update bookmark status of an article (alias for toggleBookmark)
     *
     * @param articleId the ID of the article
     * @param isBookmarked whether to bookmark or unbookmark the article
     * @return LiveData emitting a boolean indicating success
     */
    public LiveData<Boolean> updateBookmark(Long articleId, boolean isBookmarked) {
        return toggleBookmark(articleId, isBookmarked);
    }

    /**
     * Refresh articles from the server
     */
    public void refreshArticles() {
        // The repository will handle refreshing when getAllArticles is called
        // This method can be used to force a refresh
        isLoading.setValue(true);
        
        // Can add additional refresh logic here if needed in the future
        
        isLoading.setValue(false);
    }

    /**
     * Insert an article into the database
     *
     * @param article the article to insert
     * @return LiveData emitting a boolean indicating success
     */
    public LiveData<Boolean> insertArticle(ArticleEntity article) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        
        disposables.add(
            articleRepository.insertArticle(article)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        // Successfully inserted
                        resultLiveData.setValue(true);
                    },
                    throwable -> {
                        errorMessage.setValue("Error inserting article: " + throwable.getMessage());
                        resultLiveData.setValue(false);
                    }
                )
        );
        
        return resultLiveData;
    }

    /**
     * Check if data is currently loading
     *
     * @return LiveData emitting loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Get error message if any
     *
     * @return LiveData emitting the error message
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set demo generation status
     * 
     * @param isGenerating Whether demo content is being generated
     */
    public void setGeneratingDemo(boolean isGenerating) {
        isGeneratingDemo.setValue(isGenerating);
    }
    
    /**
     * Get demo generation status
     * 
     * @return LiveData emitting whether demo content is being generated
     */
    public LiveData<Boolean> getIsGeneratingDemo() {
        return isGeneratingDemo;
    }

    /**
     * Clean up resources when ViewModel is no longer used
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
} 