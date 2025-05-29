# CanvaMedium API Testing Plan

This document outlines a comprehensive plan for testing all API endpoints in the CanvaMedium application.

## Prerequisites

Before beginning testing, ensure that:

1. The backend server is running at `http://localhost:8080`
2. The database is properly configured and accessible
3. You have a tool for testing APIs (Postman, curl, or the Swagger UI at `http://localhost:8080/swagger-ui/index.html`)

## Authentication Testing

### 1. Registration

**Endpoint**: `POST /api/auth/register`

**Test Cases**:

1. **Successful Registration**
   - Request: Valid username, email, password, and fullName
   - Expected Response: 201 Created with user ID and username
   - Test in Android App: Complete registration form with valid data

2. **Username Already Exists**
   - Request: Duplicate username, valid email
   - Expected Response: 409 Conflict with message "Username is already taken"
   - Test in Android App: Try to register with an existing username

3. **Email Already Exists**
   - Request: Valid username, duplicate email
   - Expected Response: 409 Conflict with message "Email is already in use"
   - Test in Android App: Try to register with an existing email

4. **Invalid Data Format**
   - Request: Missing required fields or invalid formats
   - Expected Response: 400 Bad Request with validation errors
   - Test in Android App: Submit form with invalid data

### 2. Login

**Endpoint**: `POST /api/auth/login`

**Test Cases**:

1. **Successful Login**
   - Request: Valid username/email and password
   - Expected Response: 200 OK with token, refreshToken, and user details
   - Test in Android App: Login with valid credentials

2. **Invalid Credentials**
   - Request: Invalid username or password
   - Expected Response: 401 Unauthorized with appropriate message
   - Test in Android App: Try to login with incorrect password

3. **Account Disabled**
   - Request: Credentials for a disabled account
   - Expected Response: 401 Unauthorized with message about account status
   - Test in Android App: Try to login to a disabled account

### 3. Token Refresh

**Endpoint**: `POST /api/auth/refresh`

**Test Cases**:

1. **Valid Refresh Token**
   - Request: Valid refresh token
   - Expected Response: 200 OK with new access token and refresh token
   - Test in Android App: Automatic refresh when token expires

2. **Invalid Refresh Token**
   - Request: Invalid or expired refresh token
   - Expected Response: 401 Unauthorized
   - Test in Android App: Try to use app after manual token invalidation

## User Management Testing

### 1. Get User Profile

**Endpoint**: `GET /api/users/profile`

**Test Cases**:

1. **Authenticated User**
   - Request: Valid token in Authorization header
   - Expected Response: 200 OK with user profile details
   - Test in Android App: Navigate to profile screen after login

2. **Unauthenticated User**
   - Request: No token or invalid token
   - Expected Response: 401 Unauthorized
   - Test in Android App: Try to access profile after logout

### 2. Update User Profile

**Endpoint**: `PUT /api/users/profile`

**Test Cases**:

1. **Valid Update**
   - Request: Valid token and valid update data
   - Expected Response: 200 OK with updated profile
   - Test in Android App: Change profile details and save

2. **Invalid Data**
   - Request: Valid token but invalid data
   - Expected Response: 400 Bad Request with validation errors
   - Test in Android App: Try to save invalid profile data

## Template Management Testing

### 1. Get All Templates

**Endpoint**: `GET /api/templates`

**Test Cases**:

1. **No Filters**
   - Request: No query parameters
   - Expected Response: 200 OK with paginated list of templates
   - Test in Android App: Browse templates

2. **With Pagination**
   - Request: page=0&size=10
   - Expected Response: 200 OK with first 10 templates
   - Test in Android App: Scroll through template list

3. **With Sorting**
   - Request: sort=createdAt,desc
   - Expected Response: 200 OK with templates sorted by creation date (newest first)
   - Test in Android App: Sort templates by different criteria

### 2. Get Template by ID

**Endpoint**: `GET /api/templates/{id}`

**Test Cases**:

1. **Existing Template**
   - Request: Valid template ID
   - Expected Response: 200 OK with template details
   - Test in Android App: View template details

2. **Non-existent Template**
   - Request: Invalid template ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to view a deleted template

### 3. Create Template

**Endpoint**: `POST /api/templates`

**Test Cases**:

1. **Valid Template**
   - Request: Valid template data
   - Expected Response: 201 Created with new template details
   - Test in Android App: Create a new template

2. **Invalid Template Data**
   - Request: Missing required fields
   - Expected Response: 400 Bad Request with validation errors
   - Test in Android App: Try to save incomplete template

### 4. Update Template

**Endpoint**: `PUT /api/templates/{id}`

**Test Cases**:

1. **Valid Update**
   - Request: Valid template ID and update data
   - Expected Response: 200 OK with updated template
   - Test in Android App: Edit an existing template

2. **Non-existent Template**
   - Request: Invalid template ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to update a deleted template

### 5. Delete Template

**Endpoint**: `DELETE /api/templates/{id}`

**Test Cases**:

1. **Existing Template**
   - Request: Valid template ID
   - Expected Response: 204 No Content
   - Test in Android App: Delete a template

2. **Non-existent Template**
   - Request: Invalid template ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to delete an already deleted template

## Article Management Testing

### 1. Get All Articles

**Endpoint**: `GET /api/articles`

**Test Cases**:

1. **No Filters**
   - Request: No query parameters
   - Expected Response: 200 OK with paginated list of articles
   - Test in Android App: Browse articles feed

2. **With Status Filter**
   - Request: status=PUBLISHED
   - Expected Response: 200 OK with only published articles
   - Test in Android App: Filter by article status

