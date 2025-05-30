package com.canvamedium.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Gravity;

import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.canvamedium.model.TemplateElement;
import com.google.android.material.card.MaterialCardView;
import com.canvamedium.model.Template;

/**
 * Custom view for a draggable element in the template builder.
 * This view can be dragged and resized, and represents a template element.
 */
public class DraggableElementView extends MaterialCardView {

    private float dX, dY;
    private float lastTouchX, lastTouchY;
    private boolean dragging = false;
    private TemplateElement element;
    private ViewGroup parent;
    private boolean selected = false;
    private View contentView;
    private OnPositionChangedListener listener;
    
    // For smooth animation
    private static final float DRAG_SCALE = 1.05f;
    private static final long ANIMATION_DURATION = 150;
    private Paint shadowPaint;

    /**
     * Interface for notifying when the position of the element has changed.
     */
    public interface OnPositionChangedListener {
        void onPositionChanged(DraggableElementView view, TemplateElement element);
    }

    public DraggableElementView(Context context) {
        super(context);
        init();
    }

    public DraggableElementView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableElementView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setCardElevation(4f);
        setRadius(8f);
        setUseCompatPadding(true);
        
        // Enable hardware acceleration for smoother animations
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // Initialize shadow paint for drag state
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(50);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setShadowLayer(12, 0, 0, Color.BLACK);
        
        // Handle touch events for drag-and-drop
        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();
                    dX = getX() - event.getRawX();
                    dY = getY() - event.getRawY();
                    setSelected(true);
                    
                    // Use ViewPropertyAnimator for smooth scale effect on touch
                    animate()
                        .scaleX(DRAG_SCALE)
                        .scaleY(DRAG_SCALE)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                    
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;
                    
                    // Use ViewPropertyAnimator for smoother motion
                    if (!dragging) {
                        dragging = true;
                        // Cancel any ongoing animations first
                        animate().cancel();
                    }
                    
                    // Clamp position to parent bounds
                    float clampedX = Math.max(0, Math.min(newX, parent.getWidth() - getWidth()));
                    float clampedY = Math.max(0, Math.min(newY, parent.getHeight() - getHeight()));
                    
                    // Use ViewPropertyAnimator with a very short duration for responsive yet smooth movement
                    animate()
                        .x(clampedX)
                        .y(clampedY)
                        .setDuration(0) // Immediate for responsiveness during drag
                        .start();
                    
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Reset scale with animation
                    animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                    
                    if (!dragging) {
                        performClick();
                    } else {
                        dragging = false;
                        element.setX((int) getX());
                        element.setY((int) getY());
                        if (listener != null) {
                            listener.onPositionChanged(this, element);
                        }
                    }
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    @Override
    public boolean performClick() {
        setSelected(!selected);
        return super.performClick();
    }

    /**
     * Sets the selection state of the element.
     *
     * @param selected true if selected, false otherwise
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            // Use ViewPropertyAnimator for smooth elevation change
            animate()
                .translationZ(8f)
                .setDuration(ANIMATION_DURATION)
                .start();
                
            GradientDrawable border = new GradientDrawable();
            border.setStroke(4, Color.BLUE);
            ViewCompat.setBackground(this, border);
        } else {
            // Use ViewPropertyAnimator for smooth elevation change
            animate()
                .translationZ(4f)
                .setDuration(ANIMATION_DURATION)
                .start();
                
            setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        parent = (ViewGroup) getParent();
    }

    /**
     * Sets the element associated with this view.
     *
     * @param element the TemplateElement to associate with this view
     */
    public void setElement(TemplateElement element) {
        this.element = element;
        updateFromElement();
    }

    /**
     * Gets the element associated with this view.
     *
     * @return the TemplateElement associated with this view
     */
    public TemplateElement getElement() {
        return element;
    }

    /**
     * Updates the view based on the associated element.
     */
    public void updateFromElement() {
        if (element == null) return;
        
        // Use ViewPropertyAnimator for smooth position transition when loading
        animate()
            .x(element.getX())
            .y(element.getY())
            .setDuration(ANIMATION_DURATION)
            .start();
        
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(element.getWidth(), element.getHeight());
        } else {
            params.width = element.getWidth();
            params.height = element.getHeight();
        }
        setLayoutParams(params);
        
