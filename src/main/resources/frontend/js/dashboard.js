// InkLink Dashboard JavaScript - FIXED SESSION VERSION
class Dashboard {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    async init() {
        await this.checkAuthentication();
        this.setupEventListeners();
        this.loadMockData();
    }

    async checkAuthentication() {
        try {
            console.log('Checking session authentication...');

            const response = await fetch('/api/auth/me', {
                method: 'GET',
                credentials: 'include' // CRITICAL: Include session cookies
            });

            if (!response.ok) {
                console.log('Session invalid, redirecting to login');
                window.location.href = 'login.html';
                return;
            }

            const result = await response.json();

            if (result.success && result.data) {
                this.currentUser = result.data;
                this.updateAuthUI();
                console.log('User authenticated via session:', this.currentUser);

                // Optional: Sync with localStorage for display purposes only
                localStorage.setItem('currentUser', JSON.stringify(result.data));
            } else {
                console.log('No valid session found');
                window.location.href = 'login.html';
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            window.location.href = 'login.html';
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

            // Update user info in sidebar
            this.safeUpdateElement('userAvatar', this.currentUser.username?.charAt(0)?.toUpperCase() || 'U');
            this.safeUpdateElement('userName', this.currentUser.username);
            this.safeUpdateElement('userEmail', this.currentUser.email);
            this.safeUpdateElement('welcomeName', this.currentUser.username);
        }
    }

    setupEventListeners() {
        // Tab navigation
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchTab(e.currentTarget.dataset.tab);
            });
        });

        // Logout functionality
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.logout());
        }
    }

    async logout() {
        try {
            await fetch('/api/auth/signout', {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Clear both session and localStorage
            localStorage.removeItem('currentUser');
            localStorage.removeItem('isAuthenticated');
            window.location.href = 'login.html';
        }
    }

    switchTab(tabName) {
        // Update active tab in sidebar
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });

        const tabElement = document.querySelector(`[data-tab="${tabName}"]`);
        if (tabElement) {
            tabElement.classList.add('active');
        }

        // Update active tab content
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });

        const tabContent = document.getElementById(`${tabName}-tab`);
        if (tabContent) {
            tabContent.classList.add('active');
        }
    }

    loadMockData() {
        console.log('Loading mock dashboard data');

        const mockStats = {
            totalViews: 1250,
            totalLikes: 342,
            storyCount: 2,
            followerCount: 87,
            commentCount: 23,
            viewsToday: 45,
            likesToday: 12
        };

        this.updateStats(mockStats);
        this.updateStoriesCount();
        this.updateRecentActivity();
        this.updateTopStories();

        console.log('✅ Dashboard loaded successfully with mock data');
    }

    updateStats(stats) {
        console.log('Updating stats with:', stats);

        // Overview stats
        this.safeUpdateElement('totalViews', this.formatNumber(stats.totalViews || 0));
        this.safeUpdateElement('totalLikes', this.formatNumber(stats.totalLikes || 0));
        this.safeUpdateElement('totalStories', this.formatNumber(stats.storyCount || 0));
        this.safeUpdateElement('totalFollowers', this.formatNumber(stats.followerCount || 0));

        // Quick stats in sidebar
        this.safeUpdateElement('quickViews', this.formatNumber(stats.viewsToday || 0));
        this.safeUpdateElement('quickLikes', this.formatNumber(stats.likesToday || 0));

        // Badge counts
        this.safeUpdateElement('storiesCount', stats.storyCount || 0);
        this.safeUpdateElement('followersCount', stats.followerCount || 0);
        this.safeUpdateElement('commentsCount', stats.commentCount || 0);
    }

    updateStoriesCount() {
        this.safeUpdateElement('storiesCount', '2'); // Mock count
    }

    updateRecentActivity() {
        const container = document.getElementById('recentActivity');
        if (!container) return;

        const activities = [
            {
                text: 'New comment on "The Last Summit"',
                time: '2 hours ago',
                icon: 'fas fa-comment',
                color: '#17a2b8'
            },
            {
                text: 'Your story reached 100 views',
                time: '5 hours ago',
                icon: 'fas fa-chart-line',
                color: '#28a745'
            },
            {
                text: 'New follower: Jane Smith',
                time: '1 day ago',
                icon: 'fas fa-user-plus',
                color: '#fdbb2d'
            }
        ];

        container.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon" style="background: ${activity.color || '#fdbb2d'};">
                    <i class="${activity.icon || 'fas fa-bell'}"></i>
                </div>
                <div class="activity-content">
                    <div class="activity-text">${activity.text}</div>
                    <div class="activity-time">${activity.time}</div>
                </div>
            </div>
        `).join('');
    }

    updateTopStories() {
        const container = document.getElementById('topStories');
        if (!container) return;

        const stories = [
            {
                title: 'The Last Summit',
                viewCount: 2847,
                likeCount: 342,
                category: 'FICTION',
                performance: 'good'
            },
            {
                title: 'Echoes of Tomorrow',
                viewCount: 1521,
                likeCount: 198,
                category: 'SCI_FI',
                performance: 'average'
            }
        ];

        container.innerHTML = stories.map(story => `
            <div class="story-item">
                <div class="story-cover">
                    <i class="fas fa-${this.getStoryIcon(story.category)}"></i>
                </div>
                <div class="story-info">
                    <div class="story-title">${this.escapeHtml(story.title)}</div>
                    <div class="story-stats">
                        <span>${this.formatNumber(story.viewCount || 0)} views</span>
                        <span>${this.formatNumber(story.likeCount || 0)} likes</span>
                    </div>
                </div>
                <div class="story-performance">
                    <span class="performance-badge ${story.performance === 'good' ? 'good' : 'average'}">
                        ${story.performance === 'good' ? 'High' : 'Avg'}
                    </span>
                </div>
            </div>
        `).join('');
    }

    safeUpdateElement(elementId, value) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = value;
        }
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

    getStoryIcon(category) {
        const icons = {
            'FICTION': 'book',
            'NON_FICTION': 'newspaper',
            'POETRY': 'feather-alt',
            'FANTASY': 'dragon',
            'SCI_FI': 'robot',
            'MYSTERY': 'search',
            'ROMANCE': 'heart',
            'HORROR': 'ghost'
        };
        return icons[category] || 'book';
    }
}

// Initialize the dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing dashboard...');
    window.dashboard = new Dashboard();
});