// InkLink Stories Page JavaScript
class StoriesPage {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 12;
        this.totalPages = 0;
        this.filters = {
            search: '',
            category: '',
            sort: 'createdAt,desc',
            timePeriod: '',
            tags: []
        };
        this.viewMode = 'grid';
        this.isLoading = false;
        this.currentUser = null; // Initialize as null
        this.init();
    }

    async init() {
        // Check if authButtons exists before doing authentication
        const authButtons = document.getElementById('authButtons');
        if (authButtons) {
            await this.checkAuthentication();
        }

        this.setupEventListeners();
        this.loadPopularTags();
        this.loadStories();
        this.updateStats();
    }

    async checkAuthentication() {
        try {
            // Check with backend to see if user is actually authenticated
            const response = await fetch('/api/auth/me', {
                method: 'GET',
                credentials: 'include' // Important: include session cookies
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    this.currentUser = result.data;
                    // Optional: store in localStorage for UI purposes only
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
        }

        this.updateAuthUI();
    }

    updateAuthUI() {
        const authButtons = document.getElementById("auth-buttons");

        if (this.currentUser) {
            authButtons.innerHTML = `
            <div class="user-menu">
                <div class="user-info">
                    <div class="user-avatar"></div>
                    <span class="username">${this.currentUser.username}</span>
                </div>
                <div class="user-dropdown">
                    <a href="#profile">Profile</a>
                    <a href="#my-stories">My Stories</a>
                    <a href="#settings">Settings</a>
                    <button class="logout-btn">Logout</button>
                </div>
            </div>
        `;
        } else {
            authButtons.innerHTML = `
            <div class="auth-buttons">
                <a href="login.html">Login</a>
                <a href="register.html">Register</a>
            </div>
        `;
        }
    }

    setupEventListeners() {
        // Search functionality
        const storySearch = document.getElementById('storySearch');
        const clearSearch = document.getElementById('clearSearch');
        const categoryFilter = document.getElementById('categoryFilter');
        const sortBy = document.getElementById('sortBy');
        const timeFilter = document.getElementById('timeFilter');
        const resetFilters = document.getElementById('resetFilters');
        const createStoryBtn = document.getElementById('createStoryBtn');
        const loadMoreBtn = document.getElementById('loadMoreBtn');
        const resetEmptyState = document.getElementById('resetEmptyState');
        const globalSearch = document.getElementById('globalSearch');

        if (storySearch) {
            storySearch.addEventListener('input', this.debounce(() => this.handleSearch(), 300));
        }

        if (clearSearch) {
            clearSearch.addEventListener('click', () => {
                if (storySearch) storySearch.value = '';
                this.filters.search = '';
                this.handleFilterChange();
            });
        }

        if (categoryFilter) {
            categoryFilter.addEventListener('change', (e) => {
                this.filters.category = e.target.value;
                this.handleFilterChange();
            });
        }

        if (sortBy) {
            sortBy.addEventListener('change', (e) => {
                this.filters.sort = e.target.value;
                this.handleFilterChange();
            });
        }

        if (timeFilter) {
            timeFilter.addEventListener('change', (e) => {
                this.filters.timePeriod = e.target.value;
                this.handleFilterChange();
            });
        }

        if (resetFilters) {
            resetFilters.addEventListener('click', () => {
                this.resetFilters();
            });
        }

        if (createStoryBtn) {
            createStoryBtn.addEventListener('click', () => {
                if (this.currentUser) {
                    if (window.app) {
                        window.app.openStoryEditor();
                    } else {
                        window.location.href = 'create-story.html';
                    }
                } else {
                    window.location.href = 'login.html';
                }
            });
        }

        // View mode toggle
        document.querySelectorAll('.view-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.setViewMode(e.target.closest('.view-btn').dataset.view);
            });
        });

        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', () => {
                this.loadMoreStories();
            });
        }

        if (resetEmptyState) {
            resetEmptyState.addEventListener('click', () => {
                this.resetFilters();
            });
        }

        if (globalSearch) {
            globalSearch.addEventListener('input', this.debounce(() => this.handleGlobalSearch(), 300));
        }
    }

    async loadPopularTags() {
        try {
            // Use fetch with credentials instead of api.tags
            const response = await fetch('/api/tags/popular?limit=20', {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const tags = await response.json();
                this.displayTags(tags);
            }
        } catch (error) {
            console.error('Error loading tags:', error);
        }
    }

    displayTags(tags) {
        const tagsCloud = document.getElementById('tagsCloud');
        if (!tagsCloud) return;

        if (!tags || tags.length === 0) {
            tagsCloud.innerHTML = '<p>No tags available</p>';
            return;
        }

        tagsCloud.innerHTML = tags.map(tag => `
            <span class="tag" data-tag="${tag.name}">
                ${tag.name} <span class="tag-count">(${tag.storyCount || 0})</span>
            </span>
        `).join('');

        // Add click listeners to tags
        tagsCloud.querySelectorAll('.tag').forEach(tag => {
            tag.addEventListener('click', (e) => {
                const tagName = e.currentTarget.dataset.tag;
                this.toggleTagFilter(tagName);
            });
        });
    }

    toggleTagFilter(tagName) {
        const index = this.filters.tags.indexOf(tagName);

        if (index > -1) {
            this.filters.tags.splice(index, 1);
        } else {
            this.filters.tags.push(tagName);
        }

        this.handleFilterChange();
    }

    async loadStories(resetPage = true) {
        if (this.isLoading) return;

        this.isLoading = true;
        this.showLoadingState();

        if (resetPage) {
            this.currentPage = 0;
        }

        try {
            // Build query parameters
            const params = new URLSearchParams({
                page: this.currentPage,
                size: this.pageSize,
                sort: this.filters.sort
            });

            if (this.filters.search) params.append('search', this.filters.search);
            if (this.filters.category) params.append('genre', this.filters.category);

            const apiUrl = `/api/stories?${params.toString()}`;
            console.log('🔍 Fetching stories from:', apiUrl);

            const response = await fetch(apiUrl, {
                method: 'GET',
                credentials: 'include'
            });

            console.log('📊 Response status:', response.status, response.statusText);
            console.log('📋 Response headers:', Object.fromEntries(response.headers.entries()));

            // Get raw response text first
            const responseText = await response.text();
            console.log('📏 Raw response length:', responseText.length);

            // Check for common issues
            if (responseText.length === 0) {
                throw new Error('Empty response from server');
            }

            if (responseText.includes('<!DOCTYPE html>') || responseText.includes('<html>')) {
                console.error('❌ Server returned HTML instead of JSON. Possible issues:');
                console.error('   - API endpoint not found');
                console.error('   - Authentication required');
                console.error('   - Server error page');
                console.log('📄 HTML preview:', responseText.substring(0, 500));
                throw new Error('Server returned HTML instead of JSON');
            }

            if (responseText.includes('Whitelabel Error Page')) {
                console.error('❌ Spring Boot error page detected');
                throw new Error('Server error: Whitelabel Error Page');
            }

            // Log the problematic area
            const problemStart = Math.max(0, 203360);
            const problemEnd = Math.min(responseText.length, 203380);
            console.log('🔍 Problematic JSON area:', responseText.substring(problemStart, problemEnd));

            // Check for special characters
            const specialChars = responseText.substring(problemStart, problemEnd).match(/[^\x20-\x7E]/g);
            if (specialChars) {
                console.error('🚫 Special characters found:', specialChars);
            }

            try {
                const stories = JSON.parse(responseText);
                console.log('✅ JSON parsed successfully');
                this.handleStoriesResponse(stories, resetPage);
            } catch (parseError) {
                console.error('❌ JSON Parse Error details:', {
                    message: parseError.message,
                    position: parseError instanceof SyntaxError ? parseError.at : 'unknown'
                });

                // Try to find where the JSON breaks
                this.debugJSON(responseText, 203370);
                throw parseError;
            }

        } catch (error) {
            console.error('💥 Load stories error:', error);
            this.handleLoadError(error);
        } finally {
            this.isLoading = false;
            this.hideLoadingState();
        }
    }

// Add this debug method to your StoriesPage class
    debugJSON(jsonString, problemPosition) {
        console.log('🔍 JSON Debug Analysis:');

        // Show context around the problem
        const start = Math.max(0, problemPosition - 50);
        const end = Math.min(jsonString.length, problemPosition + 50);
        console.log('Context around problem:', jsonString.substring(start, end));

        // Check for common issues
        const context = jsonString.substring(problemPosition - 10, problemPosition + 10);
        console.log('Problem area:', context);

        // Check for unescaped characters
        const unescaped = context.match(/[\\"\\n\\r\\t]/g);
        if (unescaped) {
            console.log('Unescaped characters found:', unescaped);
        }

        // Check if it's a string termination issue
        const before = jsonString.substring(0, problemPosition);
        const openQuotes = (before.match(/"/g) || []).length;
        const closeQuotes = (before.match(/\\"/g) || []).length;
        console.log('Quote balance - open:', openQuotes, 'escaped:', closeQuotes);
    }

    handleStoriesResponse(response, resetPage) {
        console.log('🔍 Raw API response:', response); // Debug log

        // Extract data from the ApiResponse format
        let stories = [];
        let totalPages = 1;

        if (response && response.success) {
            // Handle the ApiResponse format: {success: true, message: "...", data: {...}}
            const responseData = response.data;

            if (responseData && responseData.content && Array.isArray(responseData.content)) {
                // If data is a Page object with content array
                stories = responseData.content;
                totalPages = responseData.totalPages || 1;
                console.log(`✅ Loaded ${stories.length} stories, total pages: ${totalPages}`);
            } else if (Array.isArray(responseData)) {
                // If data is directly an array
                stories = responseData;
                console.log(`✅ Loaded ${stories.length} stories directly`);
            } else if (responseData && Array.isArray(responseData)) {
                // Additional check for array
                stories = responseData;
                console.log(`✅ Loaded ${stories.length} stories from data array`);
            } else {
                // Fallback: try to use data directly or empty array
                stories = responseData || [];
                console.warn('⚠️ Unexpected data format, using fallback:', responseData);
            }
        } else if (Array.isArray(response)) {
            // If response is directly an array (fallback)
            stories = response;
            totalPages = 1;
            console.log(`✅ Loaded ${stories.length} stories from direct array`);
        } else if (response && response.content && Array.isArray(response.content)) {
            // If response is Page format without ApiResponse wrapper
            stories = response.content;
            totalPages = response.totalPages || 1;
            console.log(`✅ Loaded ${stories.length} stories from page content`);
        } else {
            // Handle error response or completely unexpected format
            console.warn('❌ API returned error or unexpected format:', response);
            stories = [];
        }

        // Ensure stories is always an array
        if (!Array.isArray(stories)) {
            console.error('❌ Stories is not an array, converting to empty array:', stories);
            stories = [];
        }

        this.totalPages = totalPages;

        if (resetPage) {
            this.displayStories(stories);
        } else {
            this.appendStories(stories);
        }

        this.updateStoriesTitle();
        this.updateActiveFilters();
        this.toggleLoadMoreButton();

        if (stories.length === 0 && resetPage) {
            this.showEmptyState();
        } else {
            this.hideEmptyState();
        }
    }

    displayStories(stories) {
        const storiesGrid = document.getElementById('storiesGrid');
        if (!storiesGrid) return;

        // Safety check
        if (!stories || !Array.isArray(stories)) {
            console.error('displayStories: stories is not an array:', stories);
            stories = [];
        }

        if (stories.length === 0) {
            storiesGrid.innerHTML = '<div class="no-stories">No stories found</div>';
            return;
        }

        try {
            storiesGrid.innerHTML = stories.map(story => this.createStoryCard(story)).join('');
            this.attachStoryEventListeners();
        } catch (error) {
            console.error('Error displaying stories:', error);
            storiesGrid.innerHTML = '<div class="error-message">Error displaying stories</div>';
        }
    }

    appendStories(stories) {
        const storiesGrid = document.getElementById('storiesGrid');
        if (!storiesGrid) return;

        // Safety check
        if (!stories || !Array.isArray(stories)) {
            console.error('appendStories: stories is not an array:', stories);
            return;
        }

        if (stories.length === 0) {
            return;
        }

        try {
            storiesGrid.innerHTML += stories.map(story => this.createStoryCard(story)).join('');
            this.attachStoryEventListeners();
        } catch (error) {
            console.error('Error appending stories:', error);
        }
    }

    appendStories(stories) {
        const storiesGrid = document.getElementById('storiesGrid');
        if (!storiesGrid) return;

        storiesGrid.innerHTML += stories.map(story => this.createStoryCard(story)).join('');
        this.attachStoryEventListeners();
    }

    createStoryCard(story) {
        // Safety checks
        if (!story) {
            console.warn('createStoryCard: story is null or undefined');
            return '<div class="story-card error">Invalid story data</div>';
        }

        const isLiked = story.liked || false;
        const excerpt = story.content ?
            story.content.substring(0, 150) + (story.content.length > 150 ? '...' : '') :
            'No content available';

        // Safe property access with fallbacks
        const authorName = story.author?.username || 'Unknown Author';
        const authorInitial = authorName.charAt(0).toUpperCase();
        const tags = story.tags || [];
        const displayTags = tags.slice(0, 3);

        return `
        <div class="story-card ${this.viewMode === 'list' ? 'list-view' : ''}" data-story-id="${story.id || ''}">
            <div class="story-image">
                <i class="fas fa-${this.getStoryIcon(story.genre)}"></i>
            </div>
            <div class="story-content">
                <h3 class="story-title">${this.escapeHtml(story.title || 'Untitled')}</h3>
                <div class="story-author">
                    <div class="author-avatar">${authorInitial}</div>
                    <span>${this.escapeHtml(authorName)}</span>
                </div>
                <p class="story-excerpt">${this.escapeHtml(excerpt)}</p>
                <div class="story-tags">
                    ${displayTags.map(tag =>
            `<span class="story-tag">${this.escapeHtml(tag.name || 'tag')}</span>`
        ).join('')}
                </div>
                <div class="story-stats">
                    <span><i class="fas fa-eye"></i> ${story.viewCount || 0}</span>
                    <span><i class="fas fa-heart"></i> ${story.likeCount || 0}</span>
                    <span><i class="fas fa-comment"></i> ${story.commentCount || 0}</span>
                </div>
                <div class="story-actions">
                    <button class="btn btn-outline read-story">Read Story</button>
                    ${this.currentUser ? `
                        <button class="btn btn-icon ${isLiked ? 'liked' : ''}" data-action="like">
                            <i class="fas fa-heart"></i>
                        </button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
    }

    attachStoryEventListeners() {
        // Read story buttons
        document.querySelectorAll('.read-story').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const storyId = e.target.closest('.story-card').dataset.storyId;
                this.readStory(storyId);
            });
        });

        // Like buttons
        document.querySelectorAll('[data-action="like"]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                if (!this.currentUser) {
                    window.location.href = 'login.html';
                    return;
                }

                const storyId = e.target.closest('.story-card').dataset.storyId;
                this.toggleLike(storyId, e.target);
            });
        });
    }

    async readStory(storyId) {
        try {
            if (window.app) {
                await window.app.readStory(storyId);
            } else {
                // Fallback: redirect to story page
                window.location.href = `story.html?id=${storyId}`;
            }
        } catch (error) {
            console.error('Error reading story:', error);
            this.showNotification('Failed to open story', 'error');
        }
    }

    async toggleLike(storyId, likeBtn) {
        try {
            const response = await fetch(`/api/stories/${storyId}/like`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();

                // Update UI immediately for better UX
                if (result.liked) {
                    likeBtn.classList.add('liked');
                } else {
                    likeBtn.classList.remove('liked');
                }

                // Update count in stats
                const stats = likeBtn.closest('.story-card').querySelector('.story-stats');
                const likeCountElem = stats.querySelector('span:nth-child(2)');
                const currentCount = parseInt(likeCountElem.textContent.match(/\d+/)[0]) || 0;
                const newCount = result.liked ? currentCount + 1 : Math.max(0, currentCount - 1);
                likeCountElem.innerHTML = `<i class="fas fa-heart"></i> ${newCount}`;
            } else {
                throw new Error('Failed to toggle like');
            }
        } catch (error) {
            console.error('Error toggling like:', error);
            this.showNotification('Failed to like story', 'error');
        }
    }

    async loadMoreStories() {
        if (this.currentPage >= this.totalPages - 1) return;

        this.currentPage++;
        await this.loadStories(false);
    }

    handleSearch() {
        const storySearch = document.getElementById('storySearch');
        if (storySearch) {
            const searchTerm = storySearch.value.trim();
            this.filters.search = searchTerm;
            this.handleFilterChange();
        }
    }

    handleGlobalSearch() {
        const globalSearch = document.getElementById('globalSearch');
        const storySearch = document.getElementById('storySearch');

        if (globalSearch && storySearch) {
            const searchTerm = globalSearch.value.trim();
            storySearch.value = searchTerm;
            this.filters.search = searchTerm;
            this.handleFilterChange();
        }
    }

    handleFilterChange() {
        this.currentPage = 0;
        this.applyFilters();
        this.loadStories(true);
    }

    applyFilters() {
        // Build query parameters based on filters
        const params = new URLSearchParams({
            page: this.currentPage,
            size: this.pageSize,
            sort: this.filters.sort
        });

        if (this.filters.search) {
            params.append('search', this.filters.search);
        }

        if (this.filters.category) {
            params.append('genre', this.filters.category);
        }

        if (this.filters.timePeriod) {
            params.append('timePeriod', this.filters.timePeriod);
        }

        if (this.filters.tags.length > 0) {
            params.append('tags', this.filters.tags.join(','));
        }

        // Update URL without page reload
        const newUrl = `${window.location.pathname}?${params.toString()}`;
        window.history.replaceState({}, '', newUrl);
    }

    resetFilters() {
        // Reset filter inputs
        const storySearch = document.getElementById('storySearch');
        const categoryFilter = document.getElementById('categoryFilter');
        const sortBy = document.getElementById('sortBy');
        const timeFilter = document.getElementById('timeFilter');

        if (storySearch) storySearch.value = '';
        if (categoryFilter) categoryFilter.value = '';
        if (sortBy) sortBy.value = 'createdAt,desc';
        if (timeFilter) timeFilter.value = '';

        // Reset filter state
        this.filters = {
            search: '',
            category: '',
            sort: 'createdAt,desc',
            timePeriod: '',
            tags: []
        };

        // Reload stories
        this.handleFilterChange();

        // Clear URL parameters
        window.history.replaceState({}, '', window.location.pathname);
    }

    setViewMode(mode) {
        this.viewMode = mode;

        // Update UI
        document.querySelectorAll('.view-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.view === mode);
        });

        const storiesGrid = document.getElementById('storiesGrid');
        if (storiesGrid) {
            storiesGrid.classList.toggle('list-view', mode === 'list');
        }

        // Re-render stories with new view mode
        this.loadStories(true);
    }

    updateStoriesTitle() {
        const title = document.getElementById('storiesTitle');
        if (!title) return;

        let baseTitle = 'Featured Stories';

        if (this.filters.search) {
            baseTitle = `Search: "${this.filters.search}"`;
        } else if (this.filters.category) {
            baseTitle = `${this.filters.category.replace('_', ' ')} Stories`;
        } else if (this.filters.tags.length > 0) {
            baseTitle = `Tag: ${this.filters.tags.join(', ')}`;
        }

        title.textContent = baseTitle;
    }

    updateActiveFilters() {
        const container = document.getElementById('activeFilters');
        if (!container) return;

        const activeFilters = [];

        if (this.filters.search) {
            activeFilters.push(this.createFilterTag('Search', this.filters.search));
        }

        if (this.filters.category) {
            activeFilters.push(this.createFilterTag('Category', this.filters.category));
        }

        if (this.filters.timePeriod) {
            activeFilters.push(this.createFilterTag('Time', this.filters.timePeriod));
        }

        this.filters.tags.forEach(tag => {
            activeFilters.push(this.createFilterTag('Tag', tag));
        });

        container.innerHTML = activeFilters.join('');

        // Add remove functionality
        container.querySelectorAll('.filter-tag').forEach(tag => {
            const removeBtn = tag.querySelector('button');
            const filterType = tag.dataset.type;
            const filterValue = tag.dataset.value;

            removeBtn.addEventListener('click', () => {
                this.removeFilter(filterType, filterValue);
            });
        });
    }

    createFilterTag(type, value) {
        return `
            <div class="filter-tag" data-type="${type.toLowerCase()}" data-value="${value}">
                ${type}: ${value}
                <button><i class="fas fa-times"></i></button>
            </div>
        `;
    }

    removeFilter(type, value) {
        switch (type) {
            case 'search':
                this.filters.search = '';
                const storySearch = document.getElementById('storySearch');
                if (storySearch) storySearch.value = '';
                break;
            case 'category':
                this.filters.category = '';
                const categoryFilter = document.getElementById('categoryFilter');
                if (categoryFilter) categoryFilter.value = '';
                break;
            case 'time':
                this.filters.timePeriod = '';
                const timeFilter = document.getElementById('timeFilter');
                if (timeFilter) timeFilter.value = '';
                break;
            case 'tag':
                this.filters.tags = this.filters.tags.filter(tag => tag !== value);
                break;
        }

        this.handleFilterChange();
    }

    async updateStats() {
        try {
            // In a real app, you'd have API endpoints for these stats
            // For now, we'll use placeholder values
            const stats = {
                totalStories: '12,847',
                totalWriters: '3,452',
                totalLikes: '284,956'
            };

            const totalStories = document.getElementById('totalStories');
            const totalWriters = document.getElementById('totalWriters');
            const totalLikes = document.getElementById('totalLikes');

            if (totalStories) totalStories.textContent = stats.totalStories;
            if (totalWriters) totalWriters.textContent = stats.totalWriters;
            if (totalLikes) totalLikes.textContent = stats.totalLikes;
        } catch (error) {
            console.error('Error loading stats:', error);
        }
    }

    // UI State Management
    showLoadingState() {
        const loadingState = document.getElementById('loadingState');
        const storiesGrid = document.getElementById('storiesGrid');
        const emptyState = document.getElementById('emptyState');

        if (loadingState) loadingState.classList.remove('hidden');
        if (storiesGrid) storiesGrid.classList.add('hidden');
        if (emptyState) emptyState.classList.add('hidden');
    }

    hideLoadingState() {
        const loadingState = document.getElementById('loadingState');
        const storiesGrid = document.getElementById('storiesGrid');

        if (loadingState) loadingState.classList.add('hidden');
        if (storiesGrid) storiesGrid.classList.remove('hidden');
    }

    showEmptyState() {
        const emptyState = document.getElementById('emptyState');
        const storiesGrid = document.getElementById('storiesGrid');
        const loadMoreBtn = document.getElementById('loadMoreBtn');

        if (emptyState) emptyState.classList.remove('hidden');
        if (storiesGrid) storiesGrid.classList.add('hidden');
        if (loadMoreBtn) loadMoreBtn.classList.add('hidden');
    }

    hideEmptyState() {
        const emptyState = document.getElementById('emptyState');
        const storiesGrid = document.getElementById('storiesGrid');

        if (emptyState) emptyState.classList.add('hidden');
        if (storiesGrid) storiesGrid.classList.remove('hidden');
    }

    toggleLoadMoreButton() {
        const loadMoreBtn = document.getElementById('loadMoreBtn');
        if (loadMoreBtn) {
            if (this.currentPage >= this.totalPages - 1) {
                loadMoreBtn.classList.add('hidden');
            } else {
                loadMoreBtn.classList.remove('hidden');
            }
        }
    }

    handleLoadError(error) {
        console.error('Error loading stories:', error);

        // More detailed error information
        if (error.name === 'SyntaxError') {
            this.showNotification('Server returned invalid data. Please try again.', 'error');
        } else if (error.message.includes('HTTP')) {
            this.showNotification('Server error. Please try again later.', 'error');
        } else {
            this.showNotification('Failed to load stories. Please try again.', 'error');
        }

        this.showEmptyState();
    }

    async logout() {
        try {
            // Call backend logout endpoint to invalidate session
            await fetch('/api/auth/signout', {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Clear frontend state
            this.currentUser = null;
            localStorage.removeItem('currentUser');

            // Reload the page to reset everything
            window.location.reload();
        }
    }

    // Utility Methods
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    getStoryIcon(category) {
        const icons = {
            'FICTION': 'book',
            'NON_FICTION': 'newspaper',
            'FANTASY': 'dragon',
            'SCI_FI': 'robot',
            'MYSTERY': 'search',
            'ROMANCE': 'heart',
            'HORROR': 'ghost'
        };
        return icons[category] || 'book';
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

    showNotification(message, type = 'info') {
        // Use the notification system from main.js or auth.js
        if (window.app) {
            window.app.showNotification(message, type);
        } else if (window.authManager) {
            window.authManager.showNotification(message, type);
        } else {
            alert(message); // Fallback
        }
    }
}

// Initialize the stories page
const storiesPage = new StoriesPage();

// Make globally available
window.storiesPage = storiesPage;