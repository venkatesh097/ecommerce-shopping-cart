# E-Commerce Shopping Cart Backend

A modular Spring Boot backend service for the E-Commerce Shopping Cart college project. Built for Java 17 and 21 compatibility using Maven, Spring Data JPA, PostgreSQL, and JUnit 5.

---

## 🛠️ Tech Stack & Architecture

- **Java**: Java 17 / 21
- **Framework**: Spring Boot 3.3.5
- **Web**: Spring MVC (RESTful API endpoints)
- **Data Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL (with in-memory H2 profile for zero-config local runs and isolated unit testing)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test, MockMvc

---

## 📁 Package Architecture

The project adheres to a clean layered architecture under `com.example.ecommerce`:

```
src/main/java/com/example/ecommerce/
├── config/           # Spring configuration classes (CORS, security, custom beans)
├── controller/       # REST API controllers (ProductController, CartController, CheckoutController)
├── dto/              # Request & response DTOs (ProductDto, CartDto, CheckoutRequest, OrderResponse, etc.)
├── entity/           # JPA entities (Product, Cart, CartItem, Order, OrderItem, OrderStatus)
├── exception/        # Custom exceptions & global exception handlers (@RestControllerAdvice)
├── repository/       # Spring Data JPA repositories (ProductRepository, CartRepository, OrderRepository)
├── service/          # Service interfaces & implementations (ProductService, CartService, PricingService, CheckoutService)
└── EcommerceApplication.java # Spring Boot application entry point

src/main/resources/
├── application.properties    # Main application properties driven by environment variables (PostgreSQL default)
└── application-h2.properties # Zero-config local H2 in-memory development profile

src/test/
├── java/com/example/ecommerce/
│   ├── controller/           # Web layer slice tests (ProductControllerTest, CartControllerTest, CheckoutControllerTest)
│   ├── service/              # Service unit tests (ProductServiceTest, CartServiceTest, PricingServiceTest, CheckoutServiceTest)
│   ├── ProductAndCartIntegrationTest.java # Full Spring Boot integration tests (H2 in-memory)
│   ├── PostgresApiIntegrationTest.java    # Full Spring Boot API integration tests (Live PostgreSQL)
│   └── PostgresAvailableCondition.java    # JUnit 5 environment condition guard
└── resources/
    ├── application.properties               # In-memory H2 test configuration
    └── application-postgres-test.properties # Real PostgreSQL live integration test configuration
```

---

## ⚙️ Environment Variables & Database Configuration

Passwords and sensitive database credentials must **never** be hard-coded. The application reads configuration from environment variables with sensible defaults for local development:

| Variable | Description | Default Value |
|---|---|---|
| `DB_URL` | Full JDBC connection URL | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` |
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database schema name | `ecommerce_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database user password | *(empty)* |
| `SERVER_PORT` | Embedded server port | `8080` |
| `JPA_DDL_AUTO` | Hibernate schema management | `update` (or `validate`) |
| `JPA_SHOW_SQL` | Hibernate SQL logging | `false` |

---

## 🔌 API Endpoints & Examples

### Product Catalog (`/api/products`)

- **List All Products**:
  ```bash
  curl -X GET http://localhost:8080/api/products
  ```
- **Get Product by ID**:
  ```bash
  curl -X GET http://localhost:8080/api/products/1
  ```
- **Create Product**:
  ```bash
  curl -X POST http://localhost:8080/api/products \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Wireless Noise-Cancelling Headphones",
      "description": "Premium over-ear wireless headphones",
      "price": 199.99,
      "stockQuantity": 20,
      "category": "Electronics"
    }'
  ```

### Shopping Cart (`/api/cart`)

- **View Demo Cart**:
  ```bash
  curl -X GET http://localhost:8080/api/cart
  ```
- **Add Item to Cart**:
  ```bash
  curl -X POST http://localhost:8080/api/cart/items \
    -H "Content-Type: application/json" \
    -d '{
      "productId": 1,
      "quantity": 2
    }'
  ```