        // Create or update content based on element type
        removeAllViews();
        String elementType = element.getType();
        if (elementType == null) return;
        
        // Use case-insensitive comparison for element types
        elementType = elementType.toUpperCase();
        
        if (Template.ELEMENT_TYPE_TEXT.equals(elementType)) {
            createTextView();
        } else if (Template.ELEMENT_TYPE_IMAGE.equals(elementType)) {
            setupImageElement(element);
        } else if (Template.ELEMENT_TYPE_HEADER.equals(elementType)) {
            createHeaderView();
        } else if (Template.ELEMENT_TYPE_DIVIDER.equals(elementType)) {
            createDividerView();
        } else if (Template.ELEMENT_TYPE_QUOTE.equals(elementType)) {
            createQuoteView();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw an extra shadow when dragging for better visual feedback
        if (dragging) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), shadowPaint);
        }
        super.draw(canvas);
    }

    private void createTextView() {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        textView.setPadding(16, 16, 16, 16);
        
        String text = "Sample Text";
        if (element.getProperties() != null && element.getProperty("text") != null) {
            text = element.getProperty("text").toString();
        }
        textView.setText(text);
        
        addView(textView);
        contentView = textView;
    }

    private void createHeaderView() {
        TextView headerView = new TextView(getContext());
        headerView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        headerView.setPadding(16, 16, 16, 16);
        headerView.setTextSize(20);
        headerView.setTextColor(Color.BLACK);
        
        String text = "Header Title";
        if (element.getProperties() != null && element.getProperty("text") != null) {
            text = element.getProperty("text").toString();
        }
        headerView.setText(text);
        
        addView(headerView);
        contentView = headerView;
    }

    /**
     * Updates the view to display an image element with a placeholder if no image is set.
     * 
     * @param element The template element to display
     */
    private void setupImageElement(TemplateElement element) {
        // Create an ImageView for the image element
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Get the image URL from the element properties
        String imageUrl = null;
        if (element.getProperties() != null && element.getProperty("url") != null) {
            imageUrl = element.getProperty("url").toString();
        }
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load the image using Glide
            Glide.with(getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(createImagePlaceholder())
                    .into(imageView);
        } else {
            // Display a placeholder for the image
            imageView.setImageDrawable(createImagePlaceholder());
            
            // Add a "tap to add image" text overlay
            TextView placeholderText = new TextView(getContext());
            placeholderText.setText("Tap to add image");
            placeholderText.setTextColor(Color.WHITE);
            placeholderText.setGravity(Gravity.CENTER);
            placeholderText.setBackgroundColor(Color.parseColor("#66000000")); // Semi-transparent black
            placeholderText.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            
            addView(imageView);
            addView(placeholderText);
            
            // Set the content view to the image view
            contentView = imageView;
            return;
        }
        
        // Add the ImageView to the layout
        addView(imageView);
        
        // Set the content view to the image view
        contentView = imageView;
    }
    
    /**
     * Creates a placeholder drawable for image elements.
     * 
     * @return A drawable to use as a placeholder
     */
    private GradientDrawable createImagePlaceholder() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor("#E0E0E0"));
        
        // Add a simple pattern to indicate it's an image placeholder
        drawable.setStroke(2, Color.parseColor("#BDBDBD"));
        
        return drawable;
    }

    private void createDividerView() {
        View dividerView = new View(getContext());
        dividerView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                4));
        dividerView.setBackgroundColor(Color.DKGRAY);
        
        FrameLayout container = new FrameLayout(getContext());
        container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        container.addView(dividerView);
        
        // Center divider vertically
        ((FrameLayout.LayoutParams) dividerView.getLayoutParams()).gravity = android.view.Gravity.CENTER;
        
        addView(container);
        contentView = container;
    }

    private void createQuoteView() {
        TextView quoteView = new TextView(getContext());
        quoteView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        quoteView.setPadding(32, 16, 32, 16);
        quoteView.setTypeface(quoteView.getTypeface(), Typeface.ITALIC);
        
        String text = "Sample Quote";
        if (element.getProperties() != null && element.getProperty("text") != null) {
            text = element.getProperty("text").toString();
        }
        quoteView.setText(text);
        
        addView(quoteView);
        contentView = quoteView;
    }

    /**
     * Sets the listener for position changes.
     *
     * @param listener the listener to set
     */
    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.listener = listener;
    }
} 