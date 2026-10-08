package com.example.stock_reservation.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Table(name = "user_product_order")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Timestamp orderDate;

    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "userProductOrder")
    private List<OrderItem> orderItems;

}
