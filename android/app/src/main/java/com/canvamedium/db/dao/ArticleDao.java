package com.canvamedium.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.canvamedium.db.entity.ArticleEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Article entities
 */
@Dao
public interface ArticleDao {

    /**
     * Insert one or more articles into the database
     *
     * @param articles the articles to insert
     * @return a Completable that completes when the insertion is done
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertArticles(ArticleEntity... articles);

    /**
     * Insert a list of articles into the database
     *
     * @param articles the list of articles to insert
     * @return a Completable that completes when the insertion is done
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertArticleList(List<ArticleEntity> articles);

    /**
     * Update an article in the database
     *
     * @param article the article to update
     * @return a Completable that completes when the update is done
     */
    @Update
    Completable updateArticle(ArticleEntity article);

    /**
     * Delete an article from the database
     *
     * @param article the article to delete
     * @return a Completable that completes when the deletion is done
     */
    @Delete
    Completable deleteArticle(ArticleEntity article);

    /**
     * Get an article by its ID
     *
     * @param id the ID of the article to get
     * @return a Single that emits the article with the given ID, or an error if not found
     */
    @Query("SELECT * FROM articles WHERE id = :id")
    Single<ArticleEntity> getArticleById(long id);

    /**
     * Get all articles in the database, ordered by creation date (newest first)
     *
     * @return a LiveData object that emits the list of articles whenever it changes
     */
    @Query("SELECT * FROM articles ORDER BY created_at DESC")
    LiveData<List<ArticleEntity>> getAllArticles();

    /**
     * Get all bookmarked articles, ordered by creation date (newest first)
     *
     * @return a LiveData object that emits the list of bookmarked articles whenever it changes
     */
    @Query("SELECT * FROM articles WHERE is_bookmarked = 1 ORDER BY created_at DESC")
    LiveData<List<ArticleEntity>> getBookmarkedArticles();

    /**
     * Get all articles in a category, ordered by creation date (newest first)
     *
     * @param categoryId the ID of the category
     * @return a LiveData object that emits the list of articles in the category whenever it changes
     */
    @Query("SELECT * FROM articles WHERE category_id = :categoryId ORDER BY created_at DESC")
    LiveData<List<ArticleEntity>> getArticlesByCategory(long categoryId);

    /**
     * Search for articles by title or content
     *
     * @param query the search query
     * @return a LiveData object that emits the list of matching articles whenever it changes
     */
    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR " +
           "content LIKE '%' || :query || '%' ORDER BY created_at DESC")
    LiveData<List<ArticleEntity>> searchArticles(String query);

    /**
     * Get all articles that need to be synced with the server
     *
     * @return a Single that emits the list of articles that need to be synced
     */
    @Query("SELECT * FROM articles WHERE is_synced = 0")
    Single<List<ArticleEntity>> getUnsyncedArticles();

    /**
     * Mark an article as synced
     *
     * @param id the ID of the article to mark as synced
     * @return a Completable that completes when the update is done
     */
    @Query("UPDATE articles SET is_synced = 1, last_sync_time = :timestamp WHERE id = :id")
    Completable markArticleAsSynced(long id, long timestamp);

    /**
     * Delete all articles in the database
     *
     * @return a Completable that completes when the deletion is done
     */
    @Query("DELETE FROM articles")
    Completable deleteAllArticles();
} 