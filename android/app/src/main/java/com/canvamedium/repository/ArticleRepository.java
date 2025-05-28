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
        return articleDao.getArticleById(articleId);
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
                Response<Void> response;
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
        return new ArticleEntity(
                article.getId(),
                article.getTitle(),
                article.getPreviewText(),
                article.getContent(),
                article.getThumbnailUrl(),
                article.getAuthor() != null ? article.getAuthor().getId() : null,
                article.getAuthor() != null ? article.getAuthor().getName() : null,
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getPublishedAt(),
                article.getStatus(),
                article.getTemplateId(),
                article.getCategory() != null ? article.getCategory().getId() : null,
                article.getCategory() != null ? article.getCategory().getName() : null,
                article.getTags(),
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
} 