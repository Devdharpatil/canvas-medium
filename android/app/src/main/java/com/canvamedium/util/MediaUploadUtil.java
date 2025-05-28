package com.canvamedium.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class for handling media uploads to the server.
 */
public class MediaUploadUtil {

    private static final String TAG = "MediaUploadUtil";

    /**
     * Interface for upload result callbacks.
     */
    public interface UploadCallback {
        void onSuccess(String fileUrl);
        void onFailure(String errorMessage);
    }
    
    /**
     * Interface for upload with thumbnail result callbacks.
     */
    public interface UploadWithThumbnailCallback {
        void onSuccess(String fileUrl, String thumbnailUrl);
        void onFailure(String errorMessage);
    }

    /**
     * Uploads a bitmap to the server.
     *
     * @param context   The context
     * @param bitmap    The bitmap to upload
     * @param callback  The callback to handle the result
     */
    public static void uploadBitmap(Context context, Bitmap bitmap, UploadCallback callback) {
        try {
            // Convert bitmap to file
            File file = convertBitmapToFile(context, bitmap);
            uploadFile(context, file, callback);
        } catch (IOException e) {
            Log.e(TAG, "Error uploading bitmap", e);
            callback.onFailure("Error processing image: " + e.getMessage());
        }
    }

    /**
     * Uploads a file from a URI to the server.
     *
     * @param context   The context
     * @param uri       The URI of the file to upload
     * @param callback  The callback to handle the result
     */
    public static void uploadUri(Context context, Uri uri, UploadCallback callback) {
        try {
            File file = createFileFromUri(context, uri);
            if (file != null) {
                uploadFile(context, file, callback);
            } else {
                callback.onFailure("Failed to create file from URI");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error uploading URI", e);
            callback.onFailure("Error processing image: " + e.getMessage());
        }
    }

    /**
     * Uploads a file to the server.
     *
     * @param context   The context
     * @param file      The file to upload
     * @param callback  The callback to handle the result
     */
    private static void uploadFile(Context context, File file, UploadCallback callback) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        // Create request body
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        
        // Make API call
        Call<Map<String, String>> call = apiService.uploadFile(body);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().get("url");
                    if (url != null) {
                        callback.onSuccess(url);
                    } else {
                        callback.onFailure("URL not found in response");
                    }
                } else {
                    callback.onFailure("Upload failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "Upload failed", t);
                callback.onFailure("Upload failed: " + t.getMessage());
            }
        });
    }
    
    /**
     * Uploads a bitmap to the server with thumbnail generation.
     *
     * @param context   The context
     * @param bitmap    The bitmap to upload
     * @param callback  The callback to handle the result
     */
    public static void uploadBitmapWithThumbnail(Context context, Bitmap bitmap, UploadWithThumbnailCallback callback) {
        try {
            // Convert bitmap to file
            File file = convertBitmapToFile(context, bitmap);
            uploadFileWithThumbnail(context, file, callback);
        } catch (IOException e) {
            Log.e(TAG, "Error uploading bitmap", e);
            callback.onFailure("Error processing image: " + e.getMessage());
        }
    }
    
    /**
     * Uploads a file with thumbnail generation.
     *
     * @param context   The context
     * @param file      The file to upload
     * @param callback  The callback to handle the result
     */
    private static void uploadFileWithThumbnail(Context context, File file, UploadWithThumbnailCallback callback) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        // Create request body
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        
        // Make API call
        Call<Map<String, String>> call = apiService.uploadFileWithThumbnail(body);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().get("url");
                    String thumbnailUrl = response.body().get("thumbnailUrl");
                    
                    if (url != null && thumbnailUrl != null) {
                        callback.onSuccess(url, thumbnailUrl);
                    } else {
                        callback.onFailure("URLs not found in response");
                    }
                } else {
                    callback.onFailure("Upload failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "Upload failed", t);
                callback.onFailure("Upload failed: " + t.getMessage());
            }
        });
    }
    
    /**
     * Generates a thumbnail for an existing image URL.
     * 
     * @param imageUrl  The URL of the image to generate a thumbnail for
     * @param callback  The callback to handle the result
     */
    public static void generateThumbnail(String imageUrl, UploadCallback callback) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        Map<String, String> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        
        Call<Map<String, String>> call = apiService.generateThumbnail(params);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String thumbnailUrl = response.body().get("thumbnailUrl");
                    
                    if (thumbnailUrl != null) {
                        callback.onSuccess(thumbnailUrl);
                    } else {
                        callback.onFailure("Thumbnail URL not found in response");
                    }
                } else {
                    callback.onFailure("Thumbnail generation failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "Thumbnail generation failed", t);
                callback.onFailure("Thumbnail generation failed: " + t.getMessage());
            }
        });
    }

    /**
     * Creates a file from a URI.
     *
     * @param context The context
     * @param uri     The URI to convert
     * @return The created file
     * @throws IOException if there's an error reading the URI
     */
    private static File createFileFromUri(Context context, Uri uri) throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileExtension = getFileExtension(context, uri);
        String fileName = "image_" + timeStamp + "." + fileExtension;
        
        File file = new File(context.getCacheDir(), fileName);
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            
            if (inputStream == null) {
                throw new IOException("Failed to open input stream for URI: " + uri);
            }
            
            byte[] buffer = new byte[4 * 1024]; // 4k buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            outputStream.flush();
            return file;
        }
    }

    /**
     * Converts a bitmap to a file.
     *
     * @param context The context
     * @param bitmap  The bitmap to convert
     * @return The created file
     * @throws IOException if there's an error writing the file
     */
    private static File convertBitmapToFile(Context context, Bitmap bitmap) throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileName = "image_" + timeStamp + ".jpg";
        
        File file = new File(context.getCacheDir(), fileName);
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            return file;
        }
    }

    /**
     * Gets the file extension from a URI.
     *
     * @param context The context
     * @param uri     The URI to check
     * @return The file extension
     */
    private static String getFileExtension(Context context, Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        
        return extension != null ? extension : "jpg";
    }
} 