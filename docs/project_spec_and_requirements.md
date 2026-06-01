# **Project Specification & Requirements Document: Distributed Flash-Sale Engine**

**Document Version:** 1.0
**Architecture Target:** Kubernetes-Native Distributed Microservices Mesh

## **1\. Executive Summary & Strategic Objectives**

During a high-scale consumer flash-sale event, backend systems experience sudden, massive traffic spikes that can exhaust database connection pools, introduce race conditions, and cause cascading failures. This document defines the comprehensive architecture, data design, and testing protocols for a **High-Throughput Distributed Flash-Sale Engine**.  
This system leverages an asynchronous, event-driven architecture coupled with an 8-service microservices mesh to decouple rapid traffic ingestion from permanent data persistence. It guarantees zero inventory overselling, sub-second response times, and massive horizontal scalability.

## **2\. Distributed System Architecture: The 8-Service Topology**

The backend is entirely decoupled into isolated microservices running in a Kubernetes cluster. Critical path ingestion is kept synchronous and highly available, while heavy state mutations are offloaded to asynchronous background workers.

| Service Name                 | HTTP Port | gRPC Port | Core Responsibilities & Domain Logic                                                                                                                       |
| :--------------------------- | :-------- | :-------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User & Identity Service**  | 8081      | 9091      | User registration, authentication, and JWT signing. Serves public keys via gRPC for sub-1ms stateless token verification at the edge.                      |
| **Product Catalog Service**  | 8082      | 9092      | Manages items, descriptions, and initial stock states. Broadcasts updates via Kafka.                                                                       |
| **Promo & Coupon Service**   | 8083      | 9093      | Validates promotional codes, pricing tiers, and eligibility rules during the checkout process.                                                             |
| **Ingestion Edge Service**   | 8084      | 9094      | The ultra-fast entry point for POST /checkout. Runs rate limits, requests token validations, and initiates the Redis allocation. **Completely stateless.** |
| **Inventory Cache Service**  | 8085      | 9095      | Wraps the Redis cluster. Executes atomic Lua scripts for deductions and idempotency.                                                                       |
| **Order Processing Service** | 8086      | 9096      | Consumes Kafka events. Transforms memory reservations into durable SQL ledger entries.                                                                     |
| **Payment Gateway Service**  | 8087      | 9097      | Manages 3rd-party gateway integrations, webhooks, and payment state machines.                                                                              |
| **Notification Service**     | 8088      | 9098      | Listens to final order statuses and triggers WebSockets, SMS, or Emails to the user.                                                                       |

## **3\. Inter-Service Communication & Data Flow**

To ensure this cluster handles 20,000+ requests per second, the communication patterns are strictly divided between the Synchronous Path (for speed) and the Asynchronous Path (for durability).

### **3.1 The 100ms Ingestion Loop (Synchronous Path)**

1. **Request Ingestion:** Client hits the Ingestion Edge Service (8084) to claim an item.
2. **Auth Verification:** Ingestion Edge validates the user signature via an internal gRPC call to the User Service (9091).
3. **Atomic Allocation:** Ingestion Edge sends a gRPC reservation command to the Inventory Cache Service (9095). The Lua script executes. If stock is available, it returns Success immediately.
4. **Event Dispatch:** Ingestion Edge drops a message into the Kafka order_intents topic and returns HTTP 202 Accepted. The user's checkout is legally reserved in under 50ms.

### **3.2 The Background Convergence (Asynchronous Path)**

1. **Micro-Batch Processing:** Order Processing Service (8086) pulls batches of 100 intent events from Kafka.
2. **Persistence:** Items are written directly to PostgreSQL. Once committed, an order.created event is fired into Kafka.
3. **Completion & Notification:** Payment Gateway and Notification Services consume order.created, finalizing the invoice and sending a WebSocket alert to the client browser.

## **4\. Functional Requirements**

- **Inventory Discovery:** Users can query the real-time availability status of flash-sale items globally.
- **Instant Reservation:** Users can submit instant order intents bypassing heavy UI flows.
- **Idempotent Checkout:** Network retries must be inherently safe (no duplicate orders/deductions) over a 5-minute rolling window.
- **Flash-Sale Seeding (Admin):** Internal users can load cache structures with exact stock allocations prior to the countdown hitting zero.

