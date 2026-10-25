package com.example.ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end API integration tests verifying product retrieval, checkout flow,
 * stock decrement, and order persistence against a REAL PostgreSQL database instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@ExtendWith(PostgresAvailableCondition.class)
class PostgresApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void cleanAndSeedDatabase() {
        cleanDatabase();

        testProduct1 = productRepository.save(new Product(
                null,
                "Mechanical Gaming Keyboard",
                "RGB mechanical gaming keyboard with tactile switches",
                new BigDecimal("129.99"),
                20,
                "Electronics",
                "http://example.com/keyboard.png"
        ));

        testProduct2 = productRepository.save(new Product(
                null,
                "Wireless Ergonomic Mouse",
                "High precision ergonomic mouse",
                new BigDecimal("49.99"),
                15,
                "Electronics",
                "http://example.com/mouse.png"
        ));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Verify that the test suite is running against a REAL PostgreSQL database")
    void testVerifyRealPostgresConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String dbProduct = conn.getMetaData().getDatabaseProductName();
            String dbVersion = conn.getMetaData().getDatabaseProductVersion();
            assertEquals("PostgreSQL", dbProduct, "Tests must run against real PostgreSQL, not an in-memory substitute");
           assertTrue(
        dbVersion.matches("(?s).*\\b(13|14|15|16|17|18)\\..*"),
        "Expected PostgreSQL version 13–18, got: " + dbVersion
);
        }
    }

    @Test
    @DisplayName("Product data can be retrieved through the REST API")
    void testProductRetrievalThroughApi() throws Exception {
        // List products
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Mechanical Gaming Keyboard"))
                .andExpect(jsonPath("$[0].price").value(129.99))
                .andExpect(jsonPath("$[1].name").value("Wireless Ergonomic Mouse"))
                .andExpect(jsonPath("$[1].price").value(49.99));

        // Get single product by ID
        mockMvc.perform(get("/api/products/" + testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct1.getId()))
                .andExpect(jsonPath("$.name").value("Mechanical Gaming Keyboard"))
                .andExpect(jsonPath("$.stockQuantity").value(20));
    }

    @Test
    @DisplayName("Valid checkout request creates an order, decrements stock in PostgreSQL, and clears the cart")
    void testValidCheckoutCreatesOrderAndDeductsStock() throws Exception {
        String cartKey = "e2e-postgres-cart";

        // 1. Add item to cart
        AddToCartRequest addReq = new AddToCartRequest(testProduct1.getId(), 2);
        mockMvc.perform(post("/api/cart/items?cartKey=" + cartKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.subtotal").value(259.98));

        // 2. Perform checkout
        CheckoutRequest checkoutReq = new CheckoutRequest(
                cartKey,
                "Robert Johnson",
                "robert.johnson@example.com",
                "456 Tech Park Way, Austin, TX",
                "DISCOUNT10",
                "CREDIT_CARD"
        );

        MvcResult result = mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.customerName").value("Robert Johnson"))
                .andExpect(jsonPath("$.subtotal").value(259.98))
                .andExpect(jsonPath("$.discountAmount").value(26.00))
                .andExpect(jsonPath("$.taxAmount").value(23.40))
                .andExpect(jsonPath("$.totalAmount").value(257.38))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Mechanical Gaming Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long orderId = responseNode.get("id").asLong();

        // 3. Verify stock in PostgreSQL decremented from 20 to 18
        Product updatedProduct = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertEquals(18, updatedProduct.getStockQuantity(), "Product stock in PostgreSQL should be decremented by ordered quantity");

        // 4. Verify cart in PostgreSQL is emptied
        Cart clearedCart = cartRepository.findByCartKey(cartKey).orElseThrow();
        assertTrue(clearedCart.getItems().isEmpty(), "Customer shopping cart in PostgreSQL should be cleared after checkout");

        // 5. Verify order is retrievable via GET /api/orders/{id}
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerEmail").value("robert.johnson@example.com"))
                .andExpect(jsonPath("$.totalAmount").value(257.38));
    }

    @Test
    @DisplayName("Order and order-item records are properly persisted in PostgreSQL tables")
    void testOrderAndOrderItemsPersistedInPostgres() throws Exception {
        String cartKey = "persist-check-cart";

        // Add 2 distinct products
        mockMvc.perform(post("/api/cart/items?cartKey=" + cartKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(testProduct1.getId(), 1))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/items?cartKey=" + cartKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(testProduct2.getId(), 2))))
                .andExpect(status().isOk());

        // Checkout with SAVE20
        CheckoutRequest checkoutReq = new CheckoutRequest(
                cartKey,
                "Elena Gomez",
                "elena@example.com",
                "789 Silicon Ave, San Jose, CA",
                "SAVE20",
                "DEBIT_CARD"
        );

        MvcResult result = mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isOk())
                .andReturn();

        Long orderId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Direct database verification via Spring Data JPA connected to PostgreSQL
        Order persistedOrder = orderRepository.findById(orderId).orElse(null);
        assertNotNull(persistedOrder, "Order entity must be persisted in PostgreSQL 'orders' table");
        assertEquals("Elena Gomez", persistedOrder.getCustomerName());
        assertEquals("elena@example.com", persistedOrder.getCustomerEmail());
        assertEquals(OrderStatus.CONFIRMED, persistedOrder.getStatus());
        assertEquals(2, persistedOrder.getItems().size(), "Order must have 2 OrderItem records in PostgreSQL 'order_items' table");

        // Verify subtotal: 129.99 * 1 + 49.99 * 2 = 129.99 + 99.98 = 229.97
        assertEquals(new BigDecimal("229.97"), persistedOrder.getSubtotal());
        // SAVE20: 229.97 * 0.20 = 45.994 -> 45.99
        assertEquals(new BigDecimal("45.99"), persistedOrder.getDiscountAmount());
        // Taxable: 229.97 - 45.99 = 183.98 => Tax (10%): 18.398 -> 18.40
        assertEquals(new BigDecimal("18.40"), persistedOrder.getTaxAmount());
        // Grand Total: 183.98 + 18.40 = 202.38
        assertEquals(new BigDecimal("202.38"), persistedOrder.getTotalAmount());
    }

    @Test
    @DisplayName("Empty checkout request returns HTTP 400 Bad Request")
    void testEmptyCartCheckoutReturnsBadRequest() throws Exception {
        CheckoutRequest emptyCheckout = new CheckoutRequest(
                "empty-cart-key",
                "Ghost Customer",
                "ghost@example.com",
                "Nowhere Street",
                null,
                "CASH"
        );

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyCheckout)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot checkout an empty or non-existent cart"));
    }

    @Test
    @DisplayName("Invalid checkout payload with missing customerName and invalid email returns HTTP 400")
    void testInvalidCheckoutPayloadReturnsBadRequest() throws Exception {
        CheckoutRequest invalidCheckout = new CheckoutRequest(
                "demo-cart",
                "", // Blank name
                "not-an-email", // Malformed email
                "Somewhere",
                null,
                null
        );

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCheckout)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Checkout fails with HTTP 400 when product stock is insufficient, leaving inventory unchanged")
    void testInsufficientStockRejectionWithRollback() throws Exception {
        String cartKey = "stock-check-cart";

        // Add 1 item
        mockMvc.perform(post("/api/cart/items?cartKey=" + cartKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(testProduct1.getId(), 5))))
                .andExpect(status().isOk());

        // Reduce stock in DB to 2
        testProduct1.setStockQuantity(2);
        productRepository.save(testProduct1);

        CheckoutRequest checkoutReq = new CheckoutRequest(
                cartKey,
                "Greedy Buyer",
                "buyer@example.com",
                "123 Street",
                null,
                "CARD"
        );

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Insufficient Stock"));

        // Verify stock was not decremented further
        Product afterFailedCheckout = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertEquals(2, afterFailedCheckout.getStockQuantity());

        // Verify no order was persisted
        assertEquals(0, orderRepository.count());
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns HTTP 404 for non-existent order in PostgreSQL")
    void testGetNonExistentOrderReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
