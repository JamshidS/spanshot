package com.example.stock_reservation.controller;

import com.example.stock_reservation.dto.CreateOrderRequest;
import com.example.stock_reservation.dto.OrderResponse;
import com.example.stock_reservation.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderRequest request) {
        orderService.createOrder(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {

        return new ResponseEntity<>(orderService.getOrder(orderId), HttpStatus.OK);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long orderId) {
        return new ResponseEntity<>(orderService.confirmOrder(orderId), HttpStatus.OK);
    }
}
