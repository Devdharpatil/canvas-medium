# CanvaMedium Setup Guide

This guide provides detailed instructions for setting up the CanvaMedium project for development.

## Prerequisites

Before starting, ensure you have the following installed:

1. **JDK 17 or later** - Required for both backend and Android development
   - Download from: https://adoptium.net/
   - Verify installation: `java -version`

2. **PostgreSQL** - Database for the backend
   - Download from: https://www.postgresql.org/download/
   - Create a database named `canvamedium`
   - Default credentials (username: postgres, password: postgres)

3. **Maven** - Build tool for the backend
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

4. **Android Studio** - IDE for Android development
   - Download from: https://developer.android.com/studio
   - Install with Android SDK 34 or later

5. **Git** - Version control
   - Download from: https://git-scm.com/downloads
   - Verify installation: `git --version`

## Backend Setup

### Step 1: Configure PostgreSQL

1. Install PostgreSQL if not already installed
2. Create a database named `canvamedium`:
   ```sql
   CREATE DATABASE canvamedium;
   ```
3. Verify you can connect to the database using pgAdmin or psql

### Step 2: Configure Spring Boot Application

1. Verify the database configuration in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/canvamedium
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```
   Update these values if your PostgreSQL configuration is different.

2. Add Swagger/OpenAPI dependency to `backend/pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.1.0</version>
   </dependency>
   ```

3. Create a Swagger configuration class in `backend/src/main/java/com/canvamedium/config/OpenApiConfig.java`:
   ```java
   package com.canvamedium.config;
   
   import io.swagger.v3.oas.models.OpenAPI;
   import io.swagger.v3.oas.models.info.Info;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   
   @Configuration
   public class OpenApiConfig {
       
       @Bean
       public OpenAPI canvaMediumOpenAPI() {
           return new OpenAPI()
                   .info(new Info()
                           .title("CanvaMedium API")
                           .description("RESTful API for CanvaMedium content platform")
                           .version("1.0.0"));
       }
   }
   ```

### Step 3: Run Backend Application

1. Navigate to the backend directory:
   ```
   cd backend
   ```

2. Build the application:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn spring-boot:run
   ```

4. Verify the backend is running by accessing:
   - API endpoints: http://localhost:8080/api/contents
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

## Android Setup

### Step 1: Configure Android Project

1. Open the `android` directory in Android Studio

2. Verify Retrofit dependencies in `android/app/build.gradle`:
   ```gradle
   implementation 'com.squareup.retrofit2:retrofit:2.9.0'
   implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
   implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
   ```

3. Add OkHttp logging interceptor if not already included:
   ```gradle
   implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
   ```

4. Verify the API base URL in `android/app/src/main/java/com/canvamedium/api/ApiClient.java`:
   ```java
   private static final String BASE_URL = "http://10.0.2.2:8080/";
   ```
   Note: `10.0.2.2` is the special IP that allows Android emulator to access the host's localhost.

### Step 2: Configure Android Emulator

1. Open Android Studio's AVD Manager (Tools > AVD Manager)
2. Create a new virtual device with:
   - Phone hardware profile (e.g., Pixel 6)
   - System image with API level 24 or higher (preferably the latest stable)
   - Default settings for other options

### Step 3: Run Android Application

1. Select the created emulator from the device dropdown
2. Click the "Run" button (green triangle) or press Shift+F10
3. Wait for the emulator to start and the application to install and launch

## Testing the Full Stack

1. Ensure the backend is running
2. Launch the Android application in the emulator
3. The app should display an empty list with a floating action button
4. Click the FAB to create a new content item
5. Fill in the title, description, and content fields
6. Click Save
7. The new content should appear in the list
8. Click on an item to edit it
9. Make changes and save
10. Pull down to refresh the list

## Troubleshooting

### Backend Issues

1. **Database connection errors**:
   - Verify PostgreSQL is running: `pg_isready`
   - Check credentials in application.properties
   - Ensure the database exists: `\l` in psql

