-- Create database
CREATE DATABASE IF NOT EXISTS inklink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE inklink_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    avatar_url VARCHAR(500) DEFAULT '/images/avatar-default.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN DEFAULT TRUE
    );

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
    );

-- Stories table
CREATE TABLE IF NOT EXISTS stories (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT,
    cover_image VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
    );

-- Reactions table
CREATE TABLE IF NOT EXISTS reactions (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         user_id BIGINT NOT NULL,
                                         story_id BIGINT NOT NULL,
                                         type ENUM('LIKE', 'BOOKMARK') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_story_reaction (user_id, story_id, type)
    );

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        content TEXT NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        story_id BIGINT NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        deleted BOOLEAN DEFAULT FALSE,
                                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE
    );

-- Insert sample data
INSERT IGNORE INTO categories (name, description) VALUES
('Fiction', 'Imaginative storytelling'),
('Technology', 'Tech-related articles and tutorials'),
('Science', 'Scientific discoveries and research'),
('Personal', 'Personal stories and experiences');

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, role) VALUES
    ('admin', 'admin@inklink.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2Ui.', 'ADMIN');

-- Insert sample users (password: password123)
INSERT INTO users (username, email, password) VALUES
                                                  ('author1', 'author1@inklink.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2Ui.'),
                                                  ('reader1', 'reader1@inklink.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2Ui.');

-- Create indexes for performance
CREATE INDEX idx_stories_author ON stories(author_id);
CREATE INDEX idx_stories_category ON stories(category_id);
CREATE INDEX idx_stories_created ON stories(created_at);
CREATE INDEX idx_stories_deleted ON stories(deleted);
CREATE INDEX idx_reactions_user_story ON reactions(user_id, story_id);
CREATE INDEX idx_reactions_story_type ON reactions(story_id, type);
CREATE INDEX idx_reactions_user_type ON reactions(user_id, type);
CREATE INDEX idx_comments_story ON comments(story_id);
CREATE INDEX idx_comments_user ON comments(user_id);
CREATE INDEX idx_comments_created ON comments(created_at);