package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartDto;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Product product1;
    private Product product2;
    private Cart cart;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Laptop Stand", "Aluminum Stand", new BigDecimal("25.00"), 10, "Accessories", null);
        product2 = new Product(2L, "Mouse Pad", "Extended Pad", new BigDecimal("15.00"), 5, "Accessories", null);
        cart = new Cart(1L, "demo-cart");
    }

    @Test
    @DisplayName("Viewing an empty cart returns 0 items and 0.00 subtotal")
    void testGetCart_emptyCart_returnsZeroItemsAndSubtotal() {
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        CartDto result = cartService.getCart("demo-cart", null);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getGrandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Viewing cart with items returns correct items and calculated subtotal")
    void testGetCart_existingItems_returnsItemsAndSubtotal() {
        CartItem item1 = new CartItem(cart, product1, 2, product1.getPrice());
        cart.addItem(item1);

        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        CartDto result = cartService.getCart("demo-cart", null);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getItems().get(0).getTotalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Adding a new product to cart succeeds")
    void testAddToCart_newProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDto result = cartService.addToCart("demo-cart", 1L, 2);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Adding existing product in cart increments its quantity")
    void testAddToCart_existingProduct_incrementsQuantity() {
        CartItem existingItem = new CartItem(cart, product1, 2, product1.getPrice());
        cart.addItem(existingItem);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDto result = cartService.addToCart("demo-cart", 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("Adding unknown product throws ResourceNotFoundException")
    void testAddToCart_unknownProduct_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart("demo-cart", 999L, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Adding zero or negative quantity throws IllegalArgumentException")
    void testAddToCart_invalidQuantity_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cartService.addToCart("demo-cart", 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than zero");

        assertThatThrownBy(() -> cartService.addToCart("demo-cart", 1L, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("Adding quantity exceeding product stock throws InsufficientStockException")
    void testAddToCart_exceedsStock_throwsInsufficientStockException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.addToCart("demo-cart", 1L, 15))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product 'Laptop Stand'");
    }

    @Test
    @DisplayName("Updating quantity of item in cart updates quantity and total price")
    void testUpdateItemQuantity_success() {
        CartItem item = new CartItem(cart, product1, 2, product1.getPrice());
        cart.addItem(item);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDto result = cartService.updateItemQuantity("demo-cart", 1L, 4);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(4);
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Updating quantity with unknown product throws ResourceNotFoundException")
    void testUpdateItemQuantity_unknownProduct_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity("demo-cart", 999L, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Updating quantity for product not present in cart throws ResourceNotFoundException")
    void testUpdateItemQuantity_itemNotInCart_throwsResourceNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItemQuantity("demo-cart", 1L, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product with id 1 is not in the cart");
    }

    @Test
    @DisplayName("Updating quantity exceeding stock throws InsufficientStockException")
    void testUpdateItemQuantity_exceedsStock_throwsInsufficientStockException() {
        CartItem item = new CartItem(cart, product1, 2, product1.getPrice());
        cart.addItem(item);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItemQuantity("demo-cart", 1L, 20))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Removing item from cart deletes the item")
    void testRemoveItem_success() {
        CartItem item = new CartItem(cart, product1, 2, product1.getPrice());
        cart.addItem(item);

        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDto result = cartService.removeItem("demo-cart", 1L);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Removing item not present in cart throws ResourceNotFoundException")
    void testRemoveItem_notInCart_throwsResourceNotFoundException() {
        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.removeItem("demo-cart", 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product with id 1 is not in the cart");
    }

    @Test
    @DisplayName("Clearing cart empties all items")
    void testClearCart_success() {
        CartItem item = new CartItem(cart, product1, 2, product1.getPrice());
        cart.addItem(item);

        when(cartRepository.findByCartKey("demo-cart")).thenReturn(Optional.of(cart));

        cartService.clearCart("demo-cart");

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }
}
