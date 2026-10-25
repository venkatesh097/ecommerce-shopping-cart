package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartDto;
import com.example.ecommerce.dto.CartItemDto;
import com.example.ecommerce.dto.PricingResultDto;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation managing shopping cart operations.
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final String DEFAULT_CART_KEY = "demo-cart";

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PricingService pricingService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                           ProductRepository productRepository,
                           @Autowired(required = false) PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(String cartKey, String couponCode) {
        Cart cart = getOrCreateCartEntity(cartKey);
        return mapToDto(cart, couponCode);
    }

    @Override
    public CartDto addToCart(String cartKey, Long productId, Integer quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID must not be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Cart cart = getOrCreateCartEntity(cartKey);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int currentQty = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int totalRequested = currentQty + quantity;

        if (product.getStockQuantity() < totalRequested) {
            throw new InsufficientStockException("Insufficient stock for product '" + product.getName()
                    + "'. Available: " + product.getStockQuantity() + ", requested: " + totalRequested);
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(totalRequested);
            existingItem.setUnitPrice(product.getPrice());
        } else {
            CartItem newItem = new CartItem(cart, product, quantity, product.getPrice());
            cart.addItem(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return mapToDto(saved, null);
    }

    @Override
    public CartDto updateItemQuantity(String cartKey, Long productId, Integer quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID must not be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Cart cart = getOrCreateCartEntity(cartKey);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " is not in the cart"));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product '" + product.getName()
                    + "'. Available: " + product.getStockQuantity() + ", requested: " + quantity);
        }

        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());

        Cart saved = cartRepository.save(cart);
        return mapToDto(saved, null);
    }

    @Override
    public CartDto removeItem(String cartKey, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID must not be null");
        }

        Cart cart = getOrCreateCartEntity(cartKey);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " is not in the cart"));

        cart.removeItem(item);
        Cart saved = cartRepository.save(cart);
        return mapToDto(saved, null);
    }

    @Override
    public void clearCart(String cartKey) {
        Cart cart = getOrCreateCartEntity(cartKey);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartEntity(String cartKey) {
        return getOrCreateCartEntity(cartKey);
    }

    private Cart getOrCreateCartEntity(String cartKey) {
        String effectiveKey = (cartKey == null || cartKey.trim().isEmpty()) ? DEFAULT_CART_KEY : cartKey.trim();
        return cartRepository.findByCartKey(effectiveKey)
                .orElseGet(() -> cartRepository.save(new Cart(null, effectiveKey)));
    }

    private CartDto mapToDto(Cart cart, String couponCode) {
        List<CartItemDto> itemDtos = cart.getItems().stream().map(item -> {
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            return new CartItemDto(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    unitPrice,
                    item.getQuantity(),
                    lineTotal
            );
        }).collect(Collectors.toList());

        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (pricingService != null) {
            PricingResultDto pricing = pricingService.calculatePricing(cart, couponCode);
            return new CartDto(
                    cart.getId(),
                    cart.getCartKey(),
                    itemDtos,
                    pricing.getSubtotal(),
                    pricing.getDiscountAmount(),
                    pricing.getTaxAmount(),
                    pricing.getGrandTotal(),
                    pricing.getAppliedCoupon()
            );
        }

        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return new CartDto(
                cart.getId(),
                cart.getCartKey(),
                itemDtos,
                subtotal,
                zero,
                zero,
                subtotal,
                couponCode
        );
    }
}
