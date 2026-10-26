# Project Status & Task Tracking

This document tracks all tasks, owners, implementation status, build/test results, blockers, unresolved decisions, and next steps for the E-Commerce Shopping Cart Backend project.

---

## Current Status Overview

| Task ID | Task Description | Owner | Status | Build / Test Results |
|---|---|---|---|---|
| `TASK-001` | Initialize Spring Boot Maven Backend Project & Base Architecture | Backend Lead / Agent | `COMPLETED` | `BUILD SUCCESS` (1 test passed) |
| `TASK-002` | API Contracts, Domain Model Foundations & Member Coordination | Member 1 (with Member 2) | `COMPLETED` | `BUILD SUCCESS` (24 source files compiled, 1 test passed) |
| `TASK-003` | Implement Product Catalog Feature (Repository, Service, Controller, Tests) | Member 1 | `COMPLETED` | `BUILD SUCCESS` (10 tests passed) |
| `TASK-004` | Implement Cart Feature (Repository, Service, Controller, Exception Handling, Tests) | Member 1 | `COMPLETED` | `BUILD SUCCESS` (36 tests passed) |
| `TASK-005` | Quality Verification, End-to-End Integration Testing & Member 2 Handover | Member 1 | `COMPLETED` | `BUILD SUCCESS` (39 tests passed, 0 failures) |
| `TASK-006` | Codebase Inspection, Contract Validation & Member 2 Onboarding | Member 2 | `COMPLETED` | `BUILD SUCCESS` (39 tests passed, 0 failures) |
| `TASK-007` | Implement Pricing Engine (`PricingServiceImpl`) & Pricing Tests | Member 2 | `COMPLETED` | `BUILD SUCCESS` (57 tests passed, 0 failures) |
| `TASK-008` | Implement Checkout & Order Processing API | Member 2 | `COMPLETED` | `BUILD SUCCESS` (72 tests passed, 0 failures) |
| `TASK-009` | Real PostgreSQL API Integration Testing & Local Setup Documentation | Member 2 | `COMPLETED` | `BUILD SUCCESS` (80 tests passed, 0 failures) |
| `TASK-010` | End-to-End Integration Verification, Feature Alignment & CI Readiness | Member 2 (with Member 1) | `COMPLETED` | `BUILD SUCCESS` (81 tests passed, 0 failures; 8/8 PostgreSQL tests passed) |
| `TASK-011` | Build Interactive Single-Page Application (SPA) Frontend | Agent | `COMPLETED` | `BUILD SUCCESS` (81 tests passed, 0 failures; static UI verified) |

---

## Current Status Overview Summary
- **Overall Project Health**: **GREEN / READY FOR CI**
- **Unit & Slice Test Coverage**: 81 automated tests passing across controller, service, repository, and integration layers.
- **Database Integration**: 8 end-to-end integration tests passing directly against a live PostgreSQL 17 database cluster (`ecommerce_test_db`).
- **End-to-End Verification**: Complete user purchase flow (product browse -> cart add -> cart modify -> pricing breakdown -> checkout -> order retrieval -> cart empty) verified in both in-memory H2 and live PostgreSQL test suites.
- **Blockers**: None.
- **CI Readiness**: Fully ready for GitHub Actions CI setup.

---

## Task Details

