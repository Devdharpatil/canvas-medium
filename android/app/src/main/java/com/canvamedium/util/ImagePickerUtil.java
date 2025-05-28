package com.canvamedium.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for handling image selection and processing.
 */
public class ImagePickerUtil {

    public interface ImagePickedListener {
        void onImagePicked(Uri imageUri, Bitmap bitmap);
    }

    private final AppCompatActivity activity;
    private final ImagePickedListener listener;
    private File tempImageFile;
    private Uri tempImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher;
    private final ActivityResultLauncher<Intent> cameraLauncher;

    /**
     * Constructor for ImagePickerUtil.
     *
     * @param activity The activity that will handle the result
     * @param listener The listener that will receive the picked image
     */
    public ImagePickerUtil(AppCompatActivity activity, ImagePickedListener listener) {
        this.activity = activity;
        this.listener = listener;

        // Initialize launchers
        galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            handleImageResult(selectedImageUri);
                        }
                    }
                });

        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (tempImageUri != null) {
                            handleImageResult(tempImageUri);
                        }
                    }
                });
    }

    /**
     * Opens the gallery to pick an image.
     */
    public void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    /**
     * Opens the camera to take a picture.
     */
    public void pickFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Create temporary file to store the image
        tempImageFile = createTempImageFile();
        if (tempImageFile != null) {
            tempImageUri = FileProvider.getUriForFile(
                    activity,
                    activity.getApplicationContext().getPackageName() + ".provider",
                    tempImageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
            cameraLauncher.launch(intent);
        }
    }

    /**
     * Creates a temporary file for storing camera images.
     *
     * @return A temporary file
     */
    private File createTempImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getCacheDir();
        
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handles the image result from gallery or camera.
     *
     * @param imageUri The URI of the selected image
     */
    private void handleImageResult(Uri imageUri) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);
            if (bitmap != null) {
                // Create a copy of the image in app's cache directory
                File imageFile = createImageFile(bitmap);
                if (imageFile != null) {
                    Uri savedUri = Uri.fromFile(imageFile);
                    listener.onImagePicked(savedUri, bitmap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a bitmap from a URI.
     *
     * @param uri The URI of the image
     * @return A bitmap of the image
     * @throws IOException if there's an error reading the image
     */
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ContentResolver contentResolver = activity.getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(uri);
        return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * Creates an image file from a bitmap.
     *
     * @param bitmap The bitmap to save
     * @return The created file
     */
    private File createImageFile(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "CANVAMEDIUM_" + timeStamp + "_";
        File storageDir = activity.getCacheDir();
        
        try {
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            
            // Compress the bitmap and save to file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
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
     * Gets the file extension from a URI.
     *
     * @param uri The URI to check
     * @return The file extension
     */
    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = activity.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Resizes a bitmap to a specified width and height.
     *
     * @param bitmap The bitmap to resize
     * @param width  The target width
     * @param height The target height
     * @return The resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
} 