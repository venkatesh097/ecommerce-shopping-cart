package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Shopping Cart summary and items.
 */
public class CartDto {

    private Long id;
    private String cartKey;
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String appliedCoupon;

    public CartDto() {
    }

    public CartDto(Long id, String cartKey, List<CartItemDto> items, BigDecimal subtotal, BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal grandTotal, String appliedCoupon) {
        this.id = id;
        this.cartKey = cartKey;
        this.items = items != null ? items : new ArrayList<>();
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.grandTotal = grandTotal;
        this.appliedCoupon = appliedCoupon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCartKey() {
        return cartKey;
    }

    public void setCartKey(String cartKey) {
        this.cartKey = cartKey;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(String appliedCoupon) {
        this.appliedCoupon = appliedCoupon;
    }
}
