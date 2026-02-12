package com.example.order_service_new;

import java.time.Duration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.order_service_new.model.Order;
import com.example.order_service_new.model.OrderKey;
import com.example.order_service_new.service.OrderManagementService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
@EnableAsync
@EnableScheduling
@Slf4j
public class OrderServiceNewApplication {

    private final OrderManagementService orderManagementService;
    public static final String TOPIC_ORDERS = "orders";
    public static final String TOPIC_ORDERS_PAYMENT = "orders.payment";
    public static final String TOPIC_ORDERS_STOCK = "orders.stock";

    public OrderServiceNewApplication(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceNewApplication.class, args);
    }

    /* -------------------- TOPICS -------------------- */

    @Bean
    public NewTopic topicOrders() {
        return TopicBuilder.name(TOPIC_ORDERS)
                .partitions(6)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic topicOrdersPayment() {
        return TopicBuilder.name(TOPIC_ORDERS_PAYMENT)
                .partitions(6)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic topicOrdersStock() {
        return TopicBuilder.name(TOPIC_ORDERS_STOCK)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /* -------------------- STREAM JOIN -------------------- */

    @Bean
    public KStream<Long, Order> orderJoinStream(StreamsBuilder builder) {

        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        orderSerde.deserializer().addTrustedPackages("*");

        KStream<Long, Order> paymentStream =
                builder.stream(TOPIC_ORDERS_PAYMENT,
                        Consumed.with(Serdes.Long(), orderSerde));

        KStream<Long, Order> stockStream =
                builder.stream(TOPIC_ORDERS_STOCK,
                        Consumed.with(Serdes.Long(), orderSerde));

        paymentStream
                .join(stockStream,
                        orderManagementService::confirm,
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.Long(), orderSerde, orderSerde))
                .peek((k, v) -> log.info("✅ Final Order: {}", v))
                .to(TOPIC_ORDERS, Produced.with(Serdes.Long(), orderSerde));

        return paymentStream;
    }


    /* -------------------- STATE STORE -------------------- */

    @Bean
    public KTable<Long, Order> ordersTable(StreamsBuilder builder) {

        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);

        KeyValueBytesStoreSupplier store =
                Stores.persistentKeyValueStore("orders");

        return builder
                .stream(TOPIC_ORDERS, Consumed.with(Serdes.Long(), orderSerde))
                .toTable(Materialized.<Long, Order>as(store)
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(orderSerde));
    }


    /* -------------------- ASYNC EXECUTOR -------------------- */

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafkaSender-");
        executor.initialize();
        return executor;
    }
}
