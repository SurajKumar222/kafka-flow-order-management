package com.example.stock_service.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.stock_service.domain.Product;
import com.example.stock_service.model.Order;
import com.example.stock_service.model.OrderKey;
import com.example.stock_service.repository.ProductRepository;

@Service
@Slf4j
public record OrderManageService(ProductRepository repository,
                                 KafkaTemplate<Long, Order> template) {
	
	public static final String TOPIC_ORDERS_STOCK = "orders.stock";

    private static final String SOURCE = "stock";

    @PostConstruct
    public void ini(){
        template.setObservationEnabled(true);
    }

    public void reserve(Order order) {
        Product product = repository.findById(order.getProductId()).orElseThrow();
        log.info("Found: {}", product);
        if (order.getStatus().equals("NEW")) {
            if (order.getProductCount() < product.getAvailableItems()) {
                product.setReservedItems(product.getReservedItems() + order.getProductCount());
                product.setAvailableItems(product.getAvailableItems() - order.getProductCount());
                order.setStatus("ACCEPT");
                repository.save(product);
            } else {
                order.setStatus("REJECT");
            }
            template.send(TOPIC_ORDERS_STOCK, order.getId(), order);
            log.info("Sent: {}", order);
        }
    }

    public void confirm(Order order) {
        Product product = repository.findById(order.getProductId()).orElseThrow();
        log.info("Found: {}", product);
        if (order.getStatus().equals("CONFIRMED")) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            repository.save(product);
        } else if (order.getStatus().equals("ROLLBACK") && !order.getSource().equals(SOURCE)) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            product.setAvailableItems(product.getAvailableItems() + order.getProductCount());
            repository.save(product);
        }
    }

}