- **Update Item Quantity**:
  ```bash
  curl -X PATCH http://localhost:8080/api/cart/items/1 \
    -H "Content-Type: application/json" \
    -d '{
      "quantity": 4
    }'
  ```
- **Remove Item from Cart**:
  ```bash
  curl -X DELETE http://localhost:8080/api/cart/items/1
  ```
- **Clear Cart**:
  ```bash
  curl -X DELETE http://localhost:8080/api/cart
  ```

### Checkout & Orders (`/api/checkout`, `/api/orders`)

- **Process Checkout**:
  ```bash
  curl -X POST http://localhost:8080/api/checkout \
    -H "Content-Type: application/json" \
    -d '{
      "cartKey": "demo-cart",
      "customerName": "Jane Doe",
      "customerEmail": "jane.doe@example.com",
      "shippingAddress": "123 Campus Lane, College Park",
      "couponCode": "DISCOUNT10",
      "paymentMethod": "CREDIT_CARD"
    }'
  ```
- **Get Order by ID**:
  ```bash
  curl -X GET http://localhost:8080/api/orders/1
  ```

---

## 🐘 PostgreSQL Integration Testing

The project includes end-to-end API integration tests (`PostgresApiIntegrationTest.java`) that execute against a **real PostgreSQL database**, verifying live database queries, stock deduction, pricing calculations, and transactional persistence of `Order` and `OrderItem` records.

### How to Start PostgreSQL Locally

#### Option A: Local PostgreSQL Server (Installed Service)
1. Ensure your PostgreSQL service is running on port `5432` or `5433`.
2. Create the test database if it does not exist:
   ```sql
   CREATE DATABASE ecommerce_test_db;
   ```

#### Option B: Standalone Local PostgreSQL Cluster (No Admin Required)
If running manually without service administration:
```bash
# 1. Initialize cluster in temporary directory
initdb -U postgres -A trust -D "C:/Users/<Username>/AppData/Local/Temp/pg_test_db"

# 2. Start PostgreSQL daemon on port 5433
postgres -D "C:/Users/<Username>/AppData/Local/Temp/pg_test_db" -p 5433

# 3. In another terminal, create test database
psql -U postgres -p 5433 -h 127.0.0.1 -d postgres -c "CREATE DATABASE ecommerce_test_db;"
```

### Running the Integration Tests Against PostgreSQL

```bash
# Run all tests (including PostgresApiIntegrationTest if PostgreSQL is reachable)
./mvnw test

# Or run the PostgreSQL integration suite directly:
./mvnw test -Dtest=PostgresApiIntegrationTest
```

#### Customizing PostgreSQL Test Connection Parameters:
Override connection details using environment variables or Maven system properties:
- `POSTGRES_TEST_HOST`: Default `localhost`
- `POSTGRES_TEST_PORT`: Default `5433` (or `5432`)
- `POSTGRES_TEST_DB`: Default `ecommerce_test_db`
- `POSTGRES_TEST_USER`: Default `postgres`
- `POSTGRES_TEST_PASSWORD`: Default empty

Example:
```bash
./mvnw test -Dtest=PostgresApiIntegrationTest -DPOSTGRES_TEST_PORT=5432 -DPOSTGRES_TEST_PASSWORD=my_password
```

> **Note on Environment Resilience**: `PostgresApiIntegrationTest` is guarded by `PostgresAvailableCondition`. If PostgreSQL is not reachable at the configured port, the suite will cleanly skip without breaking standard development builds, adhering to `AGENTS.md`.

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 or JDK 21+ installed
- Maven 3.8+ (or use the provided Maven wrapper `./mvnw`)
- PostgreSQL 14+ (or run with `--spring.profiles.active=h2` for zero-config mode)

### Build & Run Tests
```bash
./mvnw test
```

### Run the Application
```bash
# Run with local PostgreSQL
./mvnw spring-boot:run

# Or run zero-configuration mode with H2 in-memory (PowerShell requires quotes):
./mvnw spring-boot:run "-Dspring-boot.run.profiles=h2"
# Or via environment variable in PowerShell:
$env:SPRING_PROFILES_ACTIVE="h2"; ./mvnw.cmd spring-boot:run
```
