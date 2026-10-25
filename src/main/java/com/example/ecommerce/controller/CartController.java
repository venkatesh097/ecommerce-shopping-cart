package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartDto;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Shopping Cart management endpoints.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * GET /api/cart : Retrieve active cart and items with pricing.
     *
     * @param cartKey optional cart key, defaults to "demo-cart"
     * @param couponCode optional coupon code for discount calculations
     * @return 200 OK with CartDto
     */
    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestParam(required = false, defaultValue = "demo-cart") String cartKey,
            @RequestParam(required = false) String couponCode) {
        CartDto cart = cartService.getCart(cartKey, couponCode);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items : Add a product item to the shopping cart.
     *
     * @param request AddToCartRequest with productId and quantity
     * @param cartKey optional cart key, defaults to "demo-cart"
     * @return 200 OK with updated CartDto
     */
    @PostMapping("/items")
    public ResponseEntity<CartDto> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestParam(required = false, defaultValue = "demo-cart") String cartKey) {
        CartDto updatedCart = cartService.addToCart(cartKey, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * PATCH /api/cart/items/{productId} : Update quantity of an item in the cart.
     *
     * @param productId ID of product in cart
     * @param request UpdateCartItemRequest with new quantity
     * @param cartKey optional cart key, defaults to "demo-cart"
     * @return 200 OK with updated CartDto
     */
    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartDto> patchItemQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestParam(required = false, defaultValue = "demo-cart") String cartKey) {
        CartDto updatedCart = cartService.updateItemQuantity(cartKey, productId, request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * PUT /api/cart/items/{productId} : Update quantity (PUT alias for PATCH).
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> putItemQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestParam(required = false, defaultValue = "demo-cart") String cartKey) {
        CartDto updatedCart = cartService.updateItemQuantity(cartKey, productId, request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * DELETE /api/cart/items/{productId} : Remove a product from the shopping cart.
     *
     * @param productId ID of product to remove
     * @param cartKey optional cart key, defaults to "demo-cart"
     * @return 200 OK with updated CartDto
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "demo-cart") String cartKey) {
        CartDto updatedCart = cartService.removeItem(cartKey, productId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * DELETE /api/cart : Clear all items from the shopping cart.
     *
     * @param cartKey optional cart key, defaults to "demo-cart"
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestParam(required = false, defaultValue = "demo-cart") String cartKey) {
        cartService.clearCart(cartKey);
        return ResponseEntity.noContent().build();
    }
}
