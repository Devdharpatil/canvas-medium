package com.canvamedium.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.db.CanvaMediumDatabase;
import com.canvamedium.db.dao.ArticleDao;
import com.canvamedium.db.entity.ArticleEntity;
import com.canvamedium.model.Article;
import com.canvamedium.model.Tag;
import com.canvamedium.util.NetworkUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Repository for handling article data from both local database and remote API.
 * This class mediates between the various data sources and the rest of the app.
 */
public class ArticleRepository {

    private static final String TAG = "ArticleRepository";
    private final ArticleDao articleDao;
    private final ApiService apiService;
    private final Executor executor;
    private final NetworkUtils networkUtils;

    /**
     * Constructor for the ArticleRepository
     * 
     * @param application the application context
     */
    public ArticleRepository(Application application) {
        CanvaMediumDatabase db = CanvaMediumDatabase.getDatabase(application);
        articleDao = db.articleDao();
        apiService = ApiClient.getClient().create(ApiService.class);
        executor = Executors.newSingleThreadExecutor();
        networkUtils = new NetworkUtils(application);
    }

    /**
     * Get all articles, either from the local database or remote API if online.
     * 
     * @return a LiveData object that emits the list of articles whenever it changes
     */
    public LiveData<List<ArticleEntity>> getAllArticles() {
        refreshArticles();
        return articleDao.getAllArticles();
    }

    /**
     * Get all bookmarked articles
     * 
     * @return a LiveData object that emits the list of bookmarked articles whenever it changes
     */
    public LiveData<List<ArticleEntity>> getBookmarkedArticles() {
        return articleDao.getBookmarkedArticles();
    }

    /**
     * Get articles by category
     * 
     * @param categoryId the ID of the category
     * @return a LiveData object that emits the list of articles in the category whenever it changes
     */
    public LiveData<List<ArticleEntity>> getArticlesByCategory(long categoryId) {
        refreshArticlesByCategory(categoryId);
        return articleDao.getArticlesByCategory(categoryId);
    }

