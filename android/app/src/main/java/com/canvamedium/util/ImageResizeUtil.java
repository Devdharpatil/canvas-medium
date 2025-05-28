package com.canvamedium.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for resizing and cropping images.
 */
public class ImageResizeUtil {

    private static final String TAG = "ImageResizeUtil";
    private static final int MAX_IMAGE_DIMENSION = 1920;
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int THUMBNAIL_HEIGHT = 225;
    private static final int COMPRESSION_QUALITY = 85;

    /**
     * Resizes a bitmap to fit within the maximum dimensions while maintaining aspect ratio.
     *
     * @param bitmap The bitmap to resize
     * @return The resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        
        // If already smaller than max dimensions, return original
        if (originalWidth <= MAX_IMAGE_DIMENSION && originalHeight <= MAX_IMAGE_DIMENSION) {
            return bitmap;
        }
        
        // Calculate new dimensions while maintaining aspect ratio
        float scale;
        if (originalWidth > originalHeight) {
            scale = (float) MAX_IMAGE_DIMENSION / originalWidth;
        } else {
            scale = (float) MAX_IMAGE_DIMENSION / originalHeight;
        }
        
        int newWidth = Math.round(originalWidth * scale);
        int newHeight = Math.round(originalHeight * scale);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    /**
     * Resizes a bitmap to specific dimensions.
     *
     * @param bitmap The bitmap to resize
     * @param width  The target width
     * @param height The target height
     * @return The resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null) return null;
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    
    /**
     * Creates a thumbnail from a bitmap.
     *
     * @param bitmap The source bitmap
     * @return A thumbnail bitmap
     */
    public static Bitmap createThumbnail(Bitmap bitmap) {
        if (bitmap == null) return null;
        return resizeBitmap(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
    }
    
    /**
     * Crops a bitmap to the specified dimensions.
     *
     * @param bitmap The bitmap to crop
     * @param x      The x-coordinate of the top-left corner
     * @param y      The y-coordinate of the top-left corner
     * @param width  The width of the cropped area
     * @param height The height of the cropped area
     * @return The cropped bitmap
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int x, int y, int width, int height) {
        if (bitmap == null) return null;
        
        // Ensure crop rectangle is within bitmap bounds
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > bitmap.getWidth()) width = bitmap.getWidth() - x;
        if (y + height > bitmap.getHeight()) height = bitmap.getHeight() - y;
        
        return Bitmap.createBitmap(bitmap, x, y, width, height);
    }
    
    /**
     * Creates a thumbnail from a bitmap with center-crop.
     *
     * @param bitmap The source bitmap
     * @return A center-cropped thumbnail
     */
    public static Bitmap createCenterCropThumbnail(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        float aspectRatio = (float) THUMBNAIL_WIDTH / THUMBNAIL_HEIGHT;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float bitmapAspect = (float) bitmapWidth / bitmapHeight;
        
        int x = 0, y = 0;
        int width, height;
        
        if (bitmapAspect > aspectRatio) {
            // Bitmap is wider than we want, crop the width
            height = bitmapHeight;
            width = Math.round(height * aspectRatio);
            x = (bitmapWidth - width) / 2;
        } else {
            // Bitmap is taller than we want, crop the height
            width = bitmapWidth;
            height = Math.round(width / aspectRatio);
            y = (bitmapHeight - height) / 2;
        }
        
        // Crop to the desired aspect ratio
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        
        // Resize to thumbnail size
        return resizeBitmap(croppedBitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
    }
    
    /**
     * Saves a bitmap to a file in the app's cache directory.
     *
     * @param context The context
     * @param bitmap  The bitmap to save
     * @param quality The compression quality (0-100)
     * @return The saved file, or null if saving failed
     */
    public static File saveBitmapToFile(Context context, Bitmap bitmap, int quality) {
        if (bitmap == null || context == null) return null;
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "CANVAMEDIUM_" + timeStamp + "_";
        File storageDir = context.getCacheDir();
        
        try {
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            
            // Compress the bitmap and save to file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            byte[] bitmapData = bos.toByteArray();
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets a bitmap from a URI.
     *
     * @param context The context
     * @param uri     The URI of the image
     * @return The bitmap, or null if loading failed
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Rotates a bitmap by the specified angle.
     * 
     * @param bitmap The bitmap to rotate
     * @param angle  The angle to rotate in degrees
     * @return The rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float angle) {
        if (bitmap == null) return null;
        
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
} 