package com.chimax.chimax_backend.entity; // Paket adını kontrol et

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Bir sipariş içindeki tek bir ürün kalemini temsil eden veritabanı varlığı (entity).
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bu sipariş kaleminin ait olduğu sipariş.
     * ManyToOne: Birden çok sipariş kalemi bir siparişe ait olabilir.
     * FetchType.LAZY: Kalem yüklendiğinde sipariş bilgisi hemen yüklenmez.
     * @JoinColumn: orders tablosuna bağlanacak foreign key sütununu belirtir.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Bir sipariş kalemi mutlaka bir siparişe ait olmalı
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Sipariş edilen ürün.
     * ManyToOne: Birden çok sipariş kalemi aynı ürüne ait olabilir.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Bir sipariş kalemi mutlaka bir ürüne ait olmalı
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Bu üründen kaç adet sipariş edildiği.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Ürünün sipariş anındaki birim fiyatı.
     * Fiyat zamanla değişebileceği için sipariş anındaki fiyatı kaydetmek önemlidir.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrder; // Sipariş anındaki fiyat

}

