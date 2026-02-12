package com.example.order_service_new.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.order_service_new.model.Order;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderGeneratorService {

    public static final String SOURCE = "order";
    private static final Random SECURE_RANDOM = new Random();
    private final KafkaTemplate<Long, Order> template;
    public static final String TOPIC_ORDERS = "orders";

    @PostConstruct
    public void init() {
        template.setObservationEnabled(true);
    }

    @Async
    public void generate() {
        generateOrders(10);
    }

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.MINUTES)
    public void generateOrdersScheduled() {
        generateOrders(1);
    }

    private void generateOrders(int orderCount) {
        final AtomicLong id = new AtomicLong();
        for (int i = 0; i < orderCount; i++) {
            int x = SECURE_RANDOM.nextInt(5) + 1;

            Order o = Order.builder()
                    .id(id.incrementAndGet())
                    .customerId(SECURE_RANDOM.nextLong(100) + 1)
                    .productId(SECURE_RANDOM.nextLong(100) + 1)
                    .status("NEW")
                    .price(100 * x)
                    .productCount(x)
                    .source(SOURCE)
                    .build();

            log.info("Generated order: {}", o);
            template.send(TOPIC_ORDERS, o.getId(), o);
        }
    }
}
