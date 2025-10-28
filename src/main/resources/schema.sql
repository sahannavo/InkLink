-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    bio VARCHAR(500),
    location VARCHAR(100),
    website_url VARCHAR(500),
    twitter_handle VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    profile_picture VARCHAR(500),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Create stories table if it doesn't exist (you'll need this for the indexes)
CREATE TABLE IF NOT EXISTS stories (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    excerpt VARCHAR(500),
    status VARCHAR(20) DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    author_id BIGINT,
    category_id BIGINT,
    view_count INT DEFAULT 0,
    reading_time INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
    );

-- Create categories table if it doesn't exist
CREATE TABLE IF NOT EXISTS categories (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create follows table
CREATE TABLE IF NOT EXISTS follows (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       follower_id BIGINT NOT NULL,
                                       following_id BIGINT NOT NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (follower_id, following_id)
    );

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_stories_status_published ON stories(status, published_at);
CREATE INDEX IF NOT EXISTS idx_stories_author_status ON stories(author_id, status);
CREATE INDEX IF NOT EXISTS idx_stories_category_status ON stories(category_id, status);
CREATE INDEX IF NOT EXISTS idx_stories_views ON stories(view_count);
CREATE INDEX IF NOT EXISTS idx_stories_reading_time ON stories(reading_time);

-- Create analytics table for future use
CREATE TABLE IF NOT EXISTS story_analytics (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               story_id BIGINT NOT NULL,
                                               event_type VARCHAR(50) NOT NULL,
    event_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE
    );

-- Create user_analytics table
CREATE TABLE IF NOT EXISTS user_analytics (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              event_type VARCHAR(50) NOT NULL,
    event_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );