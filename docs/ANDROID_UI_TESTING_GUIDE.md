# CanvaMedium Android UI Testing Guide

This guide provides detailed instructions for testing the Android UI flows of the CanvaMedium application. Follow each section methodically to ensure all user interfaces and interactions work as expected.

## Prerequisites

Before starting UI testing, ensure:

1. The backend server is running at `http://localhost:8080`
2. The Android emulator is properly set up and running
3. The APK is built and installed on the emulator
4. The PostgreSQL database is accessible and populated with test data

## Test Environment Setup

1. Use an Android emulator with API level 24 or higher (preferably a recent version)
2. Install the latest debug build of the CanvaMedium app
3. Reset app data before starting tests to ensure a clean state

## 1. Authentication Flow Testing

### 1.1 User Registration

1. **Launch the app**
   - Verify the splash screen displays correctly
   - Verify navigation to login screen

2. **Navigate to Registration**
   - Tap "Create Account" or "Register" button
   - Verify registration form appears with:
     - Username field
     - Email field
     - Password field
     - Confirm password field
     - Full name field
     - Registration button

3. **Form Validation Tests**
   - **Empty fields test**:
     - Leave all fields empty and tap Register
     - Verify error messages appear for required fields
   
   - **Invalid email test**:
     - Enter invalid email (e.g., "user@incomplete")
     - Verify email validation error appears
   
   - **Password mismatch test**:
     - Enter different passwords in Password and Confirm Password fields
     - Verify password mismatch error appears
   
   - **Short password test**:
     - Enter password shorter than minimum required length
     - Verify password length error appears

4. **Successful Registration**
   - Fill all fields with valid data:
     - Username: "uitestuser"
     - Email: "uitest@example.com"
     - Password: "password123"
     - Confirm Password: "password123"
     - Full Name: "UI Test User"
   - Tap Register button
   - Verify success message appears
   - Verify automatic navigation to login screen

### 1.2 User Login

1. **Navigate to Login Screen**
   - If not already there, navigate to login screen
   - Verify login form appears with:
     - Username/Email field
     - Password field
     - Login button
     - "Forgot password" option (if implemented)
     - "Register" option

2. **Form Validation Tests**
   - **Empty fields test**:
     - Leave fields empty and tap Login
     - Verify error messages appear
   
   - **Invalid credentials test**:
     - Enter invalid username and password
     - Verify error message appears

3. **Successful Login**
   - Enter credentials for registered user:
     - Username: "uitestuser"
     - Password: "password123"
   - Tap Login button
   - Verify successful login (navigation to main feed)
   - Verify user session persistence (close and reopen app)

### 1.3 Logout

1. **Perform Logout**
   - Navigate to user profile or settings
   - Tap Logout button
   - Verify confirmation dialog appears
   - Confirm logout
   - Verify return to login screen
   - Verify session termination (auth-protected screens inaccessible)

## 2. Feed View Testing

### 2.1 Article List Display

1. **Article Feed Loading**
   - Login to the app
   - Verify articles load in the feed
   - Verify article cards display:
     - Title
     - Preview text
     - Featured image (if available)
     - Publication date

2. **Empty State Handling**
   - Clear cache or use a test account with no articles
   - Verify appropriate empty state message displays

3. **Pull-to-Refresh**
   - Pull down on the article list
   - Verify refresh indicator appears
   - Verify list updates with fresh data

4. **Pagination**
   - Scroll down to the bottom of the list
   - Verify more articles load automatically
   - Verify loading indicator appears during pagination

### 2.2 Article Filtering and Sorting

1. **Category Filtering**
   - Tap category filter option
   - Select a specific category
   - Verify only articles from selected category appear

2. **Search Functionality**
   - Tap search icon
   - Enter search query
   - Verify search results display correctly
   - Verify empty search results handled appropriately

3. **Sort Options**
   - Tap sort option (if available)
   - Try different sort orders:
     - Newest first
     - Oldest first
     - Most popular (if implemented)
   - Verify articles display in correct order

### 2.3 Bookmarking

1. **Bookmark Article**
   - Tap bookmark icon on an article
   - Verify visual feedback (icon change)
   - Navigate away and return to feed
   - Verify bookmark status persists

2. **View Bookmarks**
   - Navigate to bookmarked articles section
   - Verify all bookmarked articles appear
   - Verify removing bookmark updates list

## 3. Article Detail Testing

### 3.1 Article View

1. **Open Article**
   - Tap an article in the feed
   - Verify smooth transition animation
   - Verify article content loads correctly:
     - Title
     - Author information
     - Publication date
     - Content (text, images, formatting)

2. **Interactions**
   - Test bookmark toggle
   - Test share functionality (if implemented)
   - Test text selection (if supported)

3. **Navigation**
   - Test back button/gesture
   - Verify return to correct position in feed

### 3.2 Rich Content

1. **Image Handling**
   - Verify images load properly
   - Test image tapping behavior (if implemented)
   - Verify image placeholders during loading

2. **Formatting**
   - Verify text formatting is preserved:
     - Bold text
     - Italic text
     - Headings
     - Lists
   - Verify links are clickable

## 4. Template Builder Testing

### 4.1 Template Creation

1. **Launch Template Builder**
   - Navigate to template creation screen
   - Verify empty canvas appears
   - Verify template element toolbar displays

