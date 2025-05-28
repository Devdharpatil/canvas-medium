package com.canvamedium.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for handling media operations.
 */
public interface MediaService {
    
    /**
     * Stores an uploaded file and returns its URL.
     *
     * @param file The file to store
     * @return The URL to access the stored file
     * @throws IOException If an I/O error occurs
     */
    String storeFile(MultipartFile file) throws IOException;
    
    /**
     * Stores an uploaded file and generates a thumbnail, returning both URLs.
     * 
     * @param file The file to store
     * @return Array with [fileUrl, thumbnailUrl]
     * @throws IOException If an I/O error occurs
     */
    String[] storeFileWithThumbnail(MultipartFile file) throws IOException;
    
    /**
     * Generates a thumbnail for an existing image file.
     * 
     * @param imageUrl The URL of the existing image
     * @return The URL of the generated thumbnail
     * @throws IOException If an I/O error occurs
     */
    String generateThumbnail(String imageUrl) throws IOException;
    
    /**
     * Deletes a file by its filename.
     *
     * @param filename The name of the file to delete
     * @return true if successful, false otherwise
     */
    boolean deleteFile(String filename);
} 