3. **With Category Filter**
   - Request: categoryId=1
   - Expected Response: 200 OK with articles in the specified category
   - Test in Android App: Browse by category

### 2. Get Article by ID

**Endpoint**: `GET /api/articles/{id}`

**Test Cases**:

1. **Existing Article**
   - Request: Valid article ID
   - Expected Response: 200 OK with article details
   - Test in Android App: View article details

2. **Non-existent Article**
   - Request: Invalid article ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to view a deleted article

### 3. Create Article

**Endpoint**: `POST /api/articles`

**Test Cases**:

1. **Valid Article**
   - Request: Valid article data
   - Expected Response: 201 Created with new article details
   - Test in Android App: Create a new article

2. **Invalid Article Data**
   - Request: Missing required fields
   - Expected Response: 400 Bad Request with validation errors
   - Test in Android App: Try to save incomplete article

### 4. Update Article

**Endpoint**: `PUT /api/articles/{id}`

**Test Cases**:

1. **Valid Update**
   - Request: Valid article ID and update data
   - Expected Response: 200 OK with updated article
   - Test in Android App: Edit an existing article

2. **Non-existent Article**
   - Request: Invalid article ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to update a deleted article

### 5. Delete Article

**Endpoint**: `DELETE /api/articles/{id}`

**Test Cases**:

1. **Existing Article**
   - Request: Valid article ID
   - Expected Response: 204 No Content
   - Test in Android App: Delete an article

2. **Non-existent Article**
   - Request: Invalid article ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to delete an already deleted article

### 6. Publish Article

**Endpoint**: `PUT /api/articles/{id}/publish`

**Test Cases**:

1. **Draft Article**
   - Request: Valid article ID of a draft article
   - Expected Response: 200 OK with updated article (status: PUBLISHED)
   - Test in Android App: Publish a draft article

2. **Already Published Article**
   - Request: Valid article ID of a published article
   - Expected Response: 400 Bad Request or appropriate error
   - Test in Android App: Try to publish an already published article

### 7. Archive Article

**Endpoint**: `PUT /api/articles/{id}/archive`

**Test Cases**:

1. **Published Article**
   - Request: Valid article ID of a published article
   - Expected Response: 200 OK with updated article (status: ARCHIVED)
   - Test in Android App: Archive a published article

2. **Already Archived Article**
   - Request: Valid article ID of an archived article
   - Expected Response: 400 Bad Request or appropriate error
   - Test in Android App: Try to archive an already archived article

## Category Management Testing

### 1. Get All Categories

**Endpoint**: `GET /api/categories`

**Test Cases**:

1. **No Filters**
   - Request: No query parameters
   - Expected Response: 200 OK with list of all categories
   - Test in Android App: Browse categories

2. **With Featured Filter**
   - Request: featured=true
   - Expected Response: 200 OK with only featured categories
   - Test in Android App: View featured categories

### 2. Get Category by ID

**Endpoint**: `GET /api/categories/{id}`

**Test Cases**:

1. **Existing Category**
   - Request: Valid category ID
   - Expected Response: 200 OK with category details
   - Test in Android App: View category details

2. **Non-existent Category**
   - Request: Invalid category ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to view a deleted category

## Tag Management Testing

### 1. Get All Tags

**Endpoint**: `GET /api/tags`

**Test Cases**:

1. **No Filters**
   - Request: No query parameters
   - Expected Response: 200 OK with list of all tags
   - Test in Android App: Browse tags

### 2. Get Articles by Tag

**Endpoint**: `GET /api/articles/tag/{tagId}`

**Test Cases**:

1. **Existing Tag**
   - Request: Valid tag ID
   - Expected Response: 200 OK with articles having the specified tag
   - Test in Android App: View articles by tag

2. **Non-existent Tag**
   - Request: Invalid tag ID
   - Expected Response: 404 Not Found or empty list
   - Test in Android App: Try to view articles for a non-existent tag

## Media Management Testing

### 1. Upload Media

**Endpoint**: `POST /api/media/upload`

**Test Cases**:

1. **Valid Image Upload**
   - Request: Valid image file (JPEG, PNG)
   - Expected Response: 200 OK with media URL
   - Test in Android App: Upload an image in template or article editor

2. **Invalid File Type**
   - Request: Unsupported file type
   - Expected Response: 400 Bad Request with error message
   - Test in Android App: Try to upload an unsupported file

3. **File Too Large**
   - Request: File exceeding size limit
   - Expected Response: 413 Payload Too Large
   - Test in Android App: Try to upload a very large image

### 2. Get Media by ID

**Endpoint**: `GET /api/media/{id}`

**Test Cases**:

1. **Existing Media**
   - Request: Valid media ID
   - Expected Response: 200 OK with media file
   - Test in Android App: Display an image

2. **Non-existent Media**
   - Request: Invalid media ID
   - Expected Response: 404 Not Found
   - Test in Android App: Try to display a deleted image

## Running Tests in Postman or Using curl

Examples of API test requests using curl:

### Registration Test
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

### Login Test
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Get Articles Test
```bash
curl -X GET http://localhost:8080/api/articles \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Testing in Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui/index.html`
2. Authenticate via the login endpoint
3. Copy the token from the response
4. Click the "Authorize" button at the top of the page
5. Enter the token in the format `Bearer YOUR_TOKEN_HERE`
6. Test other endpoints with authentication automatically applied

## Conclusion

This testing plan provides a comprehensive approach to verify the functionality of all API endpoints in the CanvaMedium application. By following these test cases, you can ensure that the backend is working correctly and properly integrated with the Android application.

After completing these tests, update the TASK.md file to mark the "Test Backend API Endpoints" task as completed in the Full Stack Testing phase. 