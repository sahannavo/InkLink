// InkLink API Service Layer
class InkLinkAPI {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api';
        this.defaultHeaders = {
            'Content-Type': 'application/json',
        };
    }

    // Helper method to handle API requests
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            credentials: 'include', // Important for session cookies
            headers: { ...this.defaultHeaders, ...options.headers },
            ...options
        };

        // Remove Content-Type for FormData requests
        if (options.body instanceof FormData) {
            delete config.headers['Content-Type'];
        }

        try {
            const response = await fetch(url, config);

            // Handle non-JSON responses
            const contentType = response.headers.get('content-type');
            const isJson = contentType && contentType.includes('application/json');

            let data;
            if (isJson) {
                data = await response.json();
            } else if (response.status === 204) {
                data = null; // No content
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                throw new APIError(
                    data?.message || `HTTP ${response.status}: ${response.statusText}`,
                    response.status,
                    data
                );
            }

            return data;
        } catch (error) {
            if (error instanceof APIError) {
                throw error;
            }

            // Network errors or other issues
            console.error('API request failed:', error);
            throw new APIError(
                'Network error: Unable to connect to the server',
                0,
                { originalError: error.message }
            );
        }
    }

    // Authentication API
    // Authentication API
    auth = {
        login: async (credentials) => {
            return await this.request('/auth/signin', {
                method: 'POST',
                body: JSON.stringify(credentials)
            });
        },

        register: async (userData) => {
            return await this.request('/auth/signup', {
                method: 'POST',
                body: JSON.stringify(userData)
            });
        },

        logout: async () => {
            return await this.request('/auth/signout', {
                method: 'POST'
            });
        },

        getCurrentUser: async () => {
            return await this.request('/auth/me');
        },

        verifySession: async () => {
            try {
                await this.request('/auth/me');
                return true;
            } catch (error) {
                return false;
            }
        }
    };

    // Stories API
    stories = {
        // Get stories with pagination and filters
        getAll: async (page = 0, size = 12, sort = 'createdAt,desc') => {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
                sort
            });

            return await this.request(`/stories?${params}`);
        },

        // Get single story
        getById: async (id) => {
            return await this.request(`/stories/${id}`);
        },

        // Create new story
        create: async (storyData) => {
            return await this.request('/stories', {
                method: 'POST',
                body: JSON.stringify(storyData)
            });
        },

        // Update story
        update: async (id, storyData) => {
            return await this.request(`/stories/${id}`, {
                method: 'PUT',
                body: JSON.stringify(storyData)
            });
        },

        // Delete story
        delete: async (id) => {
            return await this.request(`/stories/${id}`, {
                method: 'DELETE'
            });
        },

        // Get user's stories
        getByUser: async (userId, page = 0, size = 10) => {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString()
            });

            return await this.request(`/users/${userId}/stories?${params}`);
        },

        // Search stories
        search: async (query, page = 0, size = 12) => {
            const params = new URLSearchParams({
                q: query,
                page: page.toString(),
                size: size.toString()
            });

            return await this.request(`/stories/search?${params}`);
        },

        // Filter stories by tag
        getByTag: async (tagName, page = 0, size = 12) => {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString()
            });

            return await this.request(`/stories/tag/${encodeURIComponent(tagName)}?${params}`);
        },

        // Filter stories by category
        getByCategory: async (category, page = 0, size = 12) => {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString()
            });

            return await this.request(`/stories/category/${category}?${params}`);
        }
    };

    // Comments API
    comments = {
        // Get comments for a story
        getByStory: async (storyId) => {
            return await this.request(`/stories/${storyId}/comments`);
        },

        // Add comment to story
        add: async (storyId, content) => {
            return await this.request(`/stories/${storyId}/comments`, {
                method: 'POST',
                body: JSON.stringify({ content })
            });
        },

        // Update comment
        update: async (commentId, content) => {
            return await this.request(`/comments/${commentId}`, {
                method: 'PUT',
                body: JSON.stringify({ content })
            });
        },

        // Delete comment
        delete: async (commentId) => {
            return await this.request(`/comments/${commentId}`, {
                method: 'DELETE'
            });
        }
    };

    // Likes API
    likes = {
        // Toggle like on story
        toggle: async (storyId) => {
            return await this.request(`/stories/${storyId}/like`, {
                method: 'POST'
            });
        },

        // Check if user liked a story
        check: async (storyId) => {
            return await this.request(`/stories/${storyId}/like`);
        },

        // Get user's liked stories
        getUserLikes: async (userId, page = 0, size = 10) => {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString()
            });

            return await this.request(`/users/${userId}/likes?${params}`);
        }
    };

    // Users API
    users = {
        // Get user profile
        getProfile: async (userId) => {
            return await this.request(`/users/${userId}`);
        },

        // Update user profile
        updateProfile: async (userId, profileData) => {
            return await this.request(`/users/${userId}`, {
                method: 'PUT',
                body: JSON.stringify(profileData)
            });
        },

        // Upload profile picture
        uploadAvatar: async (userId, file) => {
            const formData = new FormData();
            formData.append('file', file);

            return await this.request(`/users/${userId}/avatar`, {
                method: 'POST',
                body: formData
            });
        },

        // Get user statistics
        getStats: async (userId) => {
            return await this.request(`/users/${userId}/stats`);
        }
    };

    // Tags API
    tags = {
        // Get all tags
        getAll: async () => {
            return await this.request('/tags');
        },

        // Get popular tags
        getPopular: async (limit = 20) => {
            return await this.request(`/tags/popular?limit=${limit}`);
        },

        // Create tag (admin only)
        create: async (tagName) => {
            return await this.request('/tags', {
                method: 'POST',
                body: JSON.stringify({ name: tagName })
            });
        }
    };

    // File Upload API
    files = {
        // Upload story image
        uploadStoryImage: async (storyId, file) => {
            const formData = new FormData();
            formData.append('file', file);

            return await this.request(`/stories/${storyId}/image`, {
                method: 'POST',
                body: formData
            });
        },

        // Upload multiple files
        uploadMultiple: async (files) => {
            const formData = new FormData();
            files.forEach(file => {
                formData.append('files', file);
            });

            return await this.request('/files/upload', {
                method: 'POST',
                body: formData
            });
        }
    };

    // Analytics API
    analytics = {
        // Record story view
        recordView: async (storyId) => {
            return await this.request(`/stories/${storyId}/view`, {
                method: 'POST'
            });
        },

        // Get story analytics
        getStoryAnalytics: async (storyId) => {
            return await this.request(`/analytics/stories/${storyId}`);
        },

        // Get user analytics
        getUserAnalytics: async (userId) => {
            return await this.request(`/analytics/users/${userId}`);
        }
    };

    // Utility methods
    utils = {
        // Health check
        health: async () => {
            return await this.request('/health');
        },

        // Get server status
        status: async () => {
            return await this.request('/status');
        }
    };

    // Batch operations
    batch = {
        // Batch get stories
        getStories: async (storyIds) => {
            return await this.request('/batch/stories', {
                method: 'POST',
                body: JSON.stringify({ ids: storyIds })
            });
        },

        // Batch get users
        getUsers: async (userIds) => {
            return await this.request('/batch/users', {
                method: 'POST',
                body: JSON.stringify({ ids: userIds })
            });
        }
    };
}