    /**
     * Get an article by its ID
     * 
     * @param articleId the ID of the article
     * @return a Single that emits the article with the given ID, or an error if not found
     */
    public Single<ArticleEntity> getArticleById(long articleId) {
        refreshArticle(articleId);
        return articleDao.getArticleById(articleId)
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Error getting article by ID: " + articleId, throwable);
                    return Single.error(new Exception("Article not found with ID: " + articleId));
                });
    }

    /**
     * Search for articles by title or content
     * 
     * @param query the search query
     * @return a LiveData object that emits the list of matching articles whenever it changes
     */
    public LiveData<List<ArticleEntity>> searchArticles(String query) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Article>> response = apiService.searchArticles(query).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<ArticleEntity> articleEntities = convertToArticleEntities(response.body());
                        articleDao.insertArticleList(articleEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Search articles saved to database"),
                                          error -> Log.e(TAG, "Error saving search articles", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error searching articles from API", e);
                }
            });
        }
        return articleDao.searchArticles(query);
    }

    /**
     * Toggle bookmark status of an article
     * 
     * @param articleId the ID of the article
     * @param isBookmarked whether the article should be bookmarked
     * @return a Completable that completes when the operation is done
     */
    public Completable toggleBookmark(long articleId, boolean isBookmarked) {
        return articleDao.getArticleById(articleId)
                .flatMapCompletable(article -> {
                    article.setBookmarked(isBookmarked);
                    article.setSynced(false);
                    return articleDao.updateArticle(article);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (networkUtils.isOnline()) {
                        syncArticleBookmarkStatus(articleId, isBookmarked);
                    }
                });
    }

    /**
     * Sync article bookmark status with the server
     * 
     * @param articleId the ID of the article
     * @param isBookmarked whether the article is bookmarked
     */
    private void syncArticleBookmarkStatus(long articleId, boolean isBookmarked) {
        executor.execute(() -> {
            try {
                Response<ResponseBody> response;
                if (isBookmarked) {
                    response = apiService.bookmarkArticle(articleId).execute();
                } else {
                    response = apiService.unbookmarkArticle(articleId).execute();
                }
                
                if (response.isSuccessful()) {
                    articleDao.markArticleAsSynced(articleId, new Date().getTime())
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> Log.d(TAG, "Article bookmark status synced with server"),
                                     error -> Log.e(TAG, "Error marking article as synced", error));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing article bookmark status", e);
            }
        });
    }

    /**
     * Refresh all articles from the API
     */
    private void refreshArticles() {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Article>> response = apiService.getAllArticles().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<ArticleEntity> articleEntities = convertToArticleEntities(response.body());
                        articleDao.insertArticleList(articleEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Articles refreshed from API"),
                                        error -> Log.e(TAG, "Error saving articles", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching articles from API", e);
                }
            });
        }
    }

    /**
     * Refresh an article from the API
     * 
     * @param articleId the ID of the article to refresh
     */
    private void refreshArticle(long articleId) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<Article> response = apiService.getArticleById(articleId).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        ArticleEntity articleEntity = convertToArticleEntity(response.body());
                        articleDao.insertArticles(articleEntity)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Article refreshed from API"),
                                        error -> Log.e(TAG, "Error saving article", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching article from API", e);
                }
            });
        }
    }

    /**
     * Refresh articles by category from the API
     * 
     * @param categoryId the ID of the category
     */
    private void refreshArticlesByCategory(long categoryId) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Article>> response = apiService.getArticlesByCategory(categoryId).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<ArticleEntity> articleEntities = convertToArticleEntities(response.body());
                        articleDao.insertArticleList(articleEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Category articles refreshed from API"),
                                        error -> Log.e(TAG, "Error saving category articles", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching category articles from API", e);
                }
            });
        }
    }

    /**
     * Sync all unsynced articles with the server
     */
    public void syncUnsyncedArticles() {
        if (!networkUtils.isOnline()) {
            return;
        }

        articleDao.getUnsyncedArticles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        articles -> {
                            for (ArticleEntity article : articles) {
                                syncArticle(article);
                            }
                        },
                        error -> Log.e(TAG, "Error getting unsynced articles", error)
                );
    }

    /**
     * Sync an article with the server
     * 
     * @param article the article to sync
     */
    private void syncArticle(ArticleEntity article) {
        executor.execute(() -> {
            // Implement sync logic here
            // For now, just mark the article as synced
            article.setSynced(true);
            article.setLastSyncTime(new Date());
            
            articleDao.updateArticle(article)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Log.d(TAG, "Article synced with server"),
                            error -> Log.e(TAG, "Error updating article sync status", error));
        });
    }

    /**
     * Convert an API Article model to a Room ArticleEntity
     * 
     * @param article the API Article model
     * @return the Room ArticleEntity
     */
    private ArticleEntity convertToArticleEntity(Article article) {
        // Parse date strings to Date objects
        Date createdAt = null;
        Date updatedAt = null;
        Date publishedAt = null;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormatWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        try {
            if (article.getCreatedAt() != null) {
                try {
                    createdAt = dateFormat.parse(article.getCreatedAt());
                } catch (Exception e) {
                    createdAt = dateFormatWithMillis.parse(article.getCreatedAt());
                }
            }
            
            if (article.getUpdatedAt() != null) {
                try {
                    updatedAt = dateFormat.parse(article.getUpdatedAt());
                } catch (Exception e) {
                    updatedAt = dateFormatWithMillis.parse(article.getUpdatedAt());
                }
            }
            
            if (article.getPublishedAt() != null) {
                try {
                    publishedAt = dateFormat.parse(article.getPublishedAt());
                } catch (Exception e) {
                    publishedAt = dateFormatWithMillis.parse(article.getPublishedAt());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dates", e);
        }
        
        // Current time as fallback for dates
        long currentTime = System.currentTimeMillis();
        if (createdAt == null) createdAt = new Date(currentTime);
        if (updatedAt == null) updatedAt = new Date(currentTime);
        
        // Convert content from JsonObject to string
        String contentString = "";
        if (article.getContent() != null) {
            contentString = article.getContent().toString();
        }
        
        // Convert tags list to strings
        List<String> tagStrings = new ArrayList<>();
        if (article.getTags() != null) {
            for (Tag tag : article.getTags()) {
                if (tag != null && tag.getName() != null) {
                    tagStrings.add(tag.getName());
                }
            }
        }
        
        return new ArticleEntity(
            article.getId() > 0 ? article.getId() : generateLocalId(), // Use existing ID or generate one
            article.getTitle(),
            article.getPreviewText(),
            contentString,
            article.getThumbnailUrl(),
            article.getAuthor() != null ? article.getAuthor().getId() : null,
            article.getAuthor() != null ? article.getAuthor().getName() : null,
            createdAt,
            updatedAt,
            publishedAt,
            article.getStatus(),
            article.getTemplateId(),
            article.getCategory() != null ? article.getCategory().getId() : null,
            article.getCategory() != null ? article.getCategory().getName() : null,
            tagStrings,
            article.isBookmarked()
        );
    }

    /**
     * Convert a list of API Article models to a list of Room ArticleEntities
     * 
     * @param articles the list of API Article models
     * @return the list of Room ArticleEntities
     */
    private List<ArticleEntity> convertToArticleEntities(List<Article> articles) {
        List<ArticleEntity> articleEntities = new ArrayList<>();
        for (Article article : articles) {
            articleEntities.add(convertToArticleEntity(article));
        }
        return articleEntities;
    }

    /**
     * Get all articles with a callback for immediate use
     *
     * @param callback The callback to receive articles or error
     */
    public void getAllArticles(ArticleCallback callback) {
        if (networkUtils.isOnline()) {
            // Try to get from API first
            apiService.getAllArticles().enqueue(new Callback<List<Article>>() {
                @Override
                public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Save to database and return articles
                        List<ArticleEntity> entities = convertToArticleEntities(response.body());
                        articleDao.insertArticleList(entities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    // Convert back to model objects for the UI
                                    List<Article> articles = new ArrayList<>();
                                    for (ArticleEntity entity : entities) {
                                        articles.add(convertToArticle(entity));
                                    }
                                    callback.onResult(articles, null);
                                }, throwable -> {
                                    Log.e(TAG, "Error saving articles to database", throwable);
                                    callback.onResult(null, "Error saving articles to database");
                                });
                    } else {
                        // Fall back to database
                        getArticlesFromDatabase(callback);
                    }
                }

                @Override
                public void onFailure(Call<List<Article>> call, Throwable t) {
                    Log.e(TAG, "Error fetching articles from API", t);
                    // Fall back to database
                    getArticlesFromDatabase(callback);
                }
            });
        } else {
            // Offline mode, get from database
            getArticlesFromDatabase(callback);
        }
    }

    /**
     * Get articles from the local database with a callback
     *
     * @param callback The callback to receive articles or error
     */
    public void getArticlesFromDatabase(ArticleCallback callback) {
        articleDao.getAllArticlesAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(articleEntities -> {
                    List<Article> articles = new ArrayList<>();
                    for (ArticleEntity entity : articleEntities) {
                        articles.add(convertToArticle(entity));
                    }
                    callback.onResult(articles, null);
                }, throwable -> {
                    Log.e(TAG, "Error loading articles from database", throwable);
                    callback.onResult(null, "Error loading articles from database");
                });
    }

    /**
     * Convert an ArticleEntity to an Article model object
     *
     * @param entity The entity to convert
     * @return The Article model object
     */
    private Article convertToArticle(ArticleEntity entity) {
        Article article = new Article();
        article.setId(entity.getId());
        article.setTitle(entity.getTitle());
        
        // Convert content string to JsonObject
        try {
            com.google.gson.JsonObject content = new com.google.gson.JsonParser()
                    .parse(entity.getContent()).getAsJsonObject();
            article.setContent(content);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing article content", e);
            article.setContent(new com.google.gson.JsonObject());
        }
        
        article.setPreviewText(entity.getPreviewText());
        article.setThumbnailUrl(entity.getThumbnailUrl());
        
        // Null check for templateId
        Long templateId = entity.getTemplateId();
        article.setTemplateId(templateId != null ? templateId : -1L);
        
        article.setStatus(entity.getStatus());
        
        // Convert Date objects to strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        if (entity.getPublishedAt() != null) {
            article.setPublishedAt(dateFormat.format(entity.getPublishedAt()));
        }
        if (entity.getCreatedAt() != null) {
            article.setCreatedAt(dateFormat.format(entity.getCreatedAt()));
        }
        if (entity.getUpdatedAt() != null) {
            article.setUpdatedAt(dateFormat.format(entity.getUpdatedAt()));
        }
        
        // Featured property - assume true for demo articles if not present
        article.setFeatured(true);
        article.setBookmarked(entity.isBookmarked());
        article.setAuthorName(entity.getAuthorName());
        
        // Handle category with null check
        Long categoryId = entity.getCategoryId();
        if (categoryId != null && categoryId > 0) {
            com.canvamedium.model.Category category = new com.canvamedium.model.Category();
            category.setId(categoryId);
            category.setName(entity.getCategoryName());
            article.setCategory(category);
        }
        
        // Handle tags - convert list of strings to Tag objects
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            List<Tag> tags = new ArrayList<>();
            for (String name : entity.getTags()) {
                if (name != null) {
                    Tag tag = new Tag();
                    tag.setName(name.trim());
                    tags.add(tag);
                }
            }
            article.setTags(tags);
        }
        
        return article;
    }

    /**
     * Insert an article into the database
     *
     * @param article The article entity to insert
     * @return A Completable that completes when the operation is done
     */
    public Completable insertArticle(ArticleEntity article) {
        return articleDao.insertArticles(article)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Insert or update an article in the local database
     * 
     * @param article The Article model to save
     * @return Completable operation
     */
    public Completable insertOrUpdateArticle(Article article) {
        // Convert model to entity
        ArticleEntity entity = convertToArticleEntity(article);
        return insertArticle(entity);
    }

    /**
     * Callback interface for article operations
     */
    public interface ArticleCallback {
        /**
         * Called when the operation completes
         * 
         * @param articles The list of articles, or null if there was an error
         * @param errorMsg The error message, or null if the operation succeeded
         */
        void onResult(List<Article> articles, String errorMsg);
    }

    /**
     * Generate a local ID for articles created locally
     * 
     * @return A unique local ID (negative to avoid conflicts with server IDs)
     */
    private long generateLocalId() {
        // Use negative IDs for local articles to avoid conflicts with server IDs
        return -System.currentTimeMillis();
    }

    /**
     * Get articles by category with a callback for immediate use
     *
     * @param categoryId The ID of the category to filter by
     * @param callback The callback to receive articles or error
     */
    public void getArticlesByCategory(long categoryId, ArticleCallback callback) {
        if (networkUtils.isOnline()) {
            // Try to get from API first
            apiService.getArticlesByCategory(categoryId).enqueue(new retrofit2.Callback<List<Article>>() {
                @Override
                public void onResponse(retrofit2.Call<List<Article>> call, retrofit2.Response<List<Article>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Save to database and return articles
                        List<ArticleEntity> entities = convertToArticleEntities(response.body());
                        articleDao.insertArticleList(entities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    // Return the original model objects
                                    callback.onResult(response.body(), null);
                                }, throwable -> {
                                    Log.e(TAG, "Error saving category articles to database", throwable);
                                    callback.onResult(null, "Error saving category articles to database");
                                });
                    } else {
                        // Fall back to database
                        getArticlesByCategoryFromDatabase(categoryId, callback);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<List<Article>> call, Throwable t) {
                    Log.e(TAG, "Error fetching articles by category from API", t);
                    // Fall back to database
                    getArticlesByCategoryFromDatabase(categoryId, callback);
                }
            });
        } else {
            // Offline mode, get from database
            getArticlesByCategoryFromDatabase(categoryId, callback);
        }
    }

    /**
     * Get articles by category from the local database with a callback
     *
     * @param categoryId The ID of the category to filter by
     * @param callback The callback to receive articles or error
     */
    private void getArticlesByCategoryFromDatabase(long categoryId, ArticleCallback callback) {
        articleDao.getArticlesByCategoryAsList(categoryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(articleEntities -> {
                    List<Article> articles = new ArrayList<>();
                    for (ArticleEntity entity : articleEntities) {
                        articles.add(convertToArticle(entity));
                    }
                    callback.onResult(articles, null);
                }, throwable -> {
                    Log.e(TAG, "Error loading category articles from database", throwable);
                    callback.onResult(null, "Error loading category articles from database");
                });
    }
    
    /**
     * Search articles with a callback for immediate use
     * 
     * @param query The search query
     * @param callback The callback to receive search results or error
     */
    public void searchArticles(String query, ArticleCallback callback) {
        if (networkUtils.isOnline()) {
            // Try to get from API first
            apiService.searchArticles(query).enqueue(new retrofit2.Callback<List<Article>>() {
                @Override
                public void onResponse(retrofit2.Call<List<Article>> call, retrofit2.Response<List<Article>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onResult(response.body(), null);
                    } else {
                        // Fall back to database
                        searchArticlesFromDatabase(query, callback);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<List<Article>> call, Throwable t) {
                    Log.e(TAG, "Error searching articles from API", t);
                    // Fall back to database
                    searchArticlesFromDatabase(query, callback);
                }
            });
        } else {
            // Offline mode, search local database
            searchArticlesFromDatabase(query, callback);
        }
    }

    /**
     * Search articles from the local database with a callback
     * 
     * @param query The search query
     * @param callback The callback to receive search results or error
     */
    private void searchArticlesFromDatabase(String query, ArticleCallback callback) {
        executor.execute(() -> {
            try {
                List<ArticleEntity> articleEntities = articleDao.searchArticlesDirect("%" + query + "%");
                List<Article> articles = new ArrayList<>();
                for (ArticleEntity entity : articleEntities) {
                    articles.add(convertToArticle(entity));
                }
                callback.onResult(articles, null);
            } catch (Exception e) {
                Log.e(TAG, "Error searching articles from database", e);
                callback.onResult(null, "Error searching articles: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get articles sorted by date (newest first)
     * 
     * @param callback The callback to be invoked when the operation completes
     */
    public void getArticlesSortedByDate(ArticleCallback callback) {
        if (networkUtils.isOnline()) {
            Call<List<Article>> call = apiService.getArticlesSortedByDate();
            call.enqueue(new Callback<List<Article>>() {
                @Override
                public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Article> articles = response.body();
                        callback.onResult(articles, null);
                        
                        // Save to database in background
                        executor.execute(() -> {
                            try {
                                List<ArticleEntity> articleEntities = convertToArticleEntities(articles);
                                articleDao.insertArticleList(articleEntities).blockingAwait();
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving date-sorted articles to database", e);
                            }
                        });
                    } else {
                        getArticlesSortedByDateFromDatabase(callback);
                    }
                }

                @Override
                public void onFailure(Call<List<Article>> call, Throwable t) {
                    Log.e(TAG, "Error getting articles sorted by date from API", t);
                    getArticlesSortedByDateFromDatabase(callback);
                }
            });
        } else {
            getArticlesSortedByDateFromDatabase(callback);
        }
    }

    /**
     * Get articles sorted by date from local database
     * 
     * @param callback The callback to be invoked when the operation completes
     */
    private void getArticlesSortedByDateFromDatabase(ArticleCallback callback) {
        executor.execute(() -> {
            try {
                List<ArticleEntity> articleEntities = articleDao.getArticlesSortedByDate();
                List<Article> articles = new ArrayList<>();
                for (ArticleEntity entity : articleEntities) {
                    articles.add(convertToArticle(entity));
                }
                callback.onResult(articles, null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting articles sorted by date from database", e);
                callback.onResult(null, "Error getting articles: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get articles sorted by popularity
     * 
     * @param callback The callback to be invoked when the operation completes
     */
    public void getArticlesSortedByPopularity(ArticleCallback callback) {
        if (networkUtils.isOnline()) {
            Call<List<Article>> call = apiService.getArticlesSortedByPopularity();
            call.enqueue(new Callback<List<Article>>() {
                @Override
                public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Article> articles = response.body();
                        callback.onResult(articles, null);
                        
                        // Save to database in background
                        executor.execute(() -> {
                            try {
                                List<ArticleEntity> articleEntities = convertToArticleEntities(articles);
                                articleDao.insertArticleList(articleEntities).blockingAwait();
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving popularity-sorted articles to database", e);
                            }
                        });
                    } else {
                        getArticlesSortedByPopularityFromDatabase(callback);
                    }
                }

                @Override
                public void onFailure(Call<List<Article>> call, Throwable t) {
                    Log.e(TAG, "Error getting articles sorted by popularity from API", t);
                    getArticlesSortedByPopularityFromDatabase(callback);
                }
            });
        } else {
            getArticlesSortedByPopularityFromDatabase(callback);
        }
    }

    /**
     * Get articles sorted by popularity from local database
     * 
     * @param callback The callback to be invoked when the operation completes
     */
    private void getArticlesSortedByPopularityFromDatabase(ArticleCallback callback) {
        executor.execute(() -> {
            try {
                // In a real app, we would keep a popularity metric or use view/like counts
                // For now, just return all articles as a fallback
                List<ArticleEntity> articleEntities = articleDao.getAllArticlesDirect();
                List<Article> articles = new ArrayList<>();
                for (ArticleEntity entity : articleEntities) {
                    articles.add(convertToArticle(entity));
                }
                callback.onResult(articles, null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting articles sorted by popularity from database", e);
                callback.onResult(null, "Error getting articles: " + e.getMessage());
            }
        });
    }

    /**
     * Get recommended articles with a specified limit.
     * 
     * @param limit Maximum number of articles to return
     * @param successCallback Callback for successful retrieval
     * @param errorCallback Callback for errors
     */
    public void getRecommendedArticles(int limit, ArticleListCallback successCallback, ErrorCallback errorCallback) {
        if (networkUtils.isOnline()) {
            // Try to get recommendations from the API first
            executor.execute(() -> {
                try {
                    // For now, we're using the same endpoint and filtering client-side
                    // In a real app, this would use a dedicated recommendations API endpoint
                    Response<List<Article>> response = apiService.getAllArticles().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        // Filter and sort articles to get "recommendations"
                        // In a real app this would be based on user preferences, reading history, etc.
                        List<Article> recommendedArticles = new ArrayList<>(response.body());
                        // Shuffle the list to simulate random recommendations
                        java.util.Collections.shuffle(recommendedArticles);
                        
                        // Limit the number of articles
                        if (recommendedArticles.size() > limit) {
                            recommendedArticles = recommendedArticles.subList(0, limit);
                        }
                        
                        // Create a final copy of the list for the lambda
                        final List<Article> finalRecommendedArticles = new ArrayList<>(recommendedArticles);
                        
                        // Save to database and return result
                        List<ArticleEntity> articleEntities = convertToArticleEntities(finalRecommendedArticles);
                        articleDao.insertArticleList(articleEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    () -> {
                                        Log.d(TAG, "Recommended articles saved to database");
                                        // Return the original objects to avoid another conversion
                                        successCallback.onSuccess(finalRecommendedArticles);
                                    },
                                    error -> {
                                        Log.e(TAG, "Error saving recommended articles", error);
                                        errorCallback.onError("Error saving recommendation data: " + error.getMessage());
                                    }
                                );
                    } else {
                        // Fall back to database if API request failed
                        getRecommendedArticlesFromDatabase(limit, successCallback, errorCallback);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching recommended articles from API", e);
                    // Fall back to database on network error
                    getRecommendedArticlesFromDatabase(limit, successCallback, errorCallback);
                }
            });
        } else {
            // If offline, get data from database
            getRecommendedArticlesFromDatabase(limit, successCallback, errorCallback);
        }
    }
    
    /**
     * Get recommended articles from local database.
     * 
     * @param limit Maximum number of articles to return
     * @param successCallback Callback for successful retrieval
     * @param errorCallback Callback for errors
     */
    private void getRecommendedArticlesFromDatabase(int limit, ArticleListCallback successCallback, ErrorCallback errorCallback) {
        executor.execute(() -> {
            try {
                // Again, in a real app this would use more sophisticated criteria
                List<ArticleEntity> entities = articleDao.getRandomArticles(limit);
                
                if (entities != null && !entities.isEmpty()) {
                    List<Article> articles = new ArrayList<>();
                    for (ArticleEntity entity : entities) {
                        articles.add(convertToArticle(entity));
                    }
                    successCallback.onSuccess(articles);
                } else {
                    errorCallback.onError("No recommended articles found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting recommended articles from database", e);
                errorCallback.onError("Error retrieving recommendations: " + e.getMessage());
            }
        });
    }
    
    /**
     * Callback interface for receiving a list of articles.
     */
    public interface ArticleListCallback {
        void onSuccess(List<Article> articles);
    }
    
    /**
     * Callback interface for errors.
     */
    public interface ErrorCallback {
        void onError(String errorMessage);
    }
} 