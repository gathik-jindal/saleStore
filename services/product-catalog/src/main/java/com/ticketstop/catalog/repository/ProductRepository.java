package com.ticketstop.catalog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketstop.catalog.domain.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsBySkuIgnoreCase(String sku);
    Optional<Product> findBySkuIgnoreCase(String sku);
    List<Product> findByActiveTrueOrderByCreatedAtDesc();
}
