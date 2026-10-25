package com.example.ecommerce;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.ProductDto;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private Product savedProduct1;
    private Product savedProduct2;

    @BeforeEach
    void setUp() {
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
}
