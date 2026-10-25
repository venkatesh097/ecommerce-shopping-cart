package com.example.ecommerce.service;

import com.example.ecommerce.dto.PricingResultDto;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface defining the pricing contract between Member 1 and Member 2.
 * Member 2 implements this service to provide subtotal, discount, tax, and grand total calculations.
 */
public interface PricingService {

    /**
     * Calculates the complete pricing breakdown for the given list of cart items and optional coupon.
     *
     * @param items list of CartItem entities to evaluate
     * @param couponCode optional discount/coupon code (can be null or blank)
     * @return PricingResultDto containing subtotal, discountAmount, taxAmount, grandTotal, and appliedCoupon
     */
    PricingResultDto calculatePricing(List<CartItem> items, String couponCode);

    /**
     * Overloaded helper method to calculate pricing directly from a Cart entity.
     *
     * @param cart the Cart entity
     * @param couponCode optional coupon code
     * @return PricingResultDto
     */
    default PricingResultDto calculatePricing(Cart cart, String couponCode) {
        return calculatePricing(cart != null ? cart.getItems() : List.of(), couponCode);
    }

    /**
     * Calculates the raw subtotal from cart items.
     *
     * @param items list of cart items
     * @return subtotal rounded to 2 decimal places (HALF_UP)
     */
    BigDecimal calculateSubtotal(List<CartItem> items);

    /**
     * Calculates the discount amount based on the subtotal and coupon code.
     *
     * @param subtotal raw item subtotal
     * @param couponCode coupon/discount identifier
     * @return discount deduction amount
     */
    BigDecimal calculateDiscount(BigDecimal subtotal, String couponCode);

    /**
     * Calculates the applicable tax for the taxable amount.
     *
     * @param taxableAmount amount subject to taxation
     * @return tax amount rounded to 2 decimal places (HALF_UP)
     */
    BigDecimal calculateTax(BigDecimal taxableAmount);
}
