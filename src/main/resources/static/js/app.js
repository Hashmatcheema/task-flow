// Main application logic

let currentTasks = [];
let currentFilters = {};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

function initializeApp() {
    console.debug('[initializeApp] Initializing TaskFlow application');
    console.debug('[initializeApp] Setting up event listeners');
    setupEventListeners();
    console.debug('[initializeApp] Setting up router');
    setupRouter();
    console.debug('[initializeApp] Loading initial tasks');
    loadTasks();
    console.debug('[initializeApp] Checking notifications');
    checkNotifications();
    console.debug('[initializeApp] Requesting notification permission');
    requestNotificationPermission();
    console.debug('[initializeApp] Application initialized successfully');
}

function setupEventListeners() {
    // Navigation
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const view = e.target.dataset.view;
            router.navigate(view);
        });
    });

    // Filters
    document.getElementById('filter-status').addEventListener('change', applyFilters);
    document.getElementById('filter-priority').addEventListener('change', applyFilters);
    document.getElementById('filter-date').addEventListener('change', applyFilters);
    document.getElementById('search-input').addEventListener('input', debounce(applyFilters, 300));
    document.getElementById('sort-by').addEventListener('change', applyFilters);
    document.getElementById('clear-filters').addEventListener('click', clearFilters);

    // Actions
    document.getElementById('create-task-btn').addEventListener('click', () => openTaskModal());
    document.getElementById('export-btn').addEventListener('click', handleExport);

    // Modal
    document.getElementById('close-modal').addEventListener('click', closeTaskModal);
    document.getElementById('cancel-btn').addEventListener('click', closeTaskModal);
    document.getElementById('task-form').addEventListener('submit', handleTaskSubmit);
    document.getElementById('delete-task-btn').addEventListener('click', handleDeleteTask);
    document.getElementById('edit-task-btn').addEventListener('click', () => {
        const taskId = document.getElementById('task-id').value;
        console.debug('[setupEventListeners] Edit button clicked for task:', taskId);
        if (taskId) {
            closeTaskModal();
            openTaskModal(taskId, false); // false = edit mode
        }
    });

    // Close modal on outside click
    document.getElementById('task-modal').addEventListener('click', (e) => {
        if (e.target.id === 'task-modal') {
            closeTaskModal();
        }
    });
}

function setupRouter() {
    router.register('list', () => {
        loadTasks();
    });

    router.register('stats', () => {
        loadStatistics();
    });
}

async function loadTasks() {
    console.debug('[loadTasks] Starting to load tasks with filters:', currentFilters);
    const container = document.getElementById('tasks-container');
    container.innerHTML = '<div class="loading">Loading tasks...</div>';

    try {
        console.debug('[loadTasks] Calling fetchTasks with filters:', JSON.stringify(currentFilters));
        currentTasks = await fetchTasks(currentFilters);
        console.debug('[loadTasks] Received', currentTasks.length, 'tasks from API');
        renderTasks(currentTasks);
        console.debug('[loadTasks] Tasks rendered successfully');
    } catch (error) {
        console.error('[loadTasks] Error loading tasks:', error);
        container.innerHTML = '<div class="empty-state">Error loading tasks. Please try again.</div>';
        showNotification('Failed to load tasks', 'error');
    }
}

function renderTasks(tasks) {
    console.debug('[renderTasks] Rendering', tasks.length, 'tasks');
    const container = document.getElementById('tasks-container');
    
    if (tasks.length === 0) {
        console.debug('[renderTasks] No tasks to render - showing empty state');
        container.innerHTML = '<div class="empty-state">No tasks found. Create your first task!</div>';
        return;
    }

    console.debug('[renderTasks] Creating task cards for', tasks.length, 'tasks');
    container.innerHTML = tasks.map(task => createTaskCard(task)).join('');
    console.debug('[renderTasks] Task cards created, adding event listeners');
    
    // Add event listeners to task cards
    const cardCount = container.querySelectorAll('.task-card').length;
    console.debug('[renderTasks] Found', cardCount, 'task cards in DOM');
    container.querySelectorAll('.task-card').forEach(card => {
        card.addEventListener('click', (e) => {
            if (!e.target.closest('.task-actions')) {
                const taskId = card.dataset.taskId;
                console.debug('[renderTasks] Task card clicked, opening modal for task:', taskId);
                openTaskModal(taskId, true);
            }
        });
    });

    // Add event listeners to status buttons
    const statusBtnCount = container.querySelectorAll('.status-btn').length;
    console.debug('[renderTasks] Found', statusBtnCount, 'status buttons');
    container.querySelectorAll('.status-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.stopPropagation();
            const taskId = btn.dataset.taskId;
            const newStatus = btn.dataset.status;
            console.debug('[renderTasks] Status button clicked - task:', taskId, 'new status:', newStatus);
            await updateStatus(taskId, newStatus);
        });
    });
    console.debug('[renderTasks] Rendering completed');
}

