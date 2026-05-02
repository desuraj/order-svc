# E-commerce Order Processing System

A demo Spring Boot-based order management system that handles the complete order lifecycle from creation to delivery, with automated background processing and comprehensive REST APIs.

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Database Configuration](#database-configuration)
- [API Endpoints](#api-endpoints)
- [Order Status Flow](#order-status-flow)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Edge Cases Handled](#edge-cases-handled)
- [Future Enhancements](#future-enhancements)

## Overview

The E-commerce Order Processing System is a production-ready application that manages order operations for an e-commerce platform. It provides RESTful APIs for creating orders, tracking their status, and managing the complete order lifecycle with automated background processing.

## Key Features

- ✅ **Complete Order Management**: Create, retrieve, update, and cancel orders
- ✅ **Status Tracking**: Track orders through their lifecycle (PENDING → PROCESSING → SHIPPED → DELIVERED)
- ✅ **Background Processing**: Automated scheduler that processes pending orders every 5 minutes
- ✅ **Pagination Support**: Efficient listing of orders with customizable page size and sorting
- ✅ **Validation**: Comprehensive input validation using Bean Validation
- ✅ **Error Handling**: Global exception handling with meaningful error messages
- ✅ **Concurrency Control**: Optimistic locking to handle concurrent updates
- ✅ **Database Flexibility**: H2 for development, PostgreSQL for production

## Technology Stack

### Core Framework
- **Java 17**: Modern Java LTS version
- **Spring Boot 4.0.6**: Application framework
- **Maven**: Build and dependency management

### Data Layer
- **Spring Data JPA**: Data access abstraction
- **Hibernate**: JPA implementation
- **H2 Database**: In-memory database for development
- **PostgreSQL**: Production database support

### Additional Libraries
- **Lombok**: Reduces boilerplate code
- **Bean Validation**: Request validation
- **Spring Boot Actuator**: Health checks and monitoring

### Testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing support

## Architecture

The system follows a clean, layered architecture pattern:

```
┌─────────────────────────────────────────┐
│         REST API Clients                │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│      Controller Layer                   │
│  - Request validation                   │
│  - DTO mapping                          │
│  - HTTP handling                        │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│       Service Layer                     │
│  - Business logic                       │
│  - Status validation                    │
│  - Transaction management               │
└─────────────────────────────────────────┐
                  ↓
┌─────────────────────────────────────────┐
│      Repository Layer                   │
│  - Data access                          │
│  - JPA operations                       │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Database (H2/PostgreSQL)        │
└─────────────────────────────────────────┘
```

### Design Patterns Used
- **Layered Architecture**: Clear separation of concerns
- **DTO Pattern**: Separate API models from domain entities
- **Repository Pattern**: Data access abstraction
- **Service Pattern**: Business logic encapsulation
- **Dependency Injection**: Spring's IoC container

For detailed architecture information, see [DESIGN.md](DESIGN.md).

## Prerequisites

Before running the application, ensure you have:

- **Java 17 or higher**: [Download JDK](https://adoptium.net/)
- **Maven 3.6+**: [Download Maven](https://maven.apache.org/download.cgi)
- **IDE** (optional but recommended):
  - IntelliJ IDEA
  - Eclipse
  - VS Code with Java extensions

## Getting Started

### 1. Clone or Download the Project

```bash
cd order-svc/order-svc
```

### 2. Build the Project

```bash
mvn clean install
```

This command will:
- Download all dependencies
- Compile the source code
- Run all unit tests
- Package the application

### 3. Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using Java**
```bash
java -jar target/order-svc-0.0.1-SNAPSHOT.jar
```

**Option C: From IDE**
- Run the `OrderSvcApplication` class directly

### 4. Verify the Application

Once started, the application will be available at:
```
http://localhost:8080
```

Check the health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Database Configuration

### H2 In-Memory Database (Development)

The application uses H2 in-memory database by default for development and testing.

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (leave empty)

**Features:**
- Data is reset on application restart
- Perfect for development and testing
- No installation required
- Web-based console for querying data

### PostgreSQL (Production)

For production deployment, configure PostgreSQL in `application.properties`:

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/orderdb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

## API Endpoints

Base URL: `http://localhost:8080/api/orders`

### 1. Create Order

**Endpoint:** `POST /api/orders`

**Description:** Creates a new order with the specified items.

**Request Body:**
```json
{
  "customerId": "CUST-12345",
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2,
      "price": 29.99
    },
    {
      "productId": "PROD-002",
      "quantity": 1,
      "price": 49.99
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "customerId": "CUST-12345",
  "status": "PENDING",
  "totalAmount": 109.97,
  "items": [
    {
      "id": 1,
      "productId": "PROD-001",
      "quantity": 2,
      "price": 29.99
    },
    {
      "id": 2,
      "productId": "PROD-002",
      "quantity": 1,
      "price": 49.99
    }
  ],
  "createdAt": "2026-05-02T14:30:00",
  "updatedAt": "2026-05-02T14:30:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-12345",
    "items": [
      {"productId": "PROD-001", "quantity": 2, "price": 29.99},
      {"productId": "PROD-002", "quantity": 1, "price": 49.99}
    ]
  }'
```

### 2. Get Order by ID

**Endpoint:** `GET /api/orders/{id}`

**Description:** Retrieves a specific order by its ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerId": "CUST-12345",
  "status": "PROCESSING",
  "totalAmount": 109.97,
  "items": [...],
  "createdAt": "2026-05-02T14:30:00",
  "updatedAt": "2026-05-02T14:35:00"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/orders/1
```

**Error Response:** `404 Not Found` if order doesn't exist

### 3. Update Order Status

**Endpoint:** `PUT /api/orders/{id}/status`

**Description:** Updates the status of an existing order. Must follow valid status transitions.

**Request Body:**
```json
{
  "status": "PROCESSING"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerId": "CUST-12345",
  "status": "PROCESSING",
  "totalAmount": 109.97,
  "items": [...],
  "createdAt": "2026-05-02T14:30:00",
  "updatedAt": "2026-05-02T14:40:00"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PROCESSING"}'
```

**Error Responses:**
- `400 Bad Request`: Invalid status transition
- `404 Not Found`: Order doesn't exist
- `409 Conflict`: Concurrent update detected

### 4. List Orders

**Endpoint:** `GET /api/orders`

**Description:** Retrieves a paginated list of orders with optional status filtering.

**Query Parameters:**
- `status` (optional): Filter by order status (PENDING, PROCESSING, SHIPPED, DELIVERED)
- `page` (optional): Page number (0-indexed, default: 0)
- `size` (optional): Page size (default: 20, max: 100)

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": 1,
      "customerId": "CUST-12345",
      "status": "PROCESSING",
      "totalAmount": 109.97,
      "items": [
        {
          "id": 1,
          "productId": "PROD-001",
          "quantity": 2,
          "price": 29.99
        }
      ],
      "createdAt": "2026-05-02T14:30:00",
      "updatedAt": "2026-05-02T14:35:00"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

**cURL Examples:**
```bash
# Get all orders (first page)
curl http://localhost:8080/api/orders

# Filter by status
curl http://localhost:8080/api/orders?status=PENDING

# Pagination
curl http://localhost:8080/api/orders?page=0&size=10

# Combined
curl http://localhost:8080/api/orders?status=PROCESSING&page=0&size=10
```

### 5. Cancel Order

**Endpoint:** `DELETE /api/orders/{id}`

**Description:** Cancels an order. Only orders with PENDING status can be cancelled.

**Response:** `200 OK`
```json
{
  "id": 1,
  "deletedAt": "2026-05-02T14:45:00"
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

**Error Responses:**
- `404 Not Found`: Order doesn't exist
- `409 Conflict`: Order cannot be cancelled (status is not PENDING)

### Status Codes Summary

| Code | Description |
|------|-------------|
| 200 OK | Successful GET, PUT, DELETE operations |
| 201 Created | Order successfully created |
| 400 Bad Request | Validation errors, invalid status transitions |
| 404 Not Found | Order not found |
| 409 Conflict | Concurrent update conflict, cannot cancel non-PENDING order |
| 500 Internal Server Error | Unexpected server errors |

For detailed API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

## Order Status Flow

Orders progress through the following statuses:

```
PENDING → PROCESSING → SHIPPED → DELIVERED
```

### Status Descriptions

- **PENDING**: Order created, awaiting processing
- **PROCESSING**: Order is being prepared/processed
- **SHIPPED**: Order has been shipped to customer
- **DELIVERED**: Order successfully delivered (terminal state)

### Valid Transitions

| From | To | Allowed |
|------|-----|---------|
| PENDING | PROCESSING | ✅ Yes |
| PENDING | SHIPPED | ❌ No |
| PENDING | DELIVERED | ❌ No |
| PROCESSING | SHIPPED | ✅ Yes |
| PROCESSING | DELIVERED | ❌ No |
| SHIPPED | DELIVERED | ✅ Yes |
| DELIVERED | Any | ❌ No (terminal state) |

### Background Processing

A scheduled job runs **every 5 minutes** to automatically process pending orders:

- Finds all orders with status `PENDING`
- Transitions them to `PROCESSING`
- Logs the processing activity

This ensures orders are automatically moved through the workflow without manual intervention.

**Scheduler Configuration:**
```java
@Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
public void processOrders() {
    orderService.processPendingOrders();
}
```

## Testing

### Running Unit Tests

Execute all unit tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=OrderServiceImplTest
```

### Test Coverage

The project includes comprehensive unit tests covering:

- ✅ Order creation with valid and invalid data
- ✅ Order retrieval (existing and non-existing)
- ✅ Status updates (valid and invalid transitions)
- ✅ Order listing with pagination and filtering
- ✅ Order cancellation (allowed and forbidden scenarios)
- ✅ Background processing of pending orders
- ✅ Edge cases and error scenarios

**Test Statistics:**
- Total test methods: 15+
- Coverage: Service layer business logic
- Framework: JUnit 5 + Mockito

### Manual API Testing

**Using cURL:**
See the cURL examples in the [API Endpoints](#api-endpoints) section above.

**Using Postman:**
1. Import the API endpoints into Postman
2. Set base URL: `http://localhost:8080`
3. Test each endpoint with sample data

**Using H2 Console:**
1. Access http://localhost:8080/h2-console
2. Connect to the database
3. Run SQL queries to verify data:
```sql
SELECT * FROM orders;
SELECT * FROM order_items;
```

For detailed testing guide, see [TESTING.md](TESTING.md).

## Project Structure

```
order-svc/
├── src/
│   ├── main/
│   │   ├── java/com/assignment/order_svc/
│   │   │   ├── controller/          # REST controllers
│   │   │   │   └── OrderController.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── OrderService.java
│   │   │   │   └── impl/
│   │   │   │       └── OrderServiceImpl.java
│   │   │   ├── repository/          # Data access
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── OrderItemRepository.java
│   │   │   ├── model/               # Domain entities
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── UpdateOrderStatusRequest.java
│   │   │   │   ├── OrderItemRequest.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   ├── OrderItemResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── OrderNotFoundException.java
│   │   │   │   ├── InvalidOrderStatusTransitionException.java
│   │   │   │   └── OrderCancellationException.java
│   │   │   ├── scheduler/           # Background jobs
│   │   │   │   └── OrderScheduler.java
│   │   │   └── OrderSvcApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/assignment/order_svc/
│           └── service/impl/
│               └── OrderServiceImplTest.java
├── pom.xml
├── README.md
├── API_DOCUMENTATION.md
├── TESTING.md
└── DESIGN.md
```

### Key Directories

- **controller/**: REST API endpoints and request handling
- **service/**: Business logic and transaction management
- **repository/**: Database access using Spring Data JPA
- **model/**: JPA entities representing database tables
- **dto/**: Request/response objects for API layer
- **exception/**: Custom exceptions and global error handling
- **scheduler/**: Background jobs for automated processing

## Configuration

### Application Properties

Location: `src/main/resources/application.properties`

**Key Configuration Properties:**

```properties
# Application Name
spring.application.name=order-service

# Server Port
server.port=8080

# H2 Database (Development)
spring.datasource.url=jdbc:h2:mem:orderdb
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.com.assignment.order_svc=DEBUG
```

### Switching to PostgreSQL for Production

1. Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/orderdb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

2. Ensure PostgreSQL is installed and running
3. Create the database:
```sql
CREATE DATABASE orderdb;
```

4. Use Flyway or Liquibase for schema migrations (recommended for production)

## Edge Cases Handled

The system handles various edge cases and error scenarios:

### 1. Invalid Order IDs
- **Scenario**: Request for non-existent order
- **Handling**: Returns `404 Not Found` with descriptive error message

### 2. Invalid Status Transitions
- **Scenario**: Attempting to skip status (e.g., PENDING → SHIPPED)
- **Handling**: Returns `400 Bad Request` with valid transition information

### 3. Canceling Non-PENDING Orders
- **Scenario**: Attempting to cancel order with status other than PENDING
- **Handling**: Returns `409 Conflict` explaining only PENDING orders can be cancelled

### 4. Empty Order Items
- **Scenario**: Creating order with empty items list
- **Handling**: Bean validation catches this, returns `400 Bad Request`

### 5. Invalid Quantities or Prices
- **Scenario**: Negative or zero quantities, negative prices
- **Handling**: Bean validation enforces constraints, returns `400 Bad Request`

### 6. Concurrent Updates (Optimistic Locking)
- **Scenario**: Two clients updating same order simultaneously
- **Handling**: JPA's `@Version` field detects conflict, returns `409 Conflict`

### 7. Database Connection Failures
- **Scenario**: Database becomes unavailable
- **Handling**: Returns `500 Internal Server Error`, logs error for monitoring

### 8. Pagination Edge Cases
- **Scenario**: Requesting page beyond available data
- **Handling**: Returns empty content array with proper pagination metadata

## Future Enhancements

Potential improvements for the system:

### Functional Enhancements
- **Order Cancellation Status**: Add `CANCELLED` status to OrderStatus enum
- **Order History**: Track all status changes with timestamps
- **Customer Notifications**: Email/SMS notifications for status changes
- **Inventory Integration**: Check product availability before order creation
- **Payment Integration**: Add payment processing and verification
- **Shipping Integration**: Connect with shipping providers for tracking

### Technical Enhancements
- **Caching**: Implement Redis for frequently accessed orders
- **Message Queue**: Implement asynchronous processing capabilities
- **Monitoring**: Add Prometheus metrics and Grafana dashboards
- **Security**: Implement authentication and authorization (OAuth2/JWT)
- **Rate Limiting**: Prevent API abuse with rate limiting
- **Integration Tests**: Add comprehensive integration tests with TestContainers

### Scalability Considerations
- **Database Sharding**: Partition orders by customer or date range
- **Read Replicas**: Separate read and write operations
- **Microservices**: Split into separate services (Order, Inventory, Payment)
- **Event Sourcing**: Implement event-driven architecture
- **CQRS**: Separate command and query responsibilities

---

## Support and Documentation

- **Design Document**: [DESIGN.md](DESIGN.md) - Detailed architecture and design decisions
- **API Reference**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Complete API documentation
- **Testing Guide**: [TESTING.md](TESTING.md) - Testing strategies and examples

## License

This project is created for educational and demonstration purposes.

---

**Built with ❤️ using Spring Boot**