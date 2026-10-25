package com.example.ecommerce.service;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.dto.OrderResponse;

/**
 * Service interface for processing checkout and retrieving orders.
 */
public interface CheckoutService {

    /**
     * Processes checkout for the cart specified in the request.
     * Validates cart content, checks stock, decrements inventory, calculates totals,
     * persists Order and OrderItems, and clears the cart.
     *
     * @param request valid checkout request payload
     * @return OrderResponse with confirmed order details and price summary
     */
    OrderResponse processCheckout(CheckoutRequest request);

    /**
     * Retrieves an order by its unique database ID.
     *
     * @param id order database ID
     * @return OrderResponse
     */
    OrderResponse getOrderById(Long id);
}
