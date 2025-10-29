-- Comments table
CREATE TABLE IF NOT EXISTS comments (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        content VARCHAR(1000) NOT NULL,
    author_id BIGINT NOT NULL,
    story_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    edited BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (story_id) REFERENCES stories(id),
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id),
    INDEX idx_comments_story (story_id),
    INDEX idx_comments_author (author_id),
    INDEX idx_comments_parent (parent_comment_id)
    );

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             type VARCHAR(50) NOT NULL,
    recipient_id BIGINT NOT NULL,
    triggered_by_id BIGINT,
    story_id BIGINT,
    comment_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    FOREIGN KEY (triggered_by_id) REFERENCES users(id),
    FOREIGN KEY (story_id) REFERENCES stories(id),
    FOREIGN KEY (comment_id) REFERENCES comments(id),
    INDEX idx_notifications_recipient (recipient_id),
    INDEX idx_notifications_read (is_read),
    INDEX idx_notifications_created (created_at)
    );