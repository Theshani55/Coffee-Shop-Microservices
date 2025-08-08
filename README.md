# Coffee Shop Microservices

A modern, distributed coffee shop management system built using microservices architecture with Spring Boot.

## Architecture Overview

The system consists of the following microservices:

- **Auth Service**: Handles user authentication and authorization
- **Customer Service**: Manages customer profiles and related operations
- **Menu Service**: Handles menu items, categories, and pricing
- **Order Service**: Processes and manages customer orders
- **Notification Service**: Handles customer and shop notifications
- **Queue Service**: Manages order queuing and processing
- **Shop Service**: Manages coffee shop locations and their details

## Technologies Used (for order service)

- Java 17
- Spring Boot
- Spring Cloud
- Spring Data JPA
- PostgreSQL
- Docker
- Swagger/OpenAPI for API documentation

## Prerequisites

- JDK 17 or later
- Maven
- Docker and Docker Compose
- PostgreSQL

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/yourusername/Coffee-Shop-Microservices.git
cd Coffee-Shop-Microservices
```

2. Build all services:
```bash
mvn clean package
```

3. Start the services using Docker Compose:
```bash
docker-compose up -d
```
4. Stop the services when done:
```bash
docker-compose down
```

## API Documentation

Each service includes Swagger/OpenAPI documentation. After starting the services, you can access the API documentation at:
(For now ony order service can be accessed, others are under development)

- Order Service: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:[port]/swagger-ui.html
- Customer Service: http://localhost:[port]/swagger-ui.html
- Menu Service: http://localhost:[port]/swagger-ui.html
- Shop Service: http://localhost:[port]/swagger-ui.html

## Features (For now only developed for Order Service)

- User authentication and authorization
- Customer profile management
- Menu management
- Order processing and tracking
- Shop location management
- Real-time notifications
- Order queuing and processing

## Testing

Order service contains unit tests and integration tests. To run the tests:

```bash
cd order-service
mvn test
```

## Project Structure

```
├── auth-service/         # Authentication and authorization
├── customer-service/     # Customer management
├── menu-service/        # Menu and product management
├── notification-service/ # Customer notifications
├── order-service/       # Order processing
├── queue-service/       # Order queuing
└── shop-service/        # Shop management
```

