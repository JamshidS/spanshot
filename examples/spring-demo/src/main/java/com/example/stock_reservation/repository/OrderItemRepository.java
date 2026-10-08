package com.example.stock_reservation.repository;

import com.example.stock_reservation.model.OrderItem;
import com.example.stock_reservation.projects.OrderItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


    @Query("""
                    SELECT\s
                    p.name as productName,
                    oi.quantity as quantity
                    From OrderItem oi JOIN oi.products p where oi.id in :ids
           \s""")
    List<OrderItemProjection> findAllByIdIsIn(List<Long> ids);
}