2. **Port conflicts**:
   - If port 8080 is already in use, change it in application.properties:
     ```properties
     server.port=8081
     ```
   - Don't forget to update the Android ApiClient.java BASE_URL accordingly

### Android Issues

1. **Network errors**:
   - Verify the backend is running
   - Check the BASE_URL in ApiClient.java
   - For physical devices, use your computer's IP address instead of 10.0.2.2
   - Add Internet permission in AndroidManifest.xml:
     ```xml
     <uses-permission android:name="android.permission.INTERNET" />
     ```

2. **Emulator connectivity**:
   - Ensure the emulator has network connectivity
   - Try restarting the emulator
   - Check firewall settings on your computer

## Next Steps After Setup

Once you have the basic setup working:

1. Implement unit tests for backend services
2. Add validation to the model classes
3. Implement error handling in the Android app
4. Add loading indicators for network operations
5. Implement UI tests for the Android app

# CanvaMedium Android Setup Guide

## Creating an Android Virtual Device (AVD)

1. Open Android Studio
2. Click on "Tools" > "Device Manager" from the menu
3. Click on the "+ Create device" button
4. In the "Virtual Device Configuration" window:
   - Select "Phone" category and choose a phone model (e.g., Pixel 6)
   - Click "Next"
5. In the "System Image" screen:
   - Select the "Recommended" tab
   - Choose Android API Level 33 (Android 13.0) or newer
   - If the system image is not downloaded, click the "Download" link next to it
   - After download is complete, select the image and click "Next"
6. In the "Android Virtual Device (AVD)" screen:
   - Name: Enter "CanvaMedium-Test"
   - Leave other settings at their defaults
   - Click "Finish"

## Configuring the Android Project

1. Open the CanvaMedium project in Android Studio:
   - Choose "File" > "Open" and navigate to the CanvaMedium/android directory
   - Click "OK" to open the project

2. Wait for Gradle sync to complete

3. Verify API base URL:
   - Open `app/src/main/java/com/canvamedium/api/ApiClient.java`
   - Confirm the BASE_URL is set to: `http://10.0.2.2:8080/`
   - This allows the emulator to connect to your computer's localhost (where the backend is running)

4. Verify dependencies in build.gradle:
   - Open `app/build.gradle`
   - Check that all required dependencies are included

## Starting the Backend Server

1. Ensure your PostgreSQL database is running
2. Open a terminal in the project's backend directory
3. Run the Spring Boot application:
   ```
   mvn spring-boot:run
   ```
4. Verify the backend is running by accessing http://localhost:8080/swagger-ui/index.html in your web browser

## Running the Android Application

1. Ensure the backend server is running
2. In Android Studio, select the "CanvaMedium-Test" AVD from the device dropdown in the toolbar
3. Click the Run button (green triangle) or press Shift+F10
4. Wait for the emulator to start and the app to build and install
5. The app should launch automatically on the emulator

## Troubleshooting

### Backend Connection Issues
- Verify the backend is running on port 8080
- Check that the BASE_URL in ApiClient.java is set to `http://10.0.2.2:8080/`
- Ensure your emulator has network connectivity
- Check Android Logcat for network-related errors

### Gradle Build Issues
- Sync the project with Gradle files (File > Sync Project with Gradle Files)
- Make sure all dependencies are correctly specified
- Check that the Android SDK is properly installed and configured

### Emulator Issues
- If the emulator is slow, enable hardware acceleration in the AVD settings
- Ensure you have enough disk space and RAM for running the emulator
- Try restarting Android Studio if the emulator fails to start

## Testing the Full Stack

1. Register a new user:
   - Launch the app
   - Navigate to the registration screen
   - Enter valid credentials and submit

2. Login with the registered user:
   - Enter your username/password
   - Click login
   - Verify that you see the main feed screen

3. Create an article:
   - Click the floating action button (+)
   - Select a template
   - Add content and save
   - Verify the article appears in the feed

4. View article details:
   - Click on an article in the feed
   - Verify the article details display correctly
