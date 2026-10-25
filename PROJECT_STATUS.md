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

---

## Task Details

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
  - Implemented comprehensive JUnit 5 test suite in `PricingServiceTest` (18 tests) covering:
    - Empty and null cart items.
    - Single item with quantity 1.
    - Single item with multiple quantities.
    - Multiple items with varying quantities and prices.
    - `DISCOUNT10` (10% discount on subtotal).
    - `SAVE20` (20% discount on subtotal).
    - Case-insensitivity and whitespace tolerance for coupon codes.
    - Invalid and unknown coupon handling.
    - Strict `RoundingMode.HALF_UP` validation on tax and discount calculations.
    - Direct calls to `calculateSubtotal`, `calculateDiscount`, and `calculateTax`.
    - Overloaded `calculatePricing(Cart, couponCode)` delegation.
    - Null entity protection and graceful handling of items with null fields.
    - Zero subtotal boundary handling.
    - Penny precision with fraction rounding.
- **Files Created / Modified**:
  - `src/main/java/com/example/ecommerce/service/PricingServiceImpl.java` (New pricing service implementation)
  - `src/test/java/com/example/ecommerce/service/PricingServiceTest.java` (New comprehensive pricing unit test suite, 18 tests)
  - `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` (Ensures Mockito subclass mock maker)
  - `PROJECT_STATUS.md` (Updated with TASK-007 results)
- **Actual Test / Build Results**:
  - Command: `./mvnw.cmd test`
  - Result: `BUILD SUCCESS` (Total time: 13.628 s)
  - Test Suite Breakdown:
    - `PricingServiceTest`: **18 tests passed** (0 failures, 0 errors)
    - `CartControllerTest`: 12 tests passed
    - `CartServiceTest`: 14 tests passed
    - `ProductControllerTest`: 5 tests passed
    - `ProductServiceTest`: 4 tests passed
    - `ProductAndCartIntegrationTest`: 3 tests passed
    - `EcommerceApplicationTests`: 1 test passed
    - Total: **57 tests run, 0 failures, 0 errors, 0 skipped**.
- **Blockers / Challenges**:
  - None.
- **Next Steps**:
  - Implement Member 2's Checkout and Order Processing feature (`POST /api/checkout`, `OrderRepository`, `CheckoutService`, `CheckoutController`).

---

### TASK-006: Codebase Inspection, Contract Validation & Member 2 Onboarding
- **Task ID**: `TASK-006`
- **Owner / Assignee**: Member 2
- **Status**: `COMPLETED`
- **Scope**:
  - Inspected repository structure, Maven dependencies in `pom.xml`, and Git status.
  - Concluded merge with `origin/main` to synchronize with latest shared repository changes.
  - Inspected existing JPA entities created by Member 1 (`Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`).
  - Verified `CartService` interface and its implementation `CartServiceImpl`, confirming availability of `getCartEntity(cartKey)` for direct item inspection.
  - Verified `PricingService` interface contract in `com.example.ecommerce.service` matching `API_CONTRACT.md`.
  - Audited shared DTOs (`PricingResultDto`, `CheckoutRequest`, `OrderResponse`, `OrderItemResponse`).
  - Verified database configurations (`application.properties` with PostgreSQL default using environment variables, `application-h2.properties` for zero-config local runs, and `src/test/resources/application.properties` for tests).
  - Executed existing test suite (`./mvnw.cmd test`) to ensure baseline integrity before implementing pricing and checkout logic.
  - Confirmed 0 blockers: All required shared models and interfaces are published, aligned, and ready for Member 2 implementation.
- **Files Created / Modified**:
  - `PROJECT_STATUS.md` (Updated with Member 2 inspection results)
- **Actual Test / Build Results**:
  - Command: `./mvnw.cmd test`
  - Result: `BUILD SUCCESS` (Total time: 13.393 s)
  - Total: **39 tests run, 0 failures, 0 errors, 0 skipped**.
- **Blockers / Challenges**:
  - None. All contracts and models published by Member 1 are completely intact and compatible.
- **Next Steps**:
  - Proceed to implement `PricingServiceImpl` implementing `PricingService` with agreed discount (`DISCOUNT10`, `SAVE20`), 10% tax, and `BigDecimal` scale 2 `HALF_UP` calculations.
  - Add comprehensive JUnit 5 unit tests for `PricingService`.

---

### TASK-005: Quality Verification, End-to-End Integration Testing & Member 2 Handover
- **Task ID**: `TASK-005`
- **Owner / Assignee**: Member 1
- **Status**: `COMPLETED`
- **Scope**:
  - Full code review of Product and Cart implementations against `API_CONTRACT.md`.
  - Added full end-to-end integration test (`ProductAndCartIntegrationTest.java`) exercising both Product catalog and Cart lifecycle against active repository persistence and Spring Boot web layer.
  - Verified precision consistency (`BigDecimal`, `RoundingMode.HALF_UP`), entity encapsulation (no persistence entities leaked in public API), and error responses.
  - Enhanced `README.md` with API cURL request examples and handover instructions for Member 2.
  - Executed Maven test build.
- **Actual Test / Build Results**:
  - Command: `mvn test`
  - Result: `BUILD SUCCESS` (39 tests passed).

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
