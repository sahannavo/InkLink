// Main JavaScript for InkLink

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    initializeProfileFeatures();
    initializeImageUpload();
    initializeFollowSystem();
    initializeLazyLoading();
});

function initializeApp() {
    // Initialize tooltips
    initTooltips();

    // Initialize form validation
    initFormValidation();

    // Initialize interactive elements
    initInteractiveElements();

    // Initialize search functionality
    initSearch();
}

function initTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

function initFormValidation() {
    // Add custom validation for forms
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
}

function initInteractiveElements() {
    // Add loading states to buttons
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('btn-loading');
                submitBtn.disabled = true;
            }
        });
    });

    // Initialize password toggle
    initPasswordToggle();

    // Initialize character counters
    initCharacterCounters();
}

function initPasswordToggle() {
    const toggleButtons = document.querySelectorAll('.password-toggle');

    toggleButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');

            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.replace('fa-eye', 'fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.replace('fa-eye-slash', 'fa-eye');
            }
        });
    });
}

function initCharacterCounters() {
    const counters = document.querySelectorAll('[data-max-length]');

    counters.forEach(counter => {
        const maxLength = counter.getAttribute('data-max-length');
        const counterElement = document.getElementById(counter.getAttribute('data-counter-id'));

        if (counterElement) {
            counter.addEventListener('input', function() {
                const currentLength = this.value.length;
                counterElement.textContent = `${currentLength}/${maxLength}`;

                if (currentLength > maxLength * 0.8) {
                    counterElement.classList.add('text-warning');
                } else {
                    counterElement.classList.remove('text-warning');
                }

                if (currentLength > maxLength) {
                    counterElement.classList.add('text-danger');
                } else {
                    counterElement.classList.remove('text-danger');
                }
            });

            // Trigger initial count
            counter.dispatchEvent(new Event('input'));
        }
    });
}

function initSearch() {
    const searchInput = document.querySelector('input[name="query"]');

    if (searchInput) {
        // Debounced search
        let timeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                if (this.value.length >= 3 || this.value.length === 0) {
                    this.form.submit();
                }
            }, 500);
        });
    }
}

// Profile functionality
function initializeProfileFeatures() {
    // Bio editing
    const editBioModal = document.getElementById('editBioModal');
    if (editBioModal) {
        editBioModal.addEventListener('shown.bs.modal', function() {
            const bioTextarea = document.getElementById('bio');
            if (bioTextarea) {
                bioTextarea.focus();
            }
        });
    }

    // Tab persistence
    const profileTabs = document.querySelectorAll('#profileTabs button[data-bs-toggle="tab"]');
    profileTabs.forEach(tab => {
        tab.addEventListener('shown.bs.tab', function(event) {
            const target = event.target.getAttribute('data-bs-target');
            localStorage.setItem('activeProfileTab', target);
        });
    });

    // Restore active tab
    const activeTab = localStorage.getItem('activeProfileTab');
    if (activeTab) {
        const tabElement = document.querySelector(`[data-bs-target="${activeTab}"]`);
        if (tabElement) {
            new bootstrap.Tab(tabElement).show();
        }
    }
}

function initializeImageUpload() {
    const avatarInput = document.getElementById('avatarInput');
    const avatarForm = document.querySelector('.avatar-upload-form');

    if (avatarInput && avatarForm) {
        avatarInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                // Validate file type and size
                if (!file.type.startsWith('image/')) {
                    showToast('Please select a valid image file', 'error');
                    return;
                }

                if (file.size > 5 * 1024 * 1024) { // 5MB limit
                    showToast('Image size must be less than 5MB', 'error');
                    return;
                }

                // Show preview
                const reader = new FileReader();
                reader.onload = function(e) {
                    const avatarImage = document.querySelector('.avatar-image');
                    if (avatarImage) {
                        avatarImage.src = e.target.result;
                        avatarImage.classList.add('loading');
                    }

                    // Submit form
                    avatarForm.submit();
                };
                reader.readAsDataURL(file);
            }
        });
    }
}

