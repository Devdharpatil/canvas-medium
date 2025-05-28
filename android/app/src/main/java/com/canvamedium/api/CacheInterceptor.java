package com.canvamedium.api;

import android.content.Context;

import com.canvamedium.util.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to cache data and manage offline caching behavior.
 * This will help the app work offline by serving cached data when no network is available.
 */
public class CacheInterceptor implements Interceptor {
    
    private final Context context;
    private final NetworkUtils networkUtils;
    
    /**
     * Constructor for the CacheInterceptor
     * 
     * @param context the application context
     */
    public CacheInterceptor(Context context) {
        this.context = context;
        this.networkUtils = new NetworkUtils(context);
    }
    
    /**
     * Intercept the request and modify it to use caching
     * 
     * @param chain the interceptor chain
     * @return the response, potentially from cache
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        // If offline, force use of cache
        if (!networkUtils.isOnline()) {
            CacheControl cacheControl = new CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build();
            
            request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build();
        } else {
            // If online, use network but allow cache if needed
            request = request.newBuilder()
                    .header("Cache-Control", "public, max-age=5")
                    .build();
        }
        
        Response response = chain.proceed(request);
        
        // If online, cache response for offline use
        if (networkUtils.isOnline()) {
            // Re-write response header to force use of cache
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=60")
                    .build();
        }
        
        return response;
    }
} 