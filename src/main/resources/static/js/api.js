// API communication functions

const API_BASE = '/api/tasks';

async function fetchTasks(filters = {}) {
    console.debug('[fetchTasks] Starting API call with filters:', JSON.stringify(filters));
    const params = new URLSearchParams();
    
    if (filters.status) {
        params.append('status', filters.status);
        console.debug('[fetchTasks] Added status filter:', filters.status);
    }
    if (filters.priority) {
        params.append('priority', filters.priority);
        console.debug('[fetchTasks] Added priority filter:', filters.priority);
    }
    if (filters.dueDateFrom) {
        params.append('dueDateFrom', filters.dueDateFrom);
        console.debug('[fetchTasks] Added dueDateFrom filter:', filters.dueDateFrom);
    }
    if (filters.dueDateTo) {
        params.append('dueDateTo', filters.dueDateTo);
        console.debug('[fetchTasks] Added dueDateTo filter:', filters.dueDateTo);
    }
    if (filters.searchTerm) {
        params.append('searchTerm', filters.searchTerm);
        console.debug('[fetchTasks] Added searchTerm filter:', filters.searchTerm);
    }
    if (filters.sortBy) {
        params.append('sortBy', filters.sortBy);
        console.debug('[fetchTasks] Added sortBy:', filters.sortBy);
    }
    if (filters.sortOrder) {
        params.append('sortOrder', filters.sortOrder);
        console.debug('[fetchTasks] Added sortOrder:', filters.sortOrder);
    }
    
    const url = params.toString() ? `${API_BASE}?${params}` : API_BASE;
    console.debug('[fetchTasks] Request URL:', url);
    
    try {
        console.debug('[fetchTasks] Sending GET request to:', url);
        const response = await fetch(url);
        console.debug('[fetchTasks] Response status:', response.status, response.statusText);
        
        if (!response.ok) {
            throw new Error(`Failed to fetch tasks: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        console.debug('[fetchTasks] Received', data.length, 'tasks from API');
        return data;
    } catch (error) {
        console.error('[fetchTasks] Error fetching tasks:', error);
        throw error;
    }
}

async function fetchTaskById(id) {
    console.debug('[fetchTaskById] Fetching task with ID:', id);
    try {
        const url = `${API_BASE}/${id}`;
        console.debug('[fetchTaskById] Request URL:', url);
        const response = await fetch(url);
        console.debug('[fetchTaskById] Response status:', response.status);
        if (!response.ok) throw new Error('Failed to fetch task');
        const task = await response.json();
        console.debug('[fetchTaskById] Task fetched:', task.title);
        return task;
    } catch (error) {
        console.error('[fetchTaskById] Error fetching task:', error);
        throw error;
    }
}

async function createTask(taskData) {
    console.debug('[createTask] Creating new task:', JSON.stringify(taskData));
    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(taskData),
        });
        
        console.debug('[createTask] Response status:', response.status);
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to create task');
        }
        
        const created = await response.json();
        console.debug('[createTask] Task created successfully with ID:', created.id);
        return created;
    } catch (error) {
        console.error('[createTask] Error creating task:', error);
        throw error;
    }
}

async function updateTask(id, taskData) {
    console.debug('[updateTask] Updating task', id, 'with data:', JSON.stringify(taskData));
    try {
        const url = `${API_BASE}/${id}`;
        console.debug('[updateTask] Request URL:', url);
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(taskData),
        });
        
        console.debug('[updateTask] Response status:', response.status);
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to update task');
        }
        
        const updated = await response.json();
        console.debug('[updateTask] Task updated successfully');
        return updated;
    } catch (error) {
        console.error('[updateTask] Error updating task:', error);
        throw error;
    }
}

async function updateTaskStatus(id, status) {
    console.debug('[updateTaskStatus] Updating task', id, 'status to:', status);
    try {
        const url = `${API_BASE}/${id}/status?status=${status}`;
        console.debug('[updateTaskStatus] Request URL:', url);
        const response = await fetch(url, {
            method: 'PUT',
        });
        
        console.debug('[updateTaskStatus] Response status:', response.status);
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to update task status');
        }
        
        const updated = await response.json();
        console.debug('[updateTaskStatus] Task status updated successfully');
        return updated;
    } catch (error) {
        console.error('[updateTaskStatus] Error updating task status:', error);
        throw error;
    }
}

async function deleteTask(id) {
    console.debug('[deleteTask] Deleting task with ID:', id);
    try {
        const url = `${API_BASE}/${id}`;
        console.debug('[deleteTask] Request URL:', url);
        const response = await fetch(url, {
            method: 'DELETE',
        });
        
        console.debug('[deleteTask] Response status:', response.status);
        if (!response.ok) {
            throw new Error('Failed to delete task');
        }
        
        console.debug('[deleteTask] Task deleted successfully');
        return true;
    } catch (error) {
        console.error('[deleteTask] Error deleting task:', error);
        throw error;
    }
}

async function fetchStatistics() {
    try {
        const response = await fetch(`${API_BASE}/stats`);
        if (!response.ok) throw new Error('Failed to fetch statistics');
        return await response.json();
    } catch (error) {
        console.error('Error fetching statistics:', error);
        throw error;
    }
}

async function exportBackup() {
    try {
        const response = await fetch('/api/backup/export');
        if (!response.ok) throw new Error('Failed to export backup');
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `taskflow_backup_${new Date().toISOString().split('T')[0]}.json`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        return true;
    } catch (error) {
        console.error('Error exporting backup:', error);
        throw error;
    }
}

