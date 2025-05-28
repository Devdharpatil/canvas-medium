package com.canvamedium.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.repository.CategoryRepository;
import com.canvamedium.repository.TagRepository;

/**
 * Worker class for handling background synchronization.
 */
public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    /**
     * Constructor for the SyncWorker
     * 
     * @param context the application context
     * @param workerParams the worker parameters
     */
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Do the background work
     * 
     * @return the result of the work
     */
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting background sync");

        try {
            // Create repositories
            ArticleRepository articleRepository = new ArticleRepository(getApplicationContext());
            CategoryRepository categoryRepository = new CategoryRepository(getApplicationContext());
            TagRepository tagRepository = new TagRepository(getApplicationContext());

            // Sync all unsynced data
            articleRepository.syncUnsyncedArticles();
            categoryRepository.syncUnsyncedCategories();
            tagRepository.syncUnsyncedTags();

            Log.d(TAG, "Background sync completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Background sync failed", e);
            return Result.retry();
        }
    }
} 