# E-Commerce Shopping Cart Backend

A modular Spring Boot backend service for the E-Commerce Shopping Cart college project. Built for Java 17 and 21 compatibility using Maven, Spring Data JPA, PostgreSQL, and JUnit 5.

---

## 🛠️ Tech Stack & Architecture

- **Java**: Java 17 / 21
- **Framework**: Spring Boot 3.3.5
- **Web**: Spring MVC (RESTful API endpoints)
- **Data Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL (with in-memory H2 test profile for isolated automated testing)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test

---

## 📁 Package Architecture

The project adheres to a clean layered architecture under `com.example.ecommerce`:

```
src/main/java/com/example/ecommerce/
├── config/           # Spring configuration classes (CORS, security, custom beans)
├── controller/       # REST API controllers (ProductController, CartController)
├── dto/              # Request & response DTOs (ProductDto, CartDto, AddToCartRequest, etc.)
├── entity/           # JPA entities (Product, Cart, CartItem, Order, OrderItem, OrderStatus)
├── exception/        # Custom exceptions & global exception handlers (@RestControllerAdvice)
├── repository/       # Spring Data JPA repositories (ProductRepository, CartRepository)
├── service/          # Service interfaces & implementations (ProductService, CartService, PricingService)
└── EcommerceApplication.java # Spring Boot application entry point

src/main/resources/
└── application.properties # Main application properties driven by environment variables

src/test/
├── java/com/example/ecommerce/
│   ├── controller/   # Web layer slice tests (ProductControllerTest, CartControllerTest)
│   ├── service/      # Service unit tests (ProductServiceTest, CartServiceTest)
│   └── ProductAndCartIntegrationTest.java # Full SpringBoot end-to-end integration tests
└── resources/application.properties # Isolated H2 in-memory test configuration
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

---

## 🤝 Handover Notes for Member 2 (Pricing & Checkout)

1. **Service Decoupling**:
   - `CartServiceImpl` depends on the [`PricingService`](file:///c:/Users/athis/OneDrive/Documents/Ecom%20project/src/main/java/com/example/ecommerce/service/PricingService.java) interface using optional autowiring (`@Autowired(required = false)`).
   - Currently, if no `PricingService` bean is found, cart endpoints calculate the raw subtotal with $0.00$ tax/discount.
   - Once Member 2 creates `@Service public class PricingServiceImpl implements PricingService`, Spring will automatically inject it and all cart endpoints (`/api/cart`, etc.) will seamlessly reflect discounts, taxes, and grand totals!
2. **Reading Cart Items**:
   - Member 2 can call `cartService.getCartEntity("demo-cart")` to obtain the JPA `Cart` entity, or evaluate `cart.getItems()` directly.
   - Each `CartItem` entity exposes `getUnitPrice()`, `getQuantity()`, and `getProduct()`.
3. **Monetary Precision**:
   - Remember to use `BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP` for all calculation returns in `PricingResultDto`.

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 or JDK 21+ installed
- PostgreSQL 14+ (or set environment variables pointing to PostgreSQL instance)
- Maven 3.8+ (or use the provided Maven wrapper `./mvnw`)

### Build & Test
```bash
# Run all unit and integration tests (39 tests)
mvn test
# or
./mvnw test
```

### Run the Application
```bash
# Set your environment variables
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
export DB_NAME=ecommerce_db

# Run with Spring Boot plugin
mvn spring-boot:run
```
