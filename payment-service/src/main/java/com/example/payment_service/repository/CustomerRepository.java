package com.example.payment_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.payment_service.domain.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