2. **Element Addition**
   - Add text element:
     - Tap "Add Text" button
     - Verify text element appears on canvas
     - Verify text editing functionality
   
   - Add image element:
     - Tap "Add Image" button
     - Select image from gallery or camera
     - Verify image appears on canvas

3. **Drag and Drop**
   - Test dragging elements:
     - Touch and hold element
     - Drag to new position
     - Verify element follows finger/pointer
     - Release and verify element stays in new position
   
   - Test element resizing:
     - Select element
     - Drag resize handles
     - Verify element resizes correctly

4. **Template Saving**
   - Enter template name
   - Tap save button
   - Verify success message
   - Navigate to template list and verify new template appears

### 4.2 Template Selection

1. **Browse Templates**
   - Navigate to template selection screen
   - Verify templates display correctly with thumbnails
   - Verify scrolling works properly

2. **Template Preview**
   - Tap a template
   - Verify template preview displays
   - Verify template details show correctly

3. **Template Selection**
   - Select a template for article creation
   - Verify navigation to article editor
   - Verify template is applied to new article

## 5. Article Creation Testing

### 5.1 Article Editor

1. **Create New Article**
   - Navigate to article creation
   - Select template
   - Verify editor loads with selected template structure

2. **Content Editing**
   - Edit text elements:
     - Tap text areas
     - Enter/modify text
     - Apply formatting (bold, italic, etc.)
   
   - Edit image elements:
     - Replace default images
     - Resize/reposition images
     - Apply filters (if implemented)

3. **Save Draft**
   - Tap "Save Draft" button
   - Verify success message
   - Navigate away and return
   - Verify draft content is preserved

### 5.2 Article Publishing

1. **Preview Article**
   - Tap "Preview" button
   - Verify article preview displays
   - Verify all content and formatting appears correctly

2. **Publish Flow**
   - Tap "Publish" button
   - Verify confirmation dialog
   - Confirm publishing
   - Verify success message
   - Navigate to feed and verify article appears

## 6. Category and Tag Management

### 6.1 Category Browsing

1. **View Categories**
   - Navigate to categories section
   - Verify category list displays
   - Verify category details (name, description, icon)

2. **Category Selection**
   - Select a category
   - Verify articles from that category display

### 6.2 Tag Management

1. **View Tags**
   - Navigate to tags section (if separate from categories)
   - Verify tag list displays

2. **Tag Selection**
   - Select a tag
   - Verify articles with that tag display

3. **Tag Addition**
   - If article creation allows adding tags:
     - Try adding existing tags
     - Try creating new tags
     - Verify tags appear in article metadata

## 7. Offline Functionality

### 7.1 Content Caching

1. **Cache Testing**
   - Browse articles while online
   - Switch device to airplane mode
   - Verify previously viewed articles are accessible
   - Verify appropriate offline indicators appear

2. **Offline Changes**
   - While offline, make changes (bookmark articles, create drafts)
   - Verify changes persist locally
   - Reconnect to network
   - Verify changes sync to server

### 7.2 Error Recovery

1. **Network Interruption**
   - During a network operation, disconnect network
   - Verify appropriate error message appears
   - Verify retry mechanism works
   - Reconnect and verify operation completes

## 8. Performance Testing

### 8.1 Load Times

1. **Initial Load**
   - Time app startup to first interactive screen
   - Verify it's under acceptable threshold (< 3 seconds)

2. **Feed Loading**
   - Time article feed loading
   - Verify it's under acceptable threshold (< 2 seconds)

3. **Article Opening**
   - Time from tap to article fully displayed
   - Verify it's under acceptable threshold (< 1 second)

### 8.2 Scrolling Performance

1. **Smooth Scrolling**
   - Rapidly scroll through article feed
   - Verify no visual stutters or frame drops
   - Verify images load appropriately during scroll

## 9. Edge Cases

### 9.1 Low Memory Conditions

1. **Memory Pressure**
   - Open multiple memory-intensive apps before CanvaMedium
   - Use the app and verify it handles memory pressure gracefully
   - Verify no crashes or data loss occurs

### 9.2 Orientation Changes

1. **Rotation Support**
   - Rotate device during various operations:
     - While viewing article list
     - While reading an article
     - While editing a template
     - While creating an article
   - Verify UI adapts correctly
   - Verify no data loss occurs

## Reporting Issues

When reporting UI testing issues:

1. **Issue Description**: Provide a clear, concise description of the issue
2. **Steps to Reproduce**: List exact steps to reproduce the issue
3. **Expected Behavior**: Describe what should happen
4. **Actual Behavior**: Describe what actually happens
5. **Screenshots/Videos**: Include visual evidence when possible
6. **Device Information**: Include device model, Android version, and app version
7. **Severity Rating**: Categorize as Critical, Major, Minor, or Cosmetic

## Success Criteria

The Android UI testing is considered successful when:

1. All user flows operate without errors
2. UI elements display correctly on different screen sizes
3. Interactions (taps, swipes, etc.) work as expected
4. Response times meet performance thresholds
5. Offline functionality works reliably
6. The app recovers gracefully from error conditions
7. All edge cases are handled appropriately

## Next Steps

After completing Android UI testing:
1. Update TASK.md to mark "Test Android UI Flows" as completed
2. Document any issues found in a separate file
3. Proceed to testing integration points between frontend and backend 