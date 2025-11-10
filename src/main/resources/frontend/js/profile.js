// InkLink Profile Page JavaScript
class ProfilePage {
    constructor() {
        this.currentUser = null;
        this.profileUser = null;
        this.userId = this.getUserIdFromURL();
        this.currentTab = 'stories';
        this.stories = [];
        this.followers = [];
        this.following = [];
        this.isOwnProfile = false;
        this.init();
    }

    async init() {
        await this.checkAuthentication();
        await this.loadProfileData();
        this.setupEventListeners();
        this.setupTabNavigation();
    }

    getUserIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('id') || 'current';
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
                    this.updateAuthUI();
                } else {
                    window.location.href = 'login.html';
                }
            } else {
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
                        <span class="username">${this.currentUser.username}</span>
                    </div>
                    <div class="user-dropdown">
                        <button class="dropdown-item" onclick="window.location.href='profile.html?id=${this.currentUser.id}'">View Profile</button>
                        <button class="dropdown-item" onclick="window.location.href='dashboard.html'">Dashboard</button>
                        <button class="dropdown-item logout-btn" onclick="profilePage.logout()">Logout</button>
                    </div>
                </div>
            `;
        }
    }

    async loadProfileData() {
        try {
            this.showLoadingState();

            // Determine if we're viewing own profile or another user's
            const userId = parseInt(this.userId);
            const currentUserId = this.currentUser?.id;

            if (this.userId === 'current' || userId === currentUserId) {
                this.profileUser = this.currentUser;
                this.isOwnProfile = true;
            } else {
                // Fetch other user's profile from API
                const response = await fetch(`/api/users/${userId}`, {
                    method: 'GET',
                    credentials: 'include'
                });
                
                if (response.ok) {
                    const result = await response.json();
                    if (result.success && result.data) {
                        this.profileUser = result.data;
                        this.isOwnProfile = false;
                    } else {
                        throw new Error('User not found');
                    }
                } else {
                    throw new Error('Failed to load user profile');
                }
            }

            if (!this.profileUser) {
                this.showErrorState('User not found');
                return;
            }

            this.updateProfileUI();
            await this.loadStories();
            await this.loadFollowers();
            await this.loadProfileStats();

            this.hideLoadingState();

        } catch (error) {
            console.error('Error loading profile data:', error);
            this.showErrorState('Failed to load profile');
        }
    }

    updateProfileUI() {
        // Update profile information
        document.getElementById('profileName').textContent =
            this.profileUser.displayName || this.profileUser.username;
        document.getElementById('profileUsername').textContent = `@${this.profileUser.username}`;
        document.getElementById('profileBio').textContent =
            this.profileUser.bio || 'This user hasn\'t written a bio yet.';
        document.getElementById('profileLocation').textContent =
            this.profileUser.location || 'Not specified';
        document.getElementById('profileWebsite').textContent =
            this.profileUser.website || 'No website';
        document.getElementById('profileWebsite').href = this.profileUser.website || '#';
        document.getElementById('joinDate').textContent =
            new Date(this.profileUser.createdAt).toLocaleDateString();

        // Update avatar
        document.getElementById('profileAvatar').textContent =
            this.profileUser.username?.charAt(0)?.toUpperCase() || 'U';

        // Update action buttons based on profile ownership
        this.updateActionButtons();

        // Update page title
        document.title = `${this.profileUser.displayName || this.profileUser.username} - InkLink`;
    }

    updateActionButtons() {
        const followBtn = document.getElementById('followBtn');
        const messageBtn = document.getElementById('messageBtn');
        const moreActionsBtn = document.getElementById('moreActionsBtn');
        const editBackgroundBtn = document.getElementById('editBackgroundBtn');
        const editAvatarBtn = document.getElementById('editAvatarBtn');

        if (this.isOwnProfile) {
            // Own profile - show edit options
            followBtn.style.display = 'none';
            messageBtn.style.display = 'none';
            moreActionsBtn.innerHTML = '<i class="fas fa-edit"></i>';
            moreActionsBtn.title = 'Edit Profile';
            editBackgroundBtn.style.display = 'block';
            editAvatarBtn.style.display = 'block';

            // Update dropdown menu for own profile
            const moreActionsMenu = document.getElementById('moreActionsMenu');
            moreActionsMenu.innerHTML = `
                <button class="dropdown-item" id="editProfileBtn">
                    <i class="fas fa-edit"></i>
                    Edit Profile
                </button>
                <button class="dropdown-item" id="viewDashboardBtn">
                    <i class="fas fa-tachometer-alt"></i>
                    View Dashboard
                </button>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item" id="shareProfileBtn">
                    <i class="fas fa-share-alt"></i>
                    Share Profile
                </button>
            `;
        } else {
            // Other user's profile - show follow/message options
            followBtn.style.display = 'block';
            messageBtn.style.display = 'block';
            moreActionsBtn.innerHTML = '<i class="fas fa-ellipsis-h"></i>';
            editBackgroundBtn.style.display = 'none';
            editAvatarBtn.style.display = 'none';

            // Check if already following
            this.updateFollowButton();

            // Update dropdown menu for other users
            const moreActionsMenu = document.getElementById('moreActionsMenu');
            moreActionsMenu.innerHTML = `
                <button class="dropdown-item" id="shareProfileBtn">
                    <i class="fas fa-share-alt"></i>
                    Share Profile
                </button>
                <button class="dropdown-item" id="reportUserBtn">
                    <i class="fas fa-flag"></i>
                    Report User
                </button>
            `;
        }
    }

    async updateFollowButton() {
        if (this.isOwnProfile) return;

        const followBtn = document.getElementById('followBtn');

        try {
            // Check if current user is following this profile
            const isFollowing = await this.checkIfFollowing();

            if (isFollowing) {
                followBtn.innerHTML = '<i class="fas fa-check"></i> Following';
                followBtn.classList.add('btn-outline');
                followBtn.classList.remove('btn-primary');
            } else {
                followBtn.innerHTML = '<i class="fas fa-plus"></i> Follow';
                followBtn.classList.add('btn-primary');
                followBtn.classList.remove('btn-outline');
            }
        } catch (error) {
            console.error('Error checking follow status:', error);
        }
    }

    async checkIfFollowing() {
        // Implement follow check logic
        // This would typically call an API endpoint
        return false; // Mock response
    }

    setupEventListeners() {
        // Follow button
        document.getElementById('followBtn').addEventListener('click', () => {
            this.toggleFollow();
        });

        // Message button
        document.getElementById('messageBtn').addEventListener('click', () => {
            this.startConversation();
        });

        // More actions button
        document.getElementById('moreActionsBtn').addEventListener('click', (e) => {
            this.toggleMoreActions(e);
        });

        // Share profile
        document.addEventListener('click', (e) => {
            if (e.target.closest('#shareProfileBtn')) {
                this.shareProfile();
            }
        });

        // Edit profile (own profile only)
        document.addEventListener('click', (e) => {
            if (e.target.closest('#editProfileBtn')) {
                this.openEditProfileModal();
            }
        });

        // View dashboard (own profile only)
        document.addEventListener('click', (e) => {
            if (e.target.closest('#viewDashboardBtn')) {
                window.location.href = 'dashboard.html';
            }
        });

        // Report user (other profiles only)
        document.addEventListener('click', (e) => {
            if (e.target.closest('#reportUserBtn')) {
                this.reportUser();
            }
        });

        // Edit background
        document.getElementById('editBackgroundBtn').addEventListener('click', () => {
            this.editBackground();
        });

        // Edit avatar
        document.getElementById('editAvatarBtn').addEventListener('click', () => {
            this.openAvatarModal();
        });

        // Close modals
        document.getElementById('closeEditModal').addEventListener('click', () => {
            this.closeEditProfileModal();
        });

        document.getElementById('closeAvatarModal').addEventListener('click', () => {
            this.closeAvatarModal();
        });

        // Cancel edit
        document.getElementById('cancelEdit').addEventListener('click', () => {
            this.closeEditProfileModal();
        });

        // Edit profile form submission
        document.getElementById('editProfileForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveProfileChanges();
        });

        // Stories sort
        document.getElementById('storiesSort').addEventListener('change', () => {
            this.sortStories();
        });

        // Load more stories
        document.getElementById('loadMoreStories').addEventListener('click', () => {
            this.loadMoreStories();
        });

        // Activity filter
        document.getElementById('activityFilter').addEventListener('change', (e) => {
            this.filterActivity(e.target.value);
        });

        // Create collection
        document.getElementById('createCollectionBtn').addEventListener('click', () => {
            this.createCollection();
        });

        // Follower stats toggle
        document.querySelectorAll('.follower-stat').forEach(stat => {
            stat.addEventListener('click', (e) => {
                this.switchFollowerView(e.currentTarget.dataset.type);
            });
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.profile-actions')) {
                this.closeMoreActions();
            }
        });

        // Character counter for bio
        document.getElementById('editBio').addEventListener('input', (e) => {
            this.updateCharCounter('bioCharCount', e.target.value.length, 500);
        });

        // Interests input
        document.getElementById('editInterests').addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.addInterest(e.target.value.trim());
                e.target.value = '';
            }
        });
    }

    setupTabNavigation() {
        document.querySelectorAll('.nav-item[data-tab]').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchTab(e.currentTarget.dataset.tab);
            });
        });
    }

    switchTab(tabName) {
        // Update active nav item
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

        // Update active tab content
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(`${tabName}-tab`).classList.add('active');

        this.currentTab = tabName;

        // Load tab-specific data if needed
        switch (tabName) {
            case 'about':
                this.loadAboutData();
                break;
            case 'activity':
                this.loadActivityData();
                break;
            case 'collections':
                this.loadCollectionsData();
                break;
            case 'followers':
                this.loadFollowersData();
                break;
        }
    }

    async loadStories() {
        try {
            const response = await fetch(`/api/stories/user/${this.profileUser.id}`, {
                method: 'GET',
                credentials: 'include'
            });
            
            if (response.ok) {
                const result = await response.json();
                // Handle different response formats
                if (result.success && result.data) {
                    this.stories = Array.isArray(result.data) ? result.data : result.data.content || [];
                } else if (Array.isArray(result)) {
                    this.stories = result;
                } else if (result.content && Array.isArray(result.content)) {
                    this.stories = result.content;
                } else {
                    this.stories = [];
                }
            } else {
                this.stories = [];
            }
            
            this.displayStories();
            this.updateStoriesCount();

        } catch (error) {
            console.error('Error loading stories:', error);
            this.stories = [];
            this.displayStories();
        }
    }

    displayStories() {
        const container = document.getElementById('storiesGrid');

        if (!this.stories || this.stories.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-book-open" style="font-size: 3rem; color: var(--gray-400); margin-bottom: 1rem;"></i>
                    <h3>No Stories Yet</h3>
                    <p>${this.isOwnProfile ? 'Start writing your first story!' : 'This user hasn\'t published any stories yet.'}</p>
                    ${this.isOwnProfile ? `
                        <a href="create-story.html" class="btn btn-primary" style="margin-top: 1rem;">
                            <i class="fas fa-plus"></i>
                            Write First Story
                        </a>
                    ` : ''}
                </div>
            `;
            return;
        }

        container.innerHTML = this.stories.map(story => `
            <div class="story-card" onclick="profilePage.viewStory(${story.id})">
                <div class="story-cover">
                    <i class="fas fa-${this.getStoryIcon(story.category)}"></i>
                </div>
                <div class="story-content">
                    <h3 class="story-title">${this.escapeHtml(story.title)}</h3>
                    <p class="story-excerpt">${this.escapeHtml(story.summary || story.content.substring(0, 150) + '...')}</p>
                    <div class="story-meta">
                        <div class="story-stats">
                            <span><i class="fas fa-eye"></i> ${this.formatNumber(story.viewCount || 0)}</span>
                            <span><i class="fas fa-heart"></i> ${this.formatNumber(story.likeCount || 0)}</span>
                            <span><i class="fas fa-comment"></i> ${this.formatNumber(story.commentCount || 0)}</span>
                        </div>
                        <div class="story-date">
                            ${new Date(story.createdAt).toLocaleDateString()}
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    async loadFollowers() {
        try {
            // Load followers and following data
            // This would typically call API endpoints
            this.followers = []; // Mock data
            this.following = []; // Mock data

            this.updateFollowersCount();

        } catch (error) {
            console.error('Error loading followers:', error);
        }
    }

    async loadProfileStats() {
        try {
            const response = await fetch(`/api/users/${this.profileUser.id}/stats`, {
                method: 'GET',
                credentials: 'include'
            });
            
            if (response.ok) {
                const result = await response.json();
                const stats = result.success && result.data ? result.data : result;
                this.updateProfileStats(stats);
            }

        } catch (error) {
            console.error('Error loading profile stats:', error);
            // Use default stats
            this.updateProfileStats({});
        }
    }

    updateProfileStats(stats) {
        document.getElementById('storiesCount').textContent = this.formatNumber(stats.storyCount || 0);
        document.getElementById('followersCount').textContent = this.formatNumber(stats.followerCount || 0);
        document.getElementById('followingCount').textContent = this.formatNumber(stats.followingCount || 0);
        document.getElementById('totalLikes').textContent = this.formatNumber(stats.totalLikes || 0);

        // Update nav counts
        document.getElementById('storiesNavCount').textContent = stats.storyCount || 0;
        document.getElementById('collectionsNavCount').textContent = stats.collectionCount || 0;
    }

    updateStoriesCount() {
        document.getElementById('storiesCount').textContent = this.formatNumber(this.stories.length);
        document.getElementById('storiesNavCount').textContent = this.stories.length;
    }

    updateFollowersCount() {
        document.getElementById('followersCount').textContent = this.formatNumber(this.followers.length);
        document.getElementById('followingCount').textContent = this.formatNumber(this.following.length);
        document.getElementById('followersTabCount').textContent = this.followers.length;
        document.getElementById('followingTabCount').textContent = this.following.length;
    }

    // Action Methods
    async toggleFollow() {
        if (!this.currentUser) {
            this.showNotification('Please log in to follow users', 'error');
            return;
        }

        if (this.isOwnProfile) return;

        try {
            const followBtn = document.getElementById('followBtn');
            const isCurrentlyFollowing = followBtn.textContent.includes('Following');

            if (isCurrentlyFollowing) {
                // Unfollow
                await api.users.unfollow(this.profileUser.id);
                followBtn.innerHTML = '<i class="fas fa-plus"></i> Follow';
                followBtn.classList.add('btn-primary');
                followBtn.classList.remove('btn-outline');
                this.showNotification(`Unfollowed ${this.profileUser.username}`, 'info');
            } else {
                // Follow
                await api.users.follow(this.profileUser.id);
                followBtn.innerHTML = '<i class="fas fa-check"></i> Following';
                followBtn.classList.add('btn-outline');
                followBtn.classList.remove('btn-primary');
                this.showNotification(`Following ${this.profileUser.username}`, 'success');
            }

            // Update followers count
            this.loadFollowers();

        } catch (error) {
            console.error('Error toggling follow:', error);
            this.showNotification('Failed to update follow status', 'error');
        }
    }

    startConversation() {
        if (!this.currentUser) {
            this.showNotification('Please log in to message users', 'error');
            return;
        }

        // Implement conversation start logic
        this.showNotification('Message feature coming soon!', 'info');
    }

    toggleMoreActions(e) {
        e.stopPropagation();
        const menu = document.getElementById('moreActionsMenu');
        menu.classList.toggle('show');
    }

    closeMoreActions() {
        document.getElementById('moreActionsMenu').classList.remove('show');
    }

    shareProfile() {
        const profileUrl = window.location.href;
        const profileName = this.profileUser.displayName || this.profileUser.username;

        if (navigator.share) {
            navigator.share({
                title: `${profileName} on InkLink`,
                text: `Check out ${profileName}'s profile on InkLink`,
                url: profileUrl
            });
        } else {
            navigator.clipboard.writeText(profileUrl).then(() => {
                this.showNotification('Profile link copied to clipboard!', 'success');
            });
        }
    }

    openEditProfileModal() {
        this.populateEditForm();
        document.getElementById('editProfileModal').classList.remove('hidden');
    }

    closeEditProfileModal() {
        document.getElementById('editProfileModal').classList.add('hidden');
    }

    populateEditForm() {
        document.getElementById('editDisplayName').value = this.profileUser.displayName || '';
        document.getElementById('editUsername').value = this.profileUser.username || '';
        document.getElementById('editBio').value = this.profileUser.bio || '';
        document.getElementById('editLocation').value = this.profileUser.location || '';
        document.getElementById('editWebsite').value = this.profileUser.website || '';
        document.getElementById('editOccupation').value = this.profileUser.occupation || '';
        document.getElementById('editEducation').value = this.profileUser.education || '';

        // Update character counter
        this.updateCharCounter('bioCharCount', document.getElementById('editBio').value.length, 500);

        // Populate interests
        this.populateInterests();
    }

    populateInterests() {
        const container = document.getElementById('selectedInterests');
        const interests = this.profileUser.interests || [];

        container.innerHTML = interests.map(interest => `
            <span class="interest-tag">
                ${this.escapeHtml(interest)}
                <button type="button" onclick="profilePage.removeInterest('${this.escapeHtml(interest)}')">
                    <i class="fas fa-times"></i>
                </button>
            </span>
        `).join('');
    }

    addInterest(interest) {
        if (!interest) return;

        const interests = this.profileUser.interests || [];
        if (interests.includes(interest)) {
            this.showNotification('Interest already added', 'warning');
            return;
        }

        if (interests.length >= 10) {
            this.showNotification('Maximum 10 interests allowed', 'warning');
            return;
        }

        interests.push(interest);
        this.profileUser.interests = interests;
        this.populateInterests();
    }

    removeInterest(interest) {
        this.profileUser.interests = (this.profileUser.interests || []).filter(i => i !== interest);
        this.populateInterests();
    }

    async saveProfileChanges() {
        try {
            const formData = {
                displayName: document.getElementById('editDisplayName').value,
                username: document.getElementById('editUsername').value,
                bio: document.getElementById('editBio').value,
                location: document.getElementById('editLocation').value,
                website: document.getElementById('editWebsite').value,
                occupation: document.getElementById('editOccupation').value,
                education: document.getElementById('editEducation').value,
                interests: this.profileUser.interests || []
            };

            await api.users.updateProfile(this.profileUser.id, formData);

            // Update local profile data
            this.profileUser = {...this.profileUser, ...formData};
            this.updateProfileUI();

            this.closeEditProfileModal();
            this.showNotification('Profile updated successfully', 'success');

        } catch (error) {
            console.error('Error updating profile:', error);
            this.showNotification('Failed to update profile', 'error');
        }
    }

    openAvatarModal() {
        document.getElementById('avatarModal').classList.remove('hidden');
    }

    closeAvatarModal() {
        document.getElementById('avatarModal').classList.add('hidden');
        document.getElementById('avatarPreviewContainer').classList.add('hidden');
    }

    editBackground() {
        this.showNotification('Background customization coming soon!', 'info');
    }

    viewStory(storyId) {
        window.location.href = `story.html?id=${storyId}`;
    }

    sortStories() {
        const sortBy = document.getElementById('storiesSort').value;

        const sortedStories = [...this.stories].sort((a, b) => {
            switch (sortBy) {
                case 'newest':
                    return new Date(b.createdAt) - new Date(a.createdAt);
                case 'oldest':
                    return new Date(a.createdAt) - new Date(b.createdAt);
                case 'popular':
                    return (b.viewCount || 0) - (a.viewCount || 0);
                case 'title':
                    return a.title.localeCompare(b.title);
                default:
                    return 0;
            }
        });

        this.stories = sortedStories;
        this.displayStories();
    }

    loadMoreStories() {
        // Implement pagination for stories
        this.showNotification('Loading more stories...', 'info');
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
            window.location.href = 'index.html';
        }
    }

    // UI State Management Methods
    showLoadingState() {
        const profileName = document.getElementById('profileName');
        if (profileName) {
            profileName.textContent = 'Loading...';
        }
    }

    hideLoadingState() {
        // Loading state is replaced by actual content in updateProfileUI
    }

    showErrorState(message) {
        const profileHeader = document.querySelector('.profile-header');
        if (profileHeader) {
            profileHeader.innerHTML = `
                <div style="padding: 3rem; text-align: center;">
                    <i class="fas fa-exclamation-triangle" style="font-size: 3rem; color: #dc3545; margin-bottom: 1rem;"></i>
                    <h3>${message}</h3>
                    <a href="index.html" class="btn btn-primary" style="margin-top: 1rem;">Go Home</a>
                </div>
            `;
        }
    }

    showNotification(message, type = 'info') {
        // Create a simple notification
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 1rem 1.5rem;
            background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : type === 'warning' ? '#ffc107' : '#17a2b8'};
            color: ${type === 'warning' ? '#000' : '#fff'};
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
            z-index: 2000;
            animation: slideIn 0.3s ease;
        `;
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    // Helper Methods
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    getStoryIcon(category) {
        const icons = {
            'ADVENTURE': 'mountain',
            'MYSTERY': 'search',
            'ROMANCE': 'heart',
            'HORROR': 'ghost',
            'FANTASY': 'wand-magic-sparkles',
            'SCIENCE_FICTION': 'rocket',
            'THRILLER': 'bolt'
        };
        return icons[category] || 'book';
    }

    // Placeholder methods for features to be implemented
    loadAboutData() {
        // Load about tab data
    }

    loadActivityData() {
        // Load activity tab data
    }

    loadCollectionsData() {
        // Load collections tab data
    }

    loadFollowersData() {
        // Load followers tab data
    }

    reportUser() {
        this.showNotification('User report submitted', 'success');
    }

    createCollection() {
        this.showNotification('Collection feature coming soon!', 'info');
    }

    switchFollowerView(type) {
        const followersList = document.getElementById('followersList');
        const followingList = document.getElementById('followingList');
        
        if (type === 'followers') {
            followersList.classList.remove('hidden');
            followingList.classList.add('hidden');
        } else {
            followersList.classList.add('hidden');
            followingList.classList.remove('hidden');
        }
    }

    updateCharCounter(elementId, length, max) {
        document.getElementById(elementId).textContent = length;
    }

    filterActivity(filterBy) {
        // Filter activity based on type
    }
}

// Initialize the profile page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing profile page...');
    window.profilePage = new ProfilePage();
});