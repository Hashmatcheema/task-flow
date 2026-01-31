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

For the requirements specification (functional, non-functional, acceptance criteria), see [docs/TECHNICAL.txt](docs/TECHNICAL.txt).

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

For the full requirements specification, see [docs/TECHNICAL.txt](docs/TECHNICAL.txt).

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

The project uses **JaCoCo** and **Cobertura** for code coverage with the following thresholds:

- **Line Coverage**: Minimum 80%
- **Branch Coverage**: Minimum 70%

Coverage is checked during `mvn verify` (JaCoCo check runs in the verify phase). The build fails if thresholds are not met.

#### Viewing Coverage Reports

After running tests or verify, coverage reports are available:

1. **JaCoCo HTML Report**: Open `target/site/jacoco/index.html` in your browser
2. **JaCoCo XML**: `target/site/jacoco/jacoco.xml` (for CI/CD integration)
3. **Cobertura**: When using the Cobertura profile (see below), reports are at `target/site/cobertura/`

#### Cobertura and Java 17

The **cobertura-maven-plugin** (v2.7) has known compatibility issues with Java 17+ (e.g. `tools.jar` and bytecode instrumentation). Cobertura is therefore **not run by default**. To generate Cobertura reports, use the `cobertura` profile with a Java 11 toolchain or JDK 11:

```bash
./mvnw verify -Pcobertura
```

For Java 17+ builds, rely on **JaCoCo** only; reports and thresholds are enforced via JaCoCo.

#### Coverage in CI/CD

Coverage reports are generated in CI/CD pipelines:
- **GitHub Actions**: Coverage from the build job
- **GitLab CI**: JaCoCo reports published as artifacts; coverage displayed in merge requests

#### Current Coverage Status

The project maintains high test coverage across all layers:
- **Unit Tests**: Controller, Service, DTO, and Repository layers
- **Integration Tests**: Full API integration with database
- **Reports**: `target/site/jacoco/` (and `target/site/cobertura/` when using `-Pcobertura`)

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

- **Requirements specification**: [docs/TECHNICAL.txt](docs/TECHNICAL.txt) - Functional and non-functional requirements, acceptance criteria

## License

This project is licensed under the MIT License.
