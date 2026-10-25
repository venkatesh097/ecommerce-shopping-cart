# Project Status & Task Tracking

This document tracks all tasks, owners, implementation status, build/test results, blockers, unresolved decisions, and next steps for the E-Commerce Shopping Cart Backend project.

---

## Current Status Overview

| Task ID | Task Description | Owner | Status | Build / Test Results |
|---|---|---|---|---|
| `TASK-001` | Initialize Spring Boot Maven Backend Project & Base Architecture | Backend Lead / Agent | `COMPLETED` | `BUILD SUCCESS` (1 test passed) |
| `TASK-002` | API Contracts, Domain Model Foundations & Member Coordination | Member 1 (with Member 2) | `COMPLETED` | `BUILD SUCCESS` (24 source files compiled, 1 test passed) |
| `TASK-003` | Implement Product Catalog Feature (Repository, Service, Controller, Tests) | Member 1 | `COMPLETED` | `BUILD SUCCESS` (10 tests passed) |
| `TASK-004` | Implement Cart Feature (Repository, Service, Controller, Exception Handling, Tests) | Member 1 | `COMPLETED` | `BUILD SUCCESS` (36 tests passed, 0 failures) |

---

## Task Details

### TASK-004: Shopping Cart Feature (Member 1)
- **Task ID**: `TASK-004`
- **Owner / Assignee**: Member 1
- **Status**: `COMPLETED`
- **Scope**:
  - Implement `CartRepository` extending `JpaRepository<Cart, Long>`.
  - Add `InsufficientStockException` and register handler in `GlobalExceptionHandler` returning `400 Bad Request`.
  - Implement `CartService` interface and `CartServiceImpl` handling:
    - View cart with calculated subtotal (`GET /api/cart`) defaulting to `demo-cart`.
    - Add item (`POST /api/cart/items`) with product lookup, positive quantity check, and inventory stock limit validation.
    - Update item quantity (`PATCH /api/cart/items/{productId}` and `PUT /api/cart/items/{productId}`) with stock validation.
    - Remove item (`DELETE /api/cart/items/{productId}`).
    - Clear entire cart (`DELETE /api/cart`).
    - Decoupled `PricingService` integration: cart service gracefully falls back to raw subtotal when `PricingService` bean is pending from Member 2, and automatically incorporates pricing breakdown once Member 2's implementation is registered.
    - Stable entity accessor `getCartEntity(cartKey)` allowing Member 2 to inspect cart items.
  - Document routes in `API_CONTRACT.md`.
  - Add comprehensive JUnit 5 test suites (`CartServiceTest` and `CartControllerTest`) covering add, view, update, remove, clear, empty cart, unknown product, zero/negative quantity, and stock limits.
- **Files Created / Modified**:
  - `src/main/java/com/example/ecommerce/exception/InsufficientStockException.java`
  - `src/main/java/com/example/ecommerce/exception/GlobalExceptionHandler.java`
  - `src/main/java/com/example/ecommerce/repository/CartRepository.java`
  - `src/main/java/com/example/ecommerce/service/CartService.java`
  - `src/main/java/com/example/ecommerce/service/CartServiceImpl.java`
  - `src/main/java/com/example/ecommerce/controller/CartController.java`
  - `src/test/java/com/example/ecommerce/service/CartServiceTest.java`
  - `src/test/java/com/example/ecommerce/controller/CartControllerTest.java`
  - `API_CONTRACT.md`
  - `PROJECT_STATUS.md`
- **Actual Test / Build Results**:
  - Command: `mvn test`
  - Execution Result: `BUILD SUCCESS` (Total time: 12.692 s)
  - Test Suite Breakdown:
    - `CartControllerTest`: 12 tests passed (empty cart, view with items, add item, unknown product 404, invalid quantity 400, stock exceeded 400, patch quantity, put quantity, remove item, remove missing item 404, clear cart 204).
    - `CartServiceTest`: 14 tests passed (empty cart subtotal, item subtotal, add new product, add existing product increments qty, unknown product throws 404, zero/negative qty throws 400, exceeds stock throws 400, update qty, update unknown product, update item not in cart, update exceeds stock, remove item, remove item not in cart, clear cart).
    - `ProductControllerTest`: 5 tests passed.
    - `ProductServiceTest`: 4 tests passed.
    - `EcommerceApplicationTests`: 1 test passed.
    - Total: 36 tests run, 0 failures, 0 errors, 0 skipped.
- **Blockers / Challenges**:
  - None.
- **Next Steps**:
  1. Member 2 implements `PricingService` implementation and pricing calculation tests.
  2. Implement Checkout and Order placement flow (`POST /api/checkout`, `GET /api/orders/{id}`).

---

### TASK-003: Product Catalog Feature (Member 1)
- **Task ID**: `TASK-003`
- **Owner / Assignee**: Member 1
- **Status**: `COMPLETED`
- **Scope**: Product entity, repository, service, controller (`GET /api/products`, `GET /api/products/{id}`, `POST /api/products`), and test suite.
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
