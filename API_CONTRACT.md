# API & Domain Contract Specification

This document details the shared contracts, domain models, DTOs, endpoint signatures, and pricing rules agreed upon for the **E-Commerce Shopping Cart** backend project between **Member 1** (Catalog, Cart, Checkout, Orders) and **Member 2** (Pricing, Discounts, Tax Calculation).

---

## 1. Shared Package Structure

All backend code lives under the base package `com.example.ecommerce`:

| Package | Purpose & Ownership |
|---|---|
| `com.example.ecommerce.entity` | Shared JPA database models (`Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`) |
| `com.example.ecommerce.dto` | Shared Request/Response DTOs across API boundaries |
| `com.example.ecommerce.service` | Service interfaces (`PricingService` interface agreed; Member 2 implements `PricingServiceImpl`) |
| `com.example.ecommerce.controller` | REST API Controllers (`ProductController`, `CartController`, `CheckoutController`) |
| `com.example.ecommerce.repository` | Spring Data JPA Repositories |
| `com.example.ecommerce.exception` | Custom exceptions and global exception handler |
| `com.example.ecommerce.config` | Bean definitions, security, CORS |

---

## 2. Monetary Precision & Rounding Rules

1. **Data Type**: All currency amounts must use `java.math.BigDecimal`. Under no circumstances should `double` or `float` be used for monetary values.
2. **Database Column**: SQL `DECIMAL(10, 2)` (`precision = 10, scale = 2`).
3. **Scale & Rounding**:
   - Scale is strictly **2 decimal places**.
   - Rounding mode is strictly **`RoundingMode.HALF_UP`**.
4. **Quantity**: `Integer` strictly `>= 1`.

---

## 3. Demo Cart Identification & Management

- **Demo Identifier**: Default cart is identified by `cartKey = "demo-cart"`.
- **Cart Access**:
  - `GET /api/cart` operates against the active demo cart (`cartKey = "demo-cart"`).
  - Multi-cart / session support is facilitated via `?cartKey=...` query parameter or path `/api/cart/{cartKey}` with fallback to `"demo-cart"`.
- **Lifecycle**: If no cart exists for the provided `cartKey`, the service auto-creates an empty `Cart`.

---

## 4. Pricing, Discount & Tax Contract (Member 1 ↔ Member 2 Interface)

### 4.1 Interface: `com.example.ecommerce.service.PricingService`
Member 2 implements this interface; Member 1 invokes it during Cart retrieval and Checkout order creation.

```java
public interface PricingService {
    PricingResultDto calculatePricing(List<CartItem> items, String couponCode);
    PricingResultDto calculatePricing(Cart cart, String couponCode);
    BigDecimal calculateSubtotal(List<CartItem> items);
    BigDecimal calculateDiscount(BigDecimal subtotal, String couponCode);
    BigDecimal calculateTax(BigDecimal taxableAmount);
}
```

### 4.2 Agreed Calculation Rules:
1. **Item Line Total**:
   $$\text{lineTotal} = \text{unitPrice} \times \text{quantity} \quad (\text{scale 2, HALF\_UP})$$
2. **Subtotal**:
   $$\text{subtotal} = \sum \text{lineTotal}$$
3. **Discount Assumptions**:
   - Standard coupon codes supported:
     - `DISCOUNT10`: 10% off subtotal.
     - `SAVE20`: 20% off subtotal.
   - Discount amount is capped at subtotal ($\text{discountAmount} \le \text{subtotal}$).
4. **Tax Assumptions**:
   - Standard sales tax rate: **10% (0.10)**.
   - Tax base: Tax is calculated on post-discount subtotal:
     $$\text{taxableAmount} = \max(0, \text{subtotal} - \text{discountAmount})$$
     $$\text{taxAmount} = \text{taxableAmount} \times 0.10 \quad (\text{scale 2, HALF\_UP})$$
5. **Grand Total**:
   $$\text{grandTotal} = (\text{subtotal} - \text{discountAmount}) + \text{taxAmount}$$

