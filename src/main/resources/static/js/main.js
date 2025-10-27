// Main JavaScript for InkLink

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    setupCSRF();
    setupEventListeners();
    initializeComponents();
}

// CSRF Token Setup
function setupCSRF() {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Set up AJAX requests to include CSRF token
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        }
    });
}

// Event Listeners
function setupEventListeners() {
    // Auto-dismiss alerts
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Form validation
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', handleFormSubmit);
    });

    // Infinite scroll for stories
    if (document.getElementById('storiesGrid')) {
        setupInfiniteScroll();
    }
}

// Component Initialization
function initializeComponents() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

// Form Submission Handler
function handleFormSubmit(e) {
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');

    // Add loading state
    if (submitBtn) {
        submitBtn.classList.add('btn-loading');
        submitBtn.disabled = true;
    }

    // Validate form
    if (!form.checkValidity()) {
        e.preventDefault();
        form.classList.add('was-validated');
        if (submitBtn) {
            submitBtn.classList.remove('btn-loading');
            submitBtn.disabled = false;
        }
    }
}

// Infinite Scroll
function setupInfiniteScroll() {
    let isLoading = false;
    let currentPage = 1;
    const totalPages = parseInt(document.getElementById('storiesGrid').dataset.totalPages || 1);

    window.addEventListener('scroll', function() {
        if (isLoading || currentPage >= totalPages) return;

        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;

        if (scrollTop + windowHeight >= documentHeight - 100) {
            loadMoreStories();
        }
    });

    async function loadMoreStories() {
        isLoading = true;
        currentPage++;

        try {
            const response = await fetch(`/api/stories?page=${currentPage}`);
            const stories = await response.json();

            if (stories && stories.length > 0) {
                appendStories(stories);
            }
        } catch (error) {
            console.error('Error loading more stories:', error);
        } finally {
            isLoading = false;
        }
    }

    function appendStories(stories) {
        const grid = document.getElementById('storiesGrid');

        stories.forEach(story => {
            const storyElement = createStoryElement(story);
            grid.appendChild(storyElement);
        });
    }

    function createStoryElement(story) {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4 mb-4';

        col.innerHTML = `
            <div class="card story-card h-100">
                <div class="card-body">
                    <h5 class="card-title">${story.title}</h5>
                    <p class="card-text text-muted">By ${story.author.username}</p>
                    <p class="card-text">${story.excerpt}</p>
                    <div class="story-meta d-flex justify-content-between text-muted">
                        <small>${story.readingTime} min read</small>
                        <small>${story.likeCount} likes</small>
                    </div>
                </div>
                <div class="card-footer bg-transparent">
                    <a href="/stories/${story.id}" class="btn btn-outline-primary btn-sm">Read More</a>
                </div>
            </div>
        `;

        return col;
    }
}

// API Functions
const Api = {
    // Story reactions
    async reactToStory(storyId, reactionType) {
        try {
            const response = await fetch(`/api/stories/${storyId}/reactions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ type: reactionType })
            });

            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Error reacting to story:', error);
        }
    },

    // Follow/Unfollow user
    async followUser(userId) {
        try {
            const response = await fetch(`/api/user/${userId}/follow`, {
                method: 'POST'
            });

            return response.ok;
        } catch (error) {
            console.error('Error following user:', error);
            return false;
        }
    },

    async unfollowUser(userId) {
        try {
            const response = await fetch(`/api/user/${userId}/unfollow`, {
                method: 'POST'
            });

            return response.ok;
        } catch (error) {
            console.error('Error unfollowing user:', error);
            return false;
        }
    },

    // Save story draft
    async saveDraft(storyData) {
        try {
            const response = await fetch('/api/stories/draft', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(storyData)
            });

            return response.ok;
        } catch (error) {
            console.error('Error saving draft:', error);
            return false;
        }
    }
};

// Utility Functions
const Utils = {
    // Debounce function
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
    },

    // Format date
    formatDate(dateString) {
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    },

    // Show notification
    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        notification.style.cssText = `
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
        `;

        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(notification);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);
    },

    // Copy to clipboard
    async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            this.showNotification('Copied to clipboard!', 'success');
        } catch (err) {
            console.error('Failed to copy: ', err);
            this.showNotification('Failed to copy to clipboard', 'error');
        }
    }
};

// Export to global scope
window.InkLink = {
    Api,
    Utils
};