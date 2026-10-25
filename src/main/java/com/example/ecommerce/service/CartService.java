package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartDto;
import com.example.ecommerce.entity.Cart;

/**
 * Service interface managing shopping cart lifecycle and operations.
 */
public interface CartService {

    /**
     * Retrieves the cart for the given key, creating it if it does not yet exist.
     *
     * @param cartKey unique cart/session key (defaults to "demo-cart" if null/blank)
     * @param couponCode optional coupon code for pricing
     * @return CartDto with line items and subtotal/pricing
     */
    CartDto getCart(String cartKey, String couponCode);

    /**
     * Adds an item with the given product and quantity to the cart.
     *
     * @param cartKey cart key
     * @param productId product identifier
     * @param quantity quantity to add (must be > 0)
     * @return updated CartDto
     */
    CartDto addToCart(String cartKey, Long productId, Integer quantity);

    /**
     * Updates the quantity of a specific product in the cart.
     *
     * @param cartKey cart key
     * @param productId product identifier
     * @param quantity new quantity (must be > 0)
     * @return updated CartDto
     */
    CartDto updateItemQuantity(String cartKey, Long productId, Integer quantity);

    /**
     * Removes an item for the specified product from the cart.
     *
     * @param cartKey cart key
     * @param productId product identifier
     * @return updated CartDto
     */
    CartDto removeItem(String cartKey, Long productId);

    /**
     * Clears all items from the cart.
     *
     * @param cartKey cart key
     */
    void clearCart(String cartKey);

    /**
     * Retrieves the underlying Cart JPA entity.
     * Exposes a stable contract so Member 2's pricing service or checkout can read cart items directly.
     *
     * @param cartKey cart key
     * @return Cart entity
     */
    Cart getCartEntity(String cartKey);
}
