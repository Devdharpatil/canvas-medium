package com.canvamedium;

import android.app.Application;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.canvamedium.api.ApiClient;
import com.canvamedium.db.CanvaMediumDatabase;
import com.canvamedium.sync.SyncManager;
import com.canvamedium.util.AppExceptionHandler;

/**
 * Application class for CanvaMedium.
 * This is the entry point for the application and is used to initialize
 * global components.
 */
public class CanvaMediumApplication extends Application implements Configuration.Provider {

    private SyncManager syncManager;

    /**
     * Called when the application is starting
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Install exception handler for crash reporting
        AppExceptionHandler.install(this);

        // Initialize the database
        CanvaMediumDatabase.getDatabase(this);
        
        // Initialize the API client with caching
        ApiClient.init(this);

        // Initialize WorkManager
        WorkManager.initialize(this, getWorkManagerConfiguration());

        // Initialize the sync manager
        syncManager = new SyncManager(this);

        // Schedule periodic synchronization
        syncManager.schedulePeriodicSync();
    }

    /**
     * Get the SyncManager instance
     * 
     * @return the SyncManager instance
     */
    public SyncManager getSyncManager() {
        return syncManager;
    }

    /**
     * Provides the WorkManager configuration
     * This is required as we've disabled the default initializer in the manifest
     */
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
} 