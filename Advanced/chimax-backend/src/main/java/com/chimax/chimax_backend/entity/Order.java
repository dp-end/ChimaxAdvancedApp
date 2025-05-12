package com.chimax.chimax_backend.entity; // Paket adını kendi yapınıza göre düzeltin
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Zaman damgası için

import java.math.BigDecimal;
import java.time.LocalDateTime; // Tarih ve zaman için
import java.util.ArrayList;
import java.util.List;

/**
 * Müşteri siparişlerini temsil eden veritabanı varlığı (entity).
 */
@Entity
@Table(name = "orders") // 'order' genellikle SQL anahtar kelimesi olduğu için 'orders' kullanmak daha güvenlidir.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Siparişi veren kullanıcı.
     * FetchType.LAZY: Sipariş yüklendiğinde kullanıcı bilgisi hemen yüklenmez, ihtiyaç duyulduğunda yüklenir.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // users tablosuna foreign key
    private User user;

    /**
     * Siparişin oluşturulduğu tarih ve saat.
     * @CreationTimestamp: Kayıt ilk oluşturulduğunda otomatik olarak zamanı ayarlar.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;

    /**
     * Siparişin toplam tutarı.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Siparişin durumu (örn: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED).
     * Enum kullanmak daha iyi bir pratik olabilir.
     */
    @Column(nullable = false, length = 50)
    private String status; // Başlangıçta "PENDING" veya "PROCESSING" olabilir

    // --- Teslimat Adresi Bilgileri ---
    // Ayrı bir Address entity'si oluşturmak veya @Embeddable kullanmak daha iyi olabilir.
    // Şimdilik direkt buraya ekleyelim:
    @Column(nullable = false, length = 150)
    private String shippingFullName;

    @Column(nullable = false, length = 255)
    private String shippingAddressLine1;

    @Column(nullable = false, length = 100)
    private String shippingCity;

    @Column(nullable = false, length = 20)
    private String shippingPostalCode;

    @Column(nullable = false, length = 100)
    private String shippingCountry = "Türkiye"; // Varsayılan

    @Column(nullable = false, length = 30)
    private String shippingPhone;
    // --- Teslimat Adresi Sonu ---

    /**
     * Bu siparişe ait ürün kalemleri.
     * OneToMany: Bir siparişin birden çok kalemi olabilir.
     * mappedBy = "order": OrderItem entity'sindeki 'order' alanının bu ilişkiyi yönettiğini belirtir.
     * cascade = CascadeType.ALL: Sipariş kaydedildiğinde/silindiğinde ilişkili OrderItem'lar da kaydedilsin/silinsin.
     * orphanRemoval = true: Siparişin item listesinden bir öğe çıkarılırsa, o öğe veritabanından da silinsin.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Ödeme bilgileri (opsiyonel, Payment entity'si ile ilişki kurulabilir)
    @Column(length = 100)
    private String paymentMethod; // Örn: "stripe", "paypal"

    @Column(length = 255)
    private String paymentIntentId; // Stripe Payment Intent ID'si gibi

    // Kargo bilgileri (opsiyonel)
    @Column(length = 100)
    private String trackingNumber;

    // Helper metot: Siparişe kolayca ürün eklemek için
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this); // İlişkinin çift taraflı kurulması önemlidir
    }

    // Helper metot: Siparişten ürün çıkarmak için
    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}

