package com.canvamedium.util;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.canvamedium.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom binding adapters for data binding in layouts.
 */
public class BindingAdapters {
    
    private static final String TAG = "BindingAdapters";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    
    /**
     * Binding adapter for loading images with Glide
     * @param view The ImageView to load the image into
     * @param url The URL of the image
     */
    @BindingAdapter(value = {"imageUrl", "placeholder"}, requireAll = false)
    public static void loadImage(ImageView view, String url, Drawable placeholder) {
        // Get a fallback image URL if the provided one is problematic
        String imageUrl = getWorkingImageUrl(url);
        
        if (!TextUtils.isEmpty(imageUrl)) {
            // Use Glide to load the image with enhanced configuration for reliability
            Glide.with(view.getContext())
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(new RequestOptions()
                            .placeholder(placeholder != null ? placeholder : view.getContext().getDrawable(R.drawable.ic_placeholder))
                            .error(R.drawable.ic_placeholder))
                    .into(view);
        } else {
            // If no URL is available, set the placeholder directly
            view.setImageDrawable(placeholder != null ? placeholder : view.getContext().getDrawable(R.drawable.ic_placeholder));
        }
    }
    
    /**
     * Validates an image URL and returns a working URL
     * @param url The original URL to validate
     * @return A working image URL
     */
    private static String getWorkingImageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return SampleImageProvider.getRandomImageUrl();
        }
        
        // Check if URL is already using a reliable image service
        if (url.contains("unsplash.com") || url.contains("picsum.photos")) {
            return url;
        }
        
        // Check if URL is a placeholder URL or unreliable format
        if (url.contains("placeholder.com") || url.contains("via.placeholder.com") || 
            !url.startsWith("http")) {
            return SampleImageProvider.getRandomImageUrl();
        }
        
        // If we have doubt about the URL's reliability, return a random reliable URL
        // This is a fallback for problematic URLs in sample data
        try {
            int hashCode = Math.abs(url.hashCode());
            return SampleImageProvider.getImageUrl(hashCode % SampleImageProvider.getAllImageUrls().size());
        } catch (Exception e) {
            Log.e(TAG, "Error getting sample image: " + e.getMessage());
            return SampleImageProvider.getRandomImageUrl();
        }
    }
    
    /**
     * Binding adapter for formatting dates as text.
     *
     * @param view The TextView to set the formatted date in
     * @param date The Date object to format
     */
    @BindingAdapter("dateText")
    public static void setDateText(TextView view, Date date) {
        if (date != null) {
            view.setText(DATE_FORMAT.format(date));
        } else {
            view.setText("");
        }
    }
    
    /**
     * Binding adapter for formatting date strings as text.
     *
     * @param view The TextView to set the formatted date in
     * @param dateString The date string in ISO format to format
     */
    @BindingAdapter("dateText")
    public static void setDateText(TextView view, String dateString) {
        if (!TextUtils.isEmpty(dateString)) {
            try {
                Date date = ISO_FORMAT.parse(dateString);
                if (date != null) {
                    view.setText(DATE_FORMAT.format(date));
                    return;
                }
            } catch (ParseException e) {
                // Fallback to original string if parsing fails
            }
            // If we get here, something went wrong with parsing
            view.setText(dateString);
        } else {
            view.setText("");
        }
    }
    
    /**
     * Binding adapter for setting bookmark icon based on bookmarked status.
     *
     * @param view The ImageView to set the bookmark icon in
     * @param isBookmarked The bookmark status
     */
    @BindingAdapter("isBookmarked")
    public static void setBookmarkIcon(ImageView view, boolean isBookmarked) {
        if (isBookmarked) {
            view.setImageResource(R.drawable.ic_bookmark);
        } else {
            view.setImageResource(R.drawable.ic_bookmark_border);
        }
    }
    
    /**
     * Binding adapter for setting source compatibility in ImageView.
     *
     * @param imageView The ImageView to set the source in
     * @param drawable The drawable to set
     */
    @BindingAdapter("srcCompat")
    public static void setSrcCompat(ImageView imageView, Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    /**
     * Binding adapter for setting source compatibility in ImageView from resource ID.
     *
     * @param imageView The ImageView to set the source in
     * @param resourceId The resource ID of the drawable
     */
    @BindingAdapter("srcCompat")
    public static void setSrcCompatResource(ImageView imageView, int resourceId) {
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        }
    }
} 