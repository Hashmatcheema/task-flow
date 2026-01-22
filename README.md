# TechDemo: Continuous Delivery Integration

## Checklist
For a detailed list of tasks and goals, refer to the [Checklist](./CHECKLIST.md).
This document serves as a guide to ensure all relevant CD aspects are integrated into this demo.

## Table of Contents
1. [Introduction](#Introduction)
2. [Objective](#Objective)
3. [Getting Started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Installation](#installation)
4. [Usage](#usage)
5. [Tech Stack](#tech-stack)
6. [Checklist](#checklist)
7. [Testing](#testing)
8. [Continuous Delivery Workflow](#continuous-delivery-workflow)
9. [Contributing](#contributing)
10. [License](#license)
11. [Contact](#contact)

## Introduction
This repository serves as a guide for the TechDemo of the Continuous Delivery (CD) course.
It focuses on integrating CD principles into an existing software project rather than developing new software from scratch.
The aim is to demonstrate automated builds, tests, and deployments in a realistic environment.

## Objective
The objective is to apply CD practices by automating key processes, ensuring a smoother and more efficient development lifecycle:
- Automated builds
- Automated testing (unit, integration, end-to-end)
- Continuous deployment to production-like environments

## Getting Started

### Prerequisites
Ensure the following tools are installed:
- Git
- Java JDK / Python / Node.js
- Docker (optional)

### Installation
```
git clone https://github.com/yourusername/continuous-delivery-techdemo.git
cd continuous-delivery-techdemo
```

Install dependencies depending on your language:
```
npm install
pip install -r requirements.txt
mvn install
```

## Usage
Build the project:
- Java: mvn clean install
- Python: python setup.py build
- JavaScript: npm run build

Run tests:
Execute all unit/integration tests.

Deployment:
Automated through CI/CD scripts or workflows.

## Tech Stack
- Java, Python, JavaScript
- Maven, Pip, NPM
- JUnit, pytest, Jest
- GitHub Actions, Jenkins

## Testing
Run:
```
npm test
```

Includes unit, integration and end-to-end tests.

## Continuous Delivery Workflow
- Automated builds
- Automated testing
- Continuous deployment to staging
- Approval-based production deployment

## Contributing
Contributions welcome – see contributing guidelines.

## License
MIT License.

## Contact
michael.ulm@fh-joanneum.at
