// API endpoint - adjust based on your server configuration
const API_BASE = '/api/backups';

// View state
let currentView = 'list'; // 'list', 'card', or 'gallery'

// Color palette for gallery view
const COLOR_PALETTE = [
    '#667eea', '#764ba2', '#f093fb', '#4facfe',
    '#43e97b', '#fa709a', '#fee140', '#30cfd0',
    '#a8edea', '#fed6e3', '#c471f5', '#12c2e9'
];

// DOM elements
const backupForm = document.getElementById('backupForm');
const userOrOrgInput = document.getElementById('userOrOrg');
const backupMessage = document.getElementById('backupMessage');
const backupButtonText = document.getElementById('backupButtonText');
const backupSpinner = document.getElementById('backupSpinner');
const refreshBtn = document.getElementById('refreshBtn');
const refreshButtonText = document.getElementById('refreshButtonText');
const refreshSpinner = document.getElementById('refreshSpinner');
const statusContent = document.getElementById('statusContent');
const listViewBtn = document.getElementById('listViewBtn');
const cardViewBtn = document.getElementById('cardViewBtn');
const galleryViewBtn = document.getElementById('galleryViewBtn');

// Load status on page load
document.addEventListener('DOMContentLoaded', () => {
    loadStatus();
    
    // Load saved view preference
    const savedView = localStorage.getItem('backupView') || 'list';
    setView(savedView);
});

// Handle backup form submission
backupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const userOrOrg = userOrOrgInput.value.trim();
    
    if (!userOrOrg) {
        showMessage('Please enter a GitHub user or organization', 'error');
        return;
    }
    
    // Disable form during backup
    setBackupLoading(true);
    hideMessage();
    
    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userOrOrg }),
        });
        
        if (!response.ok) {
            let errorMessage = 'Failed to create backup.';
            try {
                const errorData = await response.json();
                if (errorData && errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (parseError) {
                errorMessage += ` Server returned status ${response.status}.`;
            }
            showMessage(errorMessage, 'error');
            return;
        }
        
        const data = await response.json();
        
        if (data.success) {
            showMessage(data.message, 'success');
            userOrOrgInput.value = '';
            // Refresh status after successful backup
            setTimeout(() => loadStatus(), 1000);
        } else {
            showMessage(data.message, 'error');
        }
    } catch (error) {
        showMessage('Failed to create backup. Please check if the server is running.', 'error');
        console.error('Backup error:', error);
    } finally {
        setBackupLoading(false);
    }
});

// Handle refresh button
refreshBtn.addEventListener('click', () => {
    loadStatus();
});

// Handle view toggle buttons
listViewBtn.addEventListener('click', () => {
    setView('list');
});

cardViewBtn.addEventListener('click', () => {
    setView('card');
});

galleryViewBtn.addEventListener('click', () => {
    setView('gallery');
});

// Set view mode
function setView(view) {
    currentView = view;
    localStorage.setItem('backupView', view);
    
    // Update button states
    listViewBtn.classList.remove('active');
    cardViewBtn.classList.remove('active');
    galleryViewBtn.classList.remove('active');
    
    if (view === 'list') {
        listViewBtn.classList.add('active');
    } else if (view === 'card') {
        cardViewBtn.classList.add('active');
    } else if (view === 'gallery') {
        galleryViewBtn.classList.add('active');
    }
    
    // Update display if content is already loaded
    const userList = document.querySelector('.user-list');
    const galleryColorKey = document.querySelector('.gallery-color-key');
    const galleryRepos = document.querySelector('.gallery-repos');
    
    if (userList) {
        userList.classList.remove('card-view', 'gallery-view');
        if (view === 'card') {
            userList.classList.add('card-view');
        } else if (view === 'gallery') {
            userList.classList.add('gallery-view');
        }
    }
    
    if (galleryColorKey) {
        galleryColorKey.style.display = view === 'gallery' ? 'block' : 'none';
    }
    
    if (galleryRepos) {
        galleryRepos.style.display = view === 'gallery' ? 'grid' : 'none';
    }
}

