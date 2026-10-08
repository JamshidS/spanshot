package com.example.stock_reservation.repository;

import com.example.stock_reservation.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // atomic update
    @Modifying
    @Query("""
            UPDATE Product p
            SET p.quantityLeftOnStock = p.quantityLeftOnStock - :quantity
            WHERE p.id = :productId
              AND p.quantityLeftOnStock >= :quantity
            """)
    int decreaseQuantity(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

}