### TASK-011: Build Multi-Page Web UI & Startup Data Seeder
- **Task ID**: `TASK-011`
- **Owner / Assignee**: Frontend & DevOps Lead
- **Status**: `COMPLETED`
- **Scope**:
  - Implemented `DataInitializer.java` (`CommandLineRunner`) to auto-seed 8 realistic sample products with pricing and stock on startup when database is empty.
  - Implemented clean multi-page web UI under `src/main/resources/static/`:
    - `index.html` + `js/products.js`: Product catalog cards, stock status, "Add to Cart" interaction, dynamic header badge.
    - `cart.html` + `js/cart.js`: Cart table with quantity +/- modification, item removal, clear cart, coupon box (`DISCOUNT10`, `SAVE20`), and live subtotal/discount/tax/grand total breakdown directly rendered from backend calculations.
    - `checkout.html` + `js/checkout.js`: Customer details form, payment method select, live cart item preview, and order submission.
    - `order.html` + `js/order.js`: Order confirmation screen displaying confirmed order number, customer details, purchased item breakdown, and status.
    - `css/style.css`: Clean, responsive shared stylesheet.
    - `js/api.js`: Shared Vanilla JS fetch API wrapper with comprehensive error handling reading from `ErrorResponse`.
  - Updated `README.md` with instructions on accessing the web store at `http://localhost:8080/`.
- **Actual Test / Build Results**: `BUILD SUCCESS` (81 automated tests passed, 0 failures).

---

### TASK-010: End-to-End Integration Verification, Feature Alignment & CI Readiness
- **Task ID**: `TASK-010`
- **Owner / Assignee**: Member 2 (in coordination with Member 1)
- **Status**: `COMPLETED`
- **Scope**:
  - Reviewed Member 1's product and cart features alongside Member 2's pricing engine and checkout/order processing features.
  - Verified no contract violations or duplicate model structures across packages.
  - Added full end-to-end purchase lifecycle test `testCompletePurchaseFlow()` in `ProductAndCartIntegrationTest`:
    1. `GET /api/products`: Discover catalog items and prices.
    2. `POST /api/cart/items`: Add selected product to cart with quantity.
    3. `PATCH /api/cart/items/{id}`: Update cart item quantity and verify cart item subtotal recalculation.
    4. `GET /api/cart?couponCode=DISCOUNT10`: Verify pricing calculation with discount and 10% post-discount tax.
    5. `POST /api/checkout`: Process checkout with valid customer details and coupon code, verify order creation, stock decrement, and cart clearing.
    6. `GET /api/orders/{id}`: Verify placed order details and persisted price breakdown.
    7. `GET /api/cart`: Confirm cart is empty post-checkout.
  - Verified identical end-to-end integration behavior against live PostgreSQL 17 in `PostgresApiIntegrationTest`.
  - Audited all shared contracts in `API_CONTRACT.md` against live controllers and DTOs; zero breaking changes or regressions introduced.
  - Updated `README.md` with complete flow guide, local PostgreSQL setup, and run instructions.
- **Files Created / Modified**:
  - `src/test/java/com/example/ecommerce/ProductAndCartIntegrationTest.java` (Added `testCompletePurchaseFlow()`)
  - `README.md` (Updated setup, test commands, database instructions, and end-to-end flow)
  - `PROJECT_STATUS.md` (Updated with TASK-010 results and CI readiness confirmation)
- **Exact Commands & Actual Results**:
  1. Full Maven Build & Test Suite:
     - Command: `./mvnw.cmd test`
     - Result: `BUILD SUCCESS` (Total time: 13.218 s)
     - Test Summary: **81 tests run, 0 failures, 0 errors, 0 skipped**
       - `CartControllerTest`: 12 tests passed
       - `CheckoutControllerTest`: 6 tests passed
       - `ProductControllerTest`: 5 tests passed
       - `CartServiceTest`: 14 tests passed
       - `CheckoutServiceTest`: 9 tests passed
       - `PricingServiceTest`: 18 tests passed
       - `ProductServiceTest`: 4 tests passed
       - `ProductAndCartIntegrationTest`: 4 tests passed (including complete purchase flow)
       - `PostgresApiIntegrationTest`: 8 tests passed (against live PostgreSQL 17)
       - `EcommerceApplicationTests`: 1 test passed
  2. Dedicated PostgreSQL Integration Test Suite:
     - Command: `./mvnw.cmd test -Dtest=PostgresApiIntegrationTest`
     - Result: `BUILD SUCCESS` (Total time: 8.453 s)
     - Test Summary: **8 tests run, 0 failures, 0 errors, 0 skipped** (all executing against real PostgreSQL 17 on port 5433)