// Custom Error Class for API errors
class APIError extends Error {
    constructor(message, status, data = null) {
        super(message);
        this.name = 'APIError';
        this.status = status;
        this.data = data;
        this.timestamp = new Date().toISOString();
    }

    // Check if error is due to authentication
    isAuthError() {
        return this.status === 401 || this.status === 403;
    }

    // Check if error is due to network issues
    isNetworkError() {
        return this.status === 0;
    }

    // Check if error is due to server issues
    isServerError() {
        return this.status >= 500;
    }

    // Check if error is due to client issues
    isClientError() {
        return this.status >= 400 && this.status < 500;
    }

    // Get error type for UI handling
    getType() {
        if (this.isAuthError()) return 'authentication';
        if (this.isNetworkError()) return 'network';
        if (this.isServerError()) return 'server';
        if (this.isClientError()) return 'client';
        return 'unknown';
    }
}

// API Response Interceptor System
class APIInterceptor {
    constructor() {
        this.interceptors = {
            request: [],
            response: [],
            error: []
        };
    }

    // Add request interceptor
    addRequestInterceptor(interceptor) {
        this.interceptors.request.push(interceptor);
    }

    // Add response interceptor
    addResponseInterceptor(interceptor) {
        this.interceptors.response.push(interceptor);
    }

    // Add error interceptor
    addErrorInterceptor(interceptor) {
        this.interceptors.error.push(interceptor);
    }

    // Execute request interceptors
    async executeRequestInterceptors(config) {
        let processedConfig = { ...config };

        for (const interceptor of this.interceptors.request) {
            processedConfig = await interceptor(processedConfig);
        }

        return processedConfig;
    }

    // Execute response interceptors
    async executeResponseInterceptors(response) {
        let processedResponse = response;

        for (const interceptor of this.interceptors.response) {
            processedResponse = await interceptor(processedResponse);
        }

        return processedResponse;
    }

    // Execute error interceptors
    async executeErrorInterceptors(error) {
        let processedError = error;

        for (const interceptor of this.interceptors.error) {
            processedError = await interceptor(processedError);
        }

        return processedError;
    }
}

