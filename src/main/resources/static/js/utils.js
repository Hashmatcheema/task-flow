// Utility functions

function formatDate(dateString) {
    if (!dateString) return 'No due date';
    const date = new Date(dateString);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const taskDate = new Date(date);
    taskDate.setHours(0, 0, 0, 0);
    
    const diffTime = taskDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) {
        return { text: `Overdue (${Math.abs(diffDays)} days)`, class: 'overdue' };
    } else if (diffDays === 0) {
        return { text: 'Due today', class: 'due-today' };
    } else if (diffDays === 1) {
        return { text: 'Due tomorrow', class: '' };
    } else if (diffDays <= 7) {
        return { text: `Due in ${diffDays} days`, class: '' };
    } else {
        return { text: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }), class: '' };
    }
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', { 
        month: 'short', 
        day: 'numeric', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getPriorityClass(priority) {
    return priority ? `badge-priority-${priority.toLowerCase()}` : '';
}

function getStatusClass(status) {
    return status ? `badge-status-${status.toLowerCase().replace('_', '-')}` : '';
}

function getPriorityCardClass(priority) {
    return priority ? `${priority.toLowerCase()}-priority` : '';
}

function showNotification(message, type = 'success') {
    const container = document.getElementById('notification-container');
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    container.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideIn 0.3s ease-out reverse';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

function getDateRange(filterValue) {
    console.debug('[getDateRange] Called with filterValue:', filterValue);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    console.debug('[getDateRange] Today date:', today.toISOString().split('T')[0]);
    
    let result = {};
    switch(filterValue) {
        case 'overdue':
            const yesterday = new Date(today.getTime() - 86400000);
            result = { dueDateTo: yesterday.toISOString().split('T')[0] };
            console.debug('[getDateRange] Overdue filter - dueDateTo:', result.dueDateTo, 
                         '(tasks with dueDate <= yesterday, excluding completed)');
            return result;
        case 'today':
            const todayStr = today.toISOString().split('T')[0];
            result = { dueDateFrom: todayStr, dueDateTo: todayStr };
            console.debug('[getDateRange] Today filter - dueDateFrom:', result.dueDateFrom, 
                         'dueDateTo:', result.dueDateTo);
            return result;
        case 'this-week':
            const weekStart = new Date(today);
            weekStart.setDate(today.getDate() - today.getDay());
            const weekEnd = new Date(weekStart);
            weekEnd.setDate(weekStart.getDate() + 6);
            result = { 
                dueDateFrom: weekStart.toISOString().split('T')[0],
                dueDateTo: weekEnd.toISOString().split('T')[0]
            };
            console.debug('[getDateRange] This week filter - dueDateFrom:', result.dueDateFrom, 
                         'dueDateTo:', result.dueDateTo);
            return result;
        default:
            console.debug('[getDateRange] No date filter - returning empty object');
            return {};
    }
}

