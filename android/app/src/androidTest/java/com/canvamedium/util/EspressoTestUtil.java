package com.canvamedium.util;

import android.view.MotionEvent;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

/**
 * Utility class for Espresso tests.
 */
public class EspressoTestUtil {

    /**
     * Custom ViewAction for drag-and-drop operations.
     */
    public static class DragAction implements ViewAction {
        private final float startX;
        private final float startY;
        private final float endX;
        private final float endY;
        
        /**
         * Constructor for DragAction.
         *
         * @param startX Starting X coordinate (0-1, percentage of view width)
         * @param startY Starting Y coordinate (0-1, percentage of view height)
         * @param endX   Ending X coordinate (0-1, percentage of view width)
         * @param endY   Ending Y coordinate (0-1, percentage of view height)
         */
        public DragAction(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }
        
        @Override
        public Matcher<View> getConstraints() {
            return ViewMatchers.isDisplayed();
        }
        
        @Override
        public String getDescription() {
            return "Drag from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")";
        }
        
        @Override
        public void perform(UiController uiController, View view) {
            // Calculate absolute coordinates
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            
            int startXPixel = location[0] + (int) (view.getWidth() * startX);
            int startYPixel = location[1] + (int) (view.getHeight() * startY);
            int endXPixel = location[0] + (int) (view.getWidth() * endX);
            int endYPixel = location[1] + (int) (view.getHeight() * endY);
            
            // Simulate touch events
            long downTime = System.currentTimeMillis();
            
            // Send ACTION_DOWN event
            MotionEvent downEvent = MotionEvent.obtain(
                    downTime, downTime, MotionEvent.ACTION_DOWN, startXPixel, startYPixel, 0);
            uiController.injectMotionEvent(downEvent);
            downEvent.recycle();
            
            // Small delay to simulate human interaction
            uiController.loopMainThreadForAtLeast(100);
            
            // Send ACTION_MOVE events (multiple to simulate smooth dragging)
            int steps = 10;
            for (int i = 0; i < steps; i++) {
                float progress = (float) i / steps;
                float x = startXPixel + (endXPixel - startXPixel) * progress;
                float y = startYPixel + (endYPixel - startYPixel) * progress;
                
                long moveTime = downTime + 100 + i * 20;
                MotionEvent moveEvent = MotionEvent.obtain(
                        downTime, moveTime, MotionEvent.ACTION_MOVE, x, y, 0);
                uiController.injectMotionEvent(moveEvent);
                moveEvent.recycle();
                
                uiController.loopMainThreadForAtLeast(20);
            }
            
            // Send ACTION_UP event
            long upTime = downTime + 350;
            MotionEvent upEvent = MotionEvent.obtain(
                    downTime, upTime, MotionEvent.ACTION_UP, endXPixel, endYPixel, 0);
            uiController.injectMotionEvent(upEvent);
            upEvent.recycle();
            
            // Wait for the UI to settle
            uiController.loopMainThreadForAtLeast(300);
        }
    }
    
    /**
     * Creates a drag action from one location to another.
     *
     * @param startX Starting X coordinate (0-1, percentage of view width)
     * @param startY Starting Y coordinate (0-1, percentage of view height)
     * @param endX   Ending X coordinate (0-1, percentage of view width)
     * @param endY   Ending Y coordinate (0-1, percentage of view height)
     * @return The ViewAction to perform the drag
     */
    public static ViewAction dragFrom(float startX, float startY, float endX, float endY) {
        return new DragAction(startX, startY, endX, endY);
    }
    
    /**
     * Creates a swipe action from one location to another.
     *
     * @param start Starting location (e.g., GeneralLocation.CENTER)
     * @param end   Ending location (e.g., GeneralLocation.BOTTOM_CENTER)
     * @return The ViewAction to perform the swipe
     */
    public static ViewAction swipeFrom(GeneralLocation start, GeneralLocation end) {
        return new GeneralSwipeAction(Swipe.SLOW, start, end, Press.FINGER);
    }
} 