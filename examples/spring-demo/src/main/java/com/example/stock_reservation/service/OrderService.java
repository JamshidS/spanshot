package com.example.stock_reservation.service;

import com.example.stock_reservation.dto.CreateOrderRequest;
import com.example.stock_reservation.dto.OrderResponse;

public interface OrderService {

    void createOrder(CreateOrderRequest request);

    void cancelOrder(Long orderId);

    OrderResponse getOrder(Long orderId);

    OrderResponse confirmOrder(Long orderId);

}