- **Remaining Blockers**:
  - None. All features integrated, tested, and passing.
- **CI Readiness Assessment**:
  - **Status: READY FOR CI PIPELINE SETUP**
  - Application builds cleanly using standard Maven wrapper (`./mvnw`).
  - Unit tests run fast and deterministically with in-memory H2 default.
  - PostgreSQL integration tests can run in CI using a GitHub Actions PostgreSQL service container (`postgres:17` on port 5432 or 5433).
  - Environment fallback (`PostgresAvailableCondition`) protects the build if PostgreSQL service is offline.
  - No secret credentials or sensitive tokens hardcoded in the codebase.
- **Next Steps**:
  - Team / DevOps lead can now proceed to implement the GitHub Actions CI workflow (`.github/workflows/ci.yml`).

---

### TASK-009: Real PostgreSQL API Integration Testing & Local Setup Documentation
- **Task ID**: `TASK-009`
- **Owner / Assignee**: Member 2
- **Status**: `COMPLETED`
- **Scope**:
  - Implemented end-to-end API integration tests (`PostgresApiIntegrationTest.java`) running against a live PostgreSQL 17 database instance (not an in-memory substitute).
  - Explicitly verified that the test suite connects to a live PostgreSQL server (verified via `conn.getMetaData().getDatabaseProductName() == "PostgreSQL"`).
  - Validated key behaviors against live PostgreSQL:
    - Product catalog retrieval via `GET /api/products` and `GET /api/products/{id}`.
    - Valid checkout request (`POST /api/checkout`) generates order, decrements stock in PostgreSQL, calculates pricing breakdown, and clears customer cart.
    - Direct verification that `Order` and `OrderItem` records are persisted in PostgreSQL tables `orders` and `order_items`.
    - Verification that HTTP response contains order ID, generated `orderNumber`, status `CONFIRMED`, and accurate price summary.
    - Rejection of invalid/empty checkout requests:
      - Empty cart checkout returns HTTP 400 Bad Request.
      - Invalid validation payload (missing name, malformed email) returns HTTP 400 Bad Request.
      - Insufficient stock returns HTTP 400 Bad Request with rollback.
    - Placed order query via `GET /api/orders/{id}` retrieves persisted order from PostgreSQL.
  - Implemented strict data isolation: `@BeforeEach` and `@AfterEach` clean database tables in reverse dependency order (`orders`, `carts`, `products`), keeping tests repeatable and independent.
  - Guarded against build breaks using `PostgresAvailableCondition`, which checks PostgreSQL connectivity before starting the test context, ensuring clean fallback per `AGENTS.md`.
  - Added `application-postgres-test.properties` under `src/test/resources/`.
  - Documented local PostgreSQL startup instructions (service or standalone cluster) and test invocation in `README.md`.
- **Files Created / Modified**:
  - `src/test/java/com/example/ecommerce/PostgresApiIntegrationTest.java` (New live PostgreSQL API integration test suite, 8 tests)
  - `src/test/java/com/example/ecommerce/PostgresAvailableCondition.java` (JUnit 5 execution condition for environment resilience)
  - `src/test/resources/application-postgres-test.properties` (PostgreSQL test profile configuration)
  - `README.md` (Updated with PostgreSQL local setup instructions and test run commands)
  - `PROJECT_STATUS.md` (Updated with TASK-009 results)
