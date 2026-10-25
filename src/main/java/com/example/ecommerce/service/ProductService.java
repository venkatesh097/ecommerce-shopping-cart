package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductDto;
import java.util.List;

/**
 * Service interface for Product catalog operations.
 */
public interface ProductService {

    /**
     * Retrieves all products from the catalog.
     *
     * @return list of ProductDto objects
     */
    List<ProductDto> getAllProducts();

    /**
     * Retrieves a single product by its unique identifier.
     *
     * @param id the product ID
     * @return the ProductDto
     * @throws com.example.ecommerce.exception.ResourceNotFoundException if product not found
     */
    ProductDto getProductById(Long id);

    /**
     * Creates or updates a product in the catalog.
     *
     * @param productDto the product data transfer object
     * @return the saved ProductDto
     */
    ProductDto createProduct(ProductDto productDto);
}