// API Service with Interceptors
class InkLinkAPIService extends InkLinkAPI {
    constructor() {
        super();
        this.interceptor = new APIInterceptor();
        this.setupDefaultInterceptors();
    }

    setupDefaultInterceptors() {
        // Request interceptor to add auth token if available
        this.interceptor.addRequestInterceptor(async (config) => {
            // You can add JWT tokens here if you switch from session auth
            // const token = localStorage.getItem('authToken');
            // if (token) {
            //     config.headers.Authorization = `Bearer ${token}`;
            // }

            // Add timestamp for cache busting if needed
            if (config.method === 'GET' && !config.url.includes('?')) {
                // config.url += `?t=${Date.now()}`;
            }

            return config;
        });

        // Response interceptor to handle common transformations
        this.interceptor.addResponseInterceptor(async (response) => {
            // You can transform response data here if needed
            return response;
        });

        // Error interceptor for global error handling
        this.interceptor.addErrorInterceptor(async (error) => {
            if (error.isAuthError()) {
                // Handle authentication errors globally
                console.warn('Authentication error:', error.message);

                // Clear user session
                localStorage.removeItem('isAuthenticated');
                localStorage.removeItem('user');

                // Redirect to login if not already there
                if (!window.location.pathname.includes('login.html')) {
                    window.location.href = 'login.html';
                }
            }

            return error;
        });
    }

    // Override request method to include interceptors
    async request(endpoint, options = {}) {
        // Execute request interceptors
        let config = await this.interceptor.executeRequestInterceptors({
            endpoint,
            ...options
        });

        try {
            // Make the actual request using parent class method
            const response = await super.request(config.endpoint, config);

            // Execute response interceptors
            return await this.interceptor.executeResponseInterceptors(response);
        } catch (error) {
            // Execute error interceptors
            const processedError = await this.interceptor.executeErrorInterceptors(error);
            throw processedError;
        }
    }
}

// Cache Manager for API responses
class APICache {
    constructor() {
        this.cache = new Map();
        this.defaultTTL = 5 * 60 * 1000; // 5 minutes
    }

    set(key, data, ttl = this.defaultTTL) {
        const expiry = Date.now() + ttl;
        this.cache.set(key, { data, expiry });
    }

    get(key) {
        const item = this.cache.get(key);

        if (!item) return null;

        if (Date.now() > item.expiry) {
            this.cache.delete(key);
            return null;
        }

        return item.data;
    }

    delete(key) {
        this.cache.delete(key);
    }

    clear() {
        this.cache.clear();
    }

    // Generate cache key from request parameters
    generateKey(endpoint, params = {}) {
        const sortedParams = Object.keys(params)
            .sort()
            .map(key => `${key}=${params[key]}`)
            .join('&');

        return `${endpoint}?${sortedParams}`;
    }
}

// Enhanced API Service with Caching
class InkLinkAPIServiceWithCache extends InkLinkAPIService {
    constructor() {
        super();
        this.cache = new APICache();
        this.cacheEnabled = true;
    }

    // Enable/disable cache
    enableCache() {
        this.cacheEnabled = true;
    }

    disableCache() {
        this.cacheEnabled = false;
    }

    clearCache() {
        this.cache.clear();
    }

    // Override request method to include caching
    async request(endpoint, options = {}) {
        // Only cache GET requests
        const method = options.method || 'GET';
        const shouldCache = this.cacheEnabled && method === 'GET';

        if (shouldCache) {
            const cacheKey = this.cache.generateKey(endpoint, options.params);
            const cachedData = this.cache.get(cacheKey);

            if (cachedData) {
                console.log('Serving from cache:', cacheKey);
                return cachedData;
            }
        }

        // Make the actual request
        const response = await super.request(endpoint, options);

        // Cache the response if it's a GET request
        if (shouldCache) {
            const cacheKey = this.cache.generateKey(endpoint, options.params);
            this.cache.set(cacheKey, response);
        }

        return response;
    }

    // Invalidate cache for specific endpoints
    invalidateCache(endpointPattern) {
        for (const key of this.cache.cache.keys()) {
            if (key.startsWith(endpointPattern)) {
                this.cache.delete(key);
            }
        }
    }
}

// Create and export the API instance
const api = new InkLinkAPIServiceWithCache();

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        InkLinkAPI,
        InkLinkAPIService,
        InkLinkAPIServiceWithCache,
        APIError,
        APICache,
        api
    };
} else {
    // Make available globally
    window.InkLinkAPI = InkLinkAPI;
    window.InkLinkAPIService = InkLinkAPIService;
    window.APIError = APIError;
    window.api = api;
}

