# Java Lending Application - Microservices Case Study

## Overview

This project implements a simplified Java-based lending application designed to automate loan management processes. 
It is structured as a microservices architecture, demonstrating key concepts like service discovery, inter-service communication, and domain-driven design.

The application consists of the following microservices:
1.  **Discovery Service (Eureka Server):** Handles service registration and discovery for all other microservices.
2.  **Customer Service:** Manages customer profiles, including personal details, financial summaries, and dynamic loan limits.
3.  **Lending Service:** Manages loan products, loan applications, loan lifecycles (disbursement, repayment, overdue processing), and fee calculations.
4.  **Notification Service:** Handles event-driven notifications triggered by other services like lending service, processes templates, and simulates sending notifications.

## Technology Stack

*   **Java 21+**
*   **Spring Boot 3.4.6 (or compatible version)
*   **Spring Cloud:**
   *   Netflix Eureka (Service Discovery)
   *   OpenFeign (Declarative REST Client - used by Lending Service to call Customer Service for eligibility)
*   **Apache Kafka:** For event-driven asynchronous communication (Lending Service produces events, Notification Service consumes them).
*   **Spring Data JPA & Hibernate:** For data persistence.
*   **H2 Database:** In-memory database for each service (for simplicity in this case study).
*   **Flyway:** For database schema migrations and seed data management.
*   **Maven:** For project build and dependency management.
*   **Lombok:** To reduce boilerplate code.
*   **JUnit 5 & Mockito:** For unit testing.

## Prerequisites

*   **JDK 21** or higher installed and configured.
*   **Apache Maven 3.6+** installed and configured.
*   **Docker Desktop or docker engine run Apache Kafka.
*   An IDE like IntelliJ IDEA, Eclipse, or VS Code.
*   Git for cloning the repository.

## Project Structure

The project is organized into a  Maven multi-module project, with each module representing a microservice:

*   `discovery-service/`: Eureka server.
*   `customer-service/`: Manages customer profiles and loan limits.
*   `lending-service/`: Manages loan products and loan lifecycle.
*   `notification-service/`: Manages notifications.
*    `docker compose file` used to start kafka cluster containers and also run the services as containers.

## Setup and Running the Application

Follow these steps to set up and run the entire lending application:

### 1. Project setup

1. Clone the repo
2. cd to the project folder
3. Start kafka cluster:
    using docker compose file, start a simple kafka cluster by running `docker-compose up kafka-ui -d` this will start a 2 node kafka cluster
    running in Kraft mode i.e. without Zookeeper dependency
4. Run: `./mvnw clean install` to install the project dependencies
5. Start individual services:
   discovery service: `./mvnw spring-boot:run -pl discovery-service` once started, you can access the Eureka dashboard at: http://localhost:8761
   notification service: `./mvnw spring-boot:run -pl notification-service` 
                       once started, Check the Eureka dashboard; notification-service should appear as registered.
                       Access h2 console at http://localhost:8003/h2-console
   lending service: `./mvnw spring-boot:run -pl lending-service`
                       once started, Check the Eureka dashboard; LENDING-SERVICE should appear.
                       Access h2 console at http://localhost:8001/h2-console
                       Swagger documentation at http://localhost:8001/swagger-ui/index.html
   customer service: `./mvnw spring-boot:run -pl customer-service`
                     Once started, Check the Eureka dashboard; CUSTOMER-SERVICE should appear.
                     Access h2 console at http://localhost:8002/h2-console
                     Swagger documentation at http://localhost:8002/swagger-ui/index.html

6. For docker deployment for the service: `docker-compose up -d`


   **Architectural Overview**

   Microservices: The application is divided into independent services, each responsible for a specific domain.
   Service Discovery: Eureka Server (discovery-service) is used for dynamic registration and discovery of services.
   Inter-Service Communication:
            Synchronous (OpenFeign): lending-service calls customer-service for eligibility checks.
            Asynchronous (Apache Kafka): lending-service produces notification events to a Kafka topic (e.g., notification-events). notification-service consumes these events to process and simulate notifications.
   Database: Each service has its own dedicated H2 in-memory database, managed by Flyway.
   API Layer: RESTful APIs are exposed by customer-service and lending-service.
   Event-Driven Notifications: Notifications are triggered by business events published to Kafka.

**Further Considerations & Potential Enhancements**

1. Configuration Management: Use Spring Cloud Config Server for centralized configuration.
2. API Gateway: Implement an API Gateway (e.g., Spring Cloud Gateway) as a single entry point for external clients.
3. Security: Secure microservices using Spring Security and OAuth2/JWT.
4. Resilience: Enhance fault tolerance with more advanced circuit breaker patterns (e.g., Resilience4j) and retry mechanisms.
5. Distributed Tracing & Logging: Implement centralized logging (ELK stack) and distributed tracing (Zipkin, Jaeger) for better observability.
6. Persistent Database: Replace H2 with a persistent database like PostgreSQL or MySQL for production-like environments.
7. Real Notification Sending: Integrate with actual email (AWS SES, SendGrid) and SMS (Twilio) providers.
  