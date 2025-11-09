// InkLink Story Creation JavaScript
class StoryCreator {
    constructor() {
        this.currentUser = null;
        this.storyData = {
            title: '',
            summary: '',
            content: '',
            category: '',
            language: 'ENGLISH',
            tags: [],
            publishOption: 'PUBLIC',
            allowComments: true,
            allowLikes: true,
            matureContent: false
        };
        this.autoSaveInterval = null;
        this.hasUnsavedChanges = false;
        this.isFullscreen = false;
        this.init();
    }

    async init() {
        await this.checkAuthentication();
        this.setupEventListeners();
        this.loadDraft();
        this.startAutoSave();
        this.loadPopularTags();
    }

    async checkAuthentication() {
        try {
            // Check session with backend instead of localStorage
            const response = await fetch('/api/auth/me', {
                method: 'GET',
                credentials: 'include' // Important: include session cookies
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    this.currentUser = result.data;
                    this.updateAuthUI();
                    return; // User is authenticated
                }
            }

            // If we get here, user is not authenticated
            // Redirect to login if not authenticated
            window.location.href = 'login.html?redirect=create-story.html';
        } catch (error) {
            console.error('Auth check failed:', error);
            // Redirect to login on error
            window.location.href = 'login.html?redirect=create-story.html';
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
        }
    }

