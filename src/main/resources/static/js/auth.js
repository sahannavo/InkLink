// Password visibility toggle
document.addEventListener('DOMContentLoaded', function() {
    // Toggle password visibility
    const togglePassword = document.getElementById('togglePassword');
    if (togglePassword) {
        togglePassword.addEventListener('click', function() {
            const passwordInput = document.getElementById('password');
            const icon = this.querySelector('i');

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.classList.replace('fa-eye', 'fa-eye-slash');
            } else {
                passwordInput.type = 'password';
                icon.classList.replace('fa-eye-slash', 'fa-eye');
            }
        });
    }

    // Password strength indicator
    const passwordInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');

    if (passwordInput) {
        passwordInput.addEventListener('input', checkPasswordStrength);
    }

    if (confirmInput) {
        confirmInput.addEventListener('input', checkPasswordMatch);
    }

    // Form validation
    const registerForm = document.querySelector('form[th\\:object="${user}"]');
    if (registerForm) {
        registerForm.addEventListener('submit', validateForm);
    }
});

function checkPasswordStrength() {
    const password = this.value;
    const strengthBar = document.getElementById('passwordStrength');
    const feedback = document.getElementById('passwordFeedback');

    let strength = 0;
    let feedbackText = '';

    if (password.length >= 8) strength += 25;
    if (/[A-Z]/.test(password)) strength += 25;
    if (/[0-9]/.test(password)) strength += 25;
    if (/[^A-Za-z0-9]/.test(password)) strength += 25;

    strengthBar.style.width = strength + '%';

    if (strength < 50) {
        strengthBar.className = 'progress-bar bg-danger';
        feedbackText = 'Weak password';
    } else if (strength < 75) {
        strengthBar.className = 'progress-bar bg-warning';
        feedbackText = 'Medium strength';
    } else {
        strengthBar.className = 'progress-bar bg-success';
        feedbackText = 'Strong password';
    }

    if (feedback) {
        feedback.textContent = feedbackText;
        feedback.className = 'form-text ' + (strength < 50 ? 'text-danger' : strength < 75 ? 'text-warning' : 'text-success');
    }
}

function checkPasswordMatch() {
    const password = document.getElementById('password').value;
    const confirmPassword = this.value;
    const errorElement = document.getElementById('passwordMatchError');

    if (confirmPassword && password !== confirmPassword) {
        errorElement.textContent = 'Passwords do not match';
        this.classList.add('is-invalid');
    } else {
        errorElement.textContent = '';
        this.classList.remove('is-invalid');
        if (confirmPassword) {
            this.classList.add('is-valid');
        }
    }
}

function validateForm(e) {
    const terms = document.getElementById('terms');
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!terms.checked) {
        e.preventDefault();
        terms.focus();
        return false;
    }

    if (password !== confirmPassword) {
        e.preventDefault();
        document.getElementById('confirmPassword').focus();
        return false;
    }

    // Show loading state
    const btn = document.getElementById('registerBtn');
    btn.classList.add('btn-loading');
    btn.disabled = true;

    return true;
}