function createTaskCard(task) {
    const dueDateInfo = formatDate(task.dueDate);
    
    // Check if overdue: dueDate < today AND status != COMPLETED
    let isOverdue = false;
    if (task.dueDate && task.status !== 'COMPLETED') {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dueDate = new Date(task.dueDate);
        dueDate.setHours(0, 0, 0, 0);
        isOverdue = dueDate < today;
    }
    
    // Check if due today
    let isDueToday = false;
    if (task.dueDate) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dueDate = new Date(task.dueDate);
        dueDate.setHours(0, 0, 0, 0);
        isDueToday = dueDate.getTime() === today.getTime();
    }
    
    let cardClasses = 'task-card';
    if (isOverdue) cardClasses += ' overdue';
    if (isDueToday) cardClasses += ' due-today';
    cardClasses += ' ' + getPriorityCardClass(task.priority);

    const statusOptions = ['OPEN', 'IN_PROGRESS', 'COMPLETED'].filter(s => s !== task.status);
    
    return `
        <div class="task-card ${cardClasses}" data-task-id="${task.id}">
            <div class="task-header">
                <div>
                    <div class="task-title">${escapeHtml(task.title)}</div>
                    ${task.description ? `<div class="task-description">${escapeHtml(task.description)}</div>` : ''}
                </div>
            </div>
            <div class="task-meta">
                <span class="badge ${getPriorityClass(task.priority)}">${task.priority || 'MEDIUM'}</span>
                <span class="badge ${getStatusClass(task.status)}">${task.status.replace('_', ' ')}</span>
            </div>
            ${task.dueDate ? `<div class="task-due-date ${dueDateInfo.class}">${dueDateInfo.text}</div>` : ''}
            <div class="task-actions">
                ${statusOptions.map(status => 
                    `<button class="status-btn" data-task-id="${task.id}" data-status="${status}">
                        Mark as ${status.replace('_', ' ')}
                    </button>`
                ).join('')}
            </div>
        </div>
    `;
}

async function updateStatus(taskId, newStatus) {
    console.debug('[updateStatus] Updating task', taskId, 'to status:', newStatus);
    try {
        await updateTaskStatus(taskId, newStatus);
        console.debug('[updateStatus] Status updated successfully');
        showNotification('Task status updated successfully');
        console.debug('[updateStatus] Reloading tasks');
        loadTasks();
        if (router.getCurrentView() === 'stats') {
            console.debug('[updateStatus] Current view is stats, reloading statistics');
            loadStatistics();
        }
    } catch (error) {
        console.error('[updateStatus] Error updating status:', error);
        showNotification('Failed to update task status', 'error');
    }
}

function applyFilters() {
    console.debug('[applyFilters] Starting filter application');
    const status = document.getElementById('filter-status').value;
    const priority = document.getElementById('filter-priority').value;
    const dateFilter = document.getElementById('filter-date').value;
    const searchTerm = document.getElementById('search-input').value.trim();
    const sortValue = document.getElementById('sort-by').value;

    console.debug('[applyFilters] Raw filter values - status:', status, 'priority:', priority, 
                 'dateFilter:', dateFilter, 'searchTerm:', searchTerm, 'sortValue:', sortValue);

    // Parse sort value (format: "field-order" or empty)
    let sortBy = undefined;
    let sortOrder = undefined;
    if (sortValue && sortValue !== '') {
        const parts = sortValue.split('-');
        if (parts.length === 2) {
            sortBy = parts[0];
            sortOrder = parts[1].toUpperCase();
            console.debug('[applyFilters] Parsed sort - sortBy:', sortBy, 'sortOrder:', sortOrder);
        }
    }

    const dateRange = getDateRange(dateFilter);
    console.debug('[applyFilters] Date range calculated:', dateRange);

    currentFilters = {
        status: status || undefined,
        priority: priority || undefined,
        searchTerm: searchTerm || undefined,
        sortBy: sortBy,
        sortOrder: sortOrder,
        ...dateRange
    };

    console.debug('[applyFilters] Final filters object:', JSON.stringify(currentFilters));
    console.debug('[applyFilters] Calling loadTasks()');
    loadTasks();
}

