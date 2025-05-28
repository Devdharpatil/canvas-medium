package com.canvamedium.controller;

import com.canvamedium.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling media operations.
 */
@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "Media Upload API")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Uploads a file and returns its URL.
     *
     * @param file The file to upload
     * @return ResponseEntity with the URL
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Uploads a file and returns the URL to access it")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = mediaService.storeFile(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Uploads a file and generates a thumbnail.
     *
     * @param file The file to upload
     * @return ResponseEntity with the URLs for the original file and thumbnail
     */
    @PostMapping("/upload-with-thumbnail")
    @Operation(summary = "Upload a file with thumbnail", 
               description = "Uploads a file, generates a thumbnail, and returns URLs for both")
    public ResponseEntity<Map<String, String>> uploadFileWithThumbnail(@RequestParam("file") MultipartFile file) {
        try {
            String[] urls = mediaService.storeFileWithThumbnail(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", urls[0]);
            response.put("thumbnailUrl", urls[1]);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Generates a thumbnail for an existing image.
     *
     * @param imageUrl The URL of the image to generate a thumbnail for
     * @return ResponseEntity with the thumbnail URL
     */
    @PostMapping("/thumbnail")
    @Operation(summary = "Generate thumbnail", 
               description = "Generates a thumbnail for an existing image and returns the thumbnail URL")
    public ResponseEntity<Map<String, String>> generateThumbnail(@RequestParam("imageUrl") String imageUrl) {
        try {
            String thumbnailUrl = mediaService.generateThumbnail(imageUrl);
            Map<String, String> response = new HashMap<>();
            response.put("thumbnailUrl", thumbnailUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to generate thumbnail: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deletes a file.
     *
     * @param filename The name of the file to delete
     * @return ResponseEntity with the result
     */
    @DeleteMapping("/{filename}")
    @Operation(summary = "Delete a file", description = "Deletes a file by its filename")
    public ResponseEntity<Map<String, Boolean>> deleteFile(@PathVariable String filename) {
        boolean result = mediaService.deleteFile(filename);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", result);
        return ResponseEntity.ok(response);
    }
} 