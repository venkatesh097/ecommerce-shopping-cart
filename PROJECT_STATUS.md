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

---

## Task Details

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
  - Added test suites:
    - `CheckoutServiceTest` (9 tests): empty cart rejection, null cart rejection, missing product rejection, stock validation and deduction, order creation, order query by ID, not found handling.
    - `CheckoutControllerTest` (6 tests): MockMvc slice tests for 200 OK checkout, 400 Bad Request on empty cart, 400 Bad Request on invalid payload, 400 Bad Request on insufficient stock, 200 OK order lookup, 404 Not Found order lookup.
- **Files Created / Modified**:
  - `src/main/java/com/example/ecommerce/entity/Order.java` (Added `FetchType.EAGER` to `items`)
  - `src/main/java/com/example/ecommerce/entity/Cart.java` (Added `FetchType.EAGER` to `items`)
  - `src/main/java/com/example/ecommerce/repository/OrderRepository.java` (New order repository)
  - `src/main/java/com/example/ecommerce/service/CheckoutService.java` (New checkout service interface)
  - `src/main/java/com/example/ecommerce/service/CheckoutServiceImpl.java` (New checkout service implementation)
  - `src/main/java/com/example/ecommerce/controller/CheckoutController.java` (New checkout controller)
  - `src/test/java/com/example/ecommerce/service/CheckoutServiceTest.java` (New service unit tests, 9 tests)
  - `src/test/java/com/example/ecommerce/controller/CheckoutControllerTest.java` (New controller slice tests, 6 tests)
  - `PROJECT_STATUS.md` (Updated with TASK-008 results)
- **Actual Test / Build Results**:
  - Command: `./mvnw.cmd test`
  - Result: `BUILD SUCCESS` (Total time: 12.206 s)
  - Test Suite Breakdown:
    - `CheckoutServiceTest`: **9 tests passed**
    - `CheckoutControllerTest`: **6 tests passed**
    - `PricingServiceTest`: 18 tests passed
    - `CartControllerTest`: 12 tests passed
    - `CartServiceTest`: 14 tests passed
    - `ProductControllerTest`: 5 tests passed
    - `ProductServiceTest`: 4 tests passed
    - `ProductAndCartIntegrationTest`: 3 tests passed
    - `EcommerceApplicationTests`: 1 test passed
    - Total: **72 tests run, 0 failures, 0 errors, 0 skipped**.
- **Blockers / Challenges**:
  - None. Transactional checkout, stock decrement, and order mapping pass cleanly.
- **Next Steps**:
  - Implement real PostgreSQL API integration tests (`PostgresApiIntegrationTest.java`).

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
