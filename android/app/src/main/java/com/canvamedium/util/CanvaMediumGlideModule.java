package com.canvamedium.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Custom Glide module for the CanvaMedium app.
 * This module configures Glide for efficient image loading and caching.
 */
@GlideModule
public class CanvaMediumGlideModule extends AppGlideModule {
    
    private static final String TAG = "CanvaMediumGlideModule";
    private static final int MEMORY_CACHE_SIZE_MB = 20; // 20MB memory cache
    private static final int DISK_CACHE_SIZE_MB = 100; // 100MB disk cache
    private static final int TIMEOUT_SECONDS = 30;

    /**
     * Configure Glide with custom settings
     * 
     * @param context the application context
     * @param builder the GlideBuilder
     */
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Set memory cache size
        int memoryCacheSizeBytes = MEMORY_CACHE_SIZE_MB * 1024 * 1024;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        
        // Set disk cache size
        int diskCacheSizeBytes = DISK_CACHE_SIZE_MB * 1024 * 1024;
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, diskCacheSizeBytes));
        
        // Set default request options
        RequestOptions defaultOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and resized images
                .downsample(DownsampleStrategy.CENTER_INSIDE) // Downsample for memory efficiency
                .format(DecodeFormat.PREFER_RGB_565) // More memory-efficient format
                .timeout(TIMEOUT_SECONDS * 1000) // Set a timeout for loading
                .skipMemoryCache(false); // Use memory cache
                
        builder.setDefaultRequestOptions(defaultOptions);
        
        // Log configuration
        Log.d(TAG, "Glide configured with " + MEMORY_CACHE_SIZE_MB + "MB memory cache and " + 
                DISK_CACHE_SIZE_MB + "MB disk cache");
    }

    /**
     * Register components with Glide
     * 
     * @param context the application context
     * @param glide the Glide instance
     * @param registry the Glide registry
     */
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // Create a custom OkHttpClient for Glide with longer timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
                
        // Register the OkHttp client with Glide for efficient network operations
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
    
    /**
     * Disable manifest parsing for better performance
     * 
     * @return false (disabling manifest parsing)
     */
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
} 