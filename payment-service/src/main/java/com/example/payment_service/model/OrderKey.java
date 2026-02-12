package com.example.payment_service.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderKey implements Serializable {
    private Long orderId;
}
