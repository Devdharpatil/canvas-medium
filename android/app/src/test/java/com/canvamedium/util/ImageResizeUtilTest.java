package com.canvamedium.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ImageResizeUtil class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageResizeUtilTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private Bitmap mockBitmap;
    
    @Mock
    private Uri mockUri;
    
    @Mock
    private File mockFile;
    
    @Before
    public void setUp() {
        // Set up mock bitmap dimensions
        when(mockBitmap.getWidth()).thenReturn(2000);
        when(mockBitmap.getHeight()).thenReturn(1500);
    }
    
    @Test
    public void testResizeBitmap_NullBitmap_ReturnsNull() {
        assertNull(ImageResizeUtil.resizeBitmap(null));
    }
    
    @Test
    public void testResizeBitmap_WithDimensions_CallsCreateScaledBitmap() {
        // This test is limited due to the static method createScaledBitmap
        // We're mainly verifying the method doesn't crash
        assertNotNull(mockBitmap);
        // Should not throw an exception
        try {
            ImageResizeUtil.resizeBitmap(mockBitmap, 100, 100);
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
    
    @Test
    public void testCreateThumbnail_NullBitmap_ReturnsNull() {
        assertNull(ImageResizeUtil.createThumbnail(null));
    }
    
    @Test
    public void testCropBitmap_NullBitmap_ReturnsNull() {
        assertNull(ImageResizeUtil.cropBitmap(null, 0, 0, 100, 100));
    }
    
    @Test
    public void testCropBitmap_OutOfBoundsCrop_AdjustsBounds() {
        // Since we can't test the actual bitmap creation, we verify the method doesn't crash
        // with invalid bounds
        try {
            ImageResizeUtil.cropBitmap(mockBitmap, -10, -10, 3000, 3000);
        } catch (Exception e) {
            fail("Exception should not be thrown when adjusting bounds: " + e.getMessage());
        }
    }
    
    @Test
    public void testCreateCenterCropThumbnail_NullBitmap_ReturnsNull() {
        assertNull(ImageResizeUtil.createCenterCropThumbnail(null));
    }
    
    @Test
    public void testSaveBitmapToFile_NullBitmap_ReturnsNull() {
        assertNull(ImageResizeUtil.saveBitmapToFile(mockContext, null, 85));
    }
    
    @Test
    public void testSaveBitmapToFile_NullContext_ReturnsNull() {
        assertNull(ImageResizeUtil.saveBitmapToFile(null, mockBitmap, 85));
    }
    
    @Test
    public void testRotateBitmap_NullBitmap_ReturnsNull() {
        assertNull(ImageResizeUtil.rotateBitmap(null, 90));
    }
} 