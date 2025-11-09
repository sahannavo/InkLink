// InkLink - Main Application JavaScript
class InkLinkApp {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.currentUser = null;
        this.stories = [];
        this.init();
    }

    async init() {
        await this.checkAuthentication();
        this.setupEventListeners();
        this.loadStories();
        this.setupUIInteractions();
    }

    // Authentication Management - FIXED for session-based auth
    async checkAuthentication() {
        try {
            // Check session with backend
            const response = await fetch(`${this.apiBaseUrl}/auth/me`, {
                credentials: 'include' // Important for sessions
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    this.currentUser = result.data;
                    // Store user data in localStorage ONLY for display purposes
                    localStorage.setItem('currentUser', JSON.stringify(result.data));
                    this.updateUIForAuthenticatedUser();
                } else {
                    throw new Error('No valid session');
                }
            } else {
                throw new Error('Session check failed');
            }
        } catch (error) {
            console.error('Session verification failed:', error);
            // Clear any stale data
            localStorage.removeItem('currentUser');
            this.currentUser = null;
            this.updateUIForGuest();
        }
    }

    updateUIForAuthenticatedUser() {
        // Update navigation
        const authElements = document.querySelectorAll('.auth-element');
        authElements.forEach(element => {
            if (element.classList.contains('guest-only')) {
                element.style.display = 'none';
            } else if (element.classList.contains('user-only')) {
                element.style.display = 'block';
            }
        });

        // Update user info in navigation
        const userNavElements = document.querySelectorAll('.user-info');
        userNavElements.forEach(element => {
            if (this.currentUser) {
                const usernameElement = element.querySelector('.username');
                const avatarElement = element.querySelector('.user-avatar');

                if (usernameElement) {
                    usernameElement.textContent = this.currentUser.username;
                }

                if (avatarElement && !avatarElement.innerHTML.trim()) {
                    avatarElement.textContent = this.currentUser.username.charAt(0).toUpperCase();
                }
            }
        });

        // Enable story creation features
        const createStoryBtns = document.querySelectorAll('.create-story-btn');
        createStoryBtns.forEach(btn => {
            btn.style.display = 'block';
            btn.addEventListener('click', () => this.openStoryEditor());
        });
    }

    updateUIForGuest() {
        const authElements = document.querySelectorAll('.auth-element');
        authElements.forEach(element => {
            if (element.classList.contains('guest-only')) {
                element.style.display = 'block';
            } else if (element.classList.contains('user-only')) {
                element.style.display = 'none';
            }
        });
    }

    // Story Management
    async loadStories(page = 0, size = 12) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/stories?page=${page}&size=${size}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                this.stories = data.content || data;
                this.displayStories(this.stories);
            } else {
                throw new Error('Failed to load stories');
            }
        } catch (error) {
            console.error('Error loading stories:', error);
            this.showNotification('Failed to load stories', 'error');
        }
    }

    displayStories(stories) {
        const storiesContainer = document.getElementById('storiesContainer');
        if (!storiesContainer) return;

        if (stories.length === 0) {
            storiesContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-book-open"></i>
                    <h3>No stories yet</h3>
                    <p>Be the first to share your story!</p>
                    <button class="btn btn-primary create-story-btn">Write Your First Story</button>
                </div>
            `;
            return;
        }

        storiesContainer.innerHTML = stories.map(story => `
            <div class="story-card" data-story-id="${story.id}">
                <div class="story-image" style="background: linear-gradient(45deg, ${this.getRandomColor()}, ${this.getRandomColor()})">
                    <i class="fas fa-${this.getStoryIcon(story.genre)}"></i>
                </div>
                <div class="story-content">
                    <h3 class="story-title">${this.escapeHtml(story.title)}</h3>
                    <div class="story-author">
                        <div class="author-avatar">${story.author?.username?.charAt(0)?.toUpperCase() || 'U'}</div>
                        <span>${this.escapeHtml(story.author?.username || 'Unknown Author')}</span>
                    </div>
                    <p class="story-excerpt">${this.escapeHtml(story.content?.substring(0, 150) || '')}...</p>
                    <div class="story-tags">
                        <span class="tag">${story.genre || 'Unknown'}</span>
                    </div>
                    <div class="story-stats">
                        <span><i class="fas fa-eye"></i> ${story.readCount || 0}</span>
                        <span><i class="fas fa-heart"></i> ${story.likeCount || 0}</span>
                        <span><i class="fas fa-comment"></i> ${story.commentCount || 0}</span>
                    </div>
                    <div class="story-actions">
                        <button class="btn btn-outline read-story" onclick="app.readStory(${story.id})">Read</button>
                        ${this.currentUser ? `
                            <button class="btn btn-icon" onclick="app.toggleLike(${story.id})">
                                <i class="fas fa-heart"></i>
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `).join('');
    }

    async readStory(storyId) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/stories/${storyId}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const story = await response.json();
                this.openStoryReader(story);
            } else {
                throw new Error('Story not found');
            }
        } catch (error) {
            console.error('Error reading story:', error);
            this.showNotification('Failed to load story', 'error');
        }
    }

    openStoryReader(story) {
        const modal = this.createModal(`
            <div class="story-reader">
                <div class="story-reader-header">
                    <h2>${this.escapeHtml(story.title)}</h2>
                    <div class="story-meta">
                        <div class="author-info">
                            <div class="author-avatar">${story.author?.username?.charAt(0)?.toUpperCase() || 'U'}</div>
                            <span>${this.escapeHtml(story.author?.username || 'Unknown Author')}</span>
                        </div>
                        <div class="story-stats">
                            <span><i class="fas fa-calendar"></i> ${new Date(story.createdAt).toLocaleDateString()}</span>
                            <span><i class="fas fa-eye"></i> ${story.readCount || 0}</span>
                            <span><i class="fas fa-heart"></i> ${story.likeCount || 0}</span>
                        </div>
                    </div>
                </div>
                <div class="story-content">
                    ${this.formatStoryContent(story.content)}
                </div>
                <div class="story-reader-footer">
                    <div class="story-tags">
                        <span class="tag">${story.genre || 'Unknown'}</span>
                    </div>
                    <div class="reader-actions">
                        <button class="btn btn-primary" onclick="app.toggleLike(${story.id})">
                            <i class="fas fa-heart"></i> Like (${story.likeCount || 0})
                        </button>
                        <button class="btn btn-outline" onclick="app.openComments(${story.id})">
                            <i class="fas fa-comment"></i> Comments (${story.commentCount || 0})
                        </button>
                    </div>
                </div>
            </div>
        `);

        modal.style.maxWidth = '800px';
    }

    // Story Creation and Editing
    openStoryEditor(story = null) {
        if (!this.currentUser) {
            this.showNotification('Please log in to create stories', 'error');
            return;
        }

        const isEdit = story !== null;
        const modal = this.createModal(`
            <div class="story-editor">
                <h2>${isEdit ? 'Edit Story' : 'Create New Story'}</h2>
                <form id="storyForm">
                    <div class="form-group">
                        <label for="storyTitle">Title</label>
                        <input type="text" id="storyTitle" class="form-control" value="${story?.title || ''}" required>
                    </div>
                    <div class="form-group">
                        <label for="storyContent">Content</label>
                        <textarea id="storyContent" class="form-control" rows="15" required>${story?.content || ''}</textarea>
                    </div>
                    <div class="form-group">
                        <label for="storyGenre">Genre</label>
                        <select id="storyGenre" class="form-control">
                            <option value="FICTION" ${story?.genre === 'FICTION' ? 'selected' : ''}>Fiction</option>
                            <option value="NON_FICTION" ${story?.genre === 'NON_FICTION' ? 'selected' : ''}>Non-Fiction</option>
                            <option value="FANTASY" ${story?.genre === 'FANTASY' ? 'selected' : ''}>Fantasy</option>
                            <option value="SCI_FI" ${story?.genre === 'SCI_FI' ? 'selected' : ''}>Science Fiction</option>
                            <option value="MYSTERY" ${story?.genre === 'MYSTERY' ? 'selected' : ''}>Mystery</option>
                            <option value="ROMANCE" ${story?.genre === 'ROMANCE' ? 'selected' : ''}>Romance</option>
                            <option value="HORROR" ${story?.genre === 'HORROR' ? 'selected' : ''}>Horror</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="storyStatus">Status</label>
                        <select id="storyStatus" class="form-control">
                            <option value="DRAFT" ${story?.status === 'DRAFT' ? 'selected' : ''}>Draft</option>
                            <option value="PUBLISHED" ${story?.status === 'PUBLISHED' ? 'selected' : ''}>Published</option>
                        </select>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn btn-outline" onclick="app.closeModal()">Cancel</button>
                        <button type="submit" class="btn btn-primary">${isEdit ? 'Update Story' : 'Publish Story'}</button>
                    </div>
                </form>
            </div>
        `);

        const form = modal.querySelector('#storyForm');
        form.addEventListener('submit', (e) => this.handleStorySubmit(e, story?.id));
    }

    async handleStorySubmit(e, storyId = null) {
        e.preventDefault();

        // Get form values
        const title = document.getElementById('storyTitle').value;
        const content = document.getElementById('storyContent').value;
        const genre = document.getElementById('storyGenre').value;
        const status = document.getElementById('storyStatus').value;

        // Validate required fields
        if (!title || title.length < 3) {
            this.showNotification('Title must be at least 3 characters', 'error');
            return;
        }

        if (!content || content.length < 10) {
            this.showNotification('Content must be at least 10 characters', 'error');
            return;
        }

        if (!genre) {
            this.showNotification('Please select a genre', 'error');
            return;
        }

        // Send only the fields that backend expects
        const formData = {
            title: title,
            content: content,
            genre: genre,
            status: status
        };

        console.log('Sending story data:', formData); // Debug log

        try {
            const url = storyId ? `${this.apiBaseUrl}/stories/${storyId}` : `${this.apiBaseUrl}/stories`;
            const method = storyId ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                console.log('Story saved successfully:', result); // Debug log
                this.showNotification(storyId ? 'Story updated successfully!' : 'Story published successfully!', 'success');
                this.closeModal();
                this.loadStories(); // Refresh the stories list
            } else {
                const errorData = await response.json();
                console.error('Backend error:', errorData); // Debug log
                throw new Error(errorData.message || 'Failed to save story');
            }
        } catch (error) {
            console.error('Error saving story:', error);
            this.showNotification('Failed to save story: ' + error.message, 'error');
        }
    }

    // Comments System
    async openComments(storyId) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/stories/${storyId}/comments`, {
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                const comments = result.data || result;
                this.displayCommentsModal(storyId, comments);
            } else {
                throw new Error('Failed to load comments');
            }
        } catch (error) {
            console.error('Error loading comments:', error);
            this.showNotification('Failed to load comments', 'error');
        }
    }

    displayCommentsModal(storyId, comments) {
        const modal = this.createModal(`
            <div class="comments-section">
                <h3>Comments</h3>
                <div class="comments-list">
                    ${comments.length === 0 ? '<p class="no-comments">No comments yet. Be the first to comment!</p>' : ''}
                    ${comments.map(comment => `
                        <div class="comment">
                            <div class="comment-header">
                                <div class="comment-author">
                                    <div class="author-avatar">${comment.user?.username?.charAt(0)?.toUpperCase() || 'U'}</div>
                                    <span>${this.escapeHtml(comment.user?.username || 'Unknown User')}</span>
                                </div>
                                <span class="comment-date">${new Date(comment.createdAt).toLocaleDateString()}</span>
                            </div>
                            <div class="comment-content">
                                ${this.escapeHtml(comment.content)}
                            </div>
                            ${this.currentUser && this.currentUser.id === comment.user?.id ? `
                                <div class="comment-actions">
                                    <button class="btn btn-sm btn-outline" onclick="app.deleteComment(${comment.id})">Delete</button>
                                </div>
                            ` : ''}
                        </div>
                    `).join('')}
                </div>
                ${this.currentUser ? `
                    <div class="add-comment">
                        <textarea id="newComment" class="form-control" placeholder="Write your comment..." rows="3"></textarea>
                        <button class="btn btn-primary" onclick="app.addComment(${storyId})">Post Comment</button>
                    </div>
                ` : '<p class="login-prompt">Please log in to comment</p>'}
            </div>
        `);
    }

    async addComment(storyId) {
        const content = document.getElementById('newComment')?.value.trim();
        if (!content) {
            this.showNotification('Please enter a comment', 'error');
            return;
        }

        try {
            const response = await fetch(`${this.apiBaseUrl}/stories/${storyId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ content }),
                credentials: 'include'
            });

            if (response.ok) {
                this.showNotification('Comment added successfully!', 'success');
                this.closeModal();
                this.openComments(storyId); // Refresh comments
            } else {
                throw new Error('Failed to add comment');
            }
        } catch (error) {
            console.error('Error adding comment:', error);
            this.showNotification('Failed to add comment', 'error');
        }
    }

    async deleteComment(commentId) {
        if (!confirm('Are you sure you want to delete this comment?')) return;

        try {
            const response = await fetch(`${this.apiBaseUrl}/stories/comments/${commentId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (response.ok) {
                this.showNotification('Comment deleted successfully!', 'success');
                this.closeModal();
            } else {
                throw new Error('Failed to delete comment');
            }
        } catch (error) {
            console.error('Error deleting comment:', error);
            this.showNotification('Failed to delete comment', 'error');
        }
    }

    // Like System
    async toggleLike(storyId) {
        if (!this.currentUser) {
            this.showNotification('Please log in to like stories', 'error');
            return;
        }

        try {
            const response = await fetch(`${this.apiBaseUrl}/stories/${storyId}/like`, {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                this.showNotification('Story liked!', 'success');
                this.loadStories(); // Refresh to update like counts
            } else {
                throw new Error('Failed to like story');
            }
        } catch (error) {
            console.error('Error toggling like:', error);
            this.showNotification('Failed to like story', 'error');
        }
    }

    // Logout - FIXED for session-based auth
    async logout() {
        try {
            // Call backend logout to invalidate session
            await fetch(`${this.apiBaseUrl}/auth/signout`, {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout API call failed:', error);
        } finally {
            // Clear frontend data
            localStorage.removeItem('currentUser');
            this.currentUser = null;

            // Redirect to login page
            window.location.href = 'login.html';
        }
    }

    // Utility Methods
    createModal(content) {
        this.closeModal(); // Close any existing modal

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content">
                <button class="modal-close" onclick="app.closeModal()">&times;</button>
                ${content}
            </div>
        `;

        document.body.appendChild(modal);
        return modal;
    }

    closeModal() {
        const existingModal = document.querySelector('.modal-overlay');
        if (existingModal) {
            existingModal.remove();
        }
    }

    showNotification(message, type = 'info') {
        // Use existing notification system or create simple alert
        if (window.showNotification) {
            window.showNotification(message, type);
        } else {
            // Simple notification fallback
            const notification = document.createElement('div');
            notification.className = `notification ${type}`;
            notification.textContent = message;
            notification.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 12px 20px;
                border-radius: 5px;
                color: white;
                z-index: 10001;
                background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#17a2b8'};
            `;
            document.body.appendChild(notification);
            setTimeout(() => notification.remove(), 3000);
        }
    }

    // Helper Methods
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    formatStoryContent(content) {
        if (!content) return '';
        return content.replace(/\n/g, '<br>');
    }

    getRandomColor() {
        const colors = ['#1a2a6c', '#b21f1f', '#fdbb2d', '#6a11cb', '#2575fc', '#ff6b6b'];
        return colors[Math.floor(Math.random() * colors.length)];
    }

    getStoryIcon(genre) {
        const icons = {
            'FICTION': 'book',
            'NON_FICTION': 'newspaper',
            'FANTASY': 'dragon',
            'SCI_FI': 'robot',
            'MYSTERY': 'search',
            'ROMANCE': 'heart',
            'HORROR': 'ghost'
        };
        return icons[genre] || 'book';
    }

    // Event Listeners and other methods remain the same...
    setupEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(() => {
                this.searchStories(searchInput.value);
            }, 300));
        }
    }

    setupUIInteractions() {
        // Initialize UI interactions
    }

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    async searchStories(query) {
        if (!query.trim()) {
            this.loadStories();
            return;
        }

        try {
            const response = await fetch(`${this.apiBaseUrl}/stories?search=${encodeURIComponent(query)}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                const stories = result.content || result;
                this.displayStories(stories);
            } else {
                throw new Error('Search failed');
            }
        } catch (error) {
            console.error('Search error:', error);
            this.showNotification('Search failed', 'error');
        }
    }
}

// Initialize the application
const app = new InkLinkApp();

// Make app globally available
window.app = app;
window.logout = () => app.logout();