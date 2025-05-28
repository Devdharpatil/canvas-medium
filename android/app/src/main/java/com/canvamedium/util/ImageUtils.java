package com.canvamedium.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for handling image operations.
 */
public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_DIMENSION = 1200;
    
    /**
     * Converts a content URI to a File.
     *
     * @param context The context
     * @param uri The content URI
     * @return The File
     * @throws IOException If an error occurs while reading or writing the file
     */
    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        // First try to get the file path
        String filePath = getPathFromUri(context, uri);
        
        if (filePath != null) {
            // Return the file if a path was found
            return new File(filePath);
        } else {
            // If path not found, copy the content to a temporary file
            return createTempFileFromUri(context, uri);
        }
    }
    
    /**
     * Gets the file path from a URI.
     *
     * @param context The context
     * @param uri The URI
     * @return The file path, or null if not found
     */
    private static String getPathFromUri(Context context, Uri uri) {
        String filePath = null;
        
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file path from URI: " + e.getMessage());
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        
        return filePath;
    }
    
    /**
     * Creates a temporary file from a URI by copying its content.
     *
     * @param context The context
     * @param uri The URI
     * @return The temporary file
     * @throws IOException If an error occurs while reading or writing the file
     */
    private static File createTempFileFromUri(Context context, Uri uri) throws IOException {
        // Create a temporary file in the cache directory
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        File outputFile = new File(context.getCacheDir(), fileName);
        
        // Copy the content from the URI to the temporary file
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            
            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }
            
            byte[] buffer = new byte[4 * 1024]; // 4k buffer
            int read;
            
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            outputStream.flush();
            return outputFile;
        }
    }
    
    /**
     * Resizes an image from a URI to be within the maximum dimensions.
     *
     * @param context The context
     * @param uri The URI of the image
     * @return The resized bitmap, or null if resizing fails
     */
    public static Bitmap resizeImageFromUri(Context context, Uri uri) {
        try {
            // Get the dimensions of the original bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(inputStream, null, options);
            }
            
            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            
            // Calculate the scale factor
            int scaleFactor = Math.max(1, Math.min(
                    originalWidth / MAX_IMAGE_DIMENSION,
                    originalHeight / MAX_IMAGE_DIMENSION));
            
            // Decode the image with the scale factor
            options = new BitmapFactory.Options();
            options.inSampleSize = scaleFactor;
            options.inJustDecodeBounds = false;
            
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(inputStream, null, options);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error resizing image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Rotates a bitmap to a given angle.
     *
     * @param bitmap The bitmap to rotate
     * @param angle The rotation angle in degrees
     * @return The rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float angle) {
        if (angle == 0) return bitmap;
        
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    
    /**
     * Saves a bitmap to a file in JPEG format.
     *
     * @param bitmap The bitmap to save
     * @param file The output file
     * @param quality The JPEG quality (0-100)
     * @return True if saving was successful, false otherwise
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, File file, int quality) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file: " + e.getMessage());
            return false;
        }
    }
} 