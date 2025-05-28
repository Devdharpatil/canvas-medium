package com.canvamedium.service;

import com.canvamedium.service.impl.MediaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the MediaService implementation.
 */
@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Path tempDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary directory for testing
        tempDirectory = Files.createTempDirectory("media-test");
        
        // Set the upload path and base URL using reflection
        ReflectionTestUtils.setField(mediaService, "uploadPath", tempDirectory.toString());
        ReflectionTestUtils.setField(mediaService, "baseUrl", "http://localhost:8080");
    }

    @Test
    public void testStoreFile() throws IOException {
        // Create a mock multipart file
        MockMultipartFile mockFile = new MockMultipartFile(
                "test-file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Store the file
        String fileUrl = mediaService.storeFile(mockFile);

        // Verify the URL format
        assertTrue(fileUrl.startsWith("http://localhost:8080/uploads/"));
        assertTrue(fileUrl.endsWith(".jpg"));

        // Extract the filename from the URL
        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

        // Verify the file exists in the temporary directory
        Path storedFilePath = Paths.get(tempDirectory.toString(), filename);
        assertTrue(Files.exists(storedFilePath));

        // Clean up
        Files.deleteIfExists(storedFilePath);
    }

    @Test
    public void testStoreFileWithNullExtension() throws IOException {
        // Create a mock multipart file without extension
        MockMultipartFile mockFile = new MockMultipartFile(
                "test-file",
                "test-image",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Store the file
        String fileUrl = mediaService.storeFile(mockFile);

        // Verify the URL format (should default to .jpg)
        assertTrue(fileUrl.endsWith(".jpg"));

        // Extract the filename from the URL
        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

        // Verify the file exists in the temporary directory
        Path storedFilePath = Paths.get(tempDirectory.toString(), filename);
        assertTrue(Files.exists(storedFilePath));

        // Clean up
        Files.deleteIfExists(storedFilePath);
    }

    @Test
    public void testDeleteFile() throws IOException {
        // Create a test file
        String testFilename = "test-delete.jpg";
        Path testFilePath = Paths.get(tempDirectory.toString(), testFilename);
        Files.createFile(testFilePath);

        // Verify the file exists
        assertTrue(Files.exists(testFilePath));

        // Delete the file
        boolean result = mediaService.deleteFile(testFilename);

        // Verify the result and that the file no longer exists
        assertTrue(result);
        assertFalse(Files.exists(testFilePath));
    }

    @Test
    public void testDeleteNonExistentFile() {
        // Try to delete a non-existent file
        boolean result = mediaService.deleteFile("non-existent-file.jpg");

        // Should return false as the file doesn't exist
        assertFalse(result);
    }
} 