package com.canvamedium.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.canvamedium.db.entity.TagEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Tag entities
 */
@Dao
public interface TagDao {

    /**
     * Insert one or more tags into the database
     *
     * @param tags the tags to insert
     * @return a Completable that completes when the insertion is done
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertTags(TagEntity... tags);

    /**
     * Insert a list of tags into the database
     *
     * @param tags the list of tags to insert
     * @return a Completable that completes when the insertion is done
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertTagList(List<TagEntity> tags);

    /**
     * Update a tag in the database
     *
     * @param tag the tag to update
     * @return a Completable that completes when the update is done
     */
    @Update
    Completable updateTag(TagEntity tag);

    /**
     * Delete a tag from the database
     *
     * @param tag the tag to delete
     * @return a Completable that completes when the deletion is done
     */
    @Delete
    Completable deleteTag(TagEntity tag);

    /**
     * Get a tag by its name
     *
     * @param name the name of the tag to get
     * @return a Single that emits the tag with the given name, or an error if not found
     */
    @Query("SELECT * FROM tags WHERE name = :name")
    Single<TagEntity> getTagByName(String name);

    /**
     * Get all tags in the database, ordered by count (most used first)
     *
     * @return a LiveData object that emits the list of tags whenever it changes
     */
    @Query("SELECT * FROM tags ORDER BY count DESC")
    LiveData<List<TagEntity>> getAllTags();

    /**
     * Search for tags by name
     *
     * @param query the search query
     * @return a LiveData object that emits the list of matching tags whenever it changes
     */
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY count DESC")
    LiveData<List<TagEntity>> searchTags(String query);

    /**
     * Get top N popular tags
     * 
     * @param limit the maximum number of tags to return
     * @return a LiveData object that emits the list of most popular tags whenever it changes
     */
    @Query("SELECT * FROM tags ORDER BY count DESC LIMIT :limit")
    LiveData<List<TagEntity>> getTopTags(int limit);

    /**
     * Get all tags that need to be synced with the server
     *
     * @return a Single that emits the list of tags that need to be synced
     */
    @Query("SELECT * FROM tags WHERE is_synced = 0")
    Single<List<TagEntity>> getUnsyncedTags();

    /**
     * Mark a tag as synced
     *
     * @param name the name of the tag to mark as synced
     * @return a Completable that completes when the update is done
     */
    @Query("UPDATE tags SET is_synced = 1, last_sync_time = :timestamp WHERE name = :name")
    Completable markTagAsSynced(String name, long timestamp);

    /**
     * Delete all tags in the database
     *
     * @return a Completable that completes when the deletion is done
     */
    @Query("DELETE FROM tags")
    Completable deleteAllTags();
} 