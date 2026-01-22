# Task Flow

![Build Status](https://img.shields.io/github/actions/workflow/status/ergulerdem/task-flow/maven.yml?branch=main)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

Task Flow is a robust task management application built with Spring Boot and SQLite. It demonstrates modern Continuous Delivery (CD) principles including automated builds, comprehensive testing, and containerization.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running with Docker](#running-with-docker)
- [API Documentation](#api-documentation)
- [Testing](#testing)
  - [Running Tests](#running-tests)
  - [Code Coverage](#code-coverage)
  - [Test Structure](#test-structure)
- [Configuration](#configuration)
- [CI/CD Pipeline](#cicd-pipeline)
- [Documentation](#documentation)

## Features

- **Task Management**: Create, read, update, and delete tasks with full CRUD operations
- **Advanced Filtering**: Filter tasks by status, priority, due dates, and search terms
- **Task Statistics**: Real-time statistics on task status and overdue tasks
- **RESTful API**: Clean and well-documented API endpoints
- **Status History**: Track status changes over time
- **Sorting**: Flexible sorting by priority, due date, title, and creation date

## Architecture

Task Flow follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and responses (`TaskController`)
- **Service Layer**: Contains business logic (`TaskService`)
- **Repository Layer**: Manages data access using Spring Data JPA (`TaskRepository`)
- **Model Layer**: Defines persistent entities (`Task`, `Priority`, `Status`)

For detailed architecture documentation, see [docs/TECHNICAL.md](docs/TECHNICAL.md).

## Getting Started

### Prerequisites

- **Java 17+** - Required for building and running the application
- **Maven 3.6+** - For dependency management and building (or use included `mvnw`)
- **Docker** (optional) - For containerized deployment

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/ergulerdem/task-flow.git
   cd task-flow
   ```

2. **Build the project**:
   ```bash
   ./mvnw clean package
   ```
   On Windows:
   ```bash
   mvnw.cmd clean package
   ```

3. **Run the application**:
   ```bash
   java -jar target/Task-Flow-0.0.1-SNAPSHOT.jar
   ```

   The API will be available at `http://localhost:8080`.
   The web interface will be available at `http://localhost:8080`.

### Running with Docker

1. **Build and run with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

2. **Or build and run Docker image manually**:
   ```bash
   docker build -t taskflow:latest .
   docker run -p 8080:8080 taskflow:latest
   ```

## API Documentation

### Task Endpoints

- `GET /api/tasks` - Retrieve all tasks with optional filters
  - Query parameters: `status`, `priority`, `dueDateFrom`, `dueDateTo`, `searchTerm`, `sortBy`, `sortOrder`
- `GET /api/tasks/{id}` - Retrieve a specific task by ID
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}` - Update an existing task
- `PUT /api/tasks/{id}/status?status={status}` - Update task status
- `DELETE /api/tasks/{id}` - Delete a task
- `GET /api/tasks/stats` - Get task statistics

### Example Request

```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project",
    "description": "Finish the task flow project",
    "priority": "HIGH",
    "dueDate": "2026-01-31"
  }'
```

For complete API documentation, see [docs/TECHNICAL.md](docs/TECHNICAL.md).

## Testing

This project maintains high code quality through comprehensive automated testing.

### Running Tests

**Run all tests**:
```bash
./mvnw test
```

**Run tests with coverage**:
```bash
./mvnw clean test
```

**Run specific test class**:
```bash
./mvnw test -Dtest=TaskServiceTest
```

### Code Coverage

The project uses **JaCoCo** for code coverage analysis with the following thresholds:

- **Line Coverage**: Minimum 60%
- **Branch Coverage**: Minimum 15%

#### Viewing Coverage Reports

After running tests, JaCoCo generates comprehensive coverage reports:

1. **HTML Report**: Open `target/site/jacoco/index.html` in your browser
2. **XML Report**: Available at `target/site/jacoco/jacoco.xml` for CI/CD integration
3. **Coverage data**: Stored in `target/jacoco.exec`

#### Coverage in CI/CD

Coverage reports are automatically generated in CI/CD pipelines:
- **GitHub Actions**: Coverage reports are generated in the build job
- **GitLab CI**: Coverage reports are published as artifacts and displayed in merge requests

#### Current Coverage Status

The project maintains high test coverage across all layers:
- **Unit Tests**: Controller, Service, and Repository layers
- **Integration Tests**: Full API integration with database
- **Coverage Reports**: Available in `target/site/jacoco/` after running tests

### Test Structure

```
src/test/java/erdem/taskflow/
├── controller/
│   ├── TaskControllerTest.java      # Unit tests for controller
│   └── TaskIntegrationTest.java     # Integration tests for API
├── service/
│   └── TaskServiceTest.java         # Unit tests for service layer
├── repository/
│   └── TaskRepositoryTest.java      # Repository layer tests
└── TaskFlowApplicationTests.java    # Application context tests
```

## Configuration

### Configuration Files

**Configuration templates are versioned in the repository** to provide a starting point for setup:

- **`application.properties.example`** - Comprehensive template for local development (versioned)
- **`application-docker.properties`** - Configuration for Docker deployment (versioned)
- **`application-test.properties`** - Test-specific configuration (versioned)

**Note**: Actual `application.properties` files with sensitive data are excluded from version control via `.gitignore`.

### Setting Up Configuration

1. **Copy the example configuration**:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
   On Windows:
   ```cmd
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   ```

2. **Customize as needed**:
   - Database connection settings
   - Server port
   - JPA configuration
   - Logging levels
   - Environment-specific settings

3. **For Docker deployment**, use the `application-docker.properties` profile:
   ```bash
   docker-compose up -d
   ```

### Security Notes

- **Configuration templates are versioned** - `application.properties.example` serves as a safe template
- **Sensitive data is excluded** from version control via `.gitignore`
- **Database files** (`*.db`, `*.sqlite`) are excluded
- **Never commit** real passwords, API keys, or secrets
- **Use environment variables** for sensitive configuration in production:
  ```properties
  spring.datasource.password=${DB_PASSWORD}
  ```
- **Use Spring profiles** for different environments (dev, prod, etc.)

## CI/CD Pipeline

The project includes automated CI/CD pipelines:

### GitHub Actions

- **Location**: `.github/workflows/pipeline.yml`
- **Triggers**: Push and pull requests to `main` branch
- **Stages**: Build, Test, Docker Build

### GitLab CI

- **Location**: `.gitlab-ci.yml`
- **Stages**: Build, Test, Quality, Package, Deploy
- **Features**: Automated testing, code quality checks, Docker image building

## Documentation

- **Technical Documentation**: [docs/TECHNICAL.md](docs/TECHNICAL.md) - Architecture, API details, database schema
- **Assessment Overview**: [ASSESSMENT_EN.md](ASSESSMENT_EN.md) - Project assessment criteria
- **Checklist**: [CHECKLIST_EN.md](CHECKLIST_EN.md) - Implementation checklist

## License

This project is licensed under the MIT License.
