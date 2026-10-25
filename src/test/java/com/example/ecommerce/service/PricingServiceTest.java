package com.example.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.ecommerce.dto.PricingResultDto;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingServiceImpl();
    }

    private CartItem createItem(Long id, String name, String price, int quantity) {
        Product product = new Product(id, name, "Description", new BigDecimal(price), 100, "Category", null);
        return new CartItem(null, product, quantity, new BigDecimal(price));
    }

    @Test
    @DisplayName("Empty or null cart items return all zeros")
    void testEmptyAndNullCart() {
        PricingResultDto emptyResult = pricingService.calculatePricing(List.of(), null);
        assertEquals(new BigDecimal("0.00"), emptyResult.getSubtotal());
        assertEquals(new BigDecimal("0.00"), emptyResult.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), emptyResult.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), emptyResult.getGrandTotal());
        assertNull(emptyResult.getAppliedCoupon());

        PricingResultDto nullResult = pricingService.calculatePricing((List<CartItem>) null, null);
        assertEquals(new BigDecimal("0.00"), nullResult.getSubtotal());
        assertEquals(new BigDecimal("0.00"), nullResult.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), nullResult.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), nullResult.getGrandTotal());
    }

    @Test
    @DisplayName("Single item with quantity 1 calculates subtotal, 10% tax, and total correctly")
    void testSingleItemQuantityOne() {
        // Price: 100.00, Qty: 1 => Subtotal: 100.00, Discount: 0.00, Tax (10%): 10.00, Total: 110.00
        List<CartItem> items = List.of(createItem(1L, "Widget", "100.00", 1));

        PricingResultDto result = pricingService.calculatePricing(items, null);

        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("10.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("110.00"), result.getGrandTotal());
    }

    @Test
    @DisplayName("Single item with multiple quantities multiplies unit price by quantity")
    void testSingleItemMultipleQuantities() {
        // Price: 49.99, Qty: 3 => Subtotal: 149.97, Tax (10% of 149.97 = 14.997 -> 15.00), Total: 164.97
        List<CartItem> items = List.of(createItem(1L, "Keyboard", "49.99", 3));

        PricingResultDto result = pricingService.calculatePricing(items, null);

        assertEquals(new BigDecimal("149.97"), result.getSubtotal());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("15.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("164.97"), result.getGrandTotal());
    }

    @Test
    @DisplayName("Multiple items with varying quantities calculate correct cumulative subtotal")
    void testMultipleItems() {
        // Item 1: 25.50 * 2 = 51.00
        // Item 2: 10.00 * 3 = 30.00
        // Item 3: 5.75 * 4 = 23.00
        // Subtotal = 104.00, Tax = 10.40, Total = 114.40
        List<CartItem> items = List.of(
                createItem(1L, "Mouse", "25.50", 2),
                createItem(2L, "Pad", "10.00", 3),
                createItem(3L, "Cable", "5.75", 4)
        );

        PricingResultDto result = pricingService.calculatePricing(items, null);

        assertEquals(new BigDecimal("104.00"), result.getSubtotal());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("10.40"), result.getTaxAmount());
        assertEquals(new BigDecimal("114.40"), result.getGrandTotal());
    }

    @Test
    @DisplayName("DISCOUNT10 applies 10% discount to subtotal and taxes post-discount subtotal")
    void testDiscount10() {
        // Subtotal: 200.00
        // Discount 10%: 20.00
        // Taxable: 180.00 => Tax (10%): 18.00
        // Total: 180.00 + 18.00 = 198.00
        List<CartItem> items = List.of(createItem(1L, "Monitor", "100.00", 2));

        PricingResultDto result = pricingService.calculatePricing(items, "DISCOUNT10");

        assertEquals(new BigDecimal("200.00"), result.getSubtotal());
        assertEquals(new BigDecimal("20.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("18.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("198.00"), result.getGrandTotal());
        assertEquals("DISCOUNT10", result.getAppliedCoupon());
    }

    @Test
    @DisplayName("SAVE20 applies 20% discount to subtotal and taxes post-discount subtotal")
    void testSave20() {
        // Subtotal: 100.00
        // Discount 20%: 20.00
        // Taxable: 80.00 => Tax (10%): 8.00
        // Total: 80.00 + 8.00 = 88.00
        List<CartItem> items = List.of(createItem(1L, "Headphones", "50.00", 2));

        PricingResultDto result = pricingService.calculatePricing(items, "SAVE20");

        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(new BigDecimal("20.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("8.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("88.00"), result.getGrandTotal());
        assertEquals("SAVE20", result.getAppliedCoupon());
    }

    @Test
    @DisplayName("Discount coupons are case-insensitive and whitespace-tolerant")
    void testCouponCaseAndWhitespaceTolerance() {
        List<CartItem> items = List.of(createItem(1L, "Speaker", "100.00", 1));

        PricingResultDto resultLower = pricingService.calculatePricing(items, "  discount10  ");
        assertEquals(new BigDecimal("10.00"), resultLower.getDiscountAmount());
        assertEquals("DISCOUNT10", resultLower.getAppliedCoupon());

        PricingResultDto resultMixed = pricingService.calculatePricing(items, "SaVe20");
        assertEquals(new BigDecimal("20.00"), resultMixed.getDiscountAmount());
        assertEquals("SAVE20", resultMixed.getAppliedCoupon());
    }

    @Test
    @DisplayName("Unknown or invalid coupon code grants zero discount")
    void testInvalidCouponCode() {
        // Subtotal: 100.00, Unknown coupon => Discount: 0.00, Tax: 10.00, Total: 110.00
        List<CartItem> items = List.of(createItem(1L, "Webcam", "100.00", 1));

        PricingResultDto result = pricingService.calculatePricing(items, "INVALID_CODE");

        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("10.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("110.00"), result.getGrandTotal());
        assertEquals("INVALID_CODE", result.getAppliedCoupon());
    }

    @Test
    @DisplayName("Tax rounding follows RoundingMode.HALF_UP strictly")
    void testTaxHalfUpRounding() {
        // Subtotal: 12.35, Tax at 10% = 1.235 => should round up to 1.24
        List<CartItem> items = List.of(createItem(1L, "Book", "12.35", 1));

        PricingResultDto result = pricingService.calculatePricing(items, null);

        assertEquals(new BigDecimal("12.35"), result.getSubtotal());
        assertEquals(new BigDecimal("1.24"), result.getTaxAmount());
        assertEquals(new BigDecimal("13.59"), result.getGrandTotal());
    }

    @Test
    @DisplayName("Discount rounding follows RoundingMode.HALF_UP strictly")
    void testDiscountHalfUpRounding() {
        // Subtotal: 12.35, Discount 10% = 1.235 => rounds up to 1.24
        // Post-discount: 12.35 - 1.24 = 11.11
        // Tax: 11.11 * 0.10 = 1.111 => rounds to 1.11
        // Grand Total: 11.11 + 1.11 = 12.22
        List<CartItem> items = List.of(createItem(1L, "Book", "12.35", 1));

        PricingResultDto result = pricingService.calculatePricing(items, "DISCOUNT10");

        assertEquals(new BigDecimal("12.35"), result.getSubtotal());
        assertEquals(new BigDecimal("1.24"), result.getDiscountAmount());
        assertEquals(new BigDecimal("1.11"), result.getTaxAmount());
        assertEquals(new BigDecimal("12.22"), result.getGrandTotal());
    }

    @Test
    @DisplayName("Direct calculateSubtotal method calculates accurately")
    void testCalculateSubtotal() {
        List<CartItem> items = List.of(
                createItem(1L, "Item A", "19.99", 2),
                createItem(2L, "Item B", "5.00", 3)
        );
        // (19.99 * 2 = 39.98) + (5.00 * 3 = 15.00) = 54.98
        BigDecimal subtotal = pricingService.calculateSubtotal(items);
        assertEquals(new BigDecimal("54.98"), subtotal);
    }

    @Test
    @DisplayName("Direct calculateDiscount method calculates accurately")
    void testCalculateDiscount() {
        BigDecimal subtotal = new BigDecimal("150.00");

        BigDecimal d10 = pricingService.calculateDiscount(subtotal, "DISCOUNT10");
        assertEquals(new BigDecimal("15.00"), d10);

        BigDecimal d20 = pricingService.calculateDiscount(subtotal, "SAVE20");
        assertEquals(new BigDecimal("30.00"), d20);

        BigDecimal dNone = pricingService.calculateDiscount(subtotal, null);
        assertEquals(new BigDecimal("0.00"), dNone);
    }

    @Test
    @DisplayName("Direct calculateTax method calculates accurately")
    void testCalculateTax() {
        BigDecimal taxable = new BigDecimal("85.50");
        // 85.50 * 0.10 = 8.55
        BigDecimal tax = pricingService.calculateTax(taxable);
        assertEquals(new BigDecimal("8.55"), tax);

        // Zero taxable
        assertEquals(new BigDecimal("0.00"), pricingService.calculateTax(BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), pricingService.calculateTax(null));
    }

    @Test
    @DisplayName("Overloaded calculatePricing(Cart, couponCode) delegates to Cart.getItems()")
    void testCartEntityOverload() {
        Cart cart = new Cart(1L, "demo-cart");
        cart.addItem(createItem(1L, "Phone", "299.99", 1));

        PricingResultDto result = pricingService.calculatePricing(cart, "DISCOUNT10");

        assertNotNull(result);
        assertEquals(new BigDecimal("299.99"), result.getSubtotal());
        assertEquals(new BigDecimal("30.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("27.00"), result.getTaxAmount()); // (299.99 - 30.00 = 269.99) * 0.10 = 26.999 -> 27.00
        assertEquals(new BigDecimal("296.99"), result.getGrandTotal()); // 269.99 + 27.00 = 296.99
    }

    @Test
    @DisplayName("Overloaded calculatePricing with null Cart returns zero breakdown")
    void testNullCartEntityOverload() {
        PricingResultDto result = pricingService.calculatePricing((Cart) null, "DISCOUNT10");

        assertEquals(new BigDecimal("0.00"), result.getSubtotal());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), result.getGrandTotal());
    }

    @Test
    @DisplayName("Items with null fields are safely filtered in subtotal calculation")
    void testItemsWithNullFieldsHandledGracefully() {
        List<CartItem> items = new ArrayList<>();
        items.add(null);
        items.add(new CartItem(null, null, null, null));
        items.add(createItem(1L, "Valid", "20.00", 2));

        BigDecimal subtotal = pricingService.calculateSubtotal(items);
        assertEquals(new BigDecimal("40.00"), subtotal);
    }

    @Test
    @DisplayName("Zero subtotal produces zero discount and tax")
    void testZeroSubtotalProducesZeroDiscountAndTax() {
        BigDecimal zero = new BigDecimal("0.00");
        assertEquals(new BigDecimal("0.00"), pricingService.calculateDiscount(zero, "DISCOUNT10"));
        assertEquals(new BigDecimal("0.00"), pricingService.calculateTax(zero));
    }

    @Test
    @DisplayName("Precision test: 3 items with penny pricing and fractions")
    void testPennyPrecision() {
        // 0.33 * 3 = 0.99
        List<CartItem> items = List.of(createItem(1L, "Candy", "0.33", 3));
        PricingResultDto result = pricingService.calculatePricing(items, null);
        assertEquals(new BigDecimal("0.99"), result.getSubtotal());
        assertEquals(new BigDecimal("0.10"), result.getTaxAmount()); // 0.99 * 0.10 = 0.099 -> 0.10
        assertEquals(new BigDecimal("1.09"), result.getGrandTotal());
    }
}
