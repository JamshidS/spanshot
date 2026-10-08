package com.example.stock_reservation.repository;

import com.example.stock_reservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