    setupEventListeners() {
        // Form inputs
        document.getElementById('storyTitle').addEventListener('input', (e) => {
            this.storyData.title = e.target.value;
            this.updateCharCounter('titleCharCount', e.target.value.length, 200);
            this.markUnsavedChanges();
        });

        document.getElementById('storySummary').addEventListener('input', (e) => {
            this.storyData.summary = e.target.value;
            this.updateCharCounter('summaryCharCount', e.target.value.length, 500);
            this.markUnsavedChanges();
        });

        document.getElementById('storyCategory').addEventListener('change', (e) => {
            this.storyData.category = e.target.value;
            this.markUnsavedChanges();
        });

        document.getElementById('storyLanguage').addEventListener('change', (e) => {
            this.storyData.language = e.target.value;
            this.markUnsavedChanges();
        });

        // Tags input
        document.getElementById('storyTags').addEventListener('input', (e) => {
            this.handleTagInput(e.target.value);
        });

        document.getElementById('storyTags').addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.addTag(e.target.value.trim());
                e.target.value = '';
                this.hideTagSuggestions();
            }
        });

        // Editor toolbar
        document.querySelectorAll('.tool-btn[data-command]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.executeCommand(
                    e.target.closest('.tool-btn').dataset.command,
                    e.target.closest('.tool-btn').dataset.value
                );
            });
        });

        // Editor content
        document.getElementById('storyContent').addEventListener('input', (e) => {
            this.storyData.content = e.target.innerHTML;
            this.updateStats();
            this.markUnsavedChanges();
        });

        document.getElementById('storyContent').addEventListener('paste', (e) => {
            this.handlePaste(e);
        });

        // Publishing options
        document.querySelectorAll('input[name="publishOption"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.storyData.publishOption = e.target.value;
                this.markUnsavedChanges();
            });
        });

        document.getElementById('allowComments').addEventListener('change', (e) => {
            this.storyData.allowComments = e.target.checked;
            this.markUnsavedChanges();
        });

        document.getElementById('allowLikes').addEventListener('change', (e) => {
            this.storyData.allowLikes = e.target.checked;
            this.markUnsavedChanges();
        });

        document.getElementById('matureContent').addEventListener('change', (e) => {
            this.storyData.matureContent = e.target.checked;
            this.markUnsavedChanges();
        });

        // Action buttons
        document.getElementById('saveDraftBtn').addEventListener('click', () => {
            this.saveDraft();
        });

        document.getElementById('previewBtn').addEventListener('click', () => {
            this.showPreview();
        });

        document.getElementById('saveDraftAction').addEventListener('click', () => {
            this.saveDraft();
        });

        document.getElementById('publishBtn').addEventListener('click', (e) => {
            e.preventDefault();
            this.publishStory();
        });

        document.getElementById('cancelBtn').addEventListener('click', () => {
            this.confirmCancel();
        });

        // Editor actions
        document.getElementById('clearFormatting').addEventListener('click', () => {
            this.clearFormatting();
        });

        document.getElementById('spellCheckBtn').addEventListener('click', () => {
            this.toggleSpellCheck();
        });

        document.getElementById('fullscreenBtn').addEventListener('click', () => {
            this.toggleFullscreen();
        });

        document.getElementById('insertImageBtn').addEventListener('click', () => {
            this.openImageUploadModal();
        });

        // Preview
        document.getElementById('closePreview').addEventListener('click', () => {
            this.hidePreview();
        });

        // Image upload modal
        document.getElementById('closeImageModal').addEventListener('click', () => {
            this.closeImageUploadModal();
        });

        document.querySelectorAll('.upload-option').forEach(option => {
            option.addEventListener('click', (e) => {
                this.switchUploadMethod(e.target.closest('.upload-option').dataset.method);
            });
        });

        document.getElementById('browseFiles').addEventListener('click', () => {
            document.getElementById('imageFile').click();
        });

        document.getElementById('imageFile').addEventListener('change', (e) => {
            this.handleImageUpload(e.target.files[0]);
        });

        document.getElementById('uploadArea').addEventListener('dragover', (e) => {
            e.preventDefault();
            document.getElementById('uploadArea').classList.add('dragover');
        });

        document.getElementById('uploadArea').addEventListener('dragleave', (e) => {
            e.preventDefault();
            document.getElementById('uploadArea').classList.remove('dragover');
        });

        document.getElementById('uploadArea').addEventListener('drop', (e) => {
            e.preventDefault();
            document.getElementById('uploadArea').classList.remove('dragover');
            const file = e.dataTransfer.files[0];
            if (file && file.type.startsWith('image/')) {
                this.handleImageUpload(file);
            }
        });

        document.getElementById('insertUrlImage').addEventListener('click', () => {
            this.insertImageFromUrl();
        });

        document.getElementById('changeImage').addEventListener('click', () => {
            this.showUploadArea();
        });

        document.getElementById('insertImage').addEventListener('click', () => {
            this.insertImageIntoEditor();
        });

        // Before unload warning
        window.addEventListener('beforeunload', (e) => {
            if (this.hasUnsavedChanges) {
                e.preventDefault();
                e.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
            }
        });

        // Markdown shortcuts
        document.getElementById('storyContent').addEventListener('keydown', (e) => {
            this.handleMarkdownShortcuts(e);
        });
    }

    // Tag Management
    handleTagInput(value) {
        if (value.length > 0) {
            this.showTagSuggestions(value);
        } else {
            this.hideTagSuggestions();
        }
    }

    async loadPopularTags() {
        try {
            // FIXED: Add credentials to API call
            const response = await fetch('/api/tags/popular', {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                this.displayPopularTags(result.data || []);
            }
        } catch (error) {
            console.error('Error loading popular tags:', error);
        }
    }

    displayPopularTags(tags) {
        const container = document.getElementById('popularTags');

        if (!tags || tags.length === 0) {
            container.innerHTML = '<span>No popular tags available</span>';
            return;
        }

        container.innerHTML = tags.map(tag => `
            <span class="tag" onclick="storyCreator.addTag('${this.escapeHtml(tag.name || tag)}')">
                ${this.escapeHtml(tag.name || tag)}
                <button type="button"><i class="fas fa-plus"></i></button>
            </span>
        `).join('');
    }

    showTagSuggestions(query) {
        const suggestions = document.getElementById('tagsSuggestions');
        const popularTags = Array.from(document.querySelectorAll('#popularTags .tag'))
            .map(tag => tag.textContent.replace('+', '').trim());

        // Filter tags that match the query
        const matchingTags = popularTags.filter(tag =>
            tag.toLowerCase().includes(query.toLowerCase()) &&
            !this.storyData.tags.includes(tag)
        ).slice(0, 5);

        if (matchingTags.length > 0) {
            suggestions.innerHTML = matchingTags.map(tag => `
                <div class="tag-suggestion" onclick="storyCreator.addTag('${this.escapeHtml(tag)}')">
                    ${this.escapeHtml(tag)}
                </div>
            `).join('');
            suggestions.classList.add('show');
        } else {
            this.hideTagSuggestions();
        }
    }

    hideTagSuggestions() {
        document.getElementById('tagsSuggestions').classList.remove('show');
    }

    addTag(tagName) {
        if (!tagName || tagName.length === 0) return;

        // Clean and validate tag
        const cleanTag = tagName.trim().toLowerCase().replace(/\s+/g, '-').substring(0, 20);

        if (cleanTag.length === 0) return;

        // Check if tag already exists
        if (this.storyData.tags.includes(cleanTag)) {
            this.showNotification('Tag already added', 'warning');
            return;
        }

        // Check tag limit
        if (this.storyData.tags.length >= 10) {
            this.showNotification('Maximum 10 tags allowed', 'warning');
            return;
        }

        this.storyData.tags.push(cleanTag);
        this.displayTags();
        this.hideTagSuggestions();
        document.getElementById('storyTags').value = '';
        this.markUnsavedChanges();
    }

    removeTag(tagName) {
        this.storyData.tags = this.storyData.tags.filter(tag => tag !== tagName);
        this.displayTags();
        this.markUnsavedChanges();
    }

    displayTags() {
        const container = document.getElementById('selectedTags');

        if (this.storyData.tags.length === 0) {
            container.innerHTML = '<span class="no-tags">No tags added yet</span>';
            return;
        }

        container.innerHTML = this.storyData.tags.map(tag => `
            <span class="tag">
                ${this.escapeHtml(tag)}
                <button type="button" onclick="storyCreator.removeTag('${this.escapeHtml(tag)}')">
                    <i class="fas fa-times"></i>
                </button>
            </span>
        `).join('');
    }

    // Editor Functionality
    executeCommand(command, value = null) {
        document.getElementById('storyContent').focus();

        try {
            if (command === 'formatBlock' && value) {
                document.execCommand(command, false, value);
            } else {
                document.execCommand(command, false, null);
            }

            // Update button active states
            this.updateToolbarState();
        } catch (error) {
            console.error('Error executing command:', error);
        }
    }

    updateToolbarState() {
        // Update bold, italic, underline states
        ['bold', 'italic', 'underline'].forEach(command => {
            const btn = document.querySelector(`[data-command="${command}"]`);
            if (btn) {
                btn.classList.toggle('active', document.queryCommandState(command));
            }
        });
    }

    clearFormatting() {
        document.getElementById('storyContent').focus();
        document.execCommand('removeFormat', false, null);
        document.execCommand('unlink', false, null);
    }

    toggleSpellCheck() {
        const editor = document.getElementById('storyContent');
        editor.spellcheck = !editor.spellcheck;

        const btn = document.getElementById('spellCheckBtn');
        btn.classList.toggle('active', editor.spellcheck);

        this.showNotification(
            `Spell check ${editor.spellcheck ? 'enabled' : 'disabled'}`,
            'info'
        );
    }

    toggleFullscreen() {
        const editor = document.querySelector('.editor-container');
        const btn = document.getElementById('fullscreenBtn');

        if (this.isFullscreen) {
            editor.classList.remove('fullscreen');
            btn.innerHTML = '<i class="fas fa-expand"></i>';
            btn.title = 'Fullscreen';
        } else {
            editor.classList.add('fullscreen');
            btn.innerHTML = '<i class="fas fa-compress"></i>';
            btn.title = 'Exit Fullscreen';
        }

        this.isFullscreen = !this.isFullscreen;
    }

    handlePaste(e) {
        e.preventDefault();

        // Get plain text from clipboard
        const text = (e.clipboardData || window.clipboardData).getData('text/plain');

        // Insert text at cursor position
        document.execCommand('insertText', false, text);
    }

    handleMarkdownShortcuts(e) {
        // Convert markdown shortcuts to formatting
        if (e.key === ' ' && e.ctrlKey) {
            const selection = window.getSelection();
            if (selection.rangeCount > 0) {
                const range = selection.getRangeAt(0);
                const text = range.toString();

                let replacement = null;
                let command = null;

                if (text.startsWith('**') && text.endsWith('**')) {
                    replacement = text.slice(2, -2);
                    command = 'bold';
                } else if (text.startsWith('*') && text.endsWith('*')) {
                    replacement = text.slice(1, -1);
                    command = 'italic';
                } else if (text.startsWith('_') && text.endsWith('_')) {
                    replacement = text.slice(1, -1);
                    command = 'italic';
                }

                if (replacement !== null && command !== null) {
                    e.preventDefault();
                    range.deleteContents();
                    range.insertNode(document.createTextNode(replacement));

                    // Select the replacement text and apply formatting
                    const newRange = document.createRange();
                    newRange.setStart(range.startContainer, range.startOffset);
                    newRange.setEnd(range.startContainer, range.startOffset + replacement.length);
                    selection.removeAllRanges();
                    selection.addRange(newRange);

                    document.execCommand(command, false, null);
                }
            }
        }
    }

    // Image Upload
    openImageUploadModal() {
        document.getElementById('imageUploadModal').classList.remove('hidden');
        this.switchUploadMethod('upload');
    }

    closeImageUploadModal() {
        document.getElementById('imageUploadModal').classList.add('hidden');
        this.showUploadArea();
    }

    switchUploadMethod(method) {
        document.querySelectorAll('.upload-option').forEach(option => {
            option.classList.toggle('active', option.dataset.method === method);
        });

        document.getElementById('uploadArea').classList.toggle('hidden', method !== 'upload');
        document.getElementById('urlInput').classList.toggle('hidden', method !== 'url');

        // Reset
        if (method === 'upload') {
            document.getElementById('imageFile').value = '';
            this.showUploadArea();
        } else {
            document.getElementById('imageUrl').value = '';
        }
    }

    handleImageUpload(file) {
        if (!file || !file.type.startsWith('image/')) {
            this.showNotification('Please select a valid image file', 'error');
            return;
        }

        // Check file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
            this.showNotification('Image must be smaller than 5MB', 'error');
            return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
            document.getElementById('previewImage').src = e.target.result;
            document.getElementById('uploadContent').classList.add('hidden');
            document.getElementById('uploadPreview').classList.remove('hidden');
        };
        reader.readAsDataURL(file);
    }

    showUploadArea() {
        document.getElementById('uploadContent').classList.remove('hidden');
        document.getElementById('uploadPreview').classList.add('hidden');
        document.getElementById('imageFile').value = '';
    }

    insertImageFromUrl() {
        const url = document.getElementById('imageUrl').value.trim();

        if (!url) {
            this.showNotification('Please enter an image URL', 'error');
            return;
        }

        // Basic URL validation
        try {
            new URL(url);
        } catch {
            this.showNotification('Please enter a valid URL', 'error');
            return;
        }

        document.getElementById('previewImage').src = url;
        document.getElementById('uploadContent').classList.add('hidden');
        document.getElementById('uploadPreview').classList.remove('hidden');
    }

    insertImageIntoEditor() {
        const imageUrl = document.getElementById('previewImage').src;

        if (!imageUrl) {
            this.showNotification('No image to insert', 'error');
            return;
        }

        // Insert image into editor
        document.getElementById('storyContent').focus();
        document.execCommand('insertHTML', false, `<img src="${imageUrl}" alt="Story image" style="max-width: 100%; height: auto;">`);

        this.closeImageUploadModal();
        this.markUnsavedChanges();
    }

    // Preview
    showPreview() {
        if (!this.validateForm(true)) {
            return;
        }

        this.updatePreview();
        document.getElementById('previewSidebar').classList.add('open');
    }

    hidePreview() {
        document.getElementById('previewSidebar').classList.remove('open');
    }

    updatePreview() {
        const preview = document.getElementById('previewContent');

        preview.innerHTML = `
            <article class="story-preview">
                <header class="preview-header">
                    <div class="category-badge">${this.storyData.category || 'Uncategorized'}</div>
                    <h1>${this.escapeHtml(this.storyData.title) || 'Untitled Story'}</h1>
                    <div class="author-info">
                        <div class="author-avatar">${this.currentUser?.username?.charAt(0)?.toUpperCase() || 'U'}</div>
                        <div class="author-details">
                            <div class="author-name">${this.currentUser?.username || 'User'}</div>
                            <div class="publish-date">Preview</div>
                        </div>
                    </div>
                    ${this.storyData.summary ? `
                        <div class="story-summary">
                            <p>${this.escapeHtml(this.storyData.summary)}</p>
                        </div>
                    ` : ''}
                    ${this.storyData.tags.length > 0 ? `
                        <div class="story-tags">
                            ${this.storyData.tags.map(tag => `
                                <span class="story-tag">${this.escapeHtml(tag)}</span>
                            `).join('')}
                        </div>
                    ` : ''}
                </header>
                <div class="preview-content">
                    ${this.storyData.content || '<p><em>Start writing your story...</em></p>'}
                </div>
            </article>
        `;
    }

    // Stats and Counters
    updateStats() {
        const content = this.storyData.content;

        // Remove HTML tags for accurate counting
        const textContent = content.replace(/<[^>]*>/g, '');

        const wordCount = textContent.trim() ? textContent.trim().split(/\s+/).length : 0;
        const charCount = textContent.length;
        const readingTime = Math.ceil(wordCount / 200); // 200 words per minute

        document.getElementById('wordCount').textContent = wordCount.toLocaleString();
        document.getElementById('charCount').textContent = charCount.toLocaleString();
        document.getElementById('readingTime').textContent = `${readingTime} min`;
    }

    updateCharCounter(elementId, count, max) {
        const element = document.getElementById(elementId);
        element.textContent = count;
        element.style.color = count > max ? 'var(--danger)' : 'var(--gray-500)';
    }

    // Auto-save and Draft Management
    startAutoSave() {
        this.autoSaveInterval = setInterval(() => {
            if (this.hasUnsavedChanges) {
                this.saveDraft(true);
            }
        }, 30000); // Auto-save every 30 seconds
    }

    stopAutoSave() {
        if (this.autoSaveInterval) {
            clearInterval(this.autoSaveInterval);
        }
    }

    saveDraft(isAutoSave = false) {
        if (!this.hasUnsavedChanges && !isAutoSave) {
            this.showNotification('No changes to save', 'info');
            return;
        }

        const draft = {
            ...this.storyData,
            savedAt: new Date().toISOString(),
            wordCount: this.getWordCount()
        };

        localStorage.setItem('inklink_story_draft', JSON.stringify(draft));
        this.hasUnsavedChanges = false;

        this.showAutoSaveIndicator(isAutoSave ? 'auto' : 'manual');

        if (!isAutoSave) {
            this.showNotification('Draft saved successfully', 'success');
        }
    }

    loadDraft() {
        try {
            const draft = localStorage.getItem('inklink_story_draft');
            if (draft) {
                const parsedDraft = JSON.parse(draft);
                this.storyData = { ...this.storyData, ...parsedDraft };
                this.populateForm();
                this.showNotification('Draft loaded successfully', 'info');
            }
        } catch (error) {
            console.error('Error loading draft:', error);
        }
    }

    populateForm() {
        // Populate form fields with draft data
        document.getElementById('storyTitle').value = this.storyData.title;
        document.getElementById('storySummary').value = this.storyData.summary;
        document.getElementById('storyCategory').value = this.storyData.category;
        document.getElementById('storyLanguage').value = this.storyData.language;
        document.getElementById('storyContent').innerHTML = this.storyData.content;

        // Update counters
        this.updateCharCounter('titleCharCount', this.storyData.title.length, 200);
        this.updateCharCounter('summaryCharCount', this.storyData.summary.length, 500);

        // Update tags
        this.displayTags();

        // Update publishing options
        const publishOptionRadio = document.querySelector(`input[name="publishOption"][value="${this.storyData.publishOption}"]`);
        if (publishOptionRadio) {
            publishOptionRadio.checked = true;
        }
        document.getElementById('allowComments').checked = this.storyData.allowComments;
        document.getElementById('allowLikes').checked = this.storyData.allowLikes;
        document.getElementById('matureContent').checked = this.storyData.matureContent;

        // Update stats
        this.updateStats();
    }

    clearDraft() {
        localStorage.removeItem('inklink_story_draft');
        this.hasUnsavedChanges = false;
    }

    // Publishing
    async publishStory() {
        if (!this.validateForm()) {
            return;
        }

        try {
            this.showLoadingState('Publishing your story...');

            // Map frontend category to backend genre with validation
            const categoryToGenreMap = {
                'FICTION': 'FICTION',
                'NON_FICTION': 'NON_FICTION',
                'FANTASY': 'FANTASY',
                'SCI_FI': 'SCI_FI',
                'MYSTERY': 'MYSTERY',
                'ROMANCE': 'ROMANCE',
                'HORROR': 'HORROR',
                'POETRY': 'POETRY',
                'BIOGRAPHY': 'BIOGRAPHY',
                'HISTORICAL': 'HISTORICAL', // Add this mapping
                'OTHER': 'OTHER'
            };

            const genre = categoryToGenreMap[this.storyData.category];

            if (!genre) {
                throw new Error(`Invalid genre: ${this.storyData.category}. Please select a valid genre.`);
            }

            // Send only the fields that backend expects
            const storyData = {
                title: this.storyData.title,
                content: this.storyData.content,
                genre: genre, // Use mapped genre value
                status: "PUBLISHED"
            };

            console.log('Sending story data:', storyData);

            const response = await fetch('/api/stories', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(storyData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                console.error('Backend error:', errorData);
                throw new Error(errorData.message || 'Failed to publish story');
            }

            const result = await response.json();
            console.log('Publish success:', result);

            this.clearDraft();
            this.hideLoadingState();
            this.showNotification('Story published successfully!', 'success');

            // Redirect to the published story
            setTimeout(() => {
                if (result.data && result.data.id) {
                    window.location.href = `story.html?id=${result.data.id}`;
                } else {
                    window.location.href = 'stories.html';
                }
            }, 2000);

        } catch (error) {
            this.hideLoadingState();
            console.error('Error publishing story:', error);
            this.showNotification(error.message || 'Failed to publish story. Please try again.', 'error');
        }
    }
    validateGenre(category) {
        const validGenres = [
            'FICTION', 'NON_FICTION', 'FANTASY', 'SCI_FI',
            'MYSTERY', 'ROMANCE', 'HORROR', 'POETRY',
            'BIOGRAPHY', 'HISTORICAL', 'OTHER'
        ];

        return validGenres.includes(category);
    }

    validateForm(isPreview = false) {
        const errors = [];

        if (!this.storyData.title || this.storyData.title.trim().length < 3) {
            errors.push('Story title must be at least 3 characters');
        }

        if (!this.storyData.category) {
            errors.push('Please select a genre');
        } else if (!this.validateGenre(this.storyData.category)) {
            errors.push('Please select a valid genre');
        }

        if (!this.storyData.content || this.storyData.content.trim().length < 10) {
            errors.push('Story content must be at least 10 characters');
        }

        if (errors.length > 0) {
            if (!isPreview) {
                this.showNotification(errors.join(', '), 'error');
            }
            return false;
        }

        return true;
    }

    confirmCancel() {
        if (this.hasUnsavedChanges) {
            if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
                this.clearDraft();
                window.location.href = 'stories.html';
            }
        } else {
            window.location.href = 'stories.html';
        }
    }

    // UI Helpers
    markUnsavedChanges() {
        this.hasUnsavedChanges = true;
        this.updateAutoSaveIndicator('saving');
    }

    showAutoSaveIndicator(type) {
        const indicator = document.getElementById('autoSaveIndicator');
        const icon = indicator.querySelector('i');
        const text = indicator.querySelector('span');

        if (type === 'auto') {
            icon.className = 'fas fa-sync-alt';
            text.textContent = 'Auto-saving...';
            indicator.className = 'auto-save-indicator saving';
        } else if (type === 'manual') {
            icon.className = 'fas fa-check-circle';
            text.textContent = 'All changes saved';
            indicator.className = 'auto-save-indicator';
        }

        indicator.style.display = 'flex';

        // Hide after 3 seconds for auto-save, 5 seconds for manual save
        setTimeout(() => {
            indicator.style.display = 'none';
        }, type === 'auto' ? 3000 : 5000);
    }

    updateAutoSaveIndicator(state) {
        const indicator = document.getElementById('autoSaveIndicator');
        const icon = indicator.querySelector('i');
        const text = indicator.querySelector('span');

        if (state === 'saving') {
            icon.className = 'fas fa-sync-alt fa-spin';
            text.textContent = 'Saving...';
            indicator.className = 'auto-save-indicator saving';
        }

        indicator.style.display = 'flex';
    }

    showLoadingState(message = 'Loading...') {
        // You could implement a loading overlay here
        const publishBtn = document.getElementById('publishBtn');
        if (publishBtn) {
            publishBtn.disabled = true;
            publishBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Publishing...';
        }
    }

    hideLoadingState() {
        const publishBtn = document.getElementById('publishBtn');
        if (publishBtn) {
            publishBtn.disabled = false;
            publishBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Publish Story';
        }
    }

    showNotification(message, type = 'info') {
        // Use existing notification system
        if (window.app) {
            window.app.showNotification(message, type);
        } else if (window.authManager) {
            window.authManager.showNotification(message, type);
        } else {
            alert(message);
        }
    }

    getWordCount() {
        const text = this.storyData.content.replace(/<[^>]*>/g, '');
        return text.trim() ? text.trim().split(/\s+/).length : 0;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize the story creator
const storyCreator = new StoryCreator();

// Make globally available
window.storyCreator = storyCreator;