## **5\. Non-Functional Requirements (NFRs)**

| Metric / Attribute       | Targeted SLA Threshold   | Verification Strategy                     |
| :----------------------- | :----------------------- | :---------------------------------------- |
| **Ingestion Throughput** | ≥ 20,000 requests/sec    | k6 Distributed Load Testing               |
| **API Write Latency**    | p99 ≤ 50 ms              | Prometheus / Grafana Histograms           |
| **Data Consistency**     | 0% Inventory Variance    | SQL Check (Sum(Orders) \== Initial Stock) |
| **Resource Isolation**   | 100 reqs / sec / IP      | Redis Token Bucket Eviction Logs          |
| **Autoscaling Target**   | Pod initialization ≤ 15s | Kubernetes ReplicaSet Timestamps          |

## **6\. Infrastructure, Autoscaling & Resiliency Patterns**

### **6.1 Token Bucket Rate Limiting (DDoS Prevention)**

Each client IP maps to an active token bucket in Redis. Capacity is 10 tokens, refilled at 10/sec. If the bucket zeroes out, the Ingestion Edge Service rejects the request (HTTP 429\) before it touches any core logic.

### **6.2 Kubernetes Horizontal Pod Autoscaler (HPA) & KEDA**

- **Ingestion & Cache Layers:** CPU-based HPA targets 70% utilization. Policies are set to allow 200% scale-up within 10 seconds to absorb the "zero-second spike".
- **Order Worker Layer:** Scaled via KEDA targeting _Kafka Consumer Lag_. If the queue length exceeds 5,000 messages, Kubernetes provisions more worker pods to drain the backlog.

### **6.3 Circuit Breakers & Dead Letter Queues (DLQ)**

If the PostgreSQL instance drops connections, the Order Processing workers will trip their internal Circuit Breaker after 5 consecutive failures. Kafka consumption pauses (buffering the data safely). Corrupt singular payloads are shunted to an order_events_dlq topic to prevent head-of-line blocking.

## **7\. Schema & Data Storage Layout**

### **7.1 Redis Key Architecture (In-Memory Isolation)**

```redis
\# Core Inventory Tracker
Key: flashsale:product:{product\_id}:stock
Type: String \-\> Integer

\# Idempotency Lock Layer (5-Minute TTL)
Key: idempotency:{idempotency\_key}
Type: String \-\> "COMPLETED"

\# Double-Purchase Prevention Guard
Key: flashsale:product:{product\_id}:user:{user\_id}
Type: Hash \-\> Field: "reserved", Value: "true"
```

### **7.2 PostgreSQL Core Ledger (Relational Truth)**

```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2\) NOT NULL,
    initial\_stock INT NOT NULL,
    created\_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT\_TIMESTAMP
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    product\_id INT REFERENCES products(id),
    user\_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    idempotency\_key VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created\_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT\_TIMESTAMP
);

CREATE UNIQUE INDEX idx\_orders\_idempotency ON orders(idempotency\_key);
CREATE INDEX idx\_orders\_product\_user ON orders(product\_id, user\_id);
```

## **8\. Verification & Load Testing Protocols**

To pass production readiness, the system must survive a 3-stage k6 stress test:

1. **Ramp-Up:** 0 to 5,000 users over 60s.
2. **Sustained Surge:** 20,000 requests/sec for 180s.
3. **Ramp-Down:** To 0 users over 60s.

**Post-Load SQL Validation:**

```sql
SELECT
 p.initial_stock,
 COUNT(o.id) AS total_orders_created,
 SUM(o.quantity) AS total_items_sold,
 p.initial_stock \- SUM(o.quantity) AS residual_calculated_stock
FROM products p
LEFT JOIN orders o ON p.id \= o.product_id
WHERE p.id \= 1
GROUP BY p.initial_stock;
```

_Requirement: residual_calculated_stock MUST equal exactly 0\._
