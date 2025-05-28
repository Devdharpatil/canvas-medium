package com.canvamedium.util;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canvamedium.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom binding adapters for data binding in layouts.
 */
public class BindingAdapters {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    
    /**
     * Binding adapter for loading images from URLs.
     *
     * @param view The ImageView to load the image into
     * @param imageUrl The URL of the image to load
     */
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(view.getContext())
                .load(imageUrl)
                .apply(new RequestOptions()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image))
                .into(view);
        } else {
            view.setImageResource(R.drawable.placeholder_image);
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
     * Binding adapter for setting bookmark icon based on bookmarked status.
     *
     * @param view The ImageView to set the bookmark icon in
     * @param isBookmarked The bookmark status
     */
    @BindingAdapter("isBookmarked")
    public static void setBookmarkIcon(ImageView view, boolean isBookmarked) {
        if (isBookmarked) {
            view.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            view.setImageResource(R.drawable.ic_bookmark_border);
        }
    }
} 