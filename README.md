# Task Flow


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

The project uses **JaCoCo** for code coverage with the following thresholds:

- **Line Coverage**: Minimum 80%
- **Branch Coverage**: Minimum 70%

Coverage is checked during `mvn verify` (JaCoCo check runs in the verify phase). The build fails if thresholds are not met.

#### Viewing Coverage Reports

After running tests or verify, coverage reports are available:

1. **JaCoCo HTML Report**: Open `target/site/jacoco/index.html` in your browser
2. **JaCoCo XML**: `target/site/jacoco/jacoco.xml` (for CI/CD integration)

#### Open Clover (optional)

**Open Clover** can be used as an alternative coverage tool with the same thresholds (80% line, 70% branch). It is compatible with Java 17. To generate Open Clover reports and run its coverage check, use the `clover` profile:

```bash
./mvnw verify -Pclover
```

Reports are generated at `target/site/clover/`. The same coverage requirements (line ≥ 80%, branch ≥ 70%) are enforced when the profile is enabled.

#### Coverage in CI/CD

Coverage reports are generated in CI/CD pipelines:
- **GitHub Actions**: Coverage from the build job
- **GitLab CI**: JaCoCo reports published as artifacts; coverage displayed in merge requests

#### Current Coverage Status

The project maintains high test coverage across all layers:
- **Unit Tests**: Controller, Service, DTO, and Repository layers
- **Integration Tests**: Full API integration with database
- **Reports**: `target/site/jacoco/` (and `target/site/clover/` when using `-Pclover`)

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
├── dto/
│   └── DTOsTest.java                # DTO equals/hashCode/toString tests
└── TaskFlowApplicationTests.java    # Application context tests
```
