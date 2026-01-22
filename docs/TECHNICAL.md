# Technical Documentation

## Architecture Overview
Task Flow is a monolithic application built using the Spring Boot framework. It follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and responses.
- **Service Layer**: Contains business logic.
- **Repository Layer**: Manages data access using Spring Data JPA.
- **Model Layer**: Defines persistent entities (Task).

## Database Schema
The application uses SQLite (dev) and H2 (test) with a simple schema:

### Table: `tasks`
| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `title` | VARCHAR | Task title |
| `description` | TEXT | Detailed description |
| `priority` | ENUM | HIGH, MEDIUM, LOW |
| `status` | ENUM | OPEN, IN_PROGRESS, COMPLETED |
| `due_date` | DATE | Task deadline |
| `created_at` | TIMESTAMP | Creation time |
| `status_updated_at` | TIMESTAMP | Last status update |

## API Endpoints

### Tasks
- `GET /api/tasks` - Retrieve tasks with optional filters.
- `GET /api/tasks/{id}` - Retrieve a specific task.
- `POST /api/tasks` - Create a new task.
- `PUT /api/tasks/{id}` - Update an existing task.
- `PATCH /api/tasks/{id}/status` - Update task status.
- `DELETE /api/tasks/{id}` - Delete a task.

## Security
- Secrets are excluded from version control via `.gitignore`.
- Database files (`*.db`) are excluded.
- Inputs are validated using Jakarta Validation constraints.
