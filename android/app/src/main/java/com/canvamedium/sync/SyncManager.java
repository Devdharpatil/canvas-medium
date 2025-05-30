package com.canvamedium.sync;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.canvamedium.repository.ArticleRepository;
import com.canvamedium.repository.CategoryRepository;
import com.canvamedium.repository.TagRepository;
import com.canvamedium.util.NetworkUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Manager class for handling synchronization of local data with the server.
 */
public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final String PERIODIC_SYNC_WORK = "periodic_sync_work";
    private static final int SYNC_INTERVAL_HOURS = 6;

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NetworkUtils networkUtils;
    private final Application application;

    /**
     * Constructor for the SyncManager
     * 
     * @param application the application context
     */
    public SyncManager(Application application) {
        this.application = application;
        this.articleRepository = new ArticleRepository(application);
        this.categoryRepository = new CategoryRepository(application);
        this.tagRepository = new TagRepository(application);
        this.networkUtils = new NetworkUtils(application);
    }

    /**
     * Schedule periodic synchronization of data
     */
    public void schedulePeriodicSync() {
        // Define constraints for the work request
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Create a periodic work request
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                SYNC_INTERVAL_HOURS,
                TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        // Schedule the work
        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest);

        Log.d(TAG, "Periodic sync scheduled");
    }

    /**
     * Cancel periodic synchronization of data
     */
    public void cancelPeriodicSync() {
        WorkManager.getInstance(application).cancelUniqueWork(PERIODIC_SYNC_WORK);
        Log.d(TAG, "Periodic sync canceled");
    }

    /**
     * Synchronize all data with the server immediately
     */
    public void syncNow() {
        if (!networkUtils.isOnline()) {
            Log.d(TAG, "Cannot sync, device is offline");
            return;
        }

        Log.d(TAG, "Starting manual sync");

        // Sync all unsynced data
        articleRepository.syncUnsyncedArticles();
        categoryRepository.syncUnsyncedCategories();
        tagRepository.syncUnsyncedTags();
    }

    /**
     * Returns an RxJava observable for synchronizing data
     *
     * @return Completable observable
     */
    public Completable requestSync() {
        return Completable.fromAction(() -> {
            if (!networkUtils.isOnline()) {
                throw new IllegalStateException("Device is offline");
            }

            Log.d(TAG, "Starting manual sync");

            // Sync all unsynced data
            articleRepository.syncUnsyncedArticles();
            categoryRepository.syncUnsyncedCategories();
            tagRepository.syncUnsyncedTags();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }
} 