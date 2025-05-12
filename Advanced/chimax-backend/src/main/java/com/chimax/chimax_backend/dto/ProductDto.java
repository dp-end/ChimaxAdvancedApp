package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.*; // Validasyon anotasyonları
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Ürün oluşturma ve güncelleme işlemleri için veri taşıma nesnesi (DTO).
 * Entity'den farklı olarak validasyon kuralları içerir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    // ID genellikle güncellemede kullanılır, oluşturmada olmaz.
    // Bu DTO hem oluşturma hem güncelleme için kullanılacaksa ID opsiyonel olabilir.
    // private Long id; // Eğer Product entity'nizde id varsa ve DTO'da da gerekiyorsa eklenebilir.

    @NotEmpty(message = "Ürün adı boş olamaz")
    @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
    private String name;

    // Açıklama zorunlu olmayabilir
    @Size(max = 5000, message = "Açıklama çok uzun") // Veritabanı limitine göre ayarla
    private String description;

    @NotNull(message = "Fiyat boş olamaz")
    @DecimalMin(value = "0.01", inclusive = true, message = "Fiyat 0'dan büyük olmalıdır") // Fiyat 0.01 veya daha büyük olmalı
    @Digits(integer = 8, fraction = 2, message = "Fiyat formatı geçersiz (örn: 12345678.99)") // Maks 8 tam, 2 ondalık basamak
    private BigDecimal price;

    @Size(max = 100, message = "Kategori adı en fazla 100 karakter olabilir")
    private String category;

    /**
     * Ürünün tipi/çeşidi (örn: "Laptop", "Kulaklık", "Akıllı Saat").
     * Product entity'sindeki type alanıyla eşleşir.
     */
    @Size(max = 100, message = "Ürün tipi en fazla 100 karakter olabilir") // Yeni eklendi
    private String type; // Product entity'sindeki type alanı için

    // URL validasyonu eklenebilir (@URL Jakarta Bean Validation veya Hibernate Validator ile) ama basit tutalım
    @Size(max = 2048, message = "Resim URL'si çok uzun")
    private String imageUrl;

    @NotNull(message = "Stok miktarı boş olamaz")
    @Min(value = 0, message = "Stok miktarı negatif olamaz")
    private Integer stockQuantity = 0; // Varsayılan değer

    // Aktif durumu genellikle zorunludur
    @NotNull(message = "Aktif durumu belirtilmelidir")
    private Boolean active = true; // Varsayılan değer

    // Satıcı ID'si, bir ürün oluşturulurken veya güncellenirken gerekebilir.
    // Bu alan, controller katmanında kimliği doğrulanmış kullanıcıdan alınabilir
    // ve ProductServiceImpl'deki createProductForSeller gibi metotlara ayrıca iletilebilir.
    // Eğer DTO içinde taşımak isterseniz:
    // private Long sellerId;
}