- **Actual Test / Build Results**:
  - Command: `./mvnw.cmd test -Dtest=PostgresApiIntegrationTest`
  - Result: `BUILD SUCCESS` (8 tests passed against real PostgreSQL 17)
  - Command: `./mvnw.cmd test`
  - Result: `BUILD SUCCESS` (Total time: 10.992 s)
  - Test Suite Breakdown:
    - `PostgresApiIntegrationTest`: **8 tests passed against real PostgreSQL** (0 failures, 0 errors).
    - `CheckoutServiceTest`: 9 tests passed.
    - `CheckoutControllerTest`: 6 tests passed.
    - `PricingServiceTest`: 18 tests passed.
    - `CartControllerTest`: 12 tests passed.
    - `ProductControllerTest`: 5 tests passed.
    - `CartServiceTest`: 14 tests passed.
    - `ProductServiceTest`: 4 tests passed.
    - `ProductAndCartIntegrationTest`: 3 tests passed.
    - `EcommerceApplicationTests`: 1 test passed.
    - Total: **80 tests run, 0 failures, 0 errors, 0 skipped**.
- **Blockers / Challenges**:
  - None. Live PostgreSQL integration tests pass cleanly and repeatable data isolation is verified.
- **Next Steps**:
  - Complete backend API scope, pricing engine, checkout workflow, and live database integration verification are fully completed.

---

### TASK-008: Implement Checkout & Order Processing API
- **Task ID**: `TASK-008`
- **Owner / Assignee**: Member 2
- **Status**: `COMPLETED`
- **Scope**:
  - Implemented Spring Data JPA repository `OrderRepository` supporting query by business `orderNumber`.
  - Implemented `CheckoutService` and `CheckoutServiceImpl` with `@Transactional` management for consistent persistence of `Order` and `OrderItem` records:
    - Rejects empty, missing, or null carts with `IllegalArgumentException`.
    - Validates product stock and decrements inventory in `ProductRepository`.
    - Throws `InsufficientStockException` if requested quantities exceed available stock.
    - Calls `PricingService` for final subtotal, discount, tax, and grand total calculations.
    - Generates unique business `orderNumber` (`ORD-YYYYMMDD-XXXXXXXX`).
    - Maps and attaches `OrderItem` entities to the `Order` entity using cascading persistence.
    - Clears customer shopping cart upon successful order placement (`cartService.clearCart`).
    - Implemented `getOrderById` for querying placed orders with `ResourceNotFoundException`.
  - Implemented REST Controller `CheckoutController`:
    - `POST /api/checkout`: Processes `CheckoutRequest`, returns `200 OK` with `OrderResponse`.
    - `GET /api/orders/{id}`: Retrieves order details by ID, returns `200 OK` with `OrderResponse`.
  - Added test suites: `CheckoutServiceTest` (9 tests) and `CheckoutControllerTest` (6 tests).
- **Files Created / Modified**:
  - `src/main/java/com/example/ecommerce/entity/Order.java`
  - `src/main/java/com/example/ecommerce/entity/Cart.java`
  - `src/main/java/com/example/ecommerce/repository/OrderRepository.java`
  - `src/main/java/com/example/ecommerce/service/CheckoutService.java`
  - `src/main/java/com/example/ecommerce/service/CheckoutServiceImpl.java`
  - `src/main/java/com/example/ecommerce/controller/CheckoutController.java`
  - `src/test/java/com/example/ecommerce/service/CheckoutServiceTest.java`
  - `src/test/java/com/example/ecommerce/controller/CheckoutControllerTest.java`
  - `PROJECT_STATUS.md`
- **Actual Test / Build Results**: `BUILD SUCCESS` (72 tests passed, 0 failures).

---

