package com.canvamedium.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom view for displaying and interacting with a crop overlay.
 * Users can move and resize the crop rectangle.
 */
public class CropOverlayView extends View {

    private static final int CORNER_SIZE = 40;
    private static final int HANDLE_SIZE = 20;
    private static final int BORDER_SIZE = 3;
    
    private RectF cropRect;
    private Paint borderPaint;
    private Paint cornerPaint;
    private Paint overlayPaint;
    
    private int touchMargin = 40;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private int resizeCorner = 0; // 0: top-left, 1: top-right, 2: bottom-left, 3: bottom-right
    private float lastTouchX, lastTouchY;
    private float minWidth = 50;
    private float minHeight = 50;
    
    public CropOverlayView(Context context) {
        super(context);
        init();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(BORDER_SIZE);
        borderPaint.setStyle(Paint.Style.STROKE);
        
        cornerPaint = new Paint();
        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);
        
        overlayPaint = new Paint();
        overlayPaint.setColor(Color.parseColor("#80000000"));
        overlayPaint.setStyle(Paint.Style.FILL);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Initialize crop rectangle to be centered with 80% of view dimensions
        float cropWidth = w * 0.8f;
        float cropHeight = h * 0.8f;
        float left = (w - cropWidth) / 2;
        float top = (h - cropHeight) / 2;
        
        cropRect = new RectF(left, top, left + cropWidth, top + cropHeight);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (cropRect == null) return;
        
        // Draw semi-transparent overlay outside crop area
        canvas.drawRect(0, 0, getWidth(), cropRect.top, overlayPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, overlayPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, overlayPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), overlayPaint);
        
        // Draw border around crop area
        canvas.drawRect(cropRect, borderPaint);
        
        // Draw corner handles
        drawCornerHandles(canvas);
    }
    
    private void drawCornerHandles(Canvas canvas) {
        // Top-left
        canvas.drawRect(
                cropRect.left - HANDLE_SIZE,
                cropRect.top - HANDLE_SIZE,
                cropRect.left + HANDLE_SIZE,
                cropRect.top + HANDLE_SIZE,
                cornerPaint);
        
        // Top-right
        canvas.drawRect(
                cropRect.right - HANDLE_SIZE,
                cropRect.top - HANDLE_SIZE,
                cropRect.right + HANDLE_SIZE,
                cropRect.top + HANDLE_SIZE,
                cornerPaint);
        
        // Bottom-left
        canvas.drawRect(
                cropRect.left - HANDLE_SIZE,
                cropRect.bottom - HANDLE_SIZE,
                cropRect.left + HANDLE_SIZE,
                cropRect.bottom + HANDLE_SIZE,
                cornerPaint);
        
        // Bottom-right
        canvas.drawRect(
                cropRect.right - HANDLE_SIZE,
                cropRect.bottom - HANDLE_SIZE,
                cropRect.right + HANDLE_SIZE,
                cropRect.bottom + HANDLE_SIZE,
                cornerPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if touch is on a corner handle
                if (isInCorner(x, y, cropRect.left, cropRect.top)) {
                    isResizing = true;
                    resizeCorner = 0;
                } else if (isInCorner(x, y, cropRect.right, cropRect.top)) {
                    isResizing = true;
                    resizeCorner = 1;
                } else if (isInCorner(x, y, cropRect.left, cropRect.bottom)) {
                    isResizing = true;
                    resizeCorner = 2;
                } else if (isInCorner(x, y, cropRect.right, cropRect.bottom)) {
                    isResizing = true;
                    resizeCorner = 3;
                } else if (cropRect.contains(x, y)) {
                    isDragging = true;
                }
                
                lastTouchX = x;
                lastTouchY = y;
                return true;
                
            case MotionEvent.ACTION_MOVE:
                if (isResizing) {
                    // Resize from the appropriate corner
                    switch (resizeCorner) {
                        case 0: // Top-left
                            cropRect.left = Math.min(cropRect.right - minWidth, x);
                            cropRect.top = Math.min(cropRect.bottom - minHeight, y);
                            break;
                        case 1: // Top-right
                            cropRect.right = Math.max(cropRect.left + minWidth, x);
                            cropRect.top = Math.min(cropRect.bottom - minHeight, y);
                            break;
                        case 2: // Bottom-left
                            cropRect.left = Math.min(cropRect.right - minWidth, x);
                            cropRect.bottom = Math.max(cropRect.top + minHeight, y);
                            break;
                        case 3: // Bottom-right
                            cropRect.right = Math.max(cropRect.left + minWidth, x);
                            cropRect.bottom = Math.max(cropRect.top + minHeight, y);
                            break;
                    }
                    
                    // Ensure crop rect stays within the view bounds
                    constrainCropRectToImage();
                    invalidate();
                } else if (isDragging) {
                    // Move the entire crop rectangle
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;
                    
                    cropRect.left += deltaX;
                    cropRect.top += deltaY;
                    cropRect.right += deltaX;
                    cropRect.bottom += deltaY;
                    
                    // Ensure crop rect stays within the view bounds
                    constrainCropRectToImage();
                    
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                }
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isResizing = false;
                isDragging = false;
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    private boolean isInCorner(float x, float y, float cornerX, float cornerY) {
        float dx = Math.abs(x - cornerX);
        float dy = Math.abs(y - cornerY);
        return dx <= CORNER_SIZE && dy <= CORNER_SIZE;
    }
    
    private void constrainCropRectToImage() {
        // Constrain to the view boundaries
        if (cropRect.left < 0) {
            float width = cropRect.width();
            cropRect.left = 0;
            cropRect.right = width;
        }
        
        if (cropRect.top < 0) {
            float height = cropRect.height();
            cropRect.top = 0;
            cropRect.bottom = height;
        }
        
        if (cropRect.right > getWidth()) {
            float width = cropRect.width();
            cropRect.right = getWidth();
            cropRect.left = cropRect.right - width;
        }
        
        if (cropRect.bottom > getHeight()) {
            float height = cropRect.height();
            cropRect.bottom = getHeight();
            cropRect.top = cropRect.bottom - height;
        }
    }
    
    /**
     * Gets the crop rectangle in normalized coordinates (0.0-1.0).
     *
     * @return Array with [left, top, right, bottom] in normalized coordinates
     */
    public float[] getCropRect() {
        if (cropRect == null) return new float[]{0f, 0f, 1f, 1f};
        
        float[] rect = new float[4];
        rect[0] = cropRect.left / getWidth();
        rect[1] = cropRect.top / getHeight();
        rect[2] = cropRect.right / getWidth();
        rect[3] = cropRect.bottom / getHeight();
        
        return rect;
    }
} 