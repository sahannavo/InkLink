// frontend/utils.js

class InkLinkUtils {
    // API Base URL - automatically detects environment
    static getBaseUrl() {
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            return 'http://localhost:8080/api';
        }
        return '/api'; // Relative path for production
    }

    // CSRF Token handling for Spring Security
    static getCsrfToken() {
        return document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
    }

    static getCsrfHeader() {
        return document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
    }

    // Common headers for API requests - FIXED: Added credentials
    static getHeaders(contentType = 'application/json') {
        const headers = {
            'Content-Type': contentType
        };

        // Add CSRF token for non-GET requests
        const csrfToken = this.getCsrfToken();
        const csrfHeader = this.getCsrfHeader();
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        return headers;
    }

    // Enhanced API request method with session support
    static async apiRequest(url, options = {}) {
        const config = {
            credentials: 'include', // CRITICAL: This includes session cookies
            headers: this.getHeaders(),
            ...options
        };

        // Merge headers properly
        if (options.headers) {
            config.headers = { ...config.headers, ...options.headers };
        }

        try {
            const response = await fetch(url, config);

            // Handle authentication errors
            if (response.status === 401) {
                this.redirectToLogin();
                throw new Error('Authentication required');
            }

            return response;
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // Error handling utility
    static handleApiError(error) {
        console.error('API Error:', error);

        if (error.message === 'Authentication required' || error.status === 401) {
            this.redirectToLogin();
            return 'Please log in to continue';
        } else if (error.status === 403) {
            return 'You do not have permission to perform this action';
        } else if (error.status === 404) {
            return 'Resource not found';
        } else if (error.status >= 500) {
            return 'Server error. Please try again later';
        } else {
            return error.message || 'An unexpected error occurred';
        }
    }

    // Redirect to login with return URL
    static redirectToLogin() {
        // Clear any stored auth data (we're using sessions now)
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('currentUser');

        const currentPath = window.location.pathname + window.location.search;
        window.location.href = `/login.html?returnUrl=${encodeURIComponent(currentPath)}`;
    }

    // Check if user is authenticated - FIXED: Check session with backend
    static async isAuthenticated() {
        try {
            const response = await this.apiRequest(`${this.getBaseUrl()}/auth/me`);
            if (response.ok) {
                const result = await response.json();
                return result.success && result.data;
            }
            return false;
        } catch (error) {
            return false;
        }
    }

    // Get current user from backend session - FIXED
    static async getCurrentUser() {
        try {
            const response = await this.apiRequest(`${this.getBaseUrl()}/auth/me`);
            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    // Store in localStorage for display purposes only (not for auth)
                    localStorage.setItem('currentUser', JSON.stringify(result.data));
                    return result.data;
                }
            }
            return null;
        } catch (error) {
            return null;
        }
    }

    // Enhanced authentication check for protected pages
    static async requireAuth(redirectTo = '/login.html') {
        const isAuth = await this.isAuthenticated();
        if (!isAuth) {
            window.location.href = redirectTo;
            return false;
        }
        return true;
    }

    // Logout helper - FIXED: Clear both frontend and backend
    static async logout() {
        try {
            // Call backend logout
            await this.apiRequest(`${this.getBaseUrl()}/auth/signout`, {
                method: 'POST'
            });
        } catch (error) {
            console.error('Logout API call failed:', error);
        } finally {
            // Always clear frontend storage
            localStorage.removeItem('authToken');
            localStorage.removeItem('currentUser');
            localStorage.removeItem('isAuthenticated');
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('currentUser');
            sessionStorage.removeItem('isAuthenticated');

            // Redirect to login
            window.location.href = '/login.html';
        }
    }

    // Format date for display
    static formatDate(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
        if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
        if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    // Truncate text with ellipsis
    static truncateText(text, maxLength) {
        if (!text || text.length <= maxLength) return text;
        return text.substring(0, maxLength).trim() + '...';
    }

    // Generate excerpt from HTML content
    static generateExcerpt(htmlContent, wordCount = 50) {
        // Remove HTML tags and get plain text
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = htmlContent;
        const text = tempDiv.textContent || tempDiv.innerText || '';

        const words = text.trim().split(/\s+/);
        if (words.length <= wordCount) return text;

        return words.slice(0, wordCount).join(' ') + '...';
    }

    // Debounce function for search inputs
    static debounce(func, wait, immediate) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                timeout = null;
                if (!immediate) func(...args);
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func(...args);
        };
    }

    // File validation
    static validateFile(file, options = {}) {
        const {
            maxSize = 5 * 1024 * 1024, // 5MB default
            allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
            allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp']
        } = options;

        // Check file size
        if (file.size > maxSize) {
            return `File size must be less than ${maxSize / 1024 / 1024}MB`;
        }

        // Check MIME type
        if (!allowedTypes.includes(file.type)) {
            return `File type not supported. Please use: ${allowedExtensions.join(', ')}`;
        }

        // Check file extension
        const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
        if (!allowedExtensions.includes(fileExtension)) {
            return `File extension not allowed. Please use: ${allowedExtensions.join(', ')}`;
        }

        return null; // No error
    }

    // Create form data for file uploads
    static createFormData(data) {
        const formData = new FormData();
        Object.keys(data).forEach(key => {
            if (data[key] !== null && data[key] !== undefined) {
                formData.append(key, data[key]);
            }
        });
        return formData;
    }

    // Show notification/toast
    static showNotification(message, type = 'info', duration = 5000) {
        // Remove existing notification
        const existingNotification = document.querySelector('.inklink-notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        // Create notification element
        const notification = document.createElement('div');
        notification.className = `inklink-notification inklink-notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-message">${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;

        // Add styles if not already added
        if (!document.querySelector('#notification-styles')) {
            const styles = document.createElement('style');
            styles.id = 'notification-styles';
            styles.textContent = `
                .inklink-notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    z-index: 10000;
                    min-width: 300px;
                    max-width: 500px;
                    background: white;
                    border-left: 4px solid #007bff;
                    border-radius: 4px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                    animation: slideInRight 0.3s ease-out;
                }
                .inklink-notification-success { border-left-color: #28a745; }
                .inklink-notification-error { border-left-color: #dc3545; }
                .inklink-notification-warning { border-left-color: #ffc107; }
                .inklink-notification-info { border-left-color: #007bff; }
                .notification-content {
                    padding: 16px;
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                }
                .notification-message {
                    flex: 1;
                    margin-right: 12px;
                    line-height: 1.4;
                }
                .notification-close {
                    background: none;
                    border: none;
                    font-size: 18px;
                    cursor: pointer;
                    color: #6c757d;
                    padding: 0;
                    width: 20px;
                    height: 20px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .notification-close:hover {
                    color: #495057;
                }
                @keyframes slideInRight {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `;
            document.head.appendChild(styles);
        }

        // Add to page
        document.body.appendChild(notification);

        // Auto remove after duration
        const autoRemove = setTimeout(() => {
            notification.remove();
        }, duration);

        // Close button handler
        const closeBtn = notification.querySelector('.notification-close');
        closeBtn.addEventListener('click', () => {
            clearTimeout(autoRemove);
            notification.remove();
        });
    }

    // Loading spinner utility
    static showLoading(container) {
        const spinner = document.createElement('div');
        spinner.className = 'loading-spinner';
        spinner.innerHTML = `
            <div class="spinner"></div>
            <p>Loading...</p>
        `;

        // Add styles if not already added
        if (!document.querySelector('#spinner-styles')) {
            const styles = document.createElement('style');
            styles.id = 'spinner-styles';
            styles.textContent = `
                .loading-spinner {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    padding: 40px;
                    color: #6c757d;
                }
                .spinner {
                    width: 40px;
                    height: 40px;
                    border: 4px solid #f3f3f3;
                    border-top: 4px solid #007bff;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                    margin-bottom: 16px;
                }
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `;
            document.head.appendChild(styles);
        }

        if (container) {
            container.innerHTML = '';
            container.appendChild(spinner);
        }

        return spinner;
    }

    // Pagination helper
    static createPagination(currentPage, totalPages, onPageChange) {
        const pagination = document.createElement('div');
        pagination.className = 'pagination';

        // Previous button
        const prevButton = document.createElement('button');
        prevButton.innerHTML = '&laquo; Previous';
        prevButton.disabled = currentPage === 1;
        prevButton.addEventListener('click', () => onPageChange(currentPage - 1));
        pagination.appendChild(prevButton);

        // Page numbers
        const startPage = Math.max(1, currentPage - 2);
        const endPage = Math.min(totalPages, currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            const pageButton = document.createElement('button');
            pageButton.textContent = i;
            pageButton.className = i === currentPage ? 'active' : '';
            pageButton.addEventListener('click', () => onPageChange(i));
            pagination.appendChild(pageButton);
        }

        // Next button
        const nextButton = document.createElement('button');
        nextButton.innerHTML = 'Next &raquo;';
        nextButton.disabled = currentPage === totalPages;
        nextButton.addEventListener('click', () => onPageChange(currentPage + 1));
        pagination.appendChild(nextButton);

        return pagination;
    }

    // Sanitize HTML for safe display
    static sanitizeHtml(html) {
        const temp = document.createElement('div');
        temp.textContent = html;
        return temp.innerHTML;
    }

    // Get reading time estimate
    static getReadingTime(text) {
        const wordsPerMinute = 200;
        const words = text.trim().split(/\s+/).length;
        const minutes = Math.ceil(words / wordsPerMinute);
        return `${minutes} min read`;
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = InkLinkUtils;
}