// Load backup status
async function loadStatus() {
    setRefreshLoading(true);
    
    try {
        const response = await fetch(`${API_BASE}/status`);
        
        if (!response.ok) {
            throw new Error(`Server returned status ${response.status}`);
        }
        
        const data = await response.json();
        
        displayStatus(data);
    } catch (error) {
        statusContent.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <p>Failed to load status. Please check if the server is running.</p>
            </div>
        `;
        console.error('Status error:', error);
    } finally {
        setRefreshLoading(false);
    }
}

// Display status data
function displayStatus(data) {
    if (!data.users || data.users.length === 0) {
        statusContent.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📦</div>
                <p>No backups found yet. Create your first backup above!</p>
            </div>
        `;
        return;
    }
    
    // Assign colors to users
    const userColors = {};
    data.users.forEach((user, index) => {
        userColors[user.name] = COLOR_PALETTE[index % COLOR_PALETTE.length];
    });
    
    let html = `
        <div class="status-summary">
            <div class="stat-card">
                <div class="stat-label">Total Users/Orgs</div>
                <div class="stat-value">${data.totalUsers}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Total Repositories</div>
                <div class="stat-value">${data.totalRepositories}</div>
            </div>
        </div>
    `;
    
    // Add color key for gallery view
    html += `
        <div class="gallery-color-key" style="display: ${currentView === 'gallery' ? 'block' : 'none'}">
            <h3>📌 Color Key</h3>
            <div class="color-key-items">
    `;
    
    data.users.forEach(user => {
        html += `
            <div class="color-key-item">
                <div class="color-key-swatch" style="background: ${userColors[user.name]}"></div>
                <span class="color-key-label">${escapeHtml(user.name)}</span>
            </div>
        `;
    });
    
    html += `
            </div>
        </div>
    `;
    
    // Add gallery repos view
    html += `
        <div class="gallery-repos" style="display: ${currentView === 'gallery' ? 'grid' : 'none'}">
    `;
    
    data.users.forEach(user => {
        user.repositories.forEach(repo => {
            html += `
                <div class="gallery-repo-card" style="--card-color: ${userColors[user.name]}">
                    <div>
                        <div class="gallery-repo-name">${escapeHtml(repo.name)}</div>
                        <div class="gallery-repo-source">${escapeHtml(user.name)}</div>
                    </div>
                    <div class="gallery-repo-updated">Updated: ${escapeHtml(repo.lastUpdated)}</div>
                </div>
            `;
        });
    });
    
    html += `
        </div>
    `;
    
    // Add regular list view
    html += `
        <div class="user-list${currentView === 'card' ? ' card-view' : ''}${currentView === 'gallery' ? ' gallery-view' : ''}">
    `;
    
    data.users.forEach(user => {
        html += `
            <div class="user-item">
                <div class="user-header">
                    <span class="user-name">${escapeHtml(user.name)}</span>
                    <span class="user-repo-count">${user.repositoryCount} repositories</span>
                </div>
                <ul class="repo-list">
        `;
        
        user.repositories.forEach(repo => {
            html += `
                <li class="repo-item">
                    <span class="repo-name">${escapeHtml(repo.name)}</span>
                    <span class="repo-updated">Updated: ${escapeHtml(repo.lastUpdated)}</span>
                </li>
            `;
        });
        
        html += `
                </ul>
            </div>
        `;
    });
    
    html += `</div>`;
    
    statusContent.innerHTML = html;
}

// Show message
function showMessage(text, type) {
    backupMessage.textContent = text;
    backupMessage.className = `message ${type}`;
}

// Hide message
function hideMessage() {
    backupMessage.className = 'message';
}

// Set backup loading state
function setBackupLoading(loading) {
    const submitBtn = backupForm.querySelector('button[type="submit"]');
    submitBtn.disabled = loading;
    backupButtonText.style.display = loading ? 'none' : 'inline';
    backupSpinner.style.display = loading ? 'inline-block' : 'none';
}

// Set refresh loading state
function setRefreshLoading(loading) {
    refreshBtn.disabled = loading;
    refreshButtonText.style.display = loading ? 'none' : 'inline';
    refreshSpinner.style.display = loading ? 'inline-block' : 'none';
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
