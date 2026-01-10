// API endpoint - adjust based on your server configuration
const API_BASE = '/api/backups';

// View state
let currentView = 'list'; // 'list' or 'card'

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

// Set view mode
function setView(view) {
    currentView = view;
    localStorage.setItem('backupView', view);
    
    // Update button states
    if (view === 'list') {
        listViewBtn.classList.add('active');
        cardViewBtn.classList.remove('active');
    } else {
        cardViewBtn.classList.add('active');
        listViewBtn.classList.remove('active');
    }
    
    // Update display if content is already loaded
    const userList = document.querySelector('.user-list');
    if (userList) {
        if (view === 'card') {
            userList.classList.add('card-view');
        } else {
            userList.classList.remove('card-view');
        }
    }
}

// Load backup status
async function loadStatus() {
    setRefreshLoading(true);
    
    try {
        const response = await fetch(`${API_BASE}/status`);
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
        <div class="user-list${currentView === 'card' ? ' card-view' : ''}">
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
