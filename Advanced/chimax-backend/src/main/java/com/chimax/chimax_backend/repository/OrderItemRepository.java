package com.chimax.chimax_backend.repository; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * OrderItem entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 * Genellikle doğrudan kullanılmaz, Order entity'si üzerinden yönetilir (CascadeType.ALL sayesinde).
 * Ancak özel sorgular gerekirse buraya eklenebilir.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> { // Entity: OrderItem, PK Tipi: Long

}
