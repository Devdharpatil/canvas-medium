package com.canvamedium.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.db.CanvaMediumDatabase;
import com.canvamedium.db.dao.CategoryDao;
import com.canvamedium.db.entity.CategoryEntity;
import com.canvamedium.model.Category;
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

/**
 * Repository for handling category data from both local database and remote API.
 * This class mediates between the various data sources and the rest of the app.
 */
public class CategoryRepository {

    private static final String TAG = "CategoryRepository";
    private final CategoryDao categoryDao;
    private final ApiService apiService;
    private final Executor executor;
    private final NetworkUtils networkUtils;

    /**
     * Constructor for the CategoryRepository
     * 
     * @param application the application context
     */
    public CategoryRepository(Application application) {
        CanvaMediumDatabase db = CanvaMediumDatabase.getDatabase(application);
        categoryDao = db.categoryDao();
        apiService = ApiClient.getClient().create(ApiService.class);
        executor = Executors.newSingleThreadExecutor();
        networkUtils = new NetworkUtils(application);
    }

    /**
     * Get all categories, either from the local database or remote API if online.
     * 
     * @return a LiveData object that emits the list of categories whenever it changes
     */
    public LiveData<List<CategoryEntity>> getAllCategories() {
        refreshCategories();
        return categoryDao.getAllCategories();
    }

    /**
     * Get a category by its ID
     * 
     * @param categoryId the ID of the category to get
     * @return a Single that emits the category with the given ID, or an error if not found
     */
    public Single<CategoryEntity> getCategoryById(long categoryId) {
        refreshCategory(categoryId);
        return categoryDao.getCategoryById(categoryId);
    }

    /**
     * Search for categories by name or description
     * 
     * @param query the search query
     * @return a LiveData object that emits the list of matching categories whenever it changes
     */
    public LiveData<List<CategoryEntity>> searchCategories(String query) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Category>> response = apiService.searchCategories(query).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<CategoryEntity> categoryEntities = convertToCategoryEntities(response.body());
                        categoryDao.insertCategoryList(categoryEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Search categories saved to database"),
                                        error -> Log.e(TAG, "Error saving search categories", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error searching categories from API", e);
                }
            });
        }
        return categoryDao.searchCategories(query);
    }

    /**
     * Refresh all categories from the API
     */
    private void refreshCategories() {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<List<Category>> response = apiService.getAllCategories().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<CategoryEntity> categoryEntities = convertToCategoryEntities(response.body());
                        categoryDao.insertCategoryList(categoryEntities)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Categories refreshed from API"),
                                        error -> Log.e(TAG, "Error saving categories", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching categories from API", e);
                }
            });
        }
    }

    /**
     * Refresh a category from the API
     * 
     * @param categoryId the ID of the category to refresh
     */
    private void refreshCategory(long categoryId) {
        if (networkUtils.isOnline()) {
            executor.execute(() -> {
                try {
                    Response<Category> response = apiService.getCategoryById(categoryId).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        CategoryEntity categoryEntity = convertToCategoryEntity(response.body());
                        categoryDao.insertCategories(categoryEntity)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "Category refreshed from API"),
                                        error -> Log.e(TAG, "Error saving category", error));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching category from API", e);
                }
            });
        }
    }

    /**
     * Sync all unsynced categories with the server
     */
    public void syncUnsyncedCategories() {
        if (!networkUtils.isOnline()) {
            return;
        }

        categoryDao.getUnsyncedCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        categories -> {
                            for (CategoryEntity category : categories) {
                                syncCategory(category);
                            }
                        },
                        error -> Log.e(TAG, "Error getting unsynced categories", error)
                );
    }

    /**
     * Sync a category with the server
     * 
     * @param category the category to sync
     */
    private void syncCategory(CategoryEntity category) {
        executor.execute(() -> {
            // Implement sync logic here
            // For now, just mark the category as synced
            category.setSynced(true);
            category.setLastSyncTime(new Date());
            
            categoryDao.updateCategory(category)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Log.d(TAG, "Category synced with server"),
                            error -> Log.e(TAG, "Error updating category sync status", error));
        });
    }

    /**
     * Convert an API Category model to a Room CategoryEntity
     * 
     * @param category the API Category model
     * @return the Room CategoryEntity
     */
    private CategoryEntity convertToCategoryEntity(Category category) {
        return new CategoryEntity(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIconUrl(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getArticleCount()
        );
    }

    /**
     * Convert a list of API Category models to a list of Room CategoryEntities
     * 
     * @param categories the list of API Category models
     * @return the list of Room CategoryEntities
     */
    private List<CategoryEntity> convertToCategoryEntities(List<Category> categories) {
        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (Category category : categories) {
            categoryEntities.add(convertToCategoryEntity(category));
        }
        return categoryEntities;
    }
} 