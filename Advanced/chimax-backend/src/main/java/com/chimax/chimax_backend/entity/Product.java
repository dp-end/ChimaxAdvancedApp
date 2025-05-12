package com.chimax.chimax_backend.entity; // Paket adınızı kendi yapınıza göre düzeltin

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // Parasal değerler için double/float yerine BigDecimal önerilir

/**
 * Satışa sunulan ürünleri temsil eden veritabanı varlığı (entity).
 */
@Entity
@Table(name = "products")
@Data // Getter, Setter, toString, equals, hashCode ve gerekli constructor'ları oluşturur
@NoArgsConstructor // Argümansız constructor
@AllArgsConstructor // Tüm alanları içeren constructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Ürün açıklaması. Uzun metinler için @Lob kullanılır.
     * columnDefinition = "TEXT": MySQL gibi veritabanlarında uzun metinler için uygun sütun tipini belirtir.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Ürün fiyatı. Parasal işlemlerde kayan nokta hatalarını önlemek için BigDecimal kullanılır.
     * precision: Sayının toplam maksimum basamak sayısı.
     * scale: Ondalık noktadan sonraki basamak sayısı.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Ürünün kategorisi. Ayrı bir Category entity'si ile ilişki kuruldu.
     */
    @ManyToOne(fetch = FetchType.EAGER) // DTO dönüşümlerinde kolaylık sağlaması için EAGER olabilir.
                                        // Performans kritikse LAZY ve DTO'da dikkatli map'leme düşünülebilir.
    @JoinColumn(name = "category_id")   // 'products' tablosunda 'categories' tablosuna referans veren foreign key.
                                        // Eğer Category tablonuzun ID'si farklı bir isimdeyse referencedColumnName ekleyebilirsiniz.
    private Category category; // String yerine Category nesnesi

    /**
     * Ürünün tipi/çeşidi (örn: "Laptop", "Kulaklık", "Akıllı Saat").
     * Bu alan, ürünlerin daha detaylı sınıflandırılmasına olanak tanır.
     */
    @Column(length = 100)
    private String type;

    /**
     * Ürün resminin URL'si. Resim dosyalarını doğrudan veritabanında saklamak genellikle önerilmez.
     * URL'yi saklamak veya bir dosya depolama servisine referans vermek daha yaygındır.
     */
    @Column(length = 2048) // URL'ler için yeterli uzunlukta olmalı
    private String imageUrl;

    /**
     * Mevcut stok miktarı.
     * columnDefinition = "INT DEFAULT 0": Veritabanında varsayılan değeri 0 olan bir integer sütun oluşturur.
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int stockQuantity = 0;

    /**
     * Ürünün satışta olup olmadığını belirtir.
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;

    /**
     * Bu ürünü ekleyen/sahip olan satıcı (User).
     * Birçok ürün bir satıcıya ait olabilir (@ManyToOne ilişki).
     * FetchType.LAZY: Product yüklendiğinde ilişkili User (seller) bilgisi hemen yüklenmez,
     * sadece ihtiyaç duyulduğunda (product.getSeller() çağrıldığında) yüklenir. Bu genellikle performans için daha iyidir.
     * @JoinColumn: 'products' tablosunda 'users' tablosunun 'id' sütununa referans veren
     * 'seller_user_id' adında bir foreign key sütunu oluşturur.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Satıcı bilgisi her zaman gerekmeyebilir, LAZY daha uygun olabilir.
    @JoinColumn(name = "seller_user_id", referencedColumnName = "id")
    private User seller;

    // Lombok @Data anotasyonu getter ve setter'ları otomatik olarak oluşturacaktır.
    // Manuel getter/setter'lara gerek yoktur (Category ve Seller için).
}