function initializeFollowSystem() {
    const followBtn = document.querySelector('.follow-btn');
    if (followBtn) {
        followBtn.addEventListener('click', function() {
            const userId = this.dataset.userId;
            const isFollowing = this.classList.contains('btn-outline-primary');

            toggleFollow(userId, isFollowing, this);
        });
    }
}

function toggleFollow(userId, isCurrentlyFollowing, button) {
    const url = `/profile/${userId}/follow`;
    const method = isCurrentlyFollowing ? 'DELETE' : 'POST';

    // Add loading state
    button.disabled = true;
    button.innerHTML = '<div class="loading-spinner"></div>';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (data.following) {
                    button.classList.replace('btn-primary', 'btn-outline-primary');
                    button.innerHTML = '<i class="fas fa-user-check me-2"></i>Following';
                    showToast('You are now following this user', 'success');
                } else {
                    button.classList.replace('btn-outline-primary', 'btn-primary');
                    button.innerHTML = '<i class="fas fa-user-plus me-2"></i>Follow';
                    showToast('You have unfollowed this user', 'success');
                }
            } else {
                showToast('Operation failed', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('An error occurred', 'error');
        })
        .finally(() => {
            button.disabled = false;
        });
}

// Initialize lazy loading for images
function initializeLazyLoading() {
    const lazyImages = document.querySelectorAll('img[data-src]');

    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy-image');
                img.classList.add('loaded');
                observer.unobserve(img);
            }
        });
    });

    lazyImages.forEach(img => imageObserver.observe(img));
}

// Utility function to get CSRF token
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]').getAttribute('content');
}

// API Helper Functions
const ApiHelper = {
    async post(url, data) {
        const csrfToken = getCsrfToken();
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        });

        return await response.json();
    },

    async put(url, data) {
        const csrfToken = getCsrfToken();
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        });

        return await response.json();
    },

    async delete(url) {
        const csrfToken = getCsrfToken();
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                [csrfHeader]: csrfToken
            }
        });

        return await response.json();
    }
};

// Story Reactions
function reactToStory(storyId, reactionType) {
    const url = `/api/stories/${storyId}/react`;
    const data = { reactionType };

    ApiHelper.post(url, data)
        .then(response => {
            if (response.success) {
                // Update reaction counts
                updateReactionCounts(storyId, response.data);
            }
        })
        .catch(error => {
            console.error('Error reacting to story:', error);
            showToast('Error reacting to story', 'error');
        });
}

function updateReactionCounts(storyId, data) {
    const likeCountElement = document.querySelector(`[data-story-id="${storyId}"] .like-count`);
    if (likeCountElement) {
        likeCountElement.textContent = data.likeCount;
    }
}

// Toast Notifications
function showToast(message, type = 'info') {
    // Use existing toast container or create one
    const toastContainer = document.getElementById('toast-container') || createToastContainer();

    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${type === 'error' ? 'danger' : type} border-0`;
    toast.setAttribute('role', 'alert');

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    toastContainer.appendChild(toast);

    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    // Remove toast after it's hidden
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}

// Infinite Scroll (for future implementation)
function initInfiniteScroll(containerSelector, loadMoreUrl) {
    const container = document.querySelector(containerSelector);

    if (!container) return;

    let isLoading = false;
    let page = 1;
    let hasMore = true;

    const observer = new IntersectionObserver(async (entries) => {
        if (entries[0].isIntersecting && !isLoading && hasMore) {
            isLoading = true;
            page++;

            try {
                const response = await fetch(`${loadMoreUrl}?page=${page}`);
                const html = await response.text();

                if (html.trim()) {
                    container.insertAdjacentHTML('beforeend', html);
                } else {
                    hasMore = false;
                }
            } catch (error) {
                console.error('Error loading more content:', error);
            } finally {
                isLoading = false;
            }
        }
    });

    const sentinel = document.createElement('div');
    sentinel.className = 'infinite-scroll-sentinel';
    container.appendChild(sentinel);
    observer.observe(sentinel);
}

// Export for global access
window.InkLink = {
    ApiHelper,
    showToast,
    initInfiniteScroll,
    reactToStory,
    toggleFollow
};