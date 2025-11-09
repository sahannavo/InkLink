// frontend/js/auth.js

class AuthManager {
    constructor() {
        this.init();
    }

    init() {
        this.setupLoginForm();
        this.setupRegisterForm();
        this.setupLogout();
        this.checkAuthStatus();
    }

    setupRegisterForm() {
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                // Get values directly from input elements
                const userData = {
                    username: document.getElementById('username').value,
                    email: document.getElementById('email').value,
                    password: document.getElementById('password').value
                };

                console.log('Captured userData:', userData);

                // Validate required fields
                if (!userData.username || !userData.email || !userData.password) {
                    this.showNotification('Please fill in all required fields', 'error');
                    return;
                }

                await this.register(userData);
            });
        }
    }

    async register(userData) {
        try {
            const submitBtn = document.querySelector('#registerForm button[type="submit"]');
            submitBtn.textContent = 'Creating Account...';
            submitBtn.disabled = true;

            console.log('Sending registration data:', userData);

            const response = await fetch('http://localhost:8080/api/auth/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData)
            });

            console.log('Response status:', response.status);

            if (!response.ok) {
                // Handle validation errors from backend
                const errorResult = await response.json();
                console.log('Error response:', errorResult);

                if (errorResult.data) {
                    // Display specific field errors
                    const fieldErrors = Object.values(errorResult.data).join(', ');
                    throw new Error(fieldErrors);
                } else {
                    throw new Error(errorResult.message || 'Registration failed');
                }
            }

            const result = await response.json();
            console.log('Response data:', result);

            // FIXED: Use safe notification method
            this.showNotification('Account created successfully! Please login.', 'success');

            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);

        } catch (error) {
            console.error('Registration error:', error);
            this.showNotification(error.message || 'Registration failed', 'error');
        } finally {
            const submitBtn = document.querySelector('#registerForm button[type="submit"]');
            if (submitBtn) {
                submitBtn.textContent = 'Create Account';
                submitBtn.disabled = false;
            }
        }
    }

    setupLoginForm() {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                // Get values directly from input elements
                const credentials = {
                    username: document.getElementById('email').value, // Using email as username
                    password: document.getElementById('password').value
                };

                console.log('Login credentials:', credentials);
                await this.login(credentials);
            });
        }
    }

    async login(credentials) {
        try {
            const submitBtn = document.querySelector('#loginForm button[type="submit"]');
            submitBtn.textContent = 'Logging in...';
            submitBtn.disabled = true;

            const response = await fetch('http://localhost:8080/api/auth/signin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include', // IMPORTANT: This sends session cookies
                body: JSON.stringify(credentials)
            });

            console.log('Login response status:', response.status);

            if (!response.ok) {
                const errorResult = await response.json();
                throw new Error(errorResult.message || 'Login failed');
            }

            const result = await response.json();
            console.log('Login result:', result);

            if (response.ok && result.success) {
                this.showNotification('Login successful!', 'success');

                // Store user info in localStorage for display purposes only
                // But authentication is handled by session cookies
                if (result.data) {
                    localStorage.setItem('currentUser', JSON.stringify(result.data));
                }

                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 1000);
            } else {
                this.showNotification(result.message || 'Login failed', 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showNotification(error.message || 'Network error. Please try again.', 'error');
        } finally {
            const submitBtn = document.querySelector('#loginForm button[type="submit"]');
            if (submitBtn) {
                submitBtn.textContent = 'Login';
                submitBtn.disabled = false;
            }
        }
    }

    setupLogout() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                await this.logout();
            });
        }
    }

    async logout() {
        try {
            const response = await fetch('http://localhost:8080/api/auth/signout', {
                method: 'POST',
                credentials: 'include' // IMPORTANT: Send session cookie
            });

            // Clear localStorage
            localStorage.removeItem('currentUser');

            // Redirect to login page regardless of response
            window.location.href = 'login.html';

        } catch (error) {
            console.error('Logout error:', error);
            // Still redirect even if there's an error
            localStorage.removeItem('currentUser');
            window.location.href = 'login.html';
        }
    }

    // FIXED: Check actual session instead of localStorage
    async checkAuthStatus() {
        // Only check if we're on login/register pages
        if (window.location.pathname.includes('login.html') ||
            window.location.pathname.includes('register.html')) {

            try {
                const response = await fetch('http://localhost:8080/api/auth/me', {
                    method: 'GET',
                    credentials: 'include' // IMPORTANT: Send session cookie
                });

                if (response.ok) {
                    const result = await response.json();
                    if (result.success) {
                        // User has a valid session, redirect to dashboard
                        window.location.href = 'dashboard.html';
                    }
                }
                // If not authenticated, stay on login/register page
            } catch (error) {
                console.error('Auth check error:', error);
                // If there's an error, assume not authenticated
            }
        }
    }

    // FIXED: Safe notification method that works even without InkLinkUtils
    showNotification(message, type = 'info') {
        // Try to use InkLinkUtils if available
        if (typeof InkLinkUtils !== 'undefined' && InkLinkUtils.showNotification) {
            InkLinkUtils.showNotification(message, type);
            return;
        }

        // Fallback to simple alert
        if (type === 'error') {
            alert('Error: ' + message);
        } else if (type === 'success') {
            alert('Success: ' + message);
        } else {
            alert(message);
        }
    }
}

// Initialize auth manager when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    new AuthManager();
});