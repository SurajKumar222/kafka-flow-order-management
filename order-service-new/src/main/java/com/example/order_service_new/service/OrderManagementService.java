package com.example.order_service_new.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.order_service_new.model.Order;

@Service
@Slf4j
public class OrderManagementService {

	
    public static final String SOURCE = "ORDER";
    public static final String ORDER_STATUS_REJECT = "REJECT";

    public Order confirm(Order orderPayment, Order orderStock) {
        Order o = Order.builder()
                .id(orderPayment.getId())
                .customerId(orderPayment.getCustomerId())
                .productId(orderPayment.getProductId())
                .price(orderPayment.getPrice())
                .source(SOURCE)
                .status(orderPayment.getStatus())
                .productCount(orderPayment.getProductCount())
                .build();
        if (orderPayment.getStatus().equals("ACCEPT") &&
                orderStock.getStatus().equals("ACCEPT")) {
            o.setStatus("CONFIRMED");
        } else if (orderPayment.getStatus().equals(ORDER_STATUS_REJECT) &&
                orderStock.getStatus().equals(ORDER_STATUS_REJECT)) {
            o.setStatus(ORDER_STATUS_REJECT);
        } else if (orderPayment.getStatus().equals(ORDER_STATUS_REJECT) ||
                orderStock.getStatus().equals(ORDER_STATUS_REJECT)) {
            String source = orderPayment.getStatus().equals(ORDER_STATUS_REJECT)
                    ? "PAYMENT" : "STOCK";
            o.setStatus("ROLLBACK");
            o.setSource(source);
        }
        return o;
    }
}
