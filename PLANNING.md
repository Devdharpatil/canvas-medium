# CanvaMedium Project Planning

## Project Vision

CanvaMedium is a mobile-first content creation and publishing platform designed to blend the user-friendly design experience of Canva with the clean reading flow of Medium. The application follows a client-server architecture with an Android frontend and Spring Boot backend.

## Core Features

1. **Feed View**
   - Display a scrollable list of user-generated articles
   - Each card shows title, thumbnail, and a short preview
   - Tapping a card opens the full article in reader mode
   - Data is retrieved using REST APIs from the Spring Boot backend

2. **Custom Template Builder**
   - Drag-and-drop interface for multimedia and text blocks
   - Add multiple images and position them in desired sections
   - Implement smooth animations during interactions (drag, drop, transitions)
   - Save template layouts for future use
   - Templates and articles stored separately in the backend

3. **Article Creation Page**
   - Create and publish articles using custom templates
   - Editor with all features of the template builder
   - Store articles in the database and display them in the feed

## Architecture Overview

### System Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Android App    │◄───►│  Spring Boot    │◄───►│   PostgreSQL    │
│  (Frontend)     │     │  (Backend)      │     │   Database      │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### Frontend Architecture (Android)

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐        │
│  │ Activities  │   │ Fragments   │   │ Adapters    │        │
│  └─────────────┘   └─────────────┘   └─────────────┘        │
├─────────────────────────────────────────────────────────────┤
│                    Business Logic Layer                      │
│  ┌─────────────┐                    ┌─────────────┐         │
│  │ Models      │                    │ Utilities   │         │
│  └─────────────┘                    └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Data Layer                                │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐        │
│  │ API Service │   │ Retrofit    │   │ GSON        │        │
│  └─────────────┘   └─────────────┘   └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### Backend Architecture (Spring Boot)

```
┌─────────────────────────────────────────────────────────────┐
│                    API Layer                                 │
│  ┌─────────────┐                    ┌─────────────┐         │
│  │ Controllers │                    │ DTOs        │         │
│  └─────────────┘                    └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                             │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐        │
│  │ Services    │   │ Validation  │   │ Business    │        │
│  │             │   │             │   │ Logic       │        │
│  └─────────────┘   └─────────────┘   └─────────────┘        │
├─────────────────────────────────────────────────────────────┤
│                    Data Access Layer                         │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐        │
│  │ Repositories│   │ Entities    │   │ JPA         │        │
│  └─────────────┘   └─────────────┘   └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Frontend (Android)
- **Language**: Java (with potential migration to Kotlin in future phases)
- **UI Components**: 
  - RecyclerView with CardView for article listings
  - Custom Views for drag-and-drop template builder
  - Material Design components
- **Networking**: Retrofit for API calls
- **JSON Parsing**: GSON
- **Image Loading**: Glide or Picasso
- **Animation**: Android Animation Framework
- **Testing**: JUnit, Espresso

### Backend (Spring Boot)
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **API Documentation**: Swagger/OpenAPI
- **Validation**: Jakarta Validation
- **Testing**: JUnit, Mockito, Spring Boot Test

## Database Schema

```
┌───────────────────────┐      ┌───────────────────────┐
│ Template              │      │ Article               │
├───────────────────────┤      ├───────────────────────┤
│ id: BIGINT (PK)       │      │ id: BIGINT (PK)       │
│ name: VARCHAR         │      │ title: VARCHAR        │
│ layout: JSONB         │      │ content: JSONB        │
│ created_at: TIMESTAMP │      │ preview_text: VARCHAR │
│ updated_at: TIMESTAMP │      │ thumbnail_url: VARCHAR│
└───────────────────────┘      │ template_id: BIGINT(FK)│
                               │ status: VARCHAR       │
                               │ created_at: TIMESTAMP │
                               │ updated_at: TIMESTAMP │
                               └───────────────────────┘

┌───────────────────────┐      ┌───────────────────────┐
│ User                  │      │ UserRole              │
├───────────────────────┤      ├───────────────────────┤
│ id: BIGINT (PK)       │      │ id: BIGINT (PK)       │
│ username: VARCHAR     │      │ user_id: BIGINT (FK)  │
│ email: VARCHAR        │      │ role: VARCHAR         │
│ password: VARCHAR     │      └───────────────────────┘
│ created_at: TIMESTAMP │      
│ updated_at: TIMESTAMP │      
└───────────────────────┘      
```

## API Endpoints

### Template Endpoints

| Method | Endpoint                  | Description                           |
|--------|---------------------------|---------------------------------------|
| GET    | /api/templates            | Get all templates                     |
| GET    | /api/templates/{id}       | Get template by ID                    |
| POST   | /api/templates            | Create a new template                 |
| PUT    | /api/templates/{id}       | Update an existing template           |
| DELETE | /api/templates/{id}       | Delete a template                     |

### Article Endpoints

| Method | Endpoint                  | Description                           |
|--------|---------------------------|---------------------------------------|
| GET    | /api/articles             | Get all articles                      |
| GET    | /api/articles/{id}        | Get article by ID                     |
| POST   | /api/articles             | Create a new article                  |
| PUT    | /api/articles/{id}        | Update an existing article            |
| DELETE | /api/articles/{id}        | Delete an article                     |
| GET    | /api/articles/drafts      | Get all draft articles                |
| PUT    | /api/articles/{id}/publish| Publish an article                    |
| PUT    | /api/articles/{id}/archive| Archive an article                    |
| GET    | /api/articles/search      | Search articles by query              |

### Authentication Endpoints

| Method | Endpoint                  | Description                           |
|--------|---------------------------|---------------------------------------|
| POST   | /api/auth/register        | Register a new user                   |
| POST   | /api/auth/login           | Authenticate and receive JWT token    |
| POST   | /api/auth/refresh         | Refresh JWT token                     |
| GET    | /api/users/me             | Get current user profile              |

### Media Endpoints

| Method | Endpoint                  | Description                           |
|--------|---------------------------|---------------------------------------|
| POST   | /api/media/upload         | Upload media files                    |
| GET    | /api/media/{id}           | Get media file by ID                  |
| DELETE | /api/media/{id}           | Delete media file                     |

## Code Style & Conventions

### Java (Backend & Android)
- Class names: PascalCase (e.g., `ArticleService`)
- Variables and methods: camelCase (e.g., `getArticleById`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_ARTICLE_LENGTH`)
- Packages: lowercase with dots (e.g., `com.canvamedium.service`)

### Android Resources
- Layout files: snake_case (e.g., `activity_main.xml`, `item_article.xml`)
- IDs: snake_case (e.g., `text_title`, `recycler_view`)
- Drawable resources: snake_case (e.g., `ic_add.xml`, `bg_card.xml`)

## Development Phases

### Phase 1: Foundation
- Set up project structure
- Implement basic backend CRUD operations for templates and articles
- Create article feed UI with RecyclerView
- Implement article detail view

### Phase 2: Template Builder
- Develop drag-and-drop interface for template creation
- Implement layout saving and loading
- Add animations for interactions
- Create template selection interface

### Phase 3: Article Creation
- Implement article editor based on templates
- Add media integration (images, text formatting)
- Create publication flow
- Connect to article feed

### Phase 4: Enhancements
- Add user authentication
- Implement content categories and tags
- Add image upload functionality
- Improve UI/UX with animations and transitions

## Constraints & Considerations

- **Performance**: API responses should be under 300ms
- **Mobile Data Usage**: Minimize payload sizes, implement caching
- **Offline Support**: Basic functionality should work offline
- **Scalability**: Design with future growth in mind
- **Security**: Implement proper validation, protect against SQL injection, XSS

## Development Environment

- **IDE**: IntelliJ IDEA or Eclipse for backend, Android Studio for frontend
- **Version Control**: Git
- **Database Management**: pgAdmin or DBeaver
- **API Testing**: Postman or Insomnia

## Testing Strategy

- **Unit Testing**: Test individual components in isolation
- **Integration Testing**: Test component interactions
- **UI Testing**: Test Android UI components with Espresso
- **End-to-End Testing**: Test complete user journeys
- **Manual Testing**: Verify UI/UX, usability, and edge cases 