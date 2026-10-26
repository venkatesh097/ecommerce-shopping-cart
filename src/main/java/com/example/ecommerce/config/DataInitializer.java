package com.example.ecommerce.config;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds sample products into the database on startup if no products exist.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Product catalog is empty. Seeding initial sample products...");
            List<Product> sampleProducts = List.of(
                    new Product(null, "Wireless Noise-Canceling Headphones", "Premium over-ear Bluetooth headphones with active noise canceling and 30hr battery.", new BigDecimal("149.99"), 25, "Electronics", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80"),
                    new Product(null, "Mechanical Gaming Keyboard", "Tactile switches, RGB per-key backlighting, and detachable magnetic wrist rest.", new BigDecimal("89.99"), 15, "Electronics", "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=500&q=80"),
                    new Product(null, "Ultra-Slim 15.6\" Laptop", "High-performance laptop with 16GB RAM, 512GB NVMe SSD, and 1080p IPS display.", new BigDecimal("799.00"), 10, "Computers", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=500&q=80"),
                    new Product(null, "Ergonomic Office Chair", "Breathable mesh back with adjustable lumbar support, 3D armrests, and recline lock.", new BigDecimal("229.50"), 8, "Furniture", "https://images.unsplash.com/photo-1580481077195-c3a82da45880?w=500&q=80"),
                    new Product(null, "Smart Fitness Watch", "Water-resistant smartwatch with continuous heart rate, GPS tracking, and sleep analysis.", new BigDecimal("119.00"), 30, "Wearables", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80"),
                    new Product(null, "Stainless Steel Insulated Water Bottle", "32oz vacuum-insulated flask keeping drinks ice cold for 24 hours.", new BigDecimal("24.99"), 50, "Accessories", "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500&q=80"),
                    new Product(null, "4K UHD 27-Inch Monitor", "IPS panel with 99% sRGB coverage, HDR support, and USB-C connectivity.", new BigDecimal("349.99"), 12, "Computers", "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80"),
                    new Product(null, "USB-C Multiport Hub Adapter", "7-in-1 aluminum hub with 4K HDMI, 100W Power Delivery, SD reader, and 3x USB 3.0.", new BigDecimal("39.99"), 40, "Accessories", "https://images.unsplash.com/photo-1625842268584-8f3296236761?w=500&q=80")
            );

            productRepository.saveAll(sampleProducts);
            log.info("Successfully seeded {} sample products.", sampleProducts.size());
        }
    }
}
