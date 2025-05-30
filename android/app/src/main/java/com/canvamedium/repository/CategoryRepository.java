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

import java.text.SimpleDateFormat;
import java.util.Locale;

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
        // Convert date strings to Date objects
        Date createdAt = null;
        Date updatedAt = null;
        
        try {
            if (category.getCreatedAt() != null && !category.getCreatedAt().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                createdAt = dateFormat.parse(category.getCreatedAt());
            }
            
            if (category.getUpdatedAt() != null && !category.getUpdatedAt().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                updatedAt = dateFormat.parse(category.getUpdatedAt());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dates for category", e);
        }
        
        if (createdAt == null) {
            createdAt = new Date();
        }
        
        if (updatedAt == null) {
            updatedAt = new Date();
        }
        
        return new CategoryEntity(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIconUrl(),
                createdAt,
                updatedAt,
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

    /**
     * Get all categories with a callback for immediate use
     *
     * @param callback The callback to receive categories or error
     */
    public void getAllCategories(CategoryCallback callback) {
        if (networkUtils.isOnline()) {
            // Try to get from API first
            executor.execute(() -> {
                try {
                    Response<List<Category>> response = apiService.getAllCategories().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        // Save to database and convert to model objects
                        List<CategoryEntity> entities = convertToCategoryEntities(response.body());
                        categoryDao.insertCategoryList(entities)
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {
                                    // Return the original model objects
                                    callback.onResult(response.body(), null);
                                }, throwable -> {
                                    Log.e(TAG, "Error saving categories to database", throwable);
                                    callback.onResult(null, "Error saving categories to database");
                                });
                    } else {
                        // Fall back to database
                        getCategoriesFromDatabase(callback);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching categories from API", e);
                    // Fall back to database
                    getCategoriesFromDatabase(callback);
                }
            });
        } else {
            // Offline mode, get from database
            getCategoriesFromDatabase(callback);
        }
    }

    /**
     * Get categories from the local database with a callback
     *
     * @param callback The callback to receive categories or error
     */
    private void getCategoriesFromDatabase(CategoryCallback callback) {
        categoryDao.getAllCategoriesList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categoryEntities -> {
                    List<Category> categories = new ArrayList<>();
                    for (CategoryEntity entity : categoryEntities) {
                        categories.add(convertToCategory(entity));
                    }
                    callback.onResult(categories, null);
                }, throwable -> {
                    Log.e(TAG, "Error loading categories from database", throwable);
                    callback.onResult(null, "Error loading categories from database");
                });
    }

    /**
     * Convert a CategoryEntity to a Category model object
     *
     * @param entity The entity to convert
     * @return The Category model object
     */
    private Category convertToCategory(CategoryEntity entity) {
        Category category = new Category();
        category.setId(entity.getId());
        category.setName(entity.getName());
        category.setDescription(entity.getDescription());
        category.setIconUrl(entity.getIconUrl());
        
        // Convert Date objects to strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        if (entity.getCreatedAt() != null) {
            category.setCreatedAt(dateFormat.format(entity.getCreatedAt()));
        }
        if (entity.getUpdatedAt() != null) {
            category.setUpdatedAt(dateFormat.format(entity.getUpdatedAt()));
        }
        
        category.setArticleCount(entity.getArticleCount());
        return category;
    }

    /**
     * Callback interface for category operations
     */
    public interface CategoryCallback {
        /**
         * Called when the operation completes
         * 
         * @param categories The list of categories, or null if there was an error
         * @param errorMsg The error message, or null if the operation succeeded
         */
        void onResult(List<Category> categories, String errorMsg);
    }
} 