function clearFilters() {
    console.debug('[clearFilters] Clearing all filters');
    document.getElementById('filter-status').value = '';
    document.getElementById('filter-priority').value = '';
    document.getElementById('filter-date').value = '';
    document.getElementById('search-input').value = '';
    document.getElementById('sort-by').value = '';
    currentFilters = {};
    console.debug('[clearFilters] Filters cleared, reloading tasks');
    loadTasks();
}

function openTaskModal(taskId = null, isViewMode = false) {
    console.debug('[openTaskModal] Opening modal - taskId:', taskId, 'isViewMode:', isViewMode);
    const modal = document.getElementById('task-modal');
    const form = document.getElementById('task-form');
    const modalTitle = document.getElementById('modal-title');
    const statusGroup = document.getElementById('status-group');
    const statusHistorySection = document.getElementById('status-history-section');
    const deleteBtn = document.getElementById('delete-task-btn');
    const saveBtn = document.getElementById('save-task-btn');
    const editBtn = document.getElementById('edit-task-btn');

    if (taskId) {
        // Edit/View mode
        modalTitle.textContent = isViewMode ? 'Task Details' : 'Edit Task';
        statusGroup.style.display = 'block';
        deleteBtn.style.display = 'block';
        saveBtn.textContent = 'Update';
        
        fetchTaskById(taskId).then(task => {
            document.getElementById('task-id').value = task.id;
            document.getElementById('task-title').value = task.title;
            document.getElementById('task-description').value = task.description || '';
            document.getElementById('task-priority').value = task.priority;
            document.getElementById('task-due-date').value = task.dueDate || '';
            document.getElementById('task-status').value = task.status;
            
            if (isViewMode) {
                // View mode - show status history and edit button
                console.debug('[openTaskModal] Setting up view mode');
                console.debug('[openTaskModal] Task status history:', task.statusHistory);
                statusHistorySection.style.display = 'block';
                renderStatusHistory(task.statusHistory);
                form.querySelectorAll('input, textarea, select').forEach(el => {
                    el.disabled = true;
                });
                saveBtn.style.display = 'none';
                editBtn.style.display = 'block';
            } else {
                // Edit mode - hide status history and edit button
                console.debug('[openTaskModal] Setting up edit mode');
                statusHistorySection.style.display = 'none';
                form.querySelectorAll('input, textarea, select').forEach(el => {
                    el.disabled = false;
                });
                saveBtn.style.display = 'block';
                editBtn.style.display = 'none';
            }
        }).catch(error => {
            console.error('[openTaskModal] Error loading task:', error);
            showNotification('Failed to load task', 'error');
            closeTaskModal();
        });
    } else {
        // Create mode
        console.debug('[openTaskModal] Setting up create mode');
        modalTitle.textContent = 'Create New Task';
        form.reset();
        document.getElementById('task-id').value = '';
        statusGroup.style.display = 'none';
        statusHistorySection.style.display = 'none';
        deleteBtn.style.display = 'none';
        editBtn.style.display = 'none';
        saveBtn.textContent = 'Create';
        form.querySelectorAll('input, textarea, select').forEach(el => {
            el.disabled = false;
        });
        saveBtn.style.display = 'block';
    }

    modal.classList.add('active');
}

function closeTaskModal() {
    const modal = document.getElementById('task-modal');
    const form = document.getElementById('task-form');
    modal.classList.remove('active');
    form.reset();
    document.getElementById('status-history-section').style.display = 'none';
}

