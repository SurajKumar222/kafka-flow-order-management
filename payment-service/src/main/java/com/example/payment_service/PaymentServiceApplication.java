package com.example.payment_service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import com.example.payment_service.domain.Customer;
import com.example.payment_service.repository.CustomerRepository;
import com.github.javafaker.Faker;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableKafka
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}
	  private static final Random SECURE_RANDOM = new Random();
	  
	    @Autowired
	    private CustomerRepository repository;

	    @PostConstruct
	    public void generateData() {
	        Faker faker = new Faker();
	        for (int i = 0; i < 100; i++) {
	            int count = SECURE_RANDOM.nextInt(1000);
	            Customer c = new Customer(null, faker.name().fullName(), count, 0);
	            repository.save(c);
	        }
	    }


	    @Bean
	    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
	        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
	        //The following code enable observation in the consumer listener
	        factory.getContainerProperties().setObservationEnabled(true);
	        factory.setConsumerFactory(consumerFactory);
	        return factory;
	    }
}
