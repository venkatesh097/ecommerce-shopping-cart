package com.example.ecommerce.service;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.PricingResultDto;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CheckoutService handling order persistence and transactional checkout.
 */
@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PricingService pricingService;

    @Autowired
    public CheckoutServiceImpl(CartService cartService,
                               ProductRepository productRepository,
                               OrderRepository orderRepository,
                               PricingService pricingService) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.pricingService = pricingService;
    }

    @Override
    public OrderResponse processCheckout(CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Checkout request must not be null");
        }

        String cartKey = (request.getCartKey() != null && !request.getCartKey().isBlank())
                ? request.getCartKey()
                : "demo-cart";

        Cart cart = cartService.getCartEntity(cartKey);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty or non-existent cart");
        }

        // Validate stock for each item and prepare inventory deduction
        for (CartItem item : cart.getItems()) {
            Product currentProduct = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));

            if (currentProduct.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product '" + currentProduct.getName()
                        + "'. Available: " + currentProduct.getStockQuantity() + ", requested: " + item.getQuantity());
            }

            // Deduct stock
            currentProduct.setStockQuantity(currentProduct.getStockQuantity() - item.getQuantity());
            productRepository.save(currentProduct);
        }

        // Calculate pricing using PricingService
        PricingResultDto pricing = pricingService.calculatePricing(cart.getItems(), request.getCouponCode());

        // Create Order entity
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(request.getCustomerName().trim());
        order.setCustomerEmail(request.getCustomerEmail().trim());
        order.setShippingAddress(request.getShippingAddress().trim());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setSubtotal(pricing.getSubtotal());
        order.setDiscountAmount(pricing.getDiscountAmount());
        order.setTaxAmount(pricing.getTaxAmount());
        order.setTotalAmount(pricing.getGrandTotal());
        order.setCreatedAt(LocalDateTime.now());

        // Map CartItems to OrderItems and link with Order
        for (CartItem item : cart.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem = new OrderItem(
                    order,
                    item.getProduct(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    lineTotal
            );
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear the cart upon successful order placement
        cartService.clearCart(cartKey);

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID must not be null");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getShippingAddress(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}
