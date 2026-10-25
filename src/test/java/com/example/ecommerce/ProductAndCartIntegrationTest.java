package com.example.ecommerce;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ProductAndCartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product savedProduct1;
    private Product savedProduct2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();

        savedProduct1 = productRepository.save(new Product(
                null, "Wireless Mouse", "Ergonomic Bluetooth Mouse",
                new BigDecimal("29.99"), 15, "Electronics", null
        ));

        savedProduct2 = productRepository.save(new Product(
                null, "Mechanical Keyboard", "RGB Mechanical Keyboard",
                new BigDecimal("89.99"), 5, "Electronics", null
        ));
    }

    @Test
    @DisplayName("End-to-End: List products and fetch product by ID")
    void testProductCatalogFlow() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/products/" + savedProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Wireless Mouse")))
                .andExpect(jsonPath("$.price", is(29.99)));
    }

    @Test
    @DisplayName("End-to-End: Cart lifecycle (add, view, update, remove, clear)")
    void testCartLifecycleFlow() throws Exception {
        // 1. Initial cart is empty
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.subtotal", is(0.0)));

        // 2. Add product 1 (qty 2)
        AddToCartRequest addReq = new AddToCartRequest(savedProduct1.getId(), 2);
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Wireless Mouse")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.subtotal", is(59.98)));

        // 3. Update quantity of product 1 to 3
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest(3);
        mockMvc.perform(patch("/api/cart/items/" + savedProduct1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(3)))
                .andExpect(jsonPath("$.subtotal", is(89.97)));

        // 4. Add product 2 (qty 1)
        AddToCartRequest addReq2 = new AddToCartRequest(savedProduct2.getId(), 1);
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.subtotal", is(179.96)));

        // 5. Remove product 1
        mockMvc.perform(delete("/api/cart/items/" + savedProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Mechanical Keyboard")))
                .andExpect(jsonPath("$.subtotal", is(89.99)));

        // 6. Clear cart
        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isNoContent());

        // 7. Verify cart is empty
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.subtotal", is(0.0)));
    }

    @Test
    @DisplayName("End-to-End: Stock limit validation prevents over-ordering")
    void testStockLimitExceeded() throws Exception {
        AddToCartRequest addReq = new AddToCartRequest(savedProduct2.getId(), 10); // only 5 in stock

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Insufficient Stock")));
    }

    @Test
    @DisplayName("End-to-End Complete Flow: List products -> Add to cart -> Update quantity -> Pricing calculation -> Checkout -> View order")
    void testCompletePurchaseFlow() throws Exception {
        // Step 1: List products
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Step 2: Add product 1 (qty 1) to demo cart
        AddToCartRequest addReq = new AddToCartRequest(savedProduct1.getId(), 1);
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.subtotal", is(29.99)));

        // Step 3: Update product 1 quantity to 2
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest(2);
        mockMvc.perform(patch("/api/cart/items/" + savedProduct1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.subtotal", is(59.98)));

        // Step 4: View cart with DISCOUNT10 coupon
        // Subtotal: 59.98, Discount 10%: 6.00, Taxable: 53.98, Tax 10%: 5.40, Total: 59.38
        mockMvc.perform(get("/api/cart?couponCode=DISCOUNT10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", is(59.98)))
                .andExpect(jsonPath("$.discountAmount", is(6.00)))
                .andExpect(jsonPath("$.taxAmount", is(5.40)))
                .andExpect(jsonPath("$.grandTotal", is(59.38)))
                .andExpect(jsonPath("$.appliedCoupon", is("DISCOUNT10")));

        // Step 5: Checkout the cart
        CheckoutRequest checkoutReq = new CheckoutRequest(
                "demo-cart",
                "Charlie Brown",
                "charlie@example.com",
                "100 Peanuts Way, Minneapolis, MN",
                "DISCOUNT10",
                "CREDIT_CARD"
        );

        MvcResult checkoutResult = mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.subtotal", is(59.98)))
                .andExpect(jsonPath("$.discountAmount", is(6.00)))
                .andExpect(jsonPath("$.taxAmount", is(5.40)))
                .andExpect(jsonPath("$.totalAmount", is(59.38)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Wireless Mouse")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(checkoutResult.getResponse().getContentAsString());
        Long orderId = responseNode.get("id").asLong();
        assertNotNull(orderId);

        // Step 6: Verify stock was decremented from 15 to 13
        Product updatedProduct = productRepository.findById(savedProduct1.getId()).orElseThrow();
        assertEquals(13, updatedProduct.getStockQuantity());

        // Step 7: Verify order can be retrieved via GET /api/orders/{id}
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.customerName", is("Charlie Brown")))
                .andExpect(jsonPath("$.totalAmount", is(59.38)));

        // Step 8: Verify cart was emptied post-checkout
        Cart cart = cartRepository.findByCartKey("demo-cart").orElseThrow();
        assertTrue(cart.getItems().isEmpty());
    }
}
