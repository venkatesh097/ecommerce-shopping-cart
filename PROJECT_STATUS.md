# Project Status & Task Tracking

This document tracks all tasks, owners, implementation status, build/test results, blockers, unresolved decisions, and next steps for the E-Commerce Shopping Cart Backend project.

---

## Current Status Overview

| Task ID | Task Description | Owner | Status | Build / Test Results |
|---|---|---|---|---|
| `TASK-001` | Initialize Spring Boot Maven Backend Project & Base Architecture | Backend Lead / Agent | `COMPLETED` | `BUILD SUCCESS` (1 test passed, 0 failures) |
| `TASK-002` | API Contracts, Domain Model Foundations & Member Coordination | Member 1 (with Member 2) | `COMPLETED` | `BUILD SUCCESS` (24 source files compiled, 1 test passed) |

---

## Task Details

### TASK-002: API Contracts, Domain Model Foundations & Member Coordination
- **Task ID**: `TASK-002`
- **Owner / Assignee**: Member 1 (in coordination with Member 2)
- **Status**: `COMPLETED` (Ready for Review)
- **Scope**:
  - Establish shared package structure under `com.example.ecommerce`.
  - Agree on domain models: `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`.
  - Agree on demo cart identification (`cartKey = "demo-cart"`).
  - Agree on monetary precision and rounding: `BigDecimal`, `DECIMAL(10, 2)`, 2 decimal places with `RoundingMode.HALF_UP`.
  - Agree on Pricing Service contract (`PricingService` interface) so Member 2 can independently implement calculation logic.
  - Establish shared DTOs for Product catalog, Cart operations, and Checkout/Orders.
  - Document complete specification in `API_CONTRACT.md`.
  - Add foundation interfaces and models without implementing unnecessary business logic.
- **Files Changed / Created**:
  - `API_CONTRACT.md` (Created comprehensive contract specification)
  - `src/main/java/com/example/ecommerce/entity/Product.java`
  - `src/main/java/com/example/ecommerce/entity/Cart.java`
  - `src/main/java/com/example/ecommerce/entity/CartItem.java`
  - `src/main/java/com/example/ecommerce/entity/Order.java`
  - `src/main/java/com/example/ecommerce/entity/OrderItem.java`
  - `src/main/java/com/example/ecommerce/entity/OrderStatus.java`
  - `src/main/java/com/example/ecommerce/dto/ProductDto.java`
  - `src/main/java/com/example/ecommerce/dto/CartItemDto.java`
  - `src/main/java/com/example/ecommerce/dto/CartDto.java`
  - `src/main/java/com/example/ecommerce/dto/AddToCartRequest.java`
  - `src/main/java/com/example/ecommerce/dto/UpdateCartItemRequest.java`
  - `src/main/java/com/example/ecommerce/dto/CheckoutRequest.java`
  - `src/main/java/com/example/ecommerce/dto/PricingResultDto.java`
  - `src/main/java/com/example/ecommerce/dto/OrderItemResponse.java`
  - `src/main/java/com/example/ecommerce/dto/OrderResponse.java`
  - `src/main/java/com/example/ecommerce/service/PricingService.java`
  - `src/test/resources/application.properties`
  - `PROJECT_STATUS.md`
- **Actual Test / Build Results**:
  - Command: `mvn test`
  - Compilation: 24 source files compiled successfully with `javac [debug parameters release 17]`.
  - Execution Result: `BUILD SUCCESS`
  - Tests run: 1 (`EcommerceApplicationTests.contextLoads()`)
  - Failures: 0, Errors: 0, Skipped: 0
  - Duration: 9.75 s
- **Unresolved Decisions / Coordination Points for Review**:
  1. **Tax Base Calculation**: Currently specified to compute tax on `(subtotal - discountAmount)`. Pending Member 2 confirmation if tax should apply to pre-discount subtotal instead.
  2. **Supported Coupons**: Defaults established as `DISCOUNT10` (10% off) and `SAVE20` (20% off). Additional coupon logic to be defined by Member 2 in `PricingServiceImpl`.
  3. **Cart Item Price Refresh**: `CartItem.unitPrice` holds the price at time of addition; confirmation on whether checkout refreshes with live `Product.price`.
- **Next Steps**:
  1. Review and sign off on `API_CONTRACT.md` with Member 2.
  2. Member 1: Implement Product repository, service, and controller (`/api/products`).
  3. Member 2: Implement `PricingServiceImpl` implementing `PricingService` with discount/tax logic and JUnit unit tests.
  4. Member 1: Implement Cart operations (`/api/cart`) and Checkout flow (`/api/checkout`).

---

### TASK-001: Project Bootstrap & Base Architecture
- **Task ID**: `TASK-001`
- **Owner / Assignee**: Backend Lead / Agent
- **Status**: `COMPLETED`
- **Scope**: Initial Spring Boot 3.3.5 project setup, Maven build configuration, package structure, and environment variable configuration.
- **Actual Test / Build Results**: `BUILD SUCCESS` (1 test passed).
