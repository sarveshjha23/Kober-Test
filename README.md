# Inventory & Order Management System

Two Spring Boot microservices that handle inventory management and order processing for an e-commerce system.

## What's Inside

- **Inventory Service** (Port 8081) - Manages product inventory with batch tracking and expiry dates
- **Order Service** (Port 8082) - Handles order placement and communicates with inventory

## Getting Started

### What You Need

- Java 8 or higher
- Maven 3.x
- Ports 8081 and 8082 free

### Running the Services


**Run**

```bash
# Terminal 1 - Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 2 - Order Service  
cd order-service
mvn spring-boot:run
```



## Using the APIs

### Check Inventory

```bash
curl http://localhost:8081/inventory/1001
```

Response:
```json
{
  "productId": 1001,
  "productName": "Laptop",
  "batches": [
    {
      "batchId": 1,
      "quantity": 68,
      "expiryDate": "2026-06-25"
    }
  ]
}
```

### Place an Order

```bash
curl -X POST http://localhost:8082/order \
  -H "Content-Type: application/json" \
  -d '{"productId": 1002, "quantity": 3}'
```

Response:
```json
{
  "orderId": 11,
  "productId": 1002,
  "productName": "Smartphone",
  "quantity": 3,
  "status": "PLACED",
  "reservedFromBatchIds": [9],
  "message": "Order placed successfully. Inventory reserved from batches: [9]"
}
```

## How It Works

### FIFO Inventory Strategy

Orders are fulfilled from batches with the earliest expiry dates first. If one batch doesn't have enough stock, the system automatically uses multiple batches.

Example: Order 50 Smartwatches
- Takes 39 units from Batch 5 (expires March 31, 2026)
- Takes 11 units from Batch 7 (expires April 24, 2026)
- Result: `reservedFromBatchIds: [5, 7]`

### Factory Pattern

The inventory service uses a factory pattern to handle different inventory strategies. Currently implements FIFO, but you can easily add LIFO or other strategies:

```
inventory-service/src/.../factory/
  ├── InventoryHandler.java          (interface)
  ├── FIFOInventoryHandler.java      (current implementation)
  └── InventoryHandlerFactory.java   (creates handlers)
```

## Project Structure

```
inventory-service/
  └── src/main/java/.../inventory/
      ├── controller/      REST endpoints
      ├── service/         Business logic
      ├── repository/      Database access
      ├── entity/          JPA entities
      ├── dto/             Request/Response objects
      └── factory/         Inventory strategies

order-service/
  └── src/main/java/.../order/
      ├── controller/      REST endpoints
      ├── service/         Business logic
      ├── repository/      Database access
      ├── entity/          JPA entities
      ├── dto/             Request/Response objects
      └── client/          Inventory service calls
```

## Testing

Run all tests:
```bash
cd inventory-service && mvn test
cd order-service && mvn test
```

Both services include:
- Unit tests (business logic with mocked dependencies)
- Controller tests (API endpoint testing)
- Integration tests (full application with H2 database)

## Database

Both services use H2 in-memory databases. Data is loaded automatically from CSV files using Liquibase on startup.

Access H2 Console:
- Inventory: http://localhost:8081/h2-console (URL: `jdbc:h2:mem:inventorydb`, User: `sa`)
- Orders: http://localhost:8082/h2-console (URL: `jdbc:h2:mem:orderdb`, User: `sa`)

### Sample Data

**Inventory** - 10 batches across 5 products:
- 1001: Laptop (68 units)
- 1002: Smartphone (112 units across 2 batches)
- 1003: Tablet (56 units across 2 batches)
- 1004: Headphones (76 units across 2 batches)  
- 1005: Smartwatch (131 units across 3 batches)

**Orders** - 10 pre-loaded orders with various statuses (PLACED, SHIPPED, DELIVERED)

## Tech Stack

- Java 8
- Spring Boot 2.7.18
- Spring Data JPA
- H2 Database
- Liquibase (database migrations)
- Lombok
- JUnit 5 & Mockito
- Maven

## Common Issues

**Port already in use?**
```bash
netstat -ano | findstr :8081
taskkill /PID <process_id> /F
```

**Services not talking to each other?**
Make sure Inventory Service starts first before Order Service.

**Tests failing?**
The Liquibase XSD references have been updated to 4.9 (matches Spring Boot 2.7.18). If you see XSD errors, check the changelog files.

## API Endpoints

### Inventory Service (8081)

- `GET /inventory/{productId}` - Get all batches for a product (sorted by expiry date)
- `POST /inventory/update` - Update inventory (called by Order Service)

### Order Service (8082)

- `POST /order` - Place a new order
