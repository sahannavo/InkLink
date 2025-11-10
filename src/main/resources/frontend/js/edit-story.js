// InkLink Story Editing JavaScript
class StoryEditor extends StoryCreator {
    constructor() {
        super();
        this.storyId = this.getStoryIdFromURL();
        this.originalStoryData = null;
        this.versions = [];
        this.selectedVersion = null;
        this.changesDetected = false;
        this.initEdit();
    }

    async initEdit() {
        await this.loadStory();
        this.setupEditEventListeners();
        this.loadVersions();
        this.loadEditHistory();
        this.startChangeDetection();
    }

    getStoryIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('id');
    }

    async loadStory() {
        if (!this.storyId) {
            this.showNotification('Invalid story ID', 'error');
            window.location.href = 'stories.html';
            return;
        }

        try {
            this.showLoadingState('Loading story...');

            // Load story data
            this.storyData = await api.stories.getById(this.storyId);
            this.originalStoryData = JSON.parse(JSON.stringify(this.storyData));

            // Verify ownership
            if (this.storyData.author?.id !== this.currentUser?.id) {
                this.showNotification('You can only edit your own stories', 'error');
                window.location.href = 'stories.html';
                return;
            }

            this.populateForm();
            this.updateStatsDisplay();
            this.hideLoadingState();

        } catch (error) {
            console.error('Error loading story:', error);
            this.showNotification('Failed to load story', 'error');
            window.location.href = 'stories.html';
        }
    }

    populateForm() {
        super.populateForm();

        // Update display elements
        document.getElementById('storyTitleDisplay').textContent = this.storyData.title;
        document.title = `Edit: ${this.storyData.title} - InkLink`;
    }

    updateStatsDisplay() {
        document.getElementById('editViewCount').textContent =
            this.formatNumber(this.storyData.viewCount || 0);
        document.getElementById('editLikeCount').textContent =
            this.formatNumber(this.storyData.likeCount || 0);
        document.getElementById('editCommentCount').textContent =
            this.formatNumber(this.storyData.commentCount || 0);
    }

    setupEditEventListeners() {
        // Additional edit-specific event listeners
        document.getElementById('viewStoryBtn').addEventListener('click', () => {
            this.viewStory();
        });

        document.getElementById('showChangesBtn').addEventListener('click', () => {
            this.showChanges();
        });

        document.getElementById('compareVersionsBtn').addEventListener('click', () => {
            this.toggleVersionComparison();
        });

        document.getElementById('restoreVersionBtn').addEventListener('click', () => {
            this.restoreVersion();
        });

        document.getElementById('analyzeContentBtn').addEventListener('click', () => {
            this.analyzeContent();
        });

        document.getElementById('previewBtn').addEventListener('click', () => {
            this.showPreviewWithChanges();
        });

        document.getElementById('updateStoryBtn').addEventListener('click', (e) => {
            e.preventDefault();
            this.showUpdateConfirmation();
        });

        // Version comparison
        document.querySelectorAll('.comparison-tab').forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchComparisonPane(e.target.dataset.pane);
            });
        });

        // Changes modal
        document.getElementById('closeChangesModal').addEventListener('click', () => {
            this.closeChangesModal();
        });

        document.getElementById('cancelChanges').addEventListener('click', () => {
            this.closeChangesModal();
        });

        document.getElementById('confirmUpdate').addEventListener('click', () => {
            this.updateStory();
        });
    }

    startChangeDetection() {
        // Monitor for changes compared to original
        setInterval(() => {
            this.detectChanges();
        }, 2000);
    }

    detectChanges() {
        const changes = this.getChanges();
        this.changesDetected = changes.hasChanges;

        if (changes.hasChanges) {
            document.querySelector('.edit-notice').innerHTML = `
                <i class="fas fa-edit"></i>
                You have unsaved changes. Update the story to make them visible to readers.
                ${changes.summary ? `<br><small>Changes: ${changes.summary}</small>` : ''}
            `;
        } else {
            document.querySelector('.edit-notice').innerHTML = `
                <i class="fas fa-info-circle"></i>
                You are editing a published story. Changes will be visible to readers after you update the story.
            `;
        }
    }

    getChanges() {
        const changes = {
            hasChanges: false,
            summary: '',
            details: {}
        };

        // Check title
        if (this.storyData.title !== this.originalStoryData.title) {
            changes.hasChanges = true;
            changes.details.title = 'modified';
        }

        // Check summary
        if (this.storyData.summary !== this.originalStoryData.summary) {
            changes.hasChanges = true;
            changes.details.summary = 'modified';
        }

        // Check category
        if (this.storyData.category !== this.originalStoryData.category) {
            changes.hasChanges = true;
            changes.details.category = 'modified';
        }

        // Check content
        if (this.storyData.content !== this.originalStoryData.content) {
            changes.hasChanges = true;
            changes.details.content = 'modified';

            // Calculate content changes
            const originalText = this.stripHtml(this.originalStoryData.content);
            const currentText = this.stripHtml(this.storyData.content);
            const wordDiff = this.calculateWordDiff(originalText, currentText);

            if (wordDiff.added > 0 || wordDiff.removed > 0) {
                changes.summary = `${wordDiff.added > 0 ? `+${wordDiff.added} words` : ''} ${wordDiff.removed > 0 ? `-${wordDiff.removed} words` : ''}`.trim();
            }
        }

        // Check tags
        const originalTags = this.originalStoryData.tags?.join(',') || '';
        const currentTags = this.storyData.tags.join(',');
        if (originalTags !== currentTags) {
            changes.hasChanges = true;
            changes.details.tags = 'modified';
        }

        // Check publishing options
        if (this.storyData.publishOption !== this.originalStoryData.publishOption ||
            this.storyData.allowComments !== this.originalStoryData.allowComments ||
            this.storyData.allowLikes !== this.originalStoryData.allowLikes ||
            this.storyData.matureContent !== this.originalStoryData.matureContent) {
            changes.hasChanges = true;
            changes.details.settings = 'modified';
        }

        return changes;
    }

    stripHtml(html) {
        const tmp = document.createElement('div');
        tmp.innerHTML = html;
        return tmp.textContent || tmp.innerText || '';
    }

    calculateWordDiff(original, current) {
        const originalWords = original.trim().split(/\s+/).filter(w => w.length > 0);
        const currentWords = current.trim().split(/\s+/).filter(w => w.length > 0);

        return {
            added: Math.max(0, currentWords.length - originalWords.length),
            removed: Math.max(0, originalWords.length - currentWords.length),
            total: currentWords.length
        };
    }

    async loadVersions() {
        try {
            // In a real implementation, you'd have a versions API endpoint
            // For now, we'll create mock version data
            this.versions = [
                {
                    id: 1,
                    version: '1.0',
                    content: this.originalStoryData.content,
                    createdAt: this.originalStoryData.createdAt,
                    author: this.originalStoryData.author,
                    wordCount: this.getWordCount(this.originalStoryData.content),
                    changes: 'Initial publication'
                },
                {
                    id: 2,
                    version: '1.1',
                    content: this.storyData.content, // Current edited version
                    createdAt: new Date().toISOString(),
                    author: this.currentUser,
                    wordCount: this.getWordCount(this.storyData.content),
                    changes: 'Current edits'
                }
            ];

            this.displayVersions();

        } catch (error) {
            console.error('Error loading versions:', error);
        }
    }

    displayVersions() {
        const container = document.getElementById('versionList');

        container.innerHTML = this.versions.map(version => `
            <div class="version-item ${version.id === this.versions[this.versions.length - 1].id ? 'current' : ''}" 
                 data-version-id="${version.id}">
                <div class="version-meta">
                    <div>
                        <strong>Version ${version.version}</strong>
                        <div class="version-date">
                            ${new Date(version.createdAt).toLocaleDateString()} 
                            at ${new Date(version.createdAt).toLocaleTimeString()}
                        </div>
                        ${version.changes ? `
                            <div class="revision-changes">${version.changes}</div>
                        ` : ''}
                    </div>
                </div>
                <div class="version-stats">
                    <span>${version.wordCount} words</span>
                    <span>by ${version.author?.username || 'You'}</span>
                </div>
            </div>
        `).join('');

        // Add click listeners
        container.querySelectorAll('.version-item').forEach(item => {
            item.addEventListener('click', (e) => {
                this.selectVersion(parseInt(e.currentTarget.dataset.versionId));
            });
        });
    }

    selectVersion(versionId) {
        this.selectedVersion = this.versions.find(v => v.id === versionId);

        // Update UI
        document.querySelectorAll('.version-item').forEach(item => {
            item.classList.remove('current');
        });
        document.querySelector(`[data-version-id="${versionId}"]`).classList.add('current');

        this.updateComparisonView();
    }

    toggleVersionComparison() {
        const comparisonView = document.getElementById('comparisonView');
        comparisonView.classList.toggle('active');

        if (comparisonView.classList.contains('active') && !this.selectedVersion) {
            // Select the first version if none selected
            this.selectVersion(this.versions[0].id);
        }
    }

    switchComparisonPane(pane) {
        document.querySelectorAll('.comparison-tab').forEach(tab => {
            tab.classList.toggle('active', tab.dataset.pane === pane);
        });

        // In a real implementation, you'd update the pane content
        // For now, we'll just log the action
        console.log('Switched to pane:', pane);
    }

    updateComparisonView() {
        if (!this.selectedVersion) return;

        const currentPane = document.getElementById('currentVersionPane');
        const selectedPane = document.getElementById('selectedVersionPane');

        // Show simple diff (in a real app, you'd use a proper diff library)
        currentPane.innerHTML = this.formatContentForComparison(this.storyData.content);
        selectedPane.innerHTML = this.formatContentForComparison(this.selectedVersion.content);
    }

    formatContentForComparison(content) {
        // Simple formatting for comparison view
        return content
            .split('\n')
            .map(paragraph => {
                if (paragraph.trim() === '') return '<br>';
                return `<p>${this.escapeHtml(paragraph)}</p>`;
            })
            .join('');
    }

    async restoreVersion() {
        if (!this.selectedVersion) {
            this.showNotification('Please select a version to restore', 'warning');
            return;
        }

        if (!confirm('Are you sure you want to restore this version? Current changes will be lost.')) {
            return;
        }

        try {
            // Restore the selected version content
            this.storyData.content = this.selectedVersion.content;
            document.getElementById('storyContent').innerHTML = this.storyData.content;

            this.markUnsavedChanges();
            this.showNotification('Version restored successfully', 'success');
            this.toggleVersionComparison(); // Hide comparison view

        } catch (error) {
            console.error('Error restoring version:', error);
            this.showNotification('Failed to restore version', 'error');
        }
    }

    async loadEditHistory() {
        try {
            // Mock edit history data
            const history = [
                {
                    id: 1,
                    action: 'edited',
                    user: this.currentUser,
                    timestamp: new Date().toISOString(),
                    details: 'Updated story content',
                    changes: '+150 words'
                },
                {
                    id: 2,
                    action: 'published',
                    user: this.currentUser,
                    timestamp: this.originalStoryData.createdAt,
                    details: 'First publication',
                    changes: 'Initial version'
                }
            ];

            this.displayEditHistory(history);

        } catch (error) {
            console.error('Error loading edit history:', error);
        }
    }

    displayEditHistory(history) {
        const container = document.getElementById('editHistoryList');

        if (!history || history.length === 0) {
            container.innerHTML = '<p>No edit history available</p>';
            return;
        }

        container.innerHTML = history.map(item => `
            <div class="history-item">
                <div class="history-avatar">
                    ${item.user?.username?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <div class="history-content">
                    <div class="history-action">
                        ${this.capitalizeFirstLetter(item.action)}
                        ${item.changes ? `<span class="change-indicator">${item.changes}</span>` : ''}
                    </div>
                    <div class="history-date">
                        ${new Date(item.timestamp).toLocaleDateString()} at 
                        ${new Date(item.timestamp).toLocaleTimeString()}
                    </div>
                    <div class="history-details">
                        ${item.details}
                    </div>
                </div>
            </div>
        `).join('');
    }

    analyzeContent() {
        const content = this.stripHtml(this.storyData.content);
        const words = content.trim().split(/\s+/).filter(w => w.length > 0);
        const sentences = content.split(/[.!?]+/).filter(s => s.trim().length > 0);
        const paragraphs = this.storyData.content.split('</p>').filter(p => p.trim().length > 0);

        const analysis = {
            wordCount: words.length,
            sentenceCount: sentences.length,
            paragraphCount: paragraphs.length,
            avgSentenceLength: words.length / Math.max(sentences.length, 1),
            readingTime: Math.ceil(words.length / 200),
            readability: this.calculateReadability(words, sentences)
        };

        this.showContentAnalysis(analysis);
    }

    calculateReadability(words, sentences) {
        // Simple readability score (you could implement more complex algorithms)
        const avgSentenceLength = words.length / Math.max(sentences.length, 1);
        const avgWordLength = words.reduce((sum, word) => sum + word.length, 0) / Math.max(words.length, 1);

        let score = 'Standard';
        if (avgSentenceLength > 20 || avgWordLength > 6) {
            score = 'Difficult';
        } else if (avgSentenceLength < 10 && avgWordLength < 5) {
            score = 'Easy';
        }

        return score;
    }

    showContentAnalysis(analysis) {
        const warnings = [];

        if (analysis.wordCount < 100) {
            warnings.push('Consider adding more content to engage readers');
        }

        if (analysis.avgSentenceLength > 25) {
            warnings.push('Some sentences are quite long. Consider breaking them up for better readability');
        }

        if (analysis.readability === 'Difficult') {
            warnings.push('Content may be difficult for some readers. Consider simplifying language');
        }

        this.displayWarnings(warnings, analysis);
    }

    displayWarnings(warnings, analysis) {
        const container = document.getElementById('editorWarnings');
        const list = document.getElementById('warningsList');

        if (warnings.length === 0) {
            container.classList.add('hidden');
            return;
        }

        list.innerHTML = warnings.map(warning => `
            <div class="warning-item">
                <i class="fas fa-exclamation-circle"></i>
                <span>${warning}</span>
            </div>
        `).join('');

        // Add analysis summary
        list.innerHTML += `
            <div class="analysis-summary" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid var(--gray-300);">
                <strong>Content Analysis:</strong><br>
                • ${analysis.wordCount} words<br>
                • ${analysis.sentenceCount} sentences<br>
                • ${analysis.paragraphCount} paragraphs<br>
                • ${analysis.readingTime} min read<br>
                • Readability: ${analysis.readability}
            </div>
        `;

        container.classList.remove('hidden');
    }

    showChanges() {
        const changes = this.getChanges();

        if (!changes.hasChanges) {
            this.showNotification('No changes detected', 'info');
            return;
        }

        this.showChangesSummary(changes);
    }

    showChangesSummary(changes) {
        const container = document.getElementById('changesSummary');

        let summaryHtml = '<h4>Changes Summary</h4><ul style="margin: 1rem 0; padding-left: 1.5rem;">';

        if (changes.details.title) {
            summaryHtml += `<li>Title modified</li>`;
        }

        if (changes.details.summary) {
            summaryHtml += `<li>Summary modified</li>`;
        }

        if (changes.details.content) {
            const originalText = this.stripHtml(this.originalStoryData.content);
            const currentText = this.stripHtml(this.storyData.content);
            const wordDiff = this.calculateWordDiff(originalText, currentText);

            summaryHtml += `<li>Content modified: ${wordDiff.added > 0 ? `+${wordDiff.added} words` : ''} ${wordDiff.removed > 0 ? `-${wordDiff.removed} words` : ''}</li>`;
        }

        if (changes.details.tags) {
            summaryHtml += `<li>Tags updated</li>`;
        }

        if (changes.details.settings) {
            summaryHtml += `<li>Publishing settings modified</li>`;
        }

        summaryHtml += '</ul>';

        // Add major update warning
        const isMajorUpdate = document.getElementById('majorUpdate').checked;
        if (isMajorUpdate) {
            summaryHtml += `
                <div class="edit-notice" style="margin: 1rem 0;">
                    <i class="fas fa-bell"></i>
                    This is marked as a major update. Followers will be notified and reader positions may be reset.
                </div>
            `;
        }

        container.innerHTML = summaryHtml;
        document.getElementById('changesModal').classList.remove('hidden');
    }

    closeChangesModal() {
        document.getElementById('changesModal').classList.add('hidden');
    }

    showUpdateConfirmation() {
        if (!this.validateForm()) {
            return;
        }

        const changes = this.getChanges();
        if (!changes.hasChanges) {
            this.showNotification('No changes to update', 'info');
            return;
        }

        this.showChangesSummary(changes);
    }

    async updateStory() {
        try {
            this.showLoadingState('Updating story...');

            const storyData = {
                title: this.storyData.title,
                content: this.storyData.content,
                summary: this.storyData.summary,
                category: this.storyData.category,
                language: this.storyData.language,
                tags: this.storyData.tags,
                isPublic: this.storyData.publishOption === 'PUBLIC',
                allowComments: this.storyData.allowComments,
                allowLikes: this.storyData.allowLikes,
                matureContent: this.storyData.matureContent,
                majorUpdate: document.getElementById('majorUpdate').checked
            };

            const result = await api.stories.update(this.storyId, storyData);

            // Update original data to current state
            this.originalStoryData = JSON.parse(JSON.stringify(this.storyData));
            this.changesDetected = false;

            this.closeChangesModal();
            this.hideLoadingState();
            this.showNotification('Story updated successfully!', 'success');

            // Update the display
            this.updateStatsDisplay();
            document.getElementById('storyTitleDisplay').textContent = this.storyData.title;

        } catch (error) {
            this.hideLoadingState();
            console.error('Error updating story:', error);
            this.showNotification('Failed to update story. Please try again.', 'error');
        }
    }

    viewStory() {
        window.open(`story.html?id=${this.storyId}`, '_blank');
    }

    showPreviewWithChanges() {
        if (!this.validateForm(true)) {
            return;
        }

        // Show changes in preview
        const changes = this.getChanges();
        this.updatePreview();

        if (changes.hasChanges) {
            const previewContent = document.getElementById('previewContent');
            const notice = document.createElement('div');
            notice.className = 'edit-notice';
            notice.innerHTML = '<i class="fas fa-edit"></i> Preview showing unsaved changes';
            previewContent.prepend(notice);
        }

        document.getElementById('previewSidebar').classList.add('open');
    }

    // Override parent methods for edit-specific behavior
    markUnsavedChanges() {
        super.markUnsavedChanges();
        this.detectChanges(); // Update change detection immediately
    }

    confirmCancel() {
        if (this.hasUnsavedChanges || this.changesDetected) {
            if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
                window.location.href = `story.html?id=${this.storyId}`;
            }
        } else {
            window.location.href = `story.html?id=${this.storyId}`;
        }
    }

    // Utility methods
    capitalizeFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }

    getWordCount(content) {
        const text = content.replace(/<[^>]*>/g, '');
        return text.trim() ? text.trim().split(/\s+/).length : 0;
    }
}

// Initialize the story editor
const storyEditor = new StoryEditor();

// Make globally available
window.storyEditor = storyEditor;