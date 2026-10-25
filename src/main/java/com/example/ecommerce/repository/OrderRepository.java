package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Order entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds an order by its unique business order number.
     *
     * @param orderNumber unique business order number
     * @return Optional of Order
     */
    Optional<Order> findByOrderNumber(String orderNumber);
}
