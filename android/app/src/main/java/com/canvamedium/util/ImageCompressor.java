package com.canvamedium.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for compressing images before uploading or storing them.
 * This helps reduce bandwidth usage and storage space.
 */
public class ImageCompressor {

    private static final String TAG = "ImageCompressor";
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private static final int COMPRESS_QUALITY = 80;

    private final Context context;

    /**
     * Constructor for the ImageCompressor
     *
     * @param context the application context
     */
    public ImageCompressor(Context context) {
        this.context = context;
    }

    /**
     * Compress a bitmap image
     *
     * @param bitmap the bitmap to compress
     * @return the compressed bitmap
     */
    public Bitmap compressBitmap(Bitmap bitmap) {
        return compressBitmap(bitmap, COMPRESS_QUALITY);
    }

    /**
     * Compress a bitmap image with a specific quality
     *
     * @param bitmap the bitmap to compress
     * @param quality the compression quality (0-100)
     * @return the compressed bitmap
     */
    public Bitmap compressBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // If the image is already small enough, return it as is
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return bitmap;
        }

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            // Width is greater than height
            width = MAX_WIDTH;
            height = (int) (width / bitmapRatio);
        } else {
            // Height is greater than width
            height = MAX_HEIGHT;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Compress an image from a file URI
     *
     * @param imageUri the URI of the image file
     * @return the compressed bitmap, or null if compression fails
     */
    public Bitmap compressImage(Uri imageUri) {
        try {
            // Load the image dimensions first
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);
            options.inJustDecodeBounds = false;
            
            // Decode with inSampleSize set
            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            return compressBitmap(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image from URI", e);
            return null;
        }
    }

    /**
     * Save a compressed bitmap to a file
     *
     * @param bitmap the bitmap to save
     * @param quality the compression quality (0-100)
     * @return the File containing the compressed image, or null if saving fails
     */
    public File saveBitmapToFile(Bitmap bitmap, int quality) {
        try {
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("compressed_", ".jpg", outputDir);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.close();
            
            return outputFile;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            return null;
        }
    }

    /**
     * Calculate the sample size for downsampling an image
     *
     * @param options the BitmapFactory options containing the image dimensions
     * @param reqWidth the required width
     * @param reqHeight the required height
     * @return the calculated sample size
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
} 