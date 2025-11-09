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

// Password Validation Functionality
document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const togglePassword = document.getElementById('togglePassword');
    const validationItems = {
        length: document.getElementById('length'),
        uppercase: document.getElementById('uppercase'),
        lowercase: document.getElementById('lowercase'),
        number: document.getElementById('number'),
        special: document.getElementById('special')
    };
    const strengthMeter = document.getElementById('strengthMeter');
    const strengthText = document.getElementById('strengthText');
    const registerForm = document.getElementById('registerForm');
    const passwordError = document.getElementById('passwordError');

    // Toggle password visibility
    if (togglePassword) {
        togglePassword.addEventListener('click', function() {
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                togglePassword.textContent = '🙈';
            } else {
                passwordInput.type = 'password';
                togglePassword.textContent = '👁️';
            }
        });
    }

    // Validate password on input
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            const password = passwordInput.value;

            // Check each validation rule
            const validations = {
                length: password.length >= 8,
                uppercase: /[A-Z]/.test(password),
                lowercase: /[a-z]/.test(password),
                number: /\d/.test(password),
                special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
            };

            // Update validation UI
            Object.keys(validations).forEach(key => {
                if (validationItems[key]) {
                    if (validations[key]) {
                        validationItems[key].classList.add('valid');
                        validationItems[key].classList.remove('invalid');
                    } else {
                        validationItems[key].classList.add('invalid');
                        validationItems[key].classList.remove('valid');
                    }
                }
            });

            // Calculate password strength
            updatePasswordStrength(password, validations);

            // Clear error message when user starts typing
            if (passwordError) {
                passwordError.textContent = '';
            }
        });

        // Validate password on form submission
        if (registerForm) {
            registerForm.addEventListener('submit', function(e) {
                const password = passwordInput.value;
                const validations = {
                    length: password.length >= 8,
                    uppercase: /[A-Z]/.test(password),
                    lowercase: /[a-z]/.test(password),
                    number: /\d/.test(password),
                    special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
                };

                const allValid = Object.values(validations).every(valid => valid);

                if (!allValid && passwordError) {
                    e.preventDefault();
                    passwordError.textContent = 'Please meet all password requirements';
                    passwordInput.focus();
                }
            });
        }
    }

    function updatePasswordStrength(password, validations) {
        if (!password) {
            strengthMeter.style.width = '0%';
            strengthText.textContent = 'Password Strength';
            strengthText.className = 'strength-text';
            return;
        }

        let strength = 0;
        const requirements = Object.values(validations);

        requirements.forEach(valid => {
            if (valid) strength++;
        });

        // Additional strength calculation based on password length
        if (password.length >= 12) strength++;
        if (password.length >= 16) strength++;

        const maxStrength = requirements.length + 2; // +2 for length bonuses
        const strengthPercent = (strength / maxStrength) * 100;

        strengthMeter.style.width = `${strengthPercent}%`;

        // Set colors and text based on strength
        if (strengthPercent < 40) {
            strengthMeter.className = 'strength-meter-fill strength-weak';
            strengthText.textContent = 'Weak Password';
            strengthText.style.color = '#dc3545';
        } else if (strengthPercent < 60) {
            strengthMeter.className = 'strength-meter-fill strength-medium';
            strengthText.textContent = 'Medium Password';
            strengthText.style.color = '#fd7e14';
        } else if (strengthPercent < 80) {
            strengthMeter.className = 'strength-meter-fill strength-strong';
            strengthText.textContent = 'Strong Password';
            strengthText.style.color = '#198754';
        } else {
            strengthMeter.className = 'strength-meter-fill strength-very-strong';
            strengthText.textContent = 'Very Strong Password';
            strengthText.style.color = '#0d6efd';
        }
    }

    // Initial validation state
    if (passwordInput) {
        passwordInput.dispatchEvent(new Event('input'));
    }
});