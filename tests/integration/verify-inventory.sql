SELECT p.id,
       p.initial_stock,
       COALESCE(SUM(o.quantity), 0) AS items_sold,
       p.initial_stock - COALESCE(SUM(o.quantity), 0) AS residual_stock
FROM products p
LEFT JOIN orders o ON o.product_id = p.id
WHERE p.id = :product_id
GROUP BY p.id, p.initial_stock;

-- A successful sold-out run has residual_stock = 0 and never a negative value.