### TASK-007: Implement Pricing Engine (`PricingServiceImpl`) & Pricing Tests
- **Task ID**: `TASK-007`
- **Owner / Assignee**: Member 2
- **Status**: `COMPLETED`
- **Scope**:
  - Implemented `PricingServiceImpl` implementing the agreed `PricingService` interface:
    - Calculates subtotal as the sum of each item's unit price multiplied by quantity ($\sum \text{unitPrice} \times \text{quantity}$).
    - Enforced `BigDecimal` for all monetary values with scale 2 and `RoundingMode.HALF_UP` (no `float` or `double`).
    - Applied agreed discount rules from `API_CONTRACT.md`:
      - `DISCOUNT10`: 10% off subtotal.
      - `SAVE20`: 20% off subtotal.
      - Case-insensitive and trimmed coupon codes (e.g., `discount10` = `DISCOUNT10`).
      - Unknown or null/empty coupon codes yield 0.00 discount.
      - Discount amount is capped at subtotal ($\text{discountAmount} \le \text{subtotal}$).
    - Applied 10% sales tax strictly to post-discount taxable subtotal ($\max(0, \text{subtotal} - \text{discountAmount}) \times 0.10$).
    - Computed grand total: $(\text{subtotal} - \text{discountAmount}) + \text{taxAmount}$.
    - Returned complete `PricingResultDto` breakdown.
    - Integrated with Member 1's `CartServiceImpl` via Spring `@Service` injection without duplicating any cart logic.
  - Implemented comprehensive JUnit 5 test suite in `PricingServiceTest` (18 tests).
- **Files Created / Modified**:
  - `src/main/java/com/example/ecommerce/service/PricingServiceImpl.java`
  - `src/test/java/com/example/ecommerce/service/PricingServiceTest.java`
  - `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
  - `PROJECT_STATUS.md`
- **Actual Test / Build Results**: `BUILD SUCCESS` (57 tests passed, 0 failures).

---

### TASK-006: Codebase Inspection, Contract Validation & Member 2 Onboarding
- **Task ID**: `TASK-006`
- **Owner / Assignee**: Member 2
- **Status**: `COMPLETED`
- **Scope**: Codebase audit, Git sync, environment setup, and verification of contracts and tests.
- **Actual Test / Build Results**: `BUILD SUCCESS` (39 tests passed, 0 failures).

---

### TASK-005: Quality Verification, End-to-End Integration Testing & Member 2 Handover
- **Task ID**: `TASK-005`
- **Owner / Assignee**: Member 1
- **Status**: `COMPLETED`
- **Scope**:
  - Full code review of Product and Cart implementations against `API_CONTRACT.md`.
  - Added full end-to-end integration test (`ProductAndCartIntegrationTest.java`).
  - Enhanced `README.md` with API cURL request examples and handover instructions for Member 2.
  - Executed Maven test build.
- **Actual Test / Build Results**: `BUILD SUCCESS` (39 tests passed).

---

### TASK-004: Shopping Cart Feature (Member 1)
- **Task ID**: `TASK-004`
- **Owner / Assignee**: Member 1
- **Status**: `COMPLETED`
- **Scope**: Cart repository, service, controller (`GET /api/cart`, `POST /api/cart/items`, `PATCH/PUT /api/cart/items/{productId}`, `DELETE /api/cart/items/{productId}`, `DELETE /api/cart`), and test suite.
- **Actual Test / Build Results**: `BUILD SUCCESS` (36 tests passed).

---

### TASK-003: Product Catalog Feature (Member 1)
- **Task ID**: `TASK-003`
- **Owner / Assignee**: Member 1
- **Status**: `COMPLETED`
- **Scope**: Product repository, service, controller, and test suite.
- **Actual Test / Build Results**: `BUILD SUCCESS` (10 tests passed).

---

### TASK-002: API Contracts, Domain Model Foundations & Member Coordination
- **Task ID**: `TASK-002`
- **Owner / Assignee**: Member 1 (in coordination with Member 2)
- **Status**: `COMPLETED`
- **Scope**: Established shared domain models, DTOs, `PricingService` interface contract, and `API_CONTRACT.md`.
- **Actual Test / Build Results**: `BUILD SUCCESS` (1 test passed).

---

### TASK-001: Project Bootstrap & Base Architecture
- **Task ID**: `TASK-001`
- **Owner / Assignee**: Backend Lead / Agent
- **Status**: `COMPLETED`
- **Scope**: Initial Spring Boot 3.3.5 project setup, Maven build configuration, package structure, and environment variable configuration.
- **Actual Test / Build Results**: `BUILD SUCCESS` (1 test passed).
