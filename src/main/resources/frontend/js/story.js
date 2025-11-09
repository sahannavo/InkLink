// InkLink Story Reading Page JavaScript
class StoryPage {
    constructor() {
        this.storyId = this.getStoryIdFromURL();
        this.currentUser = null;
        this.story = null;
        this.comments = [];
        this.currentCommentPage = 0;
        this.commentPageSize = 10;
        this.fontSize = 'medium';
        this.theme = 'light';
        this.init();
    }

    async init() {
        await this.checkAuthentication();
        this.setupEventListeners();
        this.loadPopularTags();
        this.loadStories();
        this.updateStats();

        // Only setup auth UI if the element exists
        if (document.getElementById('authButtons')) {
            await this.checkAuthentication();
        }
    }

    getStoryIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('id') || '1';
    }

    async checkAuthentication() {
        try {
            const response = await fetch('/api/auth/me', {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    this.currentUser = result.data;
                    localStorage.setItem('currentUser', JSON.stringify(result.data));
                } else {
                    this.currentUser = null;
                    localStorage.removeItem('currentUser');
                }
            } else {
                this.currentUser = null;
                localStorage.removeItem('currentUser');
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            this.currentUser = null;
            localStorage.removeItem('currentUser');
        } finally {
            // Always call updateAuthUI, but it will handle null elements
            this.updateAuthUI();
        }
    }

    updateAuthUI() {
        const authButtons = document.getElementById('authButtons');

        // Add null check
        if (!authButtons) {
            console.warn('authButtons element not found on this page');
            return;
        }

        if (this.currentUser) {
            authButtons.innerHTML = `
            <div class="user-menu">
                <div class="user-info">
                    <div class="user-avatar">${this.currentUser.username?.charAt(0)?.toUpperCase() || 'U'}</div>
                    <span class="username">${this.currentUser.username}</span>
                </div>
                <div class="user-dropdown">
                    <a href="#profile" class="dropdown-item">Profile</a>
                    <a href="#my-stories" class="dropdown-item">My Stories</a>
                    <a href="#settings" class="dropdown-item">Settings</a>
                    <button class="dropdown-item logout-btn" onclick="storiesPage.logout()">Logout</button>
                </div>
            </div>
        `;
        } else {
            authButtons.innerHTML = `
            <div class="auth-buttons">
                <a href="login.html" class="btn btn-outline">Log In</a>
                <a href="register.html" class="btn btn-primary">Sign Up</a>
            </div>
        `;
        }
    }

    setupEventListeners() {
        // Like button
        const likeBtn = document.getElementById('likeBtn');
        if (likeBtn) {
            likeBtn.addEventListener('click', () => {
                this.toggleLike();
            });
        }

        // Comment scroll button
        const commentScrollBtn = document.getElementById('commentScrollBtn');
        if (commentScrollBtn) {
            commentScrollBtn.addEventListener('click', () => {
                this.scrollToComments();
            });
        }

        // Share functionality
        const shareBtn = document.getElementById('shareBtn');
        if (shareBtn) {
            shareBtn.addEventListener('click', (e) => {
                this.toggleShareDropdown(e);
            });
        }

        // Share options
        document.querySelectorAll('.share-option').forEach(option => {
            option.addEventListener('click', (e) => {
                this.handleShare(e.target.closest('.share-option').dataset.platform);
            });
        });

        // Comment submission
        const submitComment = document.getElementById('submitComment');
        if (submitComment) {
            submitComment.addEventListener('click', () => {
                this.submitComment();
            });
        }

        const cancelComment = document.getElementById('cancelComment');
        if (cancelComment) {
            cancelComment.addEventListener('click', () => {
                this.clearCommentForm();
            });
        }

        // Comment sort
        const commentsSort = document.getElementById('commentsSort');
        if (commentsSort) {
            commentsSort.addEventListener('change', (e) => {
                this.sortComments(e.target.value);
            });
        }

        // Load more comments
        const loadMoreComments = document.getElementById('loadMoreComments');
        if (loadMoreComments) {
            loadMoreComments.addEventListener('click', () => {
                this.loadMoreComments();
            });
        }

        // Follow buttons
        const followBtn = document.getElementById('followBtn');
        if (followBtn) {
            followBtn.addEventListener('click', () => {
                this.toggleFollow();
            });
        }

        const followBtnLarge = document.getElementById('followBtnLarge');
        if (followBtnLarge) {
            followBtnLarge.addEventListener('click', () => {
                this.toggleFollow();
            });
        }

        // Reading tools
        const fontSizeBtn = document.getElementById('fontSizeBtn');
        if (fontSizeBtn) {
            fontSizeBtn.addEventListener('click', () => {
                this.openFontSizeModal();
            });
        }

        const themeBtn = document.getElementById('themeBtn');
        if (themeBtn) {
            themeBtn.addEventListener('click', () => {
                this.toggleTheme();
            });
        }

        const scrollTopBtn = document.getElementById('scrollTopBtn');
        if (scrollTopBtn) {
            scrollTopBtn.addEventListener('click', () => {
                this.scrollToTop();
            });
        }

        // Font size modal
        const closeFontModal = document.getElementById('closeFontModal');
        if (closeFontModal) {
            closeFontModal.addEventListener('click', () => {
                this.closeFontSizeModal();
            });
        }

        document.querySelectorAll('.font-option').forEach(option => {
            option.addEventListener('click', (e) => {
                this.setFontSize(e.target.dataset.size);
            });
        });

        document.querySelectorAll('.theme-option').forEach(option => {
            option.addEventListener('click', (e) => {
                this.setTheme(e.target.dataset.theme);
            });
        });

        // Close share dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.social-share')) {
                this.closeShareDropdown();
            }
        });

        // Enter key for comment submission
        const commentText = document.getElementById('commentText');
        if (commentText) {
            commentText.addEventListener('keydown', (e) => {
                if (e.ctrlKey && e.key === 'Enter') {
                    this.submitComment();
                }
            });
        }
    }

    async loadStory() {
        try {
            this.showLoadingState();

            // Load story data
            const response = await fetch(`/api/stories/${this.storyId}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    this.story = result.data;
                } else {
                    throw new Error(result.message || 'Failed to load story');
                }
            } else {
                throw new Error(`HTTP ${response.status}: Failed to load story`);
            }

            // Load comments
            await this.loadComments();

            // Load author's other stories
            await this.loadRelatedStories();

            // Update view count
            await this.recordView();

            this.displayStory();
            this.hideLoadingState();

        } catch (error) {
            console.error('Error loading story:', error);
            this.showErrorState('Failed to load story');
        }
    }

    displayStory() {
        if (!this.story) return;

        // Basic story info
        this.setElementText('storyTitle', this.story.title);
        this.setElementText('storyCategory', this.formatCategory(this.story.genre));
        this.setElementText('authorName', this.story.author?.username || 'Unknown Author');
        this.setElementText('authorNameBio', this.story.author?.username || 'Unknown Author');
        this.setElementText('storyDate', new Date(this.story.createdAt).toLocaleDateString());

        // Stats
        this.setElementText('viewCount', this.formatNumber(this.story.readCount || 0));
        this.setElementText('likeCount', this.formatNumber(this.story.likeCount || 0));
        this.setElementText('commentCount', this.formatNumber(this.comments.length || 0));
        this.setElementText('likeCountBtn', this.formatNumber(this.story.likeCount || 0));

        // Author avatars
        const avatarText = this.story.author?.username?.charAt(0)?.toUpperCase() || 'U';
        this.setElementText('authorAvatar', avatarText);
        this.setElementText('authorAvatarLarge', avatarText);

        // Tags
        this.displayTags();

        // Content
        this.displayContent();

        // Like button state
        this.updateLikeButton();

        // Reading time
        this.calculateReadingTime();

        // Update page title
        document.title = `${this.story.title} - InkLink`;
    }

    setElementText(id, text) {
        const element = document.getElementById(id);
        if (element) element.textContent = text;
    }

    displayTags() {
        const tagsContainer = document.getElementById('storyTags');
        if (!tagsContainer) return;

        if (!this.story.tags || this.story.tags.length === 0) {
            tagsContainer.innerHTML = '<span class="story-tag">No tags</span>';
            return;
        }

        tagsContainer.innerHTML = this.story.tags.map(tag =>
            `<span class="story-tag">${this.escapeHtml(tag.name)}</span>`
        ).join('');
    }

    displayContent() {
        const contentContainer = document.getElementById('storyContent');
        if (!contentContainer) return;

        if (!this.story.content) {
            contentContainer.innerHTML = '<p>No content available.</p>';
            return;
        }

        const formattedContent = this.story.content
            .split('\n')
            .map(paragraph => {
                if (paragraph.trim() === '') return '<br>';
                return `<p>${this.escapeHtml(paragraph)}</p>`;
            })
            .join('');

        contentContainer.innerHTML = formattedContent;
    }

    async loadComments(reset = true) {
        try {
            if (reset) {
                this.currentCommentPage = 0;
                this.comments = [];
            }

            const response = await fetch(`/api/stories/${this.storyId}/comments`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();

                // Handle different response formats
                let commentsData = [];
                if (result.success && result.data) {
                    commentsData = result.data; // ApiResponse format
                } else if (Array.isArray(result)) {
                    commentsData = result; // Direct array format
                } else if (result.content && Array.isArray(result.content)) {
                    commentsData = result.content; // Page format
                }

                this.comments = reset ? commentsData : [...this.comments, ...commentsData];
                this.displayComments();
            } else {
                throw new Error('Failed to load comments');
            }

        } catch (error) {
            console.error('Error loading comments:', error);
            this.comments = [];
            this.displayComments();
        }
    }

    displayComments() {
        const commentsList = document.getElementById('commentsList');
        const commentsCount = document.getElementById('commentsCount');

        if (commentsCount) {
            commentsCount.textContent = `(${this.comments.length})`;
        }

        if (!commentsList) return;

        // Safety check - ensure comments is an array
        if (!this.comments || !Array.isArray(this.comments)) {
            console.warn('Comments is not an array, converting:', this.comments);
            this.comments = [];
        }

        if (this.comments.length === 0) {
            commentsList.innerHTML = `
                <div class="comment">
                    <p style="text-align: center; color: var(--muted-color);">
                        No comments yet. Be the first to comment!
                    </p>
                </div>
            `;
            return;
        }

        commentsList.innerHTML = this.comments.map(comment => `
            <div class="comment" data-comment-id="${comment.id}">
                <div class="comment-header">
                    <div class="comment-author">
                        <div class="comment-avatar">
                            ${comment.user?.username?.charAt(0)?.toUpperCase() ||
        comment.author?.username?.charAt(0)?.toUpperCase() || 'U'}
                        </div>
                        <div>
                            <div class="comment-author-name">
                                ${this.escapeHtml(comment.user?.username ||
            comment.author?.username || 'Unknown User')}
                            </div>
                            <div class="comment-date">
                                ${new Date(comment.createdAt).toLocaleDateString()}
                            </div>
                        </div>
                    </div>
                </div>
                <div class="comment-content">
                    ${this.escapeHtml(comment.content)}
                </div>
                <div class="comment-actions">
                    <button class="comment-like" data-comment-id="${comment.id}">
                        <i class="fas fa-heart"></i>
                        <span>${comment.likeCount || 0}</span>
                    </button>
                    ${this.currentUser && this.currentUser.id === (comment.user?.id || comment.author?.id) ? `
                        <button class="btn btn-outline btn-sm delete-comment" 
                                onclick="storyPage.deleteComment(${comment.id})">
                            Delete
                        </button>
                    ` : ''}
                </div>
            </div>
        `).join('');

        // Add like functionality to comment likes
        commentsList.querySelectorAll('.comment-like').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentId = e.currentTarget.dataset.commentId;
                this.toggleCommentLike(commentId, e.currentTarget);
            });
        });
    }

    async loadRelatedStories() {
        try {
            if (!this.story.author?.id) return;

            const response = await fetch(`/api/stories/user/${this.story.author.id}?page=0&size=3`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                let stories = [];

                if (result.success && result.data) {
                    stories = result.data.content || result.data || [];
                } else if (Array.isArray(result)) {
                    stories = result;
                }

                this.displayRelatedStories(stories);
            }

        } catch (error) {
            console.error('Error loading related stories:', error);
        }
    }

    displayRelatedStories(stories) {
        const container = document.getElementById('relatedStories');
        if (!container) return;

        if (!stories || stories.length === 0) {
            container.innerHTML = '<p>No other stories from this author.</p>';
            return;
        }

        // Filter out the current story
        const relatedStories = stories.filter(story => story.id !== this.story.id).slice(0, 3);

        if (relatedStories.length === 0) {
            container.innerHTML = '<p>No other stories from this author.</p>';
            return;
        }

        container.innerHTML = relatedStories.map(story => `
            <div class="related-story-card" onclick="storyPage.openStory(${story.id})">
                <h3 class="related-story-title">${this.escapeHtml(story.title)}</h3>
                <div class="related-story-meta">
                    <span>${this.formatNumber(story.readCount || 0)} views</span>
                    <span>${this.formatNumber(story.likeCount || 0)} likes</span>
                </div>
            </div>
        `).join('');
    }

    async toggleLike() {
        if (!this.currentUser) {
            window.location.href = 'login.html';
            return;
        }

        try {
            const response = await fetch(`/api/stories/${this.storyId}/like`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();

                // Update UI immediately
                this.updateLikeButton(result.liked);

                // Update counts
                const currentLikes = this.story.likeCount || 0;
                this.story.likeCount = result.liked ? currentLikes + 1 : Math.max(0, currentLikes - 1);
                this.story.liked = result.liked;

                this.updateLikeCounts();

            } else {
                throw new Error('Failed to toggle like');
            }

        } catch (error) {
            console.error('Error toggling like:', error);
            this.showNotification('Failed to like story', 'error');
        }
    }

    updateLikeButton(liked = null) {
        const likeBtn = document.getElementById('likeBtn');
        if (!likeBtn) return;

        const isLiked = liked !== null ? liked : this.story?.liked;

        if (isLiked) {
            likeBtn.classList.add('liked');
            likeBtn.innerHTML = '<i class="fas fa-heart"></i><span>Liked</span>';
        } else {
            likeBtn.classList.remove('liked');
            likeBtn.innerHTML = '<i class="fas fa-heart"></i><span>Like</span>';
        }
    }

    updateLikeCounts() {
        this.setElementText('likeCount', this.formatNumber(this.story.likeCount || 0));
        this.setElementText('likeCountBtn', this.formatNumber(this.story.likeCount || 0));
    }

    async submitComment() {
        if (!this.currentUser) {
            window.location.href = 'login.html';
            return;
        }

        const commentText = document.getElementById('commentText');
        if (!commentText) return;

        const content = commentText.value.trim();

        if (!content) {
            this.showNotification('Please enter a comment', 'error');
            return;
        }

        try {
            const response = await fetch(`/api/stories/${this.storyId}/comments`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ content })
            });

            if (response.ok) {
                this.clearCommentForm();
                this.showNotification('Comment added successfully!', 'success');

                // Reload comments
                await this.loadComments(true);

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
            const response = await fetch(`/api/comments/${commentId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (response.ok) {
                this.showNotification('Comment deleted successfully!', 'success');

                // Remove comment from UI
                const commentElement = document.querySelector(`[data-comment-id="${commentId}"]`);
                if (commentElement) {
                    commentElement.remove();
                }

                // Update local comments array
                this.comments = this.comments.filter(comment => comment.id !== commentId);
                this.displayComments();

            } else {
                throw new Error('Failed to delete comment');
            }

        } catch (error) {
            console.error('Error deleting comment:', error);
            this.showNotification('Failed to delete comment', 'error');
        }
    }

    async toggleCommentLike(commentId, likeBtn) {
        if (!this.currentUser) {
            window.location.href = 'login.html';
            return;
        }

        try {
            // Note: You might need to implement comment likes in your backend
            // For now, we'll just toggle the UI state
            const isLiked = likeBtn.classList.contains('liked');

            if (isLiked) {
                likeBtn.classList.remove('liked');
                likeBtn.querySelector('i').className = 'fas fa-heart';
            } else {
                likeBtn.classList.add('liked');
                likeBtn.querySelector('i').className = 'fas fa-heart';
                likeBtn.querySelector('i').style.fontWeight = '900';
            }

            // Update count
            const countSpan = likeBtn.querySelector('span');
            const currentCount = parseInt(countSpan.textContent) || 0;
            countSpan.textContent = isLiked ? Math.max(0, currentCount - 1) : currentCount + 1;

        } catch (error) {
            console.error('Error toggling comment like:', error);
        }
    }

    async toggleFollow() {
        if (!this.currentUser) {
            window.location.href = 'login.html';
            return;
        }

        this.showNotification('Follow functionality coming soon!', 'info');
    }

    async recordView() {
        try {
            // Simple view recording - you might want to implement this in your backend
            console.log('View recorded for story:', this.storyId);

            // If you implement a view endpoint later:
            // await fetch(`/api/stories/${this.storyId}/view`, {
            //     method: 'POST',
            //     credentials: 'include'
            // });

        } catch (error) {
            console.error('Error recording view:', error);
            // Silently fail - view counting is not critical
        }
    }

    // Reading Tools
    setupScrollProgress() {
        window.addEventListener('scroll', () => {
            const progressBar = document.getElementById('progressBar');
            if (!progressBar) return;

            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight - windowHeight;
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const progress = (scrollTop / documentHeight) * 100;

            progressBar.style.width = `${progress}%`;
        });
    }

    openFontSizeModal() {
        const modal = document.getElementById('fontSizeModal');
        if (modal) modal.classList.remove('hidden');
    }

    closeFontSizeModal() {
        const modal = document.getElementById('fontSizeModal');
        if (modal) modal.classList.add('hidden');
    }

    setFontSize(size) {
        this.fontSize = size;
        document.body.className = document.body.className.replace(/\bfont-\w+/g, '');
        document.body.classList.add(`font-${size}`);

        // Update active state in modal
        document.querySelectorAll('.font-option').forEach(option => {
            option.classList.toggle('active', option.dataset.size === size);
        });

        this.saveReadingSettings();
    }

    setTheme(theme) {
        this.theme = theme;
        document.body.setAttribute('data-theme', theme);

        // Update active state in modal
        document.querySelectorAll('.theme-option').forEach(option => {
            option.classList.toggle('active', option.dataset.theme === theme);
        });

        this.saveReadingSettings();
    }

    toggleTheme() {
        const themes = ['light', 'sepia', 'dark'];
        const currentIndex = themes.indexOf(this.theme);
        const nextIndex = (currentIndex + 1) % themes.length;
        this.setTheme(themes[nextIndex]);
    }

    loadReadingSettings() {
        const settings = JSON.parse(localStorage.getItem('readingSettings') || '{}');

        if (settings.fontSize) {
            this.setFontSize(settings.fontSize);
        }

        if (settings.theme) {
            this.setTheme(settings.theme);
        }
    }

    saveReadingSettings() {
        const settings = {
            fontSize: this.fontSize,
            theme: this.theme
        };
        localStorage.setItem('readingSettings', JSON.stringify(settings));
    }

    // Share functionality
    toggleShareDropdown(e) {
        const dropdown = document.getElementById('shareDropdown');
        if (dropdown) {
            dropdown.classList.toggle('show');
        }
        if (e) e.stopPropagation();
    }

    closeShareDropdown() {
        const dropdown = document.getElementById('shareDropdown');
        if (dropdown) dropdown.classList.remove('show');
    }

    handleShare(platform) {
        const url = window.location.href;
        const title = this.story?.title || 'Check out this story on InkLink';

        let shareUrl;

        switch (platform) {
            case 'twitter':
                shareUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(title)}&url=${encodeURIComponent(url)}`;
                break;
            case 'facebook':
                shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
                break;
            case 'linkedin':
                shareUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`;
                break;
            case 'copy':
                navigator.clipboard.writeText(url).then(() => {
                    this.showNotification('Link copied to clipboard!', 'success');
                });
                this.closeShareDropdown();
                return;
        }

        window.open(shareUrl, '_blank', 'width=600,height=400');
        this.closeShareDropdown();
    }

    // Navigation
    scrollToComments() {
        const commentsSection = document.getElementById('commentsSection');
        if (commentsSection) {
            commentsSection.scrollIntoView({
                behavior: 'smooth'
            });
        }
    }

    scrollToTop() {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    openStory(storyId) {
        window.location.href = `story.html?id=${storyId}`;
    }

    // Utility Methods
    clearCommentForm() {
        const commentText = document.getElementById('commentText');
        if (commentText) commentText.value = '';
    }

    calculateReadingTime() {
        const content = this.story?.content || '';
        const words = content.split(/\s+/).length;
        const readingTime = Math.ceil(words / 200); // 200 words per minute

        const readingTimeElement = document.getElementById('readingTime');
        if (readingTimeElement) {
            const span = readingTimeElement.querySelector('span');
            if (span) span.textContent = `${readingTime} min read`;
        }
    }

    formatCategory(category) {
        return category ? category.replace('_', ' ') : 'Uncategorized';
    }

    formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    sortComments(sortBy) {
        console.log('Sorting comments by:', sortBy);
        // Implement comment sorting logic here
    }

    loadMoreComments() {
        this.currentCommentPage++;
        this.loadComments(false);
    }

    // UI State Management
    showLoadingState() {
        const storyContent = document.getElementById('storyContent');
        if (storyContent) {
            storyContent.innerHTML = `
                <div class="content-loading">
                    <div class="loading-spinner"></div>
                    <p>Loading story content...</p>
                </div>
            `;
        }
    }

    hideLoadingState() {
        // Content will be populated by displayStory()
    }

    showErrorState(message) {
        const storyContent = document.getElementById('storyContent');
        if (storyContent) {
            storyContent.innerHTML = `
                <div class="content-loading">
                    <i class="fas fa-exclamation-triangle" style="font-size: 3rem; color: var(--danger); margin-bottom: 1rem;"></i>
                    <h3>Error Loading Story</h3>
                    <p>${message}</p>
                    <button class="btn btn-primary" onclick="location.reload()">Try Again</button>
                </div>
            `;
        }
    }

    showNotification(message, type = 'info') {
        if (window.app) {
            window.app.showNotification(message, type);
        } else if (window.authManager) {
            window.authManager.showNotification(message, type);
        } else {
            alert(message);
        }
    }

    logout() {
        localStorage.removeItem('isAuthenticated');
        localStorage.removeItem('user');
        localStorage.removeItem('currentUser');
        window.location.reload();
    }
}

// Initialize the story page
const storyPage = new StoryPage();

// Make globally available
window.storyPage = storyPage;