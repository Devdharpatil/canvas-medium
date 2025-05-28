package com.canvamedium.api;

import android.content.Context;

import com.canvamedium.util.AuthManager;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API client configuration for Retrofit.
 */
public class ApiClient {

    // 10.0.2.2 is a special IP that allows the Android emulator to connect to the host machine's localhost
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB cache
    private static Retrofit retrofit = null;
    private static OkHttpClient client = null;
    private static Context appContext = null;
    
    // For testing purposes
    private static boolean mockMode = false;
    private static AuthService mockAuthService = null;
    private static UserService mockUserService = null;
    
    /**
     * Initialize the API client with application context
     * 
     * @param context The application context
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        client = null; // Force recreation of client with cache
        retrofit = null; // Force recreation of retrofit
    }

    /**
     * Get the Retrofit client instance.
     *
     * @return The Retrofit instance
     */
    public static Retrofit getClient() {
        // For testing purposes
        if (mockMode) {
            // Create a basic Retrofit instance for mocking
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException("ApiClient not initialized. Call init() first.");
            }
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Get the Retrofit client instance with authentication headers.
     *
     * @param context The context
     * @return The Retrofit instance with auth headers
     */
    public static Retrofit getAuthenticatedClient(Context context) {
        // For testing purposes
        if (mockMode) {
            // Create a basic Retrofit instance for mocking
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getAuthenticatedOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Get the OkHttpClient instance with caching.
     *
     * @return The OkHttpClient
     */
    private static OkHttpClient getOkHttpClient() {
        if (client == null) {
            // Create a logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Setup cache directory
            File cacheDir = new File(appContext.getCacheDir(), "http-cache");
            Cache cache = new Cache(cacheDir, CACHE_SIZE);
            
            // Create cache interceptor
            CacheInterceptor cacheInterceptor = new CacheInterceptor(appContext);

            // Build the client with cache
            client = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(loggingInterceptor)
                    .addNetworkInterceptor(cacheInterceptor)
                    .build();
        }
        return client;
    }

    /**
     * Get the OkHttpClient instance with authentication interceptor and caching.
     *
     * @param context The context
     * @return The OkHttpClient with auth interceptor
     */
    private static OkHttpClient getAuthenticatedOkHttpClient(final Context context) {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Setup cache directory
        File cacheDir = new File(context.getCacheDir(), "http-cache");
        Cache cache = new Cache(cacheDir, CACHE_SIZE);
        
        // Create cache interceptor
        CacheInterceptor cacheInterceptor = new CacheInterceptor(context);

        // Create an auth interceptor
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                AuthManager authManager = AuthManager.getInstance(context);
                String token = authManager.getToken();

                Request original = chain.request();
                
                // Add auth header if token exists
                if (token != null && !token.isEmpty()) {
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(original.method(), original.body());
                    
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
                
                return chain.proceed(original);
            }
        };

        // Build the client with both interceptors and cache
        return new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .addNetworkInterceptor(cacheInterceptor)
                .build();
    }
    
    /**
     * Set mock mode for testing.
     * 
     * @param isMockMode true to enable mock mode, false to disable
     */
    public static void setMockMode(boolean isMockMode) {
        mockMode = isMockMode;
    }
    
    /**
     * Set the mock AuthService for testing.
     * 
     * @param service the mock AuthService
     */
    public static void setMockAuthService(AuthService service) {
        mockAuthService = service;
    }
    
    /**
     * Set the mock UserService for testing.
     * 
     * @param service the mock UserService
     */
    public static void setMockUserService(UserService service) {
        mockUserService = service;
    }
    
    /**
     * Reset mock services.
     */
    public static void resetMockServices() {
        mockAuthService = null;
        mockUserService = null;
        mockMode = false;
    }
    
    /**
     * Set a test AuthService for unit testing.
     *
     * @param authService The mock AuthService
     */
    public static void setTestClient(AuthService authService) {
        mockAuthService = authService;
        mockMode = true;
    }

    /**
     * Set a test client for unit testing.
     *
     * @param userService The mock user service
     */
    public static void setTestClient(UserService userService) {
        mockUserService = userService;
        mockMode = true;
    }

    /**
     * Reset the test client.
     */
    public static void resetTestClient() {
        mockAuthService = null;
        mockUserService = null;
        mockMode = false;
    }

    /**
     * Create service interface implementation - with special handling for mock services during testing.
     *
     * @param serviceClass The service interface class
     * @param <T> The type of the service
     * @return The service implementation
     */
    public static <T> T createService(Class<T> serviceClass) {
        if (mockMode) {
            if (serviceClass == AuthService.class && mockAuthService != null) {
                return (T) mockAuthService;
            }
            if (serviceClass == UserService.class && mockUserService != null) {
                return (T) mockUserService;
            }
        }
        return getClient().create(serviceClass);
    }

    /**
     * Create authenticated service interface implementation.
     *
     * @param serviceClass The service interface class
     * @param context The context for authentication
     * @param <T> The type of the service
     * @return The service implementation
     */
    public static <T> T createAuthenticatedService(Class<T> serviceClass, Context context) {
        if (mockMode) {
            if (serviceClass == AuthService.class && mockAuthService != null) {
                return (T) mockAuthService;
            }
            if (serviceClass == UserService.class && mockUserService != null) {
                return (T) mockUserService;
            }
        }
        return getAuthenticatedClient(context).create(serviceClass);
    }
} 