# CanvaMedium

A mobile-first content creation platform that combines template-based design with article authoring capabilities.

## Project Overview

CanvaMedium is a full-stack application consisting of:
- Spring Boot backend (Java)
- Android mobile application (Java)
- PostgreSQL database

The application allows users to:
- Create and manage design templates
- Author articles using these templates
- Browse and read published content
- Manage their profile and saved content

## Project Status

CanvaMedium is currently in the Setup Stage:

- **Development Stage**: ✅ Completed all four phases
- **Setup Stage**: 🔄 In Progress (Backend running successfully, Android setup in progress)
- **Demo Video Recording Stage**: ⏳ Planned

## Features

### Content Management
- Create, read, update, and delete content entries
- Article creation with template-based editor
- Publishing workflow with draft, preview, and publish states
- Rich text formatting for article content
- Thumbnail generation for templates

### Templates
- Template builder with drag-and-drop functionality
- Support for multiple element types (text, images, headers, dividers, quotes)
- Template management system
- Template selection interface for article creation

### Media Management
- Image integration in articles
- Image upload functionality with resizing and cropping
- Image compression for optimized storage and bandwidth
- Lazy loading for improved performance

### Search and Organization
- Search and filtering functionality for articles
- Category browsing interface
- Tag-based filtering
- Bookmarking functionality for favorite articles
- Advanced search capabilities (by title, content, category, tags)

### Authentication and User Management
- User authentication with JWT token-based security
- Role-based access control for different user types
- Secure password storage with BCrypt encryption
- User registration with validation
- Secure login with JWT tokens
- Token storage and management
- Automatic token refresh
- User profile display with statistics and account information
- Account settings functionality

### Performance and Offline Features
- Offline caching for articles, templates, categories, and tags
- Room database for local storage
- Network request optimization with OkHttp interceptors
- Caching headers for efficient data retrieval
- Synchronization mechanism for offline changes
- Pagination for large data sets
- Responsive UI with loading states, error handling, and offline indicators

### UI Components
- Modern Android UI with Material Design components
- Responsive and intuitive user interface
- Pull-to-refresh functionality
- Empty states and error handling
- Loading indicators and progress feedback
- Smooth transitions between screens
- Dynamic content rendering based on templates

## Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Security with JWT authentication
- PostgreSQL
- Maven
- Swagger/OpenAPI for API documentation

### Android Frontend
- Java
- Retrofit for API communication
- Room for local storage
- RecyclerView for list displays
- Material Design components

## Project Structure

- `/android` - Android application with Java
- `/backend` - Spring Boot backend services
- `/docs` - Documentation and resources

## Getting Started

### Prerequisites
- JDK 17 or later
- PostgreSQL
- Android Studio with Android SDK 34+

### Backend Setup

1. Create a PostgreSQL database named `canvamedium`
2. Update database credentials in `backend/src/main/resources/application.properties` if needed
3. Navigate to the backend directory: `cd backend`
4. Build the application: `mvn clean install`
5. Run the backend: `mvn spring-boot:run`
6. Access Swagger UI: http://localhost:8080/swagger-ui/index.html

### Android Setup

1. Open the android directory in Android Studio
2. Update the API base URL in `android/app/src/main/java/com/canvamedium/api/ApiClient.java` if needed
3. Build and run the application on an emulator or physical device

## Development Resources

The project includes several important documents:
- [PLANNING.md](PLANNING.md) - Project architecture and design decisions
- [PHASED_PLAN.md](PHASED_PLAN.md) - Implementation phases and testing procedures
- [TASK.md](TASK.md) - Task tracking and progress
- [SETUP.md](SETUP.md) - Setup instructions for all components
- [SETUP-GUIDE.md](SETUP-GUIDE.md) - Detailed Android setup guide

## API Endpoints

### Content Endpoints
- `GET /api/articles` - Get all articles
- `GET /api/articles/{id}` - Get article by ID
- `POST /api/articles` - Create a new article
- `PUT /api/articles/{id}` - Update an existing article
- `DELETE /api/articles/{id}` - Delete an article
- `GET /api/articles/drafts` - Get all draft articles
- `PUT /api/articles/{id}/publish` - Publish an article
- `PUT /api/articles/{id}/archive` - Archive an article
- `GET /api/articles/search` - Search articles by query
- `GET /api/articles/category/{categoryId}` - Get articles by category
- `GET /api/articles/tag/{tagId}` - Get articles by tag
- `GET /api/articles/bookmarked` - Get bookmarked articles
- `PUT /api/articles/{id}/bookmark` - Bookmark an article
- `PUT /api/articles/{id}/unbookmark` - Remove bookmark from article

### Template Endpoints
- `GET /api/templates` - Get all templates
- `GET /api/templates/{id}` - Get template by ID
- `POST /api/templates` - Create a new template
- `PUT /api/templates/{id}` - Update an existing template
- `DELETE /api/templates/{id}` - Delete a template

### Media Endpoints
- `POST /api/media/upload` - Upload media files
- `GET /api/media/{id}` - Get media file by ID
- `DELETE /api/media/{id}` - Delete media file

### Authentication Endpoints
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and receive JWT token
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update user profile

### Category and Tag Endpoints
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create a new category
- `PUT /api/categories/{id}` - Update a category
- `DELETE /api/categories/{id}` - Delete a category
- `GET /api/tags` - Get all tags
- `GET /api/tags/{id}` - Get tag by ID
- `POST /api/tags` - Create a new tag
- `PUT /api/tags/{id}` - Update a tag
- `DELETE /api/tags/{id}` - Delete a tag

## Template Builder

The template builder allows users to create custom layouts for their content. Features include:

- Drag-and-drop interface for element placement
- Support for multiple element types:
  - Text blocks
  - Images
  - Headers
  - Dividers
  - Quotes
- Element customization options
- Template saving and loading
- Preview functionality

## Article Editor

The article editor allows users to create and edit articles based on templates. Features include:

- Template selection interface
- Content editing based on selected template
- Text formatting tools
- Image selection and placement
- Article preview functionality
- Draft saving and publishing workflow
- Support for different content types (text, headers, images, quotes)
- Category and tag selection

## User Authentication

The system provides secure user authentication with the following features:

- JWT token-based authentication
- Role-based access control (USER, EDITOR, ADMIN)
- Secure password storage with BCrypt encryption
- Token refresh mechanism
- Registration and login endpoints
- User profile management
- Account settings

## Offline Support

The application provides robust offline support with:

- Room database for local storage of articles, templates, categories, and tags
- Network caching with OkHttp interceptors
- Synchronization mechanism for offline changes
- Visual indicators for offline mode
- Background synchronization with WorkManager
- Efficient image caching and compression

## Future Enhancements

- Email verification for new user accounts
- Data binding for more efficient UI updates
- More robust error handling system
- JPA auditing for automatic timestamp handling
- Database indexing for performance optimization
- State management pattern for article workflow
- Social login integration (Google, Facebook)
- Push notifications
- AI-powered content suggestions

## Development Approach

The project follows a phased development approach, focusing on building a solid foundation first, then incrementally adding features. Key principles include:

- Clean architecture with separation of concerns
- Comprehensive testing (unit, integration, UI)
- User-centric design focusing on simplicity and usability
- Scalable backend designed for future growth

## Contributing

If you'd like to contribute to CanvaMedium, please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 