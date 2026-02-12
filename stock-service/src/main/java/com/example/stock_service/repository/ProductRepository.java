package com.example.stock_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stock_service.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
}
