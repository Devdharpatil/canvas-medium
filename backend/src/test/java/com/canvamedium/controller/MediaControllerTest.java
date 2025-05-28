package com.canvamedium.controller;

import com.canvamedium.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for MediaController.
 */
@WebMvcTest(MediaController.class)
public class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaService mediaService;

    @Test
    public void testUploadFile() throws Exception {
        // Create a mock multipart file
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Mock the service response
        when(mediaService.storeFile(any())).thenReturn("http://localhost:8080/uploads/123e4567-e89b-12d3-a456-426614174000.jpg");

        // Perform the request and validate the response
        mockMvc.perform(multipart("/api/media/upload")
                .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:8080/uploads/123e4567-e89b-12d3-a456-426614174000.jpg"));
    }

    @Test
    public void testUploadFileFailure() throws Exception {
        // Create a mock multipart file
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Mock the service to throw an exception
        when(mediaService.storeFile(any())).thenThrow(new IOException("Test IO exception"));

        // Perform the request and validate the response
        mockMvc.perform(multipart("/api/media/upload")
                .file(mockFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testDeleteFile() throws Exception {
        // Mock the service response
        when(mediaService.deleteFile("test-file.jpg")).thenReturn(true);

        // Perform the request and validate the response
        mockMvc.perform(delete("/api/media/test-file.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    public void testDeleteFileNonExistent() throws Exception {
        // Mock the service response for non-existent file
        when(mediaService.deleteFile("non-existent.jpg")).thenReturn(false);

        // Perform the request and validate the response
        mockMvc.perform(delete("/api/media/non-existent.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(false));
    }
} 