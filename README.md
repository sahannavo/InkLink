# InkLink

A modern web-based storytelling and creative writing platform built with Spring Boot and MySQL.

## 📖 Overview

InkLink is a collaborative storytelling platform where users can create, share, and discover creative stories. The platform features user authentication, story management, commenting system, tagging, and social interactions like likes and follows.

## ✨ Features

- **User Management**
  - User registration and authentication
  - Profile customization with avatars
  - Role-based access control (USER, ADMIN, MODERATOR)

- **Story Management**
  - Create and publish stories with rich metadata
  - Multiple genres support (FANTASY, SCIENCE_FICTION, ROMANCE, MYSTERY, HORROR, THRILLER, ADVENTURE, HISTORICAL, CONTEMPORARY)
  - Story status tracking (DRAFT, PUBLISHED, ARCHIVED)
  - Tag-based categorization
  - Story likes and engagement metrics

- **Social Features**
  - Commenting system on stories
  - User profiles with bio and social links
  - Story discovery and browsing

- **Media Management**
  - File upload for profile pictures
  - Story cover images
  - Secure file storage system

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.1.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with session-based authentication
- **Build Tool**: Maven

### Frontend
- **HTML5/CSS3**: Responsive design
- **JavaScript**: Vanilla JS for client-side interactions
- **AJAX**: For asynchronous API calls

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git (optional)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd project
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE inklink_db;
```

Update database credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inklink_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the compiled JAR:

```bash
java -jar target/inklink-1.0.0.jar
```

### 5. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/project/inklink/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handling
│   │   ├── repository/      # Data repositories
│   │   ├── security/        # Security configuration
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── frontend/        # Static web assets
│       │   ├── css/        # Stylesheets
│       │   ├── js/         # JavaScript files
│       │   └── *.html      # HTML pages
│       └── application.properties
└── test/                    # Test files
```

## 🔑 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/logout` - User logout
- `GET /api/auth/current-user` - Get current user

### Stories
- `GET /api/stories` - Get all stories (with pagination)
- `GET /api/stories/{id}` - Get story by ID
- `POST /api/stories` - Create new story
- `PUT /api/stories/{id}` - Update story
- `DELETE /api/stories/{id}` - Delete story
- `POST /api/stories/{id}/like` - Like/unlike a story

### Comments
- `GET /api/stories/{storyId}/comments` - Get comments for a story
- `POST /api/comments` - Create a comment
- `DELETE /api/comments/{id}` - Delete a comment

### Users
- `GET /api/users/{username}` - Get user profile
- `PUT /api/users/{username}` - Update user profile
- `POST /api/users/upload-avatar` - Upload profile picture

### Tags
- `GET /api/tags` - Get all tags
- `GET /api/tags/popular` - Get popular tags

## 🔒 Security

The application uses Spring Security with session-based authentication:
- Password encryption using BCrypt
- CSRF protection enabled
- HTTP-only session cookies
- Session timeout: 30 minutes
- Role-based authorization

## 📝 Configuration

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/inklink_db
spring.datasource.username=root
spring.datasource.password=

# File Upload Limits
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB

# Session Configuration
server.servlet.session.timeout=1800

# Upload Directories
app.upload.profile-dir=uploads/profiles/
app.upload.story-dir=uploads/stories/

# Pagination
app.pagination.default-size=10
app.pagination.max-size=100
```

## 🧪 Testing

Run tests using Maven:

```bash
mvn test
```

## 📦 Deployment

### Production Build

1. Update `application-prod.properties` with production settings
2. Build the application:
   ```bash
   mvn clean package -DskipTests
   ```
3. Run with production profile:
   ```bash
   java -jar target/inklink-1.0.0.jar --spring.profiles.active=prod
   ```

## 🗂️ Database Schema

The application automatically creates the following main tables:
- `users` - User accounts and profiles
- `stories` - Story content and metadata
- `comments` - User comments on stories
- `tags` - Story categorization tags
- `story_likes` - Story like tracking
- `story_tags` - Many-to-many relationship between stories and tags

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

InkLink Development Team

## 🐛 Known Issues

- File upload size limited to 2MB
- Session-based authentication (consider JWT for mobile apps)

## 🔮 Future Enhancements

- [ ] Real-time notifications
- [ ] Story collaboration features
- [ ] Advanced search and filtering
- [ ] Mobile application
- [ ] Story reading analytics
- [ ] Social sharing integration
- [ ] Email verification
- [ ] Password recovery
- [ ] Rich text editor for story creation
- [ ] Story versioning

## 📞 Support

For support and queries, please open an issue in the repository.

---

Made with ❤️ by the InkLink Team

Team Members:

M A S N Weerasinghe-35173(Leader)

R M O Purwa-34657

U G D S Munasinghe-34702

K M C A Kumarasinghe-35464
