package com.example.payment_service.processor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.payment_service.model.Order;
import com.example.payment_service.service.OrderManagementService;

@Service
@Slf4j
public record OrderProcessor(OrderManagementService orderManagementService) {
	
	 public static final String TOPIC_ORDERS = "orders";
	 
    @KafkaListener(id = "orders", topics = TOPIC_ORDERS, groupId = "${spring.application.name:payment}")
    public void onEvent(Order o) {
        log.info("Received: {}", o);
        if ("NEW".equals(o.getStatus())) {
            orderManagementService.reserve(o);
        } else {
            orderManagementService.confirm(o);
        }
    }
}
