# Order Service

This microservice is responsible for managing orders in the Coffee Shop system. It handles order creation, retrieval, and status management.

## Features

### API Endpoints

#### Order Management
- `POST /api/v1/orders` - Create a new order
- `GET /api/v1/orders` - List all orders (with pagination and filtering)
- `GET /api/v1/orders/{orderId}` - Get order details by ID
- `PATCH /api/v1/orders/{orderId}/status` - Update order status
- `GET /api/v1/customers/{customerId}/orders` - Get customer-specific orders
- `GET /api/v1/shops/{shopId}/orders` - Get shop-specific orders

### Request/Response Models

#### OrderRequest
```json
{
  "customerId": "UUID",
  "shopId": "UUID",
  "items": [
    {
      "menuItemId": "UUID",
      "quantity": "integer"
    }
  ]
}
```

#### OrderResponse
```json
{
  "id": "UUID",
  "customerId": "UUID",
  "shopId": "UUID",
  "status": "PENDING|IN_PROGRESS|COMPLETED|CANCELLED",
  "orderTime": "timestamp",
  "totalAmount": "decimal",
  "items": [
    {
      "menuItemId": "UUID",
      "quantity": "integer",
      "unitPrice": "decimal",
      "subtotal": "decimal"
    }
  ]
}
```

## Technical Details

### Database
- PostgreSQL
- Liquibase for database migrations
- JPA/Hibernate for ORM

### Configuration
- Application properties in `src/main/resources/application.yml`
- Database migrations in `src/main/resources/db/changelog`

### Security
- Role-based access control (CUSTOMER, STAFF, ADMIN roles)
- Spring Security integration (currently commented out)

## Running Locally

### Prerequisites
- Java 17
- Docker and Docker Compose
- Maven

### Steps

1. Build the service:
```bash
mvn clean package
```

2. Run with Docker Compose:
```bash
docker-compose up -d
```

3. Access Swagger UI:
```
http://localhost:8081/swagger-ui.html
```

4. Stop the service:
```bash
docker-compose down
```

## Testing

### Running Tests
```bash
mvn test
```

### Test Categories
- Unit Tests: Testing service layer logic
- Integration Tests: Testing API endpoints and database interactions

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_DATASOURCE_URL | Database URL | jdbc:postgresql://localhost:5432/orders_db |
| SPRING_DATASOURCE_USERNAME | Database username | user |
| SPRING_DATASOURCE_PASSWORD | Database password | password |
| SPRING_JPA_HIBERNATE_DDL_AUTO | Hibernate DDL mode | none |
| SERVER_PORT | Application port | 8081 |

## Docker Support

The service includes:
- Dockerfile for building the application image
- docker-compose.yml for local development
- docker-compose.test.yml for running tests
