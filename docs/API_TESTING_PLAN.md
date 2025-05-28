# CanvaMedium API Testing Plan

This document outlines a comprehensive testing plan for the CanvaMedium REST API endpoints. Follow these instructions to verify that all endpoints are working correctly and resolve any issues that arise.

## Prerequisites

- Backend server is running locally on port 8080
- PostgreSQL database is configured and running
- Postman or similar API testing tool is installed
- Network connectivity to the server

## API Base URL

- Local development: `http://localhost:8080`
- Android emulator: `http://10.0.2.2:8080`

## Authentication Endpoints

### 1. User Registration

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "fullName": "Test User"
}
```

**Expected Success Response**:
- Status: 201 Created
- Body:
```json
{
  "status": 201,
  "message": "User registered successfully",
  "timestamp": "2023-05-29T10:15:30.123456",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

**Test Cases**:
1. Register with valid data
2. Register with existing username (should return 409 Conflict)
3. Register with existing email (should return 409 Conflict)
4. Register with invalid email format (should return 400 Bad Request)
5. Register with short password (should return 400 Bad Request)

### 2. User Login

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Success Response**:
- Status: 200 OK
- Body:
```json
{
  "status": 200,
  "message": "Login successful",
  "timestamp": "2023-05-29T10:20:30.123456",
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "testuser@example.com",
      "fullName": "Test User"
    },
    "auth": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }
}
```

**Test Cases**:
1. Login with valid credentials
2. Login with invalid username (should return 401 Unauthorized)
3. Login with incorrect password (should return 401 Unauthorized)
4. Login with missing fields (should return 400 Bad Request)

### 3. Token Refresh

**Endpoint**: `POST /api/auth/refresh`

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Expected Success Response**:
- Status: 200 OK
- Body:
```json
{
  "status": 200,
  "message": "Token refreshed successfully",
  "timestamp": "2023-05-29T10:25:30.123456",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Test Cases**:
1. Refresh with valid token
2. Refresh with invalid token (should return 401 Unauthorized)
3. Refresh with expired token (should return 401 Unauthorized)

### 4. Check Username Availability

**Endpoint**: `GET /api/auth/check-username/{username}`

**Expected Success Response**:
- Status: 200 OK
- Body:
```json
{
  "status": 200,
  "message": "Username availability checked",
  "timestamp": "2023-05-29T10:30:30.123456",
  "data": {
    "available": true
  }
}
```

**Test Cases**:
1. Check availability for unused username (should return available=true)
2. Check availability for existing username (should return available=false)

### 5. Check Email Availability

**Endpoint**: `GET /api/auth/check-email/{email}`

**Expected Success Response**:
- Status: 200 OK
- Body:
```json
{
  "status": 200,
  "message": "Email availability checked",
  "timestamp": "2023-05-29T10:35:30.123456",
  "data": {
    "available": true
  }
}
```

**Test Cases**:
1. Check availability for unused email (should return available=true)
2. Check availability for existing email (should return available=false)

## Template Endpoints

### 1. Create Template

**Endpoint**: `POST /api/templates`

**Headers**:
- Authorization: Bearer {token}

**Request Body**:
```json
{
  "name": "Sample Template",
  "description": "A sample template for testing",
  "templateData": "{\"elements\":[{\"type\":\"text\",\"content\":\"Sample text\",\"position\":{\"x\":10,\"y\":20},\"size\":{\"width\":200,\"height\":100}}]}"
}
```

**Expected Success Response**:
- Status: 201 Created
- Body: Template object with ID

**Test Cases**:
1. Create template with valid data
2. Create template without authentication (should return 401 Unauthorized)
3. Create template with invalid data (should return 400 Bad Request)

### 2. Get All Templates

**Endpoint**: `GET /api/templates`

**Query Parameters**:
- page (optional): Page number (default: 0)
- size (optional): Page size (default: 10)
- sort (optional): Sort field (default: "createdAt,desc")

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 200 OK
- Body: Page of template objects

**Test Cases**:
1. Get templates with authentication
2. Get templates with pagination
3. Get templates with different sorting
4. Get templates without authentication (should return 401 Unauthorized)

### 3. Get Template by ID

**Endpoint**: `GET /api/templates/{id}`

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 200 OK
- Body: Template object

**Test Cases**:
1. Get existing template
2. Get non-existent template (should return 404 Not Found)
3. Get template without authentication (should return 401 Unauthorized)

### 4. Update Template

**Endpoint**: `PUT /api/templates/{id}`

**Headers**:
- Authorization: Bearer {token}

**Request Body**:
```json
{
  "name": "Updated Template",
  "description": "An updated template for testing",
  "templateData": "{\"elements\":[{\"type\":\"text\",\"content\":\"Updated text\",\"position\":{\"x\":10,\"y\":20},\"size\":{\"width\":200,\"height\":100}}]}"
}
```

**Expected Success Response**:
- Status: 200 OK
- Body: Updated template object

**Test Cases**:
1. Update existing template
2. Update non-existent template (should return 404 Not Found)
3. Update template without authentication (should return 401 Unauthorized)
4. Update template with invalid data (should return 400 Bad Request)

### 5. Delete Template

**Endpoint**: `DELETE /api/templates/{id}`

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 204 No Content

**Test Cases**:
1. Delete existing template
2. Delete non-existent template (should return 404 Not Found)
3. Delete template without authentication (should return 401 Unauthorized)

## Article Endpoints

### 1. Create Article

**Endpoint**: `POST /api/articles`

**Headers**:
- Authorization: Bearer {token}

**Request Body**:
```json
{
  "title": "Sample Article",
  "description": "A sample article for testing",
  "content": "This is the content of the sample article.",
  "templateId": 1,
  "status": "DRAFT"
}
```

**Expected Success Response**:
- Status: 201 Created
- Body: Article object with ID

**Test Cases**:
1. Create article with valid data
2. Create article without authentication (should return 401 Unauthorized)
3. Create article with invalid template ID (should return 400 Bad Request)
4. Create article with invalid status (should return 400 Bad Request)

### 2. Get All Articles

**Endpoint**: `GET /api/articles`

**Query Parameters**:
- page (optional): Page number (default: 0)
- size (optional): Page size (default: 10)
- sort (optional): Sort field (default: "createdAt,desc")
- status (optional): Filter by status (DRAFT, PUBLISHED, ARCHIVED)
- templateId (optional): Filter by template ID

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 200 OK
- Body: Page of article objects

**Test Cases**:
1. Get articles with authentication
2. Get articles with pagination
3. Get articles filtered by status
4. Get articles filtered by template ID
5. Get articles with different sorting
6. Get articles without authentication (should return 401 Unauthorized)

### 3. Get Article by ID

**Endpoint**: `GET /api/articles/{id}`

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 200 OK
- Body: Article object

**Test Cases**:
1. Get existing article
2. Get non-existent article (should return 404 Not Found)
3. Get article without authentication (should return 401 Unauthorized)

### 4. Update Article

**Endpoint**: `PUT /api/articles/{id}`

**Headers**:
- Authorization: Bearer {token}

**Request Body**:
```json
{
  "title": "Updated Article",
  "description": "An updated article for testing",
  "content": "This is the updated content of the sample article.",
  "templateId": 1,
  "status": "PUBLISHED"
}
```

**Expected Success Response**:
- Status: 200 OK
- Body: Updated article object

**Test Cases**:
1. Update existing article
2. Update non-existent article (should return 404 Not Found)
3. Update article without authentication (should return 401 Unauthorized)
4. Update article with invalid template ID (should return 400 Bad Request)
5. Update article with invalid status (should return 400 Bad Request)

### 5. Delete Article

**Endpoint**: `DELETE /api/articles/{id}`

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 204 No Content

**Test Cases**:
1. Delete existing article
2. Delete non-existent article (should return 404 Not Found)
3. Delete article without authentication (should return 401 Unauthorized)

### 6. Publish Article

**Endpoint**: `PUT /api/articles/{id}/publish`

**Headers**:
- Authorization: Bearer {token}

**Expected Success Response**:
- Status: 200 OK
- Body: Published article object with status PUBLISHED

**Test Cases**:
1. Publish draft article
2. Publish already published article (should still work)
3. Publish non-existent article (should return 404 Not Found)
4. Publish article without authentication (should return 401 Unauthorized)

## Category Endpoints

### 1. Create Category

**Endpoint**: `POST /api/categories`

**Headers**:
- Authorization: Bearer {token}

**Request Body**:
```json
{
  "name": "Sample Category",
  "description": "A sample category for testing",
  "featured": false
}
```

**Expected Success Response**:
- Status: 201 Created
- Body: Category object with ID

**Test Cases**:
1. Create category with valid data
2. Create category without authentication (should return 401 Unauthorized)
3. Create category with duplicate name (should return 400 Bad Request)

### 2. Get All Categories

**Endpoint**: `GET /api/categories`

**Query Parameters**:
- page (optional): Page number (default: 0)
- size (optional): Page size (default: 10)
- featured (optional): Filter featured categories

**Expected Success Response**:
- Status: 200 OK
- Body: Page of category objects

**Test Cases**:
1. Get all categories
2. Get categories with pagination
3. Get featured categories only

### 3. Get Category by ID

**Endpoint**: `GET /api/categories/{id}`

**Expected Success Response**:
- Status: 200 OK
- Body: Category object

**Test Cases**:
1. Get existing category
2. Get non-existent category (should return 404 Not Found)

## Media Upload Endpoints

### 1. Upload Image

**Endpoint**: `POST /api/media/upload`

**Headers**:
- Authorization: Bearer {token}
- Content-Type: multipart/form-data

**Form Data**:
- file: (binary image file)

**Expected Success Response**:
- Status: 200 OK
- Body: Object with file URL

**Test Cases**:
1. Upload valid image file
2. Upload invalid file type (should return 400 Bad Request)
3. Upload without authentication (should return 401 Unauthorized)
4. Upload file that's too large (should return 413 Payload Too Large)

## Testing Workflow

1. Start by testing the registration endpoint to create a test user
2. Use the login endpoint to get authentication tokens
3. Test the remaining endpoints using the obtained token
4. Test error cases for each endpoint
5. Document any issues or bugs found during testing

## Common Issues and Solutions

### Authentication Issues

If you encounter authentication issues:
1. Verify that the token is valid and not expired
2. Check that the token is correctly included in the Authorization header
3. Try refreshing the token using the refresh endpoint

### Database Issues

If you encounter database-related errors:
1. Verify that the database is running and accessible
2. Check that the necessary tables exist in the database
3. Verify that the database connection settings are correct

### Server Issues

If the server returns 500 errors:
1. Check the server logs for detailed error information
2. Verify that all required dependencies are installed
3. Restart the server if necessary

## Testing Tools

You can use the following tools for API testing:

1. **Postman**: A powerful GUI tool for API testing
2. **curl**: Command-line tool for making HTTP requests
3. **Swagger UI**: Access the API documentation and test endpoints at `http://localhost:8080/swagger-ui/index.html`
4. **HTTPie**: A user-friendly command-line HTTP client

## Conclusion

This testing plan covers all the main endpoints of the CanvaMedium API. By systematically testing each endpoint with various test cases, you can ensure that the API functions correctly and handles errors appropriately. Document any issues found during testing and work with the development team to resolve them. 