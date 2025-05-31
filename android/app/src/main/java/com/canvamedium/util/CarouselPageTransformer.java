package com.canvamedium.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Custom PageTransformer for carousel effect in ViewPager2.
 * Simplified implementation for more consistent and predictable peeking behavior.
 */
public class CarouselPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.85f; // Scale for adjacent items
    private static final float MIN_ALPHA = 0.7f;  // Alpha for adjacent items

    @Override
    public void transformPage(@NonNull View page, float position) {
        // position is -1 (left), 0 (center), 1 (right) for immediately adjacent pages.
        // It can be fractional during transitions.

        float absPosition = Math.abs(position);

        if (absPosition >= 1) { // Page is an adjacent peeking page or further off-screen
            // For pages that should be peeking (absPosition is typically slightly > 1 due to item margins and VP2 padding)
            // Let's ensure they are visible but scaled and faded.
            // We rely on ViewPager2's offscreenPageLimit to ensure these are drawn.
            page.setAlpha(MIN_ALPHA);
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
            page.setRotationY(0f); // Remove rotation for simplicity
            page.setTranslationX(0f); // Remove translation for simplicity, let padding/margins handle spacing primarily
        } else { // Page is the current page or transitioning very close to center (0 > absPosition > 1)
            // Interpolate scale and alpha: Full size/opacity at center, MIN_SCALE/MIN_ALPHA at +/-1
            float scale = MIN_SCALE + (1f - MIN_SCALE) * (1f - absPosition);
            float alpha = MIN_ALPHA + (1f - MIN_ALPHA) * (1f - absPosition);

            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(alpha);
            page.setRotationY(0f); // Ensure no rotation
            page.setTranslationX(0f); // Ensure no translation
        }
    }
} 