package com.example.order_service_new.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderKey implements Serializable {
    private Long orderId;
}
