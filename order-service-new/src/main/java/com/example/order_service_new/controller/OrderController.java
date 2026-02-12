package com.example.order_service_new.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.order_service_new.dto.OrderDto;
import com.example.order_service_new.model.Order;
import com.example.order_service_new.service.OrderGeneratorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/orders")
@Slf4j
public record OrderController(
        KafkaTemplate<Long, Order> template,
        StreamsBuilderFactoryBean kafkaStreamsFactory,
        OrderGeneratorService orderGeneratorService) {

    private static final AtomicLong id = new AtomicLong();
    public static final String TOPIC_ORDERS = "orders";

    @PostMapping
    public Order create(@RequestBody Order order) {
        order.setId(id.incrementAndGet());
        order.setStatus("NEW");
        order.setSource("order"); // or OrderGeneratorService.SOURCE
        if (order.getPrice() == 0) { 
        	order.setPrice(order.getProductCount() * 100); 
        }
        // ✅ Long key, not OrderKey
        template.send(TOPIC_ORDERS, order.getId(), order);
        log.info("Sent: {}", order);

        return order;
    }

    @PostMapping("/generate")
    public void generate() {
        orderGeneratorService.generate();
    }

    @GetMapping
    public List<OrderDto> all() {
        List<OrderDto> orders = new ArrayList<>();

        KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
        if (kafkaStreams != null) {

            // ✅ Store key is Long
            ReadOnlyKeyValueStore<Long, Order> store =
                    kafkaStreams.store(
                            StoreQueryParameters.fromNameAndType(
                                    "orders",
                                    QueryableStoreTypes.keyValueStore()
                            )
                    );

            KeyValueIterator<Long, Order> it = store.all();

            while (it.hasNext()) {
                KeyValue<Long, Order> next = it.next();
                Order order = next.value;

                OrderDto dto = OrderDto.builder()
                        .id(order.getId())
                        .customerId(order.getCustomerId())
                        .productId(order.getProductId())
                        .productCount(order.getProductCount())
                        .price(order.getPrice())
                        .status(order.getStatus())
                        .source(order.getSource())
                        .build();

                orders.add(dto);
            }
        }

        return orders;
    }
}
