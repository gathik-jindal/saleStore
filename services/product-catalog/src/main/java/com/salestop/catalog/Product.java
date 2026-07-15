package com.salestop.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(name = "initial_stock", nullable = false)
  private int initialStock;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  protected Product() {}

  Product(String title, BigDecimal price, int initialStock) {
    this.title = title;
    this.price = price;
    this.initialStock = initialStock;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public int getInitialStock() {
    return initialStock;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  void update(String title, BigDecimal price, boolean active) {
    this.title = title;
    this.price = price;
    this.active = active;
  }
}
