
# FraudDetector

A real-time financial fraud detection backend built with Java 21, Spring Boot, PostgreSQL, and Apache Kafka. The project demonstrates modular backend architecture, explainable risk assessment, asynchronous event processing, idempotency, automated testing, and operational observability.

## Overview

FraudDetector simulates a transaction processing platform that evaluates financial transactions for potentially suspicious behavior. Its design separates transaction handling, fraud analysis, persistence, and asynchronous messaging.

The project focuses on backend engineering practices relevant to real-world financial systems, including validation, error handling, database migrations, event-driven processing, and reliable message publication.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application language |
| Spring Boot | Application framework |
| Spring Security | API security |
| Spring Data JPA | Persistence and ORM |
| PostgreSQL | Relational database |
| Flyway | Database schema migrations |
| Apache Kafka | Asynchronous transaction analysis |
| Gradle Kotlin DSL | Build and dependency management |
| JUnit 5 and Spring Boot Test | Automated testing |
| Testcontainers | Integration testing with containerized dependencies |
| Spring Boot Actuator | Health and application metrics |
| Springdoc OpenAPI | API documentation |
| Docker Compose | Local PostgreSQL environment |
| GitHub Actions | Continuous integration |

## Implemented Capabilities

- Transaction API with request validation and structured error handling.
- Fraud analysis using domain rules, risk scores, risk levels, decisions, and explainable reasons.
- PostgreSQL persistence with Flyway-managed migrations.
- Asynchronous transaction analysis requests published through Kafka.
- Producer publication confirmation before returning `202 Accepted` from the asynchronous endpoint.
- HTTP `503 Service Unavailable` handling when asynchronous publication fails.
- Kafka retry and dead-letter handling.
- Idempotent transaction analysis processing and consistency validation for idempotency metadata.
- Application metrics and operational health endpoints.
- API security configuration.
- Automated unit, controller, and integration tests.
- GitHub Actions workflow for automated builds and tests.

## Architecture

The application follows a modular structure that separates domain logic, application services, infrastructure, and presentation.

```text
Client
  |
  v
REST API
  |
  +--> Synchronous analysis
  |       |
  |       v
  |   Fraud detection services
  |       |
  |       v
  |   Analysis response
  |
  +--> Asynchronous analysis
          |
          v
      Request validation
          |
          v
      Kafka producer
          |
          v
      Publication confirmation
          |
          v
      HTTP 202 Accepted
          
      Kafka consumer
          |
          v
      Idempotency validation
          |
          v
      Transaction analysis
          |
          v
      Persistence and metrics
```

The asynchronous HTTP response confirms successful publication of the request. It does not mean that fraud analysis has already finished.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/v1/transactions/analyze` | Request synchronous transaction analysis |
| `POST` | `/api/v1/transactions/analyze/async` | Publish a transaction analysis request asynchronously |
| `GET` | `/actuator/health` | Application health |
| `GET` | `/actuator/info` | Application information |
| `GET` | `/actuator/metrics` | Available application metrics |
| `GET` | `/api-docs` | OpenAPI specification |
| `GET` | `/swagger-ui.html` | Interactive API documentation |

The asynchronous endpoint returns `202 Accepted` after Kafka confirms publication. Publication failures are mapped to `503 Service Unavailable`.

Security configuration and request requirements apply to the protected endpoints.

## Prerequisites

- JDK 21
- Docker and Docker Compose
- Git
- A Kafka broker for exercising asynchronous transaction publication

Docker is also required for integration tests that use Testcontainers.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ghaar-a/FraudDetector.git
cd FraudDetector
```

### 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

The local Compose configuration creates the `frauddetector` database and persists database files in a named volume.

### 3. Configure the application

The application supports these environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/frauddetector` |
| `DB_USERNAME` | `frauddetector` |
| `DB_PASSWORD` | `frauddetector` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `APP_SECURITY_USERNAME` | `fraud-detector` |
| `APP_SECURITY_PASSWORD` | `change-me` |

Override the default security credentials before running the application outside a local development environment. Do not commit real credentials or secrets to the repository.

A reachable Kafka broker is required to exercise asynchronous publication. The Compose configuration currently starts PostgreSQL only.

### 4. Run the application

On Linux or macOS:

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew.bat bootRun
```

Flyway applies database migrations when the application starts. Hibernate validates the database schema.

### 5. Open the API documentation

Once the application is running, visit:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI specification: http://localhost:8080/api-docs
- Health endpoint: http://localhost:8080/actuator/health

Protected endpoints may require HTTP Basic authentication using the configured application credentials.

## Testing

Run the complete test suite:

```bash
./gradlew clean test
```

The test suite includes automated tests for application services, REST controllers, Kafka processing, persistence, and integration scenarios.

Testcontainers-based tests require a working Docker environment.

## Continuous Integration

The GitHub Actions workflow at `.github/workflows/ci.yml` runs on pushes to `main`, pull requests targeting `main`, and manual workflow dispatches.

The pipeline provisions PostgreSQL, configures Java 21 and Gradle, and executes:

```bash
./gradlew --no-daemon clean test
```

A successful workflow verifies that the build and configured test suite pass in the CI environment.

## Engineering Principles

- Separation of concerns and modular design.
- Explicit domain models and business rules.
- Immutable domain objects where appropriate.
- DTOs and validation at API boundaries.
- Consistent error handling.
- Database schema versioning.
- Reliable asynchronous messaging.
- Idempotent event processing.
- Automated testing and continuous integration.
- Configurable credentials and environment-based settings.
- Operational health and metrics.

## Roadmap

Potential future improvements include:

- Redis-backed behavioral profiles and transaction velocity analysis.
- Machine learning integration with a documented model evaluation process.
- Performance and load testing for transaction throughput.
- Expanded monitoring and dashboards.
- Containerized application deployment.
- Additional end-to-end scenarios and operational documentation.

These items are future enhancements, not claims that the functionality is already implemented.

## Project Goal

FraudDetector is a portfolio project focused on demonstrating practical Java backend engineering skills through a realistic financial domain. It emphasizes maintainable architecture, reliable event processing, testability, explainable decisions, and incremental evolution toward production-oriented engineering.
