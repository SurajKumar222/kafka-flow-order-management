package com.example.stock_service.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.stock_service.model.Order;
import com.example.stock_service.service.OrderManageService;

@Service
@Slf4j
public record OrderProcessor(OrderManageService orderManageService) {
     
	public static final String TOPIC_ORDERS = "orders";

    @KafkaListener(id = "orders", topics = TOPIC_ORDERS, groupId = "${spring.application.name:stock}")
    public void onEvent(Order o) {
        log.info("Received: {}", o);
        if (o.getStatus().equals("NEW"))
            orderManageService.reserve(o);
        else
            orderManageService.confirm(o);
    }

}
