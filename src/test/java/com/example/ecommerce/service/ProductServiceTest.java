package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductDto;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct1;
    private Product sampleProduct2;

    @BeforeEach
    void setUp() {
        sampleProduct1 = new Product(1L, "Mechanical Keyboard", "RGB Gaming Keyboard", new BigDecimal("79.99"), 25, "Electronics", "http://example.com/keyboard.jpg");
        sampleProduct2 = new Product(2L, "Wireless Mouse", "Ergonomic Optical Mouse", new BigDecimal("39.99"), 50, "Electronics", "http://example.com/mouse.jpg");
    }

    @Test
    @DisplayName("getAllProducts returns all products mapped to DTOs")
    void testGetAllProducts_returnsList() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct1, sampleProduct2));

        List<ProductDto> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Mechanical Keyboard");
        assertThat(result.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Wireless Mouse");
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("getProductById returns DTO when product exists")
    void testGetProductById_existingProduct_returnsDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct1));

        ProductDto result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Mechanical Keyboard");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(result.getStockQuantity()).isEqualTo(25);
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("getProductById throws ResourceNotFoundException when product is missing")
    void testGetProductById_unknownProduct_throwsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");

        verify(productRepository).findById(999L);
    }

    @Test
    @DisplayName("createProduct saves entity and returns mapped DTO")
    void testCreateProduct_returnsSavedDto() {
        ProductDto inputDto = new ProductDto(null, "Gaming Monitor", "27-inch 144Hz", new BigDecimal("249.99"), 10, "Electronics", null);
        Product savedEntity = new Product(3L, "Gaming Monitor", "27-inch 144Hz", new BigDecimal("249.99"), 10, "Electronics", null);

        when(productRepository.save(any(Product.class))).thenReturn(savedEntity);

        ProductDto result = productService.createProduct(inputDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Gaming Monitor");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("249.99"));
        verify(productRepository).save(any(Product.class));
    }
}
