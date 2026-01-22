# Task Flow

![Build Status](https://img.shields.io/github/actions/workflow/status/ergulerdem/task-flow/maven.yml?branch=main)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

Task Flow is a robust task management application built with Spring Boot and SQLite. It demonstrates modern Continuous Delivery (CD) principles including automated builds, comprehensive testing, and containerization.

## Features
- **Task Management**: Create, read, update, and delete tasks.
- **Filtering**: Advanced filtering by status, priority, and due dates.
- **RESTful API**: Clean and documented API endpoints.

## Getting Started

### Prerequisites
- Java 17+
- Maven
- Docker (optional)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ergulerdem/task-flow.git
   cd task-flow
   ```

2. Build the project:
   ```bash
   ./mvnw clean package
   ```

3. Run the application:
   ```bash
   java -jar target/Task-Flow-0.0.1-SNAPSHOT.jar
   ```

   The API will be available at `http://localhost:8080`.

## Testing & Coverage

This project maintains high code quality through automated testing.

- **Run Tests**:
  ```bash
  ./mvnw test
  ```
- **Coverage Report**:
  After running tests, JaCoCo generates a coverage report at:
  `target/site/jacoco/index.html`

## Configuration

Configuration templates are available in `src/main/resources/application.properties.example`.
Sensitive data is excluded from version control.

## Documentation
For detailed technical documentation, see [docs/TECHNICAL.md](docs/TECHNICAL.md).