async function handleTaskSubmit(e) {
    e.preventDefault();
    
    const taskId = document.getElementById('task-id').value;
    const taskData = {
        title: document.getElementById('task-title').value,
        description: document.getElementById('task-description').value,
        priority: document.getElementById('task-priority').value,
        dueDate: document.getElementById('task-due-date').value || null
    };

    try {
        if (taskId) {
            await updateTask(taskId, taskData);
            showNotification('Task updated successfully');
        } else {
            await createTask(taskData);
            showNotification('Task created successfully');
        }
        closeTaskModal();
        loadTasks();
        if (router.getCurrentView() === 'stats') {
            loadStatistics();
        }
    } catch (error) {
        showNotification('Failed to save task', 'error');
    }
}

async function handleDeleteTask() {
    const taskId = document.getElementById('task-id').value;
    
    if (!taskId) return;
    
    if (!confirm('Are you sure you want to delete this task? This action cannot be undone.')) {
        return;
    }

    try {
        await deleteTask(taskId);
        showNotification('Task deleted successfully');
        closeTaskModal();
        loadTasks();
        if (router.getCurrentView() === 'stats') {
            loadStatistics();
        }
    } catch (error) {
        showNotification('Failed to delete task', 'error');
    }
}

function renderStatusHistory(history) {
    console.debug('[renderStatusHistory] Rendering status history:', history);
    const container = document.getElementById('status-history-list');
    if (!history || history.length === 0) {
        console.debug('[renderStatusHistory] No status history available');
        container.innerHTML = '<div class="empty-state">No status history available</div>';
        return;
    }

    console.debug('[renderStatusHistory] Rendering', history.length, 'status history entries');
    container.innerHTML = history.map(entry => {
        console.debug('[renderStatusHistory] Entry:', entry);
        return `
        <div class="history-item">
            <span class="badge ${getStatusClass(entry.status)}">${entry.status.replace('_', ' ')}</span>
            <span>${formatDateTime(entry.timestamp)}</span>
        </div>
    `;
    }).join('');
    console.debug('[renderStatusHistory] Status history rendered successfully');
}

async function loadStatistics() {
    try {
        const stats = await fetchStatistics();
        document.getElementById('stat-total').textContent = stats.totalTasks;
        document.getElementById('stat-open').textContent = stats.openTasks;
        document.getElementById('stat-in-progress').textContent = stats.inProgressTasks;
        document.getElementById('stat-completed').textContent = stats.completedTasks;
        document.getElementById('stat-overdue').textContent = stats.overdueTasks;
    } catch (error) {
        showNotification('Failed to load statistics', 'error');
    }
}

async function handleExport() {
    try {
        await exportBackup();
        showNotification('Backup exported successfully');
    } catch (error) {
        showNotification('Failed to export backup', 'error');
    }
}

async function checkNotifications() {
    try {
        const tasks = await fetchTasks();
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        const overdueTasks = tasks.filter(task => {
            if (!task.dueDate || task.status === 'COMPLETED') return false;
            const dueDate = new Date(task.dueDate);
            dueDate.setHours(0, 0, 0, 0);
            return dueDate < today;
        });

        const dueTodayTasks = tasks.filter(task => {
            if (!task.dueDate || task.status === 'COMPLETED') return false;
            const dueDate = new Date(task.dueDate);
            dueDate.setHours(0, 0, 0, 0);
            return dueDate.getTime() === today.getTime();
        });

        if (overdueTasks.length > 0 && Notification.permission === 'granted') {
            new Notification(`TaskFlow: ${overdueTasks.length} overdue task(s)`, {
                body: `You have ${overdueTasks.length} task(s) that are overdue.`,
                icon: '/favicon.ico',
                tag: 'overdue-tasks'
            });
        }

        if (dueTodayTasks.length > 0 && Notification.permission === 'granted') {
            new Notification(`TaskFlow: ${dueTodayTasks.length} task(s) due today`, {
                body: `You have ${dueTodayTasks.length} task(s) due today.`,
                icon: '/favicon.ico',
                tag: 'due-today-tasks'
            });
        }
    } catch (error) {
        console.error('Error checking notifications:', error);
    }
}

function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

