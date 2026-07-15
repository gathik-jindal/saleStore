package com.salestop.catalog;

import com.salestop.common.events.EventTopics;
import com.salestop.common.events.InventorySeededEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
  private final ProductRepository products;
  private final KafkaTemplate<String, Object> kafka;
  private final String adminKey;

  ProductController(
      ProductRepository products,
      KafkaTemplate<String, Object> kafka,
      @Value("${salestop.admin-key:local-admin-key}") String adminKey) {
    this.products = products;
    this.kafka = kafka;
    this.adminKey = adminKey;
  }

  @GetMapping
  List<ProductResponse> list() {
    return products.findAllByActiveTrueOrderByIdAsc().stream().map(ProductResponse::from).toList();
  }

  @GetMapping("/{id}")
  ProductResponse get(@PathVariable long id) {
    return ProductResponse.from(find(id));
  }

  @PostMapping
  ResponseEntity<ProductResponse> create(
      @RequestHeader("X-Admin-Key") String key, @Valid @RequestBody CreateProduct request) {
    authorize(key);
    Product product =
        products.save(new Product(request.title(), request.price(), request.initialStock()));
    kafka.send(
        EventTopics.INVENTORY_SEEDED,
        product.getId().toString(),
        new InventorySeededEvent(product.getId(), product.getInitialStock(), Instant.now()));
    return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
  }

  @PutMapping("/{id}")
  ProductResponse update(
      @RequestHeader("X-Admin-Key") String key,
      @PathVariable long id,
      @Valid @RequestBody UpdateProduct request) {
    authorize(key);
    Product product = find(id);
    product.update(request.title(), request.price(), request.active());
    return ProductResponse.from(products.save(product));
  }

  @PostMapping("/{id}/seed")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void seed(@RequestHeader("X-Admin-Key") String key, @PathVariable long id) {
    authorize(key);
    Product product = find(id);
    kafka.send(
        EventTopics.INVENTORY_SEEDED,
        Long.toString(id),
        new InventorySeededEvent(id, product.getInitialStock(), Instant.now()));
  }

  private Product find(long id) {
    return products
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  private void authorize(String key) {
    if (!adminKey.equals(key))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin key");
  }

  public record CreateProduct(
      @NotBlank @Size(max = 255) String title,
      @DecimalMin("0.01") BigDecimal price,
      @Min(0) int initialStock) {}

  public record UpdateProduct(
      @NotBlank @Size(max = 255) String title,
      @DecimalMin("0.01") BigDecimal price,
      boolean active) {}

  public record ProductResponse(
      long id,
      String title,
      BigDecimal price,
      int initialStock,
      boolean active,
      Instant createdAt) {
    static ProductResponse from(Product p) {
      return new ProductResponse(
          p.getId(),
          p.getTitle(),
          p.getPrice(),
          p.getInitialStock(),
          p.isActive(),
          p.getCreatedAt());
    }
  }
}
