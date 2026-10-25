package com.example.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.PricingResultDto;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private Product product;
    private Cart cart;
    private CheckoutRequest validRequest;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Laptop", "High-performance laptop", new BigDecimal("1200.00"), 10, "Electronics", null);
        cart = new Cart(1L, "demo-cart");
        validRequest = new CheckoutRequest("demo-cart", "Alice Smith", "alice@example.com", "123 Main St", "DISCOUNT10", "CREDIT_CARD");
    }

    @Test
    @DisplayName("Reject checkout when request is null")
    void testCheckoutNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> checkoutService.processCheckout(null));
    }

    @Test
    @DisplayName("Reject checkout when cart is null")
    void testCheckoutNullCart() {
        when(cartService.getCartEntity("demo-cart")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> checkoutService.processCheckout(validRequest));
        assertEquals("Cannot checkout an empty or non-existent cart", ex.getMessage());
    }

    @Test
    @DisplayName("Reject checkout when cart has no items")
    void testCheckoutEmptyCart() {
        when(cartService.getCartEntity("demo-cart")).thenReturn(cart);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> checkoutService.processCheckout(validRequest));
        assertEquals("Cannot checkout an empty or non-existent cart", ex.getMessage());
    }

    @Test
    @DisplayName("Reject checkout when cart item product is missing in database")
    void testCheckoutMissingProduct() {
        CartItem item = new CartItem(cart, product, 2, product.getPrice());
        cart.addItem(item);

        when(cartService.getCartEntity("demo-cart")).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutService.processCheckout(validRequest));
    }

    @Test
    @DisplayName("Reject checkout when requested quantity exceeds available product stock")
    void testCheckoutInsufficientStock() {
        product.setStockQuantity(1);
        CartItem item = new CartItem(cart, product, 2, product.getPrice());
        cart.addItem(item);

        when(cartService.getCartEntity("demo-cart")).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> checkoutService.processCheckout(validRequest));

        assertNotNull(ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart(anyString());
    }

    @Test
    @DisplayName("Successful checkout persists Order, decrements stock, and clears cart")
    void testSuccessfulCheckout() {
        CartItem item = new CartItem(cart, product, 2, product.getPrice());
        cart.addItem(item);

        when(cartService.getCartEntity("demo-cart")).thenReturn(cart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        PricingResultDto pricingResult = new PricingResultDto(
                new BigDecimal("2400.00"),
                new BigDecimal("240.00"),
                new BigDecimal("216.00"),
                new BigDecimal("2376.00"),
                "DISCOUNT10"
        );
        when(pricingService.calculatePricing(anyList(), any())).thenReturn(pricingResult);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(101L);
            return o;
        });

        OrderResponse response = checkoutService.processCheckout(validRequest);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Alice Smith", response.getCustomerName());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("2400.00"), response.getSubtotal());
        assertEquals(new BigDecimal("240.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("216.00"), response.getTaxAmount());
        assertEquals(new BigDecimal("2376.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());

        // Verify stock deducted from 10 to 8
        assertEquals(8, product.getStockQuantity());
        verify(productRepository).save(product);

        // Verify order saved and cart cleared
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart("demo-cart");
    }

    @Test
    @DisplayName("getOrderById returns order response for existing order")
    void testGetOrderByIdSuccess() {
        Order order = new Order();
        order.setId(101L);
        order.setOrderNumber("ORD-20261025-ABCD1234");
        order.setCustomerName("Alice Smith");
        order.setCustomerEmail("alice@example.com");
        order.setShippingAddress("123 Main St");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setSubtotal(new BigDecimal("100.00"));
        order.setDiscountAmount(new BigDecimal("10.00"));
        order.setTaxAmount(new BigDecimal("9.00"));
        order.setTotalAmount(new BigDecimal("99.00"));
        order.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));

        OrderResponse response = checkoutService.getOrderById(101L);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("ORD-20261025-ABCD1234", response.getOrderNumber());
    }

    @Test
    @DisplayName("getOrderById throws ResourceNotFoundException for unknown ID")
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutService.getOrderById(999L));
    }

    @Test
    @DisplayName("getOrderById throws IllegalArgumentException for null ID")
    void testGetOrderByIdNull() {
        assertThrows(IllegalArgumentException.class, () -> checkoutService.getOrderById(null));
    }
}
