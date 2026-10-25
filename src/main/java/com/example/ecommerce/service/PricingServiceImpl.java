package com.example.ecommerce.service;

import com.example.ecommerce.dto.PricingResultDto;
import com.example.ecommerce.entity.CartItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service implementation for cart pricing, discounts, tax calculations, and grand total.
 * Strictly maintains 2 decimal places with HALF_UP rounding using BigDecimal.
 */
@Service
public class PricingServiceImpl implements PricingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_10_RATE = new BigDecimal("0.10");
    private static final BigDecimal SAVE_20_RATE = new BigDecimal("0.20");

    @Override
    public PricingResultDto calculatePricing(List<CartItem> items, String couponCode) {
        BigDecimal subtotal = calculateSubtotal(items);
        String normalizedCoupon = normalizeCoupon(couponCode);
        BigDecimal discountAmount = calculateDiscount(subtotal, normalizedCoupon);

        BigDecimal taxableAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = calculateTax(taxableAmount);

        BigDecimal grandTotal = subtotal.subtract(discountAmount).add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new PricingResultDto(
                subtotal,
                discountAmount,
                taxAmount,
                grandTotal,
                normalizedCoupon
        );
    }

    @Override
    public BigDecimal calculateSubtotal(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return items.stream()
                .filter(item -> item != null && item.getUnitPrice() != null && item.getQuantity() != null)
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, String couponCode) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0 || couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String normalized = couponCode.trim().toUpperCase();
        BigDecimal discount = BigDecimal.ZERO;

        if ("DISCOUNT10".equals(normalized)) {
            discount = subtotal.multiply(DISCOUNT_10_RATE).setScale(2, RoundingMode.HALF_UP);
        } else if ("SAVE20".equals(normalized)) {
            discount = subtotal.multiply(SAVE_20_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTax(BigDecimal taxableAmount) {
        if (taxableAmount == null || taxableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return taxableAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCoupon(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        String trimmed = couponCode.trim();
        if ("DISCOUNT10".equalsIgnoreCase(trimmed)) {
            return "DISCOUNT10";
        }
        if ("SAVE20".equalsIgnoreCase(trimmed)) {
            return "SAVE20";
        }
        return trimmed.toUpperCase();
    }
}