---

## 5. REST API Endpoints Specification

### 5.1 Product Catalog API
| Method | Endpoint | Request Body | Response Body | Description |
|---|---|---|---|---|
| `GET` | `/api/products` | None | `List<ProductDto>` | Retrieve all available products |
| `GET` | `/api/products/{id}` | None | `ProductDto` | Retrieve product details by ID |
| `POST` | `/api/products` | `ProductDto` | `ProductDto` | Create a product (admin/seeding) |

### 5.2 Shopping Cart API
| Method | Endpoint | Request Body | Response Body | Description |
|---|---|---|---|---|
| `GET` | `/api/cart` | None (Optional `?couponCode=...`) | `CartDto` | Retrieve demo cart with pricing breakdown |
| `POST` | `/api/cart/items` | `AddToCartRequest` | `CartDto` | Add product to cart (or increment quantity) |
| `PATCH` / `PUT` | `/api/cart/items/{productId}` | `UpdateCartItemRequest` | `CartDto` | Update item quantity |
| `DELETE` | `/api/cart/items/{productId}` | None | `CartDto` | Remove specific item from cart |
| `DELETE` | `/api/cart` | None | `204 No Content` | Clear entire cart |

### 5.3 Checkout & Order API
| Method | Endpoint | Request Body | Response Body | Description |
|---|---|---|---|---|
| `POST` | `/api/checkout` | `CheckoutRequest` | `OrderResponse` | Process cart checkout, validate stock, and generate order |
| `GET` | `/api/orders/{id}` | None | `OrderResponse` | Retrieve order details by ID |

---

## 6. Shared DTO Schemas

### `AddToCartRequest`
```json
{
  "productId": 1,
  "quantity": 2
}
```

### `UpdateCartItemRequest`
```json
{
  "quantity": 3
}
```

### `CartDto`
```json
{
  "id": 1,
  "cartKey": "demo-cart",
  "items": [
    {
      "id": 10,
      "productId": 1,
      "productName": "Wireless Headphones",
      "unitPrice": 49.99,
      "quantity": 2,
      "totalPrice": 99.98
    }
  ],
  "subtotal": 99.98,
  "discountAmount": 10.00,
  "taxAmount": 9.00,
  "grandTotal": 98.98,
  "appliedCoupon": "DISCOUNT10"
}
```

### `CheckoutRequest`
```json
{
  "cartKey": "demo-cart",
  "customerName": "Jane Doe",
  "customerEmail": "jane.doe@example.com",
  "shippingAddress": "123 Campus Lane, College Park",
  "couponCode": "DISCOUNT10",
  "paymentMethod": "CREDIT_CARD"
}
```

### `OrderResponse`
```json
{
  "id": 101,
  "orderNumber": "ORD-20261025-101",
  "customerName": "Jane Doe",
  "customerEmail": "jane.doe@example.com",
  "shippingAddress": "123 Campus Lane, College Park",
  "status": "CONFIRMED",
  "subtotal": 99.98,
  "discountAmount": 10.00,
  "taxAmount": 9.00,
  "totalAmount": 98.98,
  "items": [
    {
      "productId": 1,
      "productName": "Wireless Headphones",
      "quantity": 2,
      "unitPrice": 49.99,
      "totalPrice": 99.98
    }
  ],
  "createdAt": "2026-10-25T14:15:00"
}
```

---

## 7. Open Coordination Points for Review

The following decisions are proposed and documented for Member 2's review:
1. **Tax Base Timing**: Tax is currently agreed to apply to post-discount subtotal ($\text{subtotal} - \text{discount}$). If Member 2 prefers taxing pre-discount subtotal, update section 4.2.
2. **Coupon Case Sensitivity**: Agreed that coupons are trimmed and case-insensitive (e.g. `discount10` = `DISCOUNT10`).
3. **Cart Item Price Snapshotting**: `CartItem.unitPrice` captures the product price when placed in cart; during checkout, current live product price can either be refreshed or honored based on project policy.
