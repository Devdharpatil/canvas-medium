package com.canvamedium.view;

import android.content.Context;
import android.graphics.Color;
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

import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.canvamedium.model.TemplateElement;
import com.google.android.material.card.MaterialCardView;

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
        
        // Handle touch events for drag-and-drop
        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();
                    dX = getX() - event.getRawX();
                    dY = getY() - event.getRawY();
                    setSelected(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;
                    setX(Math.max(0, Math.min(newX, parent.getWidth() - getWidth())));
                    setY(Math.max(0, Math.min(newY, parent.getHeight() - getHeight())));
                    dragging = true;
                    break;
                case MotionEvent.ACTION_UP:
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
            setCardElevation(8f);
            GradientDrawable border = new GradientDrawable();
            border.setStroke(4, Color.BLUE);
            ViewCompat.setBackground(this, border);
        } else {
            setCardElevation(4f);
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
        
        setX(element.getX());
        setY(element.getY());
        
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
        switch (element.getType()) {
            case "TEXT":
                createTextView();
                break;
            case "IMAGE":
                createImageView();
                break;
            case "HEADER":
                createHeaderView();
                break;
            case "DIVIDER":
                createDividerView();
                break;
            case "QUOTE":
                createQuoteView();
                break;
        }
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

    private void createImageView() {
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Check if we have an image URI
        if (element.getProperties() != null && element.getProperty("imageUri") != null) {
            String imageUriStr = element.getProperty("imageUri").toString();
            try {
                Uri imageUri = Uri.parse(imageUriStr);
                Glide.with(getContext())
                        .load(imageUri)
                        .centerCrop()
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setBackgroundColor(Color.LTGRAY);
            }
        } else {
            imageView.setBackgroundColor(Color.LTGRAY);
            
            // Add a placeholder text if this is a placeholder
            if (element.getProperties() != null && 
                    element.getProperty("placeholder") != null && 
                    (boolean) element.getProperty("placeholder")) {
                TextView placeholderText = new TextView(getContext());
                placeholderText.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.CENTER));
                placeholderText.setText("Tap to select image");
                placeholderText.setTextColor(Color.WHITE);
                addView(placeholderText);
            }
        }
        
        addView(imageView);
        contentView = imageView;
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