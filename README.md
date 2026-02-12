# Kafka Flow Order Management

## 📌 Overview
This project is a **distributed order management system** built with **Spring Boot, Kafka, and PostgreSQL**.  
It demonstrates how to coordinate multiple microservices (Order, Payment, Stock) using **Kafka Streams** and the **Saga pattern** for transactional consistency.

---

## 🏗️ Architecture
- **Order Service**  
  Entry point for creating orders. Publishes events to Kafka and aggregates results from Payment & Stock services.

- **Payment Service**  
  Manages customer balances. Validates whether a customer has enough funds to place an order.

- **Stock Service**  
  Manages product inventory. Validates whether enough items are available for an order.

- **Kafka Topics**
  - `orders`
  - `orders.payment`
  - `orders.stock`

- **Databases**
  - `orders_db` → Customer table
  - `orders1_db` → Product table

---

## 🔄 Flow of an Order
1. **Create Order** → `POST /orders`
2. **Payment Validation** → Payment Service consumes & responds
3. **Stock Validation** → Stock Service consumes & responds
4. **Final Status** → Order Service joins results via Kafka Streams
   - `CONFIRMED` → both accept
   - `REJECT` → both reject
   - `ROLLBACK` → one rejects
5. **Query Orders** → `GET /orders`

---

## 🚀 How to Run
1. Start **Kafka** and **Zookeeper**.
2. Run each Spring Boot service:
   - `order-service-new`
   - `payment-service`
   - `stock-service`
3. Use **Postman**:
   - `POST /orders` → create new order
   - `GET /orders` → check final status
4. Verify changes in PostgreSQL:
   - `SELECT * FROM customer;`
   - `SELECT * FROM product;`

---

## 📂 Project Structure


---

## ✨ Key Features
- Event-driven microservices with Kafka
- Saga pattern for distributed consistency
- Kafka Streams for stateful joins
- PostgreSQL persistence
- Rollback logic for partial failures
