package com.canvamedium.service.impl;

import com.canvamedium.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of the MediaService interface for handling media operations.
 */
@Service
public class MediaServiceImpl implements MediaService {

    @Value("${media.upload.path:media-uploads}")
    private String uploadPath;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int THUMBNAIL_HEIGHT = 225;

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        // Create uploads directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Normalize file name and add UUID to avoid conflicts
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // Copy file to the target location
        Path targetLocation = Paths.get(uploadPath).resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Return the URL to access the file
        return baseUrl + "/uploads/" + filename;
    }
    
    @Override
    public String[] storeFileWithThumbnail(MultipartFile file) throws IOException {
        // Store original file
        String fileUrl = storeFile(file);
        
        // Generate thumbnail
        String thumbnailUrl = generateThumbnail(fileUrl);
        
        return new String[]{fileUrl, thumbnailUrl};
    }
    
    @Override
    public String generateThumbnail(String imageUrl) throws IOException {
        // Extract filename from URL
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        Path filePath = Paths.get(uploadPath).resolve(filename);
        
        if (!Files.exists(filePath)) {
            throw new IOException("Original file not found: " + filename);
        }
        
        // Read the original image
        BufferedImage originalImage = ImageIO.read(filePath.toFile());
        
        // Create a thumbnail
        BufferedImage thumbnailImage = createThumbnail(originalImage);
        
        // Save the thumbnail
        String fileExtension = getFileExtension(filename);
        String thumbnailFilename = "thumb_" + UUID.randomUUID().toString() + "." + fileExtension;
        Path thumbnailPath = Paths.get(uploadPath).resolve(thumbnailFilename);
        
        ImageIO.write(thumbnailImage, fileExtension, thumbnailPath.toFile());
        
        // Return the URL to access the thumbnail
        return baseUrl + "/uploads/" + thumbnailFilename;
    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Gets the file extension from a filename.
     *
     * @param filename The filename to extract extension from
     * @return The file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * Creates a thumbnail from an original image.
     * 
     * @param originalImage The original image
     * @return A thumbnail image
     */
    private BufferedImage createThumbnail(BufferedImage originalImage) {
        // Calculate dimensions while maintaining aspect ratio
        double aspectRatio = (double) THUMBNAIL_WIDTH / THUMBNAIL_HEIGHT;
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double originalAspect = (double) originalWidth / originalHeight;
        
        int x = 0, y = 0;
        int width, height;
        
        if (originalAspect > aspectRatio) {
            // Original image is wider, crop the width
            height = originalHeight;
            width = (int) (height * aspectRatio);
            x = (originalWidth - width) / 2;
        } else {
            // Original image is taller, crop the height
            width = originalWidth;
            height = (int) (width / aspectRatio);
            y = (originalHeight - height) / 2;
        }
        
        // Crop to correct aspect ratio
        BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);
        
        // Create the thumbnail
        BufferedImage thumbnailImage = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnailImage.createGraphics();
        g.drawImage(croppedImage, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);
        g.dispose();
        
        return thumbnailImage;
    }
} 