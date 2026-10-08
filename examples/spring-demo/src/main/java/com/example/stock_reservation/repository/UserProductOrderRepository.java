package com.example.stock_reservation.repository;

import com.example.stock_reservation.model.UserProductOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProductOrderRepository extends JpaRepository<UserProductOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM UserProductOrder o
            WHERE o.id = :orderId
            """)
    Optional<UserProductOrder> findByIdForUpdate(
            @Param("orderId") Long orderId
    );

}
