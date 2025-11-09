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
        this.loadStory();
        this.loadReadingSettings();
        this.setupScrollProgress();
    }

    getStoryIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('id') || '1'; // Fallback for demo
    }

    async checkAuthentication() {
        const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true';
        const userData = localStorage.getItem('user');

        if (isAuthenticated && userData) {
            this.currentUser = JSON.parse(userData);
            this.updateAuthUI();
        } else {
            this.updateAuthUI();
        }
    }

    updateAuthUI() {
        const authButtons = document.getElementById('authButtons');

        if (this.currentUser) {
            authButtons.innerHTML = `
                <div class="user-menu">
                    <div class="user-info">
                        <div class="user-avatar">${this.currentUser.username?.charAt(0)?.toUpperCase() || 'U'}</div>
                        <span class="username">${this.currentUser.username}</span>
                    </div>
                </div>
            `;

            // Update comment avatar
            const commentAvatar = document.getElementById('commentUserAvatar');
            if (commentAvatar) {
                commentAvatar.textContent = this.currentUser.username?.charAt(0)?.toUpperCase() || 'U';
            }
        } else {
            authButtons.innerHTML = `
                <div class="auth-buttons">
                    <a href="login.html" class="btn btn-outline">Log In</a>
                    <a href="register.html" class="btn btn-primary">Sign Up</a>
                </div>
            `;

            // Hide comment form for guests
            document.getElementById('addComment').classList.add('hidden');
        }
    }

    setupEventListeners() {
        // Like button
        document.getElementById('likeBtn').addEventListener('click', () => {
            this.toggleLike();
        });

        // Comment scroll button
        document.getElementById('commentScrollBtn').addEventListener('click', () => {
            this.scrollToComments();
        });

        // Share functionality
        document.getElementById('shareBtn').addEventListener('click', (e) => {
            this.toggleShareDropdown(e);
        });

        // Share options
        document.querySelectorAll('.share-option').forEach(option => {
            option.addEventListener('click', (e) => {
                this.handleShare(e.target.closest('.share-option').dataset.platform);
            });
        });

        // Comment submission
        document.getElementById('submitComment').addEventListener('click', () => {
            this.submitComment();
        });

        document.getElementById('cancelComment').addEventListener('click', () => {
            this.clearCommentForm();
        });

        // Comment sort
        document.getElementById('commentsSort').addEventListener('change', (e) => {
            this.sortComments(e.target.value);
        });

        // Load more comments
        document.getElementById('loadMoreComments').addEventListener('click', () => {
            this.loadMoreComments();
        });

        // Follow buttons
        document.getElementById('followBtn').addEventListener('click', () => {
            this.toggleFollow();
        });

        document.getElementById('followBtnLarge').addEventListener('click', () => {
            this.toggleFollow();
        });

        // Reading tools
        document.getElementById('fontSizeBtn').addEventListener('click', () => {
            this.openFontSizeModal();
        });

        document.getElementById('themeBtn').addEventListener('click', () => {
            this.toggleTheme();
        });

        document.getElementById('scrollTopBtn').addEventListener('click', () => {
            this.scrollToTop();
        });

        // Font size modal
        document.getElementById('closeFontModal').addEventListener('click', () => {
            this.closeFontSizeModal();
        });

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
        document.getElementById('commentText').addEventListener('keydown', (e) => {
            if (e.ctrlKey && e.key === 'Enter') {
                this.submitComment();
            }
        });
    }

    async loadStory() {
        try {
            this.showLoadingState();

            // Load story data
            this.story = await api.stories.getById(this.storyId);

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
        document.getElementById('storyTitle').textContent = this.story.title;
        document.getElementById('storyCategory').textContent =
            this.formatCategory(this.story.category);
        document.getElementById('authorName').textContent =
            this.story.author?.username || 'Unknown Author';
        document.getElementById('authorNameBio').textContent =
            this.story.author?.username || 'Unknown Author';
        document.getElementById('storyDate').textContent =
            new Date(this.story.createdAt).toLocaleDateString();

        // Stats
        document.getElementById('viewCount').textContent =
            this.formatNumber(this.story.viewCount || 0);
        document.getElementById('likeCount').textContent =
            this.formatNumber(this.story.likeCount || 0);
        document.getElementById('commentCount').textContent =
            this.formatNumber(this.story.commentCount || 0);
        document.getElementById('likeCountBtn').textContent =
            this.formatNumber(this.story.likeCount || 0);

        // Author avatars
        const avatarText = this.story.author?.username?.charAt(0)?.toUpperCase() || 'U';
        document.getElementById('authorAvatar').textContent = avatarText;
        document.getElementById('authorAvatarLarge').textContent = avatarText;

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

    displayTags() {
        const tagsContainer = document.getElementById('storyTags');

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

        if (!this.story.content) {
            contentContainer.innerHTML = '<p>No content available.</p>';
            return;
        }

        // Simple formatting - you might want to use a markdown parser
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

            const comments = await api.comments.getByStory(this.storyId);
            this.comments = reset ? comments : [...this.comments, ...comments];

            this.displayComments();

        } catch (error) {
            console.error('Error loading comments:', error);
        }
    }

    displayComments() {
        const commentsList = document.getElementById('commentsList');
        const commentsCount = document.getElementById('commentsCount');

        commentsCount.textContent = `(${this.comments.length})`;

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
                            ${comment.author?.username?.charAt(0)?.toUpperCase() || 'U'}
                        </div>
                        <div>
                            <div class="comment-author-name">
                                ${this.escapeHtml(comment.author?.username || 'Unknown User')}
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
                    ${this.currentUser && this.currentUser.id === comment.author?.id ? `
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

            const stories = await api.stories.getByUser(this.story.author.id, 0, 3);
            this.displayRelatedStories(stories);

        } catch (error) {
            console.error('Error loading related stories:', error);
        }
    }

    displayRelatedStories(stories) {
        const container = document.getElementById('relatedStories');

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
                    <span>${this.formatNumber(story.viewCount || 0)} views</span>
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
            const result = await api.likes.toggle(this.storyId);

            // Update UI immediately
            this.updateLikeButton(result.liked);

            // Update counts
            const currentLikes = this.story.likeCount || 0;
            this.story.likeCount = result.liked ? currentLikes + 1 : Math.max(0, currentLikes - 1);
            this.story.liked = result.liked;

            this.updateLikeCounts();

        } catch (error) {
            console.error('Error toggling like:', error);
            this.showNotification('Failed to like story', 'error');
        }
    }

    updateLikeButton(liked = null) {
        const likeBtn = document.getElementById('likeBtn');
        const isLiked = liked !== null ? liked : this.story?.liked;

        if (isLiked) {
            likeBtn.classList.add('liked');
            likeBtn.innerHTML = '<i class="fas fa-heart"></i><span>Liked</span>';
        } else {
            likeBtn.classList.remove('liked');
            likeBtn.innerHTML = '<i class="fas fa-heart"></i><span>Like</span>';
        }

        // Update count
        const countSpan = likeBtn.querySelector('.count');
        if (countSpan) {
            countSpan.textContent = this.formatNumber(this.story?.likeCount || 0);
        }
    }

    updateLikeCounts() {
        document.getElementById('likeCount').textContent = this.formatNumber(this.story.likeCount || 0);
        document.getElementById('likeCountBtn').textContent = this.formatNumber(this.story.likeCount || 0);
    }

    async submitComment() {
        if (!this.currentUser) {
            window.location.href = 'login.html';
            return;
        }

        const commentText = document.getElementById('commentText').value.trim();

        if (!commentText) {
            this.showNotification('Please enter a comment', 'error');
            return;
        }

        try {
            await api.comments.add(this.storyId, commentText);

            this.clearCommentForm();
            this.showNotification('Comment added successfully!', 'success');

            // Reload comments
            await this.loadComments(true);

        } catch (error) {
            console.error('Error adding comment:', error);
            this.showNotification('Failed to add comment', 'error');
        }
    }

    async deleteComment(commentId) {
        if (!confirm('Are you sure you want to delete this comment?')) return;

        try {
            await api.comments.delete(commentId);
            this.showNotification('Comment deleted successfully!', 'success');

            // Remove comment from UI
            const commentElement = document.querySelector(`[data-comment-id="${commentId}"]`);
            if (commentElement) {
                commentElement.remove();
            }

            // Update counts
            this.story.commentCount = Math.max(0, (this.story.commentCount || 1) - 1);
            document.getElementById('commentCount').textContent =
                this.formatNumber(this.story.commentCount);

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

        // Note: Implement follow functionality in your backend
        this.showNotification('Follow functionality coming soon!', 'info');
    }

    async recordView() {
        try {
            await api.analytics.recordView(this.storyId);
        } catch (error) {
            console.error('Error recording view:', error);
        }
    }

    // Reading Tools
    setupScrollProgress() {
        window.addEventListener('scroll', () => {
            const progressBar = document.getElementById('progressBar');
            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight - windowHeight;
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const progress = (scrollTop / documentHeight) * 100;

            progressBar.style.width = `${progress}%`;
        });
    }

    openFontSizeModal() {
        document.getElementById('fontSizeModal').classList.remove('hidden');
    }

    closeFontSizeModal() {
        document.getElementById('fontSizeModal').classList.add('hidden');
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
        dropdown.classList.toggle('show');
        e.stopPropagation();
    }

    closeShareDropdown() {
        document.getElementById('shareDropdown').classList.remove('show');
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
        document.getElementById('commentsSection').scrollIntoView({
            behavior: 'smooth'
        });
    }

    scrollToTop() {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    openStory(storyId) {
        window.location.href = `story.html?id=${storyId}`;
    }

    // Utility Methods
    clearCommentForm() {
        document.getElementById('commentText').value = '';
    }

    calculateReadingTime() {
        const content = this.story?.content || '';
        const words = content.split(/\s+/).length;
        const readingTime = Math.ceil(words / 200); // 200 words per minute

        document.getElementById('readingTime').querySelector('span').textContent =
            `${readingTime} min read`;
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
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    sortComments(sortBy) {
        // Implement comment sorting logic
        console.log('Sorting comments by:', sortBy);
        // You would sort this.comments based on the sortBy parameter
        // and then call this.displayComments()
    }

    loadMoreComments() {
        this.currentCommentPage++;
        this.loadComments(false);
    }

    // UI State Management
    showLoadingState() {
        document.getElementById('storyContent').innerHTML = `
            <div class="content-loading">
                <div class="loading-spinner"></div>
                <p>Loading story content...</p>
            </div>
        `;
    }

    hideLoadingState() {
        // Content will be populated by displayStory()
    }

    showErrorState(message) {
        document.getElementById('storyContent').innerHTML = `
            <div class="content-loading">
                <i class="fas fa-exclamation-triangle" style="font-size: 3rem; color: var(--danger); margin-bottom: 1rem;"></i>
                <h3>Error Loading Story</h3>
                <p>${message}</p>
                <button class="btn btn-primary" onclick="location.reload()">Try Again</button>
            </div>
        `;
    }

    showNotification(message, type = 'info') {
        // Use existing notification system
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
        window.location.reload();
    }
}

// Initialize the story page
const storyPage = new StoryPage();

// Make globally available
window.storyPage = storyPage;