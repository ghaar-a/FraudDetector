# FraudDetector

Real-time fraud detection platform built with Java and Spring Boot, designed to analyze financial transactions and identify potentially fraudulent behavior through rule-based detection, behavioral analysis, event-driven processing, and machine learning.

## Overview

FraudDetector is a backend-focused software engineering project that simulates a real-world financial fraud detection platform.

The system is designed to receive and analyze financial transactions, identify suspicious patterns, calculate risk scores, and produce automated decisions based on multiple fraud detection strategies.

The project combines deterministic business rules, behavioral profiling, event-driven architecture, and machine learning techniques to demonstrate how modern backend systems can be designed to process high volumes of transactional data.

## Main Objectives

* Detect potentially fraudulent financial transactions.
* Analyze transaction behavior and historical patterns.
* Identify unusual transaction amounts.
* Detect suspicious locations and geographic anomalies.
* Identify unusual transaction times.
* Detect abnormal transaction frequency.
* Analyze changes in devices and transaction behavior.
* Calculate a transaction risk score.
* Produce explainable fraud detection decisions.
* Process transactions asynchronously through event-driven architecture.
* Apply machine learning to complement deterministic fraud detection rules.
* Provide observability and operational metrics.
* Demonstrate automated testing and production-oriented engineering practices.

## Planned Architecture

The project will evolve incrementally from a modular backend application into an event-driven fraud detection platform.

The planned architecture includes:

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Apache Kafka
* Redis
* Spring Security
* Docker
* Testcontainers
* OpenAPI
* Spring Boot Actuator
* Prometheus
* Grafana
* Machine Learning

The system will initially use a modular architecture and will progressively introduce asynchronous event processing and machine learning capabilities when the corresponding business and technical requirements are established.

## Fraud Detection Pipeline

The target processing flow is:

```text
Financial Transaction
        |
        v
REST API
        |
        v
Transaction Validation
        |
        v
Fraud Detection Engine
        |
        +----------------------+
        |                      |
        v                      v
Rule-Based Analysis     Behavioral Analysis
        |                      |
        +----------+-----------+
                   |
                   v
            Machine Learning
                   |
                   v
             Risk Scoring
                   |
                   v
           Fraud Decision
                   |
          +--------+--------+
          |        |        |
          v        v        v
       APPROVED  REVIEW  BLOCKED
```

## Fraud Detection Strategies

The platform is expected to evaluate multiple risk indicators, including:

### Transaction Amount

Identifies transactions significantly above the user's historical spending profile.

### Unusual Location

Detects transactions performed from locations that differ significantly from the user's historical behavior.

### Impossible Travel

Identifies geographically inconsistent transactions that could indicate account compromise or credential theft.

### Unusual Transaction Time

Detects transactions performed outside the user's typical activity periods.

### Transaction Velocity

Identifies unusually high transaction frequency within a short period.

### Device Anomaly

Detects transactions performed using previously unknown or suspicious devices.

### Behavioral Deviation

Compares current transactions against the user's historical behavioral profile.

### Machine Learning

A machine learning model will complement deterministic rules by estimating the probability that a transaction represents fraudulent behavior.

The final decision will combine multiple signals instead of relying exclusively on a single rule or model.

## Risk Classification

Transactions will be evaluated through a risk scoring mechanism.

The target classification is:

```text
LOW       -> Low risk
MEDIUM    -> Requires additional analysis
HIGH      -> Strong fraud indicators
CRITICAL  -> High probability of fraud
```

The system will also provide explainable reasons for risk classification.

Example:

```json
{
  "transactionId": "TX-123456",
  "riskScore": 0.93,
  "riskLevel": "CRITICAL",
  "decision": "BLOCKED",
  "reasons": [
    "UNUSUAL_LOCATION",
    "UNUSUAL_TIME",
    "ABNORMAL_AMOUNT"
  ]
}
```

## Engineering Practices

The project is being developed following professional software engineering principles, including:

* Clean and modular architecture.
* Separation of concerns.
* SOLID principles.
* Domain-oriented design.
* Immutable domain objects where appropriate.
* Explicit business rules.
* DTOs for API boundaries.
* Bean Validation.
* Automated testing.
* Integration testing.
* Testcontainers.
* Database migration management.
* API documentation.
* Observability.
* Structured logging.
* Secure configuration.
* Environment-based configuration.
* Containerization.
* Continuous integration.

## Project Status

The project is currently in the initial architecture and domain foundation phase.

Development will progress incrementally through functional implementation cycles.

### Planned Evolution

* [ ] Project foundation
* [ ] Domain modeling
* [ ] Transaction API
* [ ] Fraud detection rules
* [ ] Risk scoring engine
* [ ] PostgreSQL persistence
* [ ] Database migrations
* [ ] Automated tests
* [ ] Integration tests
* [ ] Testcontainers
* [ ] Kafka event-driven processing
* [ ] Redis behavioral analysis
* [ ] Machine learning integration
* [ ] Security and authentication
* [ ] Observability
* [ ] Docker
* [ ] CI/CD
* [ ] Performance testing
* [ ] Production-oriented documentation

## Project Goal

The primary goal of FraudDetector is to demonstrate practical backend engineering skills through a realistic problem domain that combines Java development, distributed systems, data processing, and artificial intelligence.

The project is intended as a professional portfolio project and will prioritize maintainability, testability, explainability, and architectural evolution over unnecessary complexity.
