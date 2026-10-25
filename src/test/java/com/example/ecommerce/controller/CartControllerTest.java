package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartDto;
import com.example.ecommerce.dto.CartItemDto;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.exception.GlobalExceptionHandler;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(GlobalExceptionHandler.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("GET /api/cart returns 200 OK and empty cart when cart has no items")
    void testViewCart_emptyCart_returns200() throws Exception {
        CartDto emptyCart = new CartDto(1L, "demo-cart", new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
        when(cartService.getCart(eq("demo-cart"), any())).thenReturn(emptyCart);

        mockMvc.perform(get("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartKey", is("demo-cart")))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.subtotal", is(0)));
    }

    @Test
    @DisplayName("GET /api/cart returns 200 OK and cart items when cart contains items")
    void testViewCart_withItems_returns200AndDetails() throws Exception {
        CartItemDto item = new CartItemDto(10L, 1L, "Laptop Stand", new BigDecimal("25.00"), 2, new BigDecimal("50.00"));
        CartDto cart = new CartDto(1L, "demo-cart", List.of(item), new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"), null);

        when(cartService.getCart(eq("demo-cart"), any())).thenReturn(cart);

        mockMvc.perform(get("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Laptop Stand")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.items[0].totalPrice", is(50.00)))
                .andExpect(jsonPath("$.subtotal", is(50.00)));
    }

    @Test
    @DisplayName("POST /api/cart/items adds product and returns 200 OK with updated cart")
    void testAddToCart_success_returns200() throws Exception {
        AddToCartRequest request = new AddToCartRequest(1L, 2);
        CartItemDto item = new CartItemDto(10L, 1L, "Laptop Stand", new BigDecimal("25.00"), 2, new BigDecimal("50.00"));
        CartDto cart = new CartDto(1L, "demo-cart", List.of(item), new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"), null);

        when(cartService.addToCart(eq("demo-cart"), eq(1L), eq(2))).thenReturn(cart);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is(1)))
                .andExpect(jsonPath("$.subtotal", is(50.00)));
    }

    @Test
    @DisplayName("POST /api/cart/items with unknown product returns 404 NOT FOUND")
    void testAddToCart_unknownProduct_returns404() throws Exception {
        AddToCartRequest request = new AddToCartRequest(999L, 1);
        when(cartService.addToCart(eq("demo-cart"), eq(999L), eq(1)))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Product not found with id: 999")));
    }

    @Test
    @DisplayName("POST /api/cart/items with invalid quantity (<= 0) returns 400 BAD REQUEST")
    void testAddToCart_invalidQuantity_returns400() throws Exception {
        AddToCartRequest request = new AddToCartRequest(1L, 0);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /api/cart/items exceeding available stock returns 400 BAD REQUEST")
    void testAddToCart_exceedsStock_returns400() throws Exception {
        AddToCartRequest request = new AddToCartRequest(1L, 50);
        when(cartService.addToCart(eq("demo-cart"), eq(1L), eq(50)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product 'Laptop Stand'"));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Insufficient Stock")));
    }

    @Test
    @DisplayName("PATCH /api/cart/items/{productId} updates quantity and returns 200 OK")
    void testUpdateItemQuantity_success_returns200() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        CartItemDto item = new CartItemDto(10L, 1L, "Laptop Stand", new BigDecimal("25.00"), 5, new BigDecimal("125.00"));
        CartDto cart = new CartDto(1L, "demo-cart", List.of(item), new BigDecimal("125.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("125.00"), null);

        when(cartService.updateItemQuantity(eq("demo-cart"), eq(1L), eq(5))).thenReturn(cart);

        mockMvc.perform(patch("/api/cart/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(5)))
                .andExpect(jsonPath("$.subtotal", is(125.00)));
    }

    @Test
    @DisplayName("PUT /api/cart/items/{productId} updates quantity and returns 200 OK")
    void testPutItemQuantity_success_returns200() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        CartItemDto item = new CartItemDto(10L, 1L, "Laptop Stand", new BigDecimal("25.00"), 3, new BigDecimal("75.00"));
        CartDto cart = new CartDto(1L, "demo-cart", List.of(item), new BigDecimal("75.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("75.00"), null);

        when(cartService.updateItemQuantity(eq("demo-cart"), eq(1L), eq(3))).thenReturn(cart);

        mockMvc.perform(put("/api/cart/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(3)))
                .andExpect(jsonPath("$.subtotal", is(75.00)));
    }

    @Test
    @DisplayName("PATCH /api/cart/items/{productId} with invalid quantity returns 400 BAD REQUEST")
    void testUpdateItemQuantity_invalidQuantity_returns400() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(0);

        mockMvc.perform(patch("/api/cart/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{productId} removes item and returns 200 OK")
    void testRemoveItem_success_returns200() throws Exception {
        CartDto emptyCart = new CartDto(1L, "demo-cart", new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
        when(cartService.removeItem(eq("demo-cart"), eq(1L))).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.subtotal", is(0)));
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{productId} with item not in cart returns 404 NOT FOUND")
    void testRemoveItem_notInCart_returns404() throws Exception {
        when(cartService.removeItem(eq("demo-cart"), eq(999L)))
                .thenThrow(new ResourceNotFoundException("Product with id 999 is not in the cart"));

        mockMvc.perform(delete("/api/cart/items/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Product with id 999 is not in the cart")));
    }

    @Test
    @DisplayName("DELETE /api/cart clears all items and returns 204 NO CONTENT")
    void testClearCart_returns204() throws Exception {
        doNothing().when(cartService).clearCart("demo-cart");

        mockMvc.perform(delete("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
