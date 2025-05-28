package com.canvamedium.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.canvamedium.db.entity.CategoryEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Category entities
 */
@Dao
public interface CategoryDao {

    /**
     * Insert one or more categories into the database
     *
     * @param categories the categories to insert
     * @return a Completable that completes when the insertion is done
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertCategories(CategoryEntity... categories);

    /**
     * Insert a list of categories into the database
     *
     * @param categories the list of categories to insert
     * @return a Completable that completes when the insertion is done
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertCategoryList(List<CategoryEntity> categories);

    /**
     * Update a category in the database
     *
     * @param category the category to update
     * @return a Completable that completes when the update is done
     */
    @Update
    Completable updateCategory(CategoryEntity category);

    /**
     * Delete a category from the database
     *
     * @param category the category to delete
     * @return a Completable that completes when the deletion is done
     */
    @Delete
    Completable deleteCategory(CategoryEntity category);

    /**
     * Get a category by its ID
     *
     * @param id the ID of the category to get
     * @return a Single that emits the category with the given ID, or an error if not found
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    Single<CategoryEntity> getCategoryById(long id);

    /**
     * Get all categories in the database, ordered by name
     *
     * @return a LiveData object that emits the list of categories whenever it changes
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    /**
     * Search for categories by name or description
     *
     * @param query the search query
     * @return a LiveData object that emits the list of matching categories whenever it changes
     */
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' OR " +
           "description LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<CategoryEntity>> searchCategories(String query);

    /**
     * Get all categories that need to be synced with the server
     *
     * @return a Single that emits the list of categories that need to be synced
     */
    @Query("SELECT * FROM categories WHERE is_synced = 0")
    Single<List<CategoryEntity>> getUnsyncedCategories();

    /**
     * Mark a category as synced
     *
     * @param id the ID of the category to mark as synced
     * @return a Completable that completes when the update is done
     */
    @Query("UPDATE categories SET is_synced = 1, last_sync_time = :timestamp WHERE id = :id")
    Completable markCategoryAsSynced(long id, long timestamp);

    /**
     * Delete all categories in the database
     *
     * @return a Completable that completes when the deletion is done
     */
    @Query("DELETE FROM categories")
    Completable deleteAllCategories();
} 