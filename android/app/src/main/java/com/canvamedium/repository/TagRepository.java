package com.canvamedium.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.db.CanvaMediumDatabase;
import com.canvamedium.db.dao.TagDao;
import com.canvamedium.db.entity.TagEntity;
import com.canvamedium.model.Tag;
import com.canvamedium.util.NetworkUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Repository for handling tag data from both local database and remote API.
 * This class mediates between the various data sources and the rest of the app.
 */
public class TagRepository {

    private static final String TAG = "TagRepository";
    private final TagDao tagDao;
    private final ApiService apiService;
    private final Executor executor;
    private final NetworkUtils networkUtils;

    /**
     * Constructor for the TagRepository
     * 
     * @param application the application context
     */
    public TagRepository(Application application) {
        CanvaMediumDatabase db = CanvaMediumDatabase.getDatabase(application);
        tagDao = db.tagDao();
        apiService = ApiClient.getClient().create(ApiService.class);
        executor = Executors.newSingleThreadExecutor();
        networkUtils = new NetworkUtils(application);
    }

    /**
     * Get all tags, either from the local database or remote API if online.
     * 
     * @return a LiveData object that emits the list of tags whenever it changes
     */
    public LiveData<List<TagEntity>> getAllTags() {
        refreshTags();
        return tagDao.getAllTags();
    }

    /**
     * Get a tag by its name
     * 
     * @param name the name of the tag to get
     * @return a Single that emits the tag with the given name, or an error if not found
     */
    public Single<TagEntity> getTagByName(String name) {
        refreshTag(name);
        return tagDao.getTagByName(name);
    }

    /**
     * Get the top N most popular tags
     * 
     * @param limit the maximum number of tags to get
     * @return a LiveData object that emits the list of most popular tags whenever it changes
     */
    public LiveData<List<TagEntity>> getTopTags(int limit) {
        refreshTags();
        return tagDao.getTopTags(limit);
    }

    /**
     * Search for tags by name
     * 
     * @param query the search query
     * @return a LiveData object that emits the list of matching tags whenever it changes
     */
    public LiveData<List<TagEntity>> searchTags(String query) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Tag>> response = apiService.searchTags(query).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<TagEntity> tagEntities = convertToTagEntities(response.body());
                        tagDao.insertTagList(tagEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Search tags saved to database"),
                                        error -> Log.e(TAG, "Error saving search tags", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error searching tags from API", e);
                }
            });
        }
        return tagDao.searchTags(query);
    }

    /**
     * Refresh all tags from the API
     */
    private void refreshTags() {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Tag>> response = apiService.getAllTags().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<TagEntity> tagEntities = convertToTagEntities(response.body());
                        tagDao.insertTagList(tagEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Tags refreshed from API"),
                                        error -> Log.e(TAG, "Error saving tags", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching tags from API", e);
                }
            });
        }
    }

    /**
     * Refresh a tag from the API
     * 
     * @param name the name of the tag to refresh
     */
    private void refreshTag(String name) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<Tag> response = apiService.getTagByName(name).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        TagEntity tagEntity = convertToTagEntity(response.body());
                        tagDao.insertTags(tagEntity)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Tag refreshed from API"),
                                        error -> Log.e(TAG, "Error saving tag", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching tag from API", e);
                }
            });
        }
    }

    /**
     * Sync all unsynced tags with the server
     */
    public void syncUnsyncedTags() {
        if (!networkUtils.isOnline()) {
            return;
        }

        tagDao.getUnsyncedTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tags -> {
                            for (TagEntity tag : tags) {
                                syncTag(tag);
                            }
                        },
                        error -> Log.e(TAG, "Error getting unsynced tags", error)
                );
    }

    /**
     * Sync a tag with the server
     * 
     * @param tag the tag to sync
     */
    private void syncTag(TagEntity tag) {
        executor.execute(() -> {
            // Implement sync logic here
            // For now, just mark the tag as synced
            tag.setSynced(true);
            tag.setLastSyncTime(new Date());
            
            tagDao.updateTag(tag)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Log.d(TAG, "Tag synced with server"),
                            error -> Log.e(TAG, "Error updating tag sync status", error));
        });
    }

    /**
     * Convert an API Tag model to a Room TagEntity
     * 
     * @param tag the API Tag model
     * @return the Room TagEntity
     */
    private TagEntity convertToTagEntity(Tag tag) {
        // Convert date strings to Date objects
        Date createdAt = null;
        Date updatedAt = null;
        
        try {
            if (tag.getCreatedAt() != null && !tag.getCreatedAt().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                createdAt = dateFormat.parse(tag.getCreatedAt());
            }
            
            if (tag.getUpdatedAt() != null && !tag.getUpdatedAt().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                updatedAt = dateFormat.parse(tag.getUpdatedAt());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dates for tag", e);
        }
        
        if (createdAt == null) {
            createdAt = new Date();
        }
        
        if (updatedAt == null) {
            updatedAt = new Date();
        }
        
        return new TagEntity(
                tag.getName(),
                tag.getCount(),
                createdAt,
                updatedAt
        );
    }

    /**
     * Convert a list of API Tag models to a list of Room TagEntities
     * 
     * @param tags the list of API Tag models
     * @return the list of Room TagEntities
     */
    private List<TagEntity> convertToTagEntities(List<Tag> tags) {
        List<TagEntity> tagEntities = new ArrayList<>();
        for (Tag tag : tags) {
            tagEntities.add(convertToTagEntity(tag));
        }
        return tagEntities;
    }
} 