package com.example.stock_reservation.service.impl;

import com.example.stock_reservation.dto.CreateOrderRequest;
import com.example.stock_reservation.dto.OrderItemResponse;
import com.example.stock_reservation.dto.OrderResponse;
import com.example.stock_reservation.dto.ProductQuantityRequest;
import com.example.stock_reservation.exception.ValidationException;
import com.example.stock_reservation.model.OrderItem;
import com.example.stock_reservation.model.Product;
import com.example.stock_reservation.model.User;
import com.example.stock_reservation.model.UserProductOrder;
import com.example.stock_reservation.projects.OrderItemProjection;
import com.example.stock_reservation.repository.OrderItemRepository;
import com.example.stock_reservation.repository.ProductRepository;
import com.example.stock_reservation.repository.UserProductOrderRepository;
import com.example.stock_reservation.repository.UserRepository;
import com.example.stock_reservation.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl  implements OrderService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserProductOrderRepository userProductOrderRepository;

    private void checkUserExists(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
    }

    // optimize this method
    private void checkProductsExists(List<Long> productIds) {
        productIds.forEach(this::checkProductExists);
    }

    private void checkProductExists(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
    }

    private void checkOrderExists(Long orderId) {
        Optional<UserProductOrder> order = userProductOrderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
    }

    private Map<Long, Integer> checkProductsQuantity(List<Product> products, List<ProductQuantityRequest> quantityRequests) {
        Map<Long, Integer> productIdQuantityMap = new HashMap<>();
        Map<Long, Integer> quantityValidationMap = new HashMap<>();
        for (Product product : products) {
            productIdQuantityMap.put(product.getId(), product.getQuantityLeftOnStock());
        }
        quantityRequests.forEach(quantityRequest -> {
            long productId = quantityRequest.productId();

            int existingQuantity = productIdQuantityMap.get(productId);

            quantityValidationMap.put(productId, existingQuantity);

        });

        return quantityValidationMap;
    }

    private UserProductOrder createOrder(CreateOrderRequest request, User user) {
        return UserProductOrder.builder()
                .user(user)
                .orderDate(Timestamp.from(Instant.now()))
                .status("PENDING")
                .build();
    }

    private void updateProductQuantity(List<ProductQuantityRequest> quantityRequests) {
        List<Long> notEnoughQuantityProducts =
                new ArrayList<>();

        for (ProductQuantityRequest request : quantityRequests) {
            int updatedRows =
                    productRepository.decreaseQuantity(
                            request.productId(),
                            request.quantity()
                    );
            if (updatedRows == 0) {
                notEnoughQuantityProducts.add(
                        request.productId()
                );
            }
        }
        if (!notEnoughQuantityProducts.isEmpty()) {
            Map<String, Object> validationMap =
                    new HashMap<>();

            validationMap.put(
                    "notEnoughQuantityProducts",
                    notEnoughQuantityProducts
            );

            throw new ValidationException(validationMap);
        }
    }

    @Transactional
    protected void createOrderItems(List<ProductQuantityRequest> quantityRequests, UserProductOrder order, List<Product> products) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (ProductQuantityRequest quantityRequest : quantityRequests) {
            Product product = products.stream()
                    .filter(p -> p.getId() == quantityRequest.productId())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found in request"));
            OrderItem orderItem = new OrderItem();
            orderItem.setUserProductOrder(order);
            orderItem.setProducts(Collections.singletonList(product));
            orderItem.setQuantity(quantityRequest.quantity());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
    }

    private void validateOrder(List<Product> products, CreateOrderRequest request) {
        Map<Long, Integer> productIdQuantityMap = new HashMap<>();

        List<Long> zeroQuantityIds = new ArrayList<>();
        for (Product product : products) {
            productIdQuantityMap.put(product.getId(), product.getQuantityLeftOnStock());
        }
        request.getProducts().forEach(quantityRequest -> {

            long productId = quantityRequest.productId();
            int existingQuantity = productIdQuantityMap.get(productId);
            int requestedQuantity = quantityRequest.quantity();
            if (existingQuantity < requestedQuantity) {
                zeroQuantityIds.add(productId);
            }

        });

        Map<String,Object> validationMap = new HashMap<>();
        validationMap.put("takeAction", zeroQuantityIds);
        validationMap.put("message", "Not enough stock");
        validationMap.put("status", 400);
        validationMap.put("timestamp", Instant.now());
        validationMap.put("existingQuantity", productIdQuantityMap);

        if (!zeroQuantityIds.isEmpty()) {
            throw new ValidationException(validationMap);
        }
    }


    @Override
    @Transactional
    public void createOrder(CreateOrderRequest request) {
        checkUserExists(request.getUserId());
        List<Long> productIds = request.getProducts()
                .stream().map(ProductQuantityRequest::productId).toList();
        checkProductsExists(productIds);
        User user = userRepository.findById(request.getUserId()).get();
        UserProductOrder order = createOrder(request, user);
        UserProductOrder createdOrder = userProductOrderRepository.save(order);

        List<Product> products = productRepository.findAllById(productIds);

        validateOrder(products, request);
        createOrderItems(request.getProducts(), createdOrder, products);
    }

    @Override
    public void cancelOrder(Long orderId) {

    }

    private List<OrderItemResponse> getOrderItems(UserProductOrder order) {
        List<OrderItemResponse> responses = new ArrayList<>();
        List<Long> oderIds = order.getOrderItems().stream().map(OrderItem::getId).toList();
        List<OrderItemProjection> orderItemProjections =  orderItemRepository.findAllByIdIsIn(oderIds);
        orderItemProjections.forEach(o -> {
            OrderItemResponse response = new OrderItemResponse(o.getProductName(), o.getQuantity());
            responses.add(response);
           }
        );
        return responses;
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        Optional<UserProductOrder> order = userProductOrderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        return OrderResponse.builder()
                .id(order.get().getId())
                .orderItems(getOrderItems(order.get()))
                .status(order.get().getStatus())
                .orderDate(order.get().getOrderDate())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Optional<UserProductOrder> order = userProductOrderRepository.findByIdForUpdate(orderId);
        if (order.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        if ((order.get().getStatus()).equals("CONFIRMED")) {
            Map<String, Object> validationMap = new HashMap<>();
            validationMap.put("message", "Order already confirmed");
            validationMap.put("status", 400);
            validationMap.put("timestamp", Instant.now());
            throw new ValidationException(validationMap);
        }

        List<Long> productIds = order.get().getOrderItems()
                .stream().map(OrderItem::getProducts).flatMap(Collection::stream).map(Product::getId).toList();

        List<Product> products = productRepository.findAllById(productIds);
        List<ProductQuantityRequest> productQuantityRequests = new ArrayList<>();
        order.get().getOrderItems().forEach(orderItem -> {
            orderItem.getProducts().forEach(product -> {
                ProductQuantityRequest request = new ProductQuantityRequest(product.getId(), orderItem.getQuantity());
                productQuantityRequests.add(request);
            });
        });

        validateOrder(products, new CreateOrderRequest(order.get().getUser().getId(), productQuantityRequests));

        updateProductQuantity(productQuantityRequests); // Race condition safe
        order.get().setStatus("CONFIRMED");

        return getOrder(orderId);
    }
}
