package com.canvamedium.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.canvamedium.BuildConfig; // Import BuildConfig

/**
 * Singleton class for Retrofit client configuration.
 */
public class RetrofitClient {
    
    private static final String BASE_URL = BuildConfig.BASE_URL; // Use BASE_URL from BuildConfig
    private static Retrofit retrofit = null;
    
    /**
     * Get a configured Retrofit instance.
     *
     * @return Configured Retrofit instance
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create HTTP client with logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            
            // Configure Gson to handle Java 8 date/time types
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
            
            // Build Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * Get an instance of the API service.
     *
     * @return Configured API service
     */
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
    
    /**
     * Create an instance of a service interface.
     *
     * @param serviceClass The service interface class
     * @param <T> The type of the service interface
     * @return An implementation of the service interface
     */
    public static <T> T createService(Class<T> serviceClass) {
        return getClient().create(serviceClass);
    }
}
