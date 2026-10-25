package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductDto;
import com.example.ecommerce.exception.GlobalExceptionHandler;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products returns 200 OK and list of products")
    void testGetAllProducts_returns200AndList() throws Exception {
        ProductDto p1 = new ProductDto(1L, "Laptop Stand", "Aluminum Stand", new BigDecimal("29.99"), 15, "Accessories", null);
        ProductDto p2 = new ProductDto(2L, "USB-C Hub", "7-in-1 Multiport Hub", new BigDecimal("45.50"), 20, "Accessories", null);

        when(productService.getAllProducts()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Laptop Stand")))
                .andExpect(jsonPath("$[0].price", is(29.99)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("USB-C Hub")))
                .andExpect(jsonPath("$[1].price", is(45.50)));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 200 OK and product details when product exists")
    void testGetProductById_existingProduct_returns200() throws Exception {
        ProductDto p1 = new ProductDto(1L, "Laptop Stand", "Aluminum Stand", new BigDecimal("29.99"), 15, "Accessories", null);

        when(productService.getProductById(1L)).thenReturn(p1);

        mockMvc.perform(get("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Laptop Stand")))
                .andExpect(jsonPath("$.price", is(29.99)))
                .andExpect(jsonPath("$.stockQuantity", is(15)))
                .andExpect(jsonPath("$.category", is("Accessories")));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 404 NOT FOUND when product does not exist")
    void testGetProductById_unknownProduct_returns404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: 999")))
                .andExpect(jsonPath("$.path", is("/api/products/999")));
    }

    @Test
    @DisplayName("POST /api/products returns 201 CREATED when payload is valid")
    void testCreateProduct_validPayload_returns201() throws Exception {
        ProductDto request = new ProductDto(null, "Wireless Mouse", "Bluetooth mouse", new BigDecimal("25.00"), 30, "Electronics", null);
        ProductDto created = new ProductDto(10L, "Wireless Mouse", "Bluetooth mouse", new BigDecimal("25.00"), 30, "Electronics", null);

        when(productService.createProduct(any(ProductDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/10"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Wireless Mouse")))
                .andExpect(jsonPath("$.price", is(25.00)));
    }

    @Test
    @DisplayName("POST /api/products returns 400 BAD REQUEST when validation fails")
    void testCreateProduct_invalidPayload_returns400() throws Exception {
        ProductDto invalidRequest = new ProductDto(null, "", null, new BigDecimal("-5.00"), -1, "", null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }
}
