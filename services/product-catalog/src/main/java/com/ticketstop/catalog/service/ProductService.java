package com.ticketstop.catalog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.catalog.domain.Product;
import com.ticketstop.catalog.dto.ProductRequest;
import com.ticketstop.catalog.dto.ProductResponse;
import com.ticketstop.catalog.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = normalizeSku(request.sku());
        if (products.existsBySkuIgnoreCase(sku)) {
            throw new IllegalArgumentException("SKU is already registered");
        }
        Product product = products.save(new Product(
            UUID.randomUUID(),
            sku,
            request.title().trim(),
            request.description(),
            request.price(),
            request.initialStock()
        ));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = products.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.update(
            request.title().trim(),
            request.description(),
            request.price(),
            request.initialStock(),
            request.active() == null || request.active()
        );
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        return products.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(boolean includeInactive) {
        return (includeInactive ? products.findAll() : products.findByActiveTrueOrderByCreatedAtDesc())
            .stream()
            .map(ProductResponse::from)
            .toList();
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }
}
