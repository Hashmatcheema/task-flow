// Simple client-side router

class Router {
    constructor() {
        this.routes = {};
        this.currentView = 'list';
    }

    register(viewName, handler) {
        this.routes[viewName] = handler;
    }

    navigate(viewName) {
        if (this.routes[viewName]) {
            // Hide all views
            document.querySelectorAll('.view').forEach(view => {
                view.classList.remove('active');
            });
            
            // Show target view
            const targetView = document.getElementById(`${viewName}-view`);
            if (targetView) {
                targetView.classList.add('active');
            }
            
            // Update nav buttons
            document.querySelectorAll('.nav-btn').forEach(btn => {
                btn.classList.remove('active');
                if (btn.dataset.view === viewName) {
                    btn.classList.add('active');
                }
            });
            
            this.currentView = viewName;
            this.routes[viewName]();
        }
    }

    getCurrentView() {
        return this.currentView;
    }
}

const router = new Router();

