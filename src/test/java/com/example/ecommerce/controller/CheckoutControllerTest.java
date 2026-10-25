package com.example.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.service.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckoutService checkoutService;

    @Test
    @DisplayName("POST /api/checkout returns 200 OK and order details on valid request")
    void testCheckoutSuccess() throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                "demo-cart",
                "Alice Smith",
                "alice@example.com",
                "123 Campus Lane, College Park",
                "DISCOUNT10",
                "CREDIT_CARD"
        );

        OrderItemResponse itemResponse = new OrderItemResponse(
                1L, "Laptop", 2, new BigDecimal("1200.00"), new BigDecimal("2400.00")
        );

        OrderResponse orderResponse = new OrderResponse(
                101L,
                "ORD-20261025-101",
                "Alice Smith",
                "alice@example.com",
                "123 Campus Lane, College Park",
                OrderStatus.CONFIRMED,
                new BigDecimal("2400.00"),
                new BigDecimal("240.00"),
                new BigDecimal("216.00"),
                new BigDecimal("2376.00"),
                List.of(itemResponse),
                LocalDateTime.now()
        );

        when(checkoutService.processCheckout(any(CheckoutRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20261025-101"))
                .andExpect(jsonPath("$.customerName").value("Alice Smith"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(2376.00))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"));
    }

    @Test
    @DisplayName("POST /api/checkout returns 400 Bad Request when cart is empty")
    void testCheckoutEmptyCart() throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                "demo-cart",
                "Alice Smith",
                "alice@example.com",
                "123 Campus Lane, College Park",
                null,
                "CREDIT_CARD"
        );

        when(checkoutService.processCheckout(any(CheckoutRequest.class)))
                .thenThrow(new IllegalArgumentException("Cannot checkout an empty or non-existent cart"));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot checkout an empty or non-existent cart"));
    }

    @Test
    @DisplayName("POST /api/checkout returns 400 Bad Request when customerName is blank")
    void testCheckoutValidationFailure() throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                "demo-cart",
                "", // Blank name
                "alice@example.com",
                "123 Campus Lane",
                null,
                "CREDIT_CARD"
        );

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/checkout returns 400 Bad Request on insufficient stock")
    void testCheckoutInsufficientStock() throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                "demo-cart",
                "Alice Smith",
                "alice@example.com",
                "123 Campus Lane",
                null,
                "CREDIT_CARD"
        );

        when(checkoutService.processCheckout(any(CheckoutRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product 'Laptop'"));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Insufficient stock for product 'Laptop'"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 200 OK with order details")
    void testGetOrderByIdSuccess() throws Exception {
        OrderResponse orderResponse = new OrderResponse(
                101L,
                "ORD-20261025-101",
                "Alice Smith",
                "alice@example.com",
                "123 Campus Lane",
                OrderStatus.CONFIRMED,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                new BigDecimal("110.00"),
                List.of(),
                LocalDateTime.now()
        );

        when(checkoutService.getOrderById(101L)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20261025-101"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 404 Not Found for non-existent order")
    void testGetOrderByIdNotFound() throws Exception {
        when(checkoutService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
