package com.msoogle.orderservice.service;

import com.msoogle.orderservice.dto.OrderDto;
        import com.msoogle.orderservice.jpa.OrderEntity;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDetails);

    OrderDto getOrderByOrderId(String orderId);

    Iterable<OrderEntity> getOrdersByUserId(String userId);
}
