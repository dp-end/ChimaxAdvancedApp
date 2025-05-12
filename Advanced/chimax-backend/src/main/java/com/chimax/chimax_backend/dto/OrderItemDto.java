package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Sipariş oluşturma isteğindeki veya sipariş detaylarındaki bir ürünü temsil eden DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    /**
     * OrderItem entity'sinin ID'si.
     * Bu alan, genellikle sipariş detayları frontend'e gönderilirken kullanılır.
     * Sipariş oluşturma isteğinde (CreateOrderRequestDto içinde) bu alan olmayabilir.
     */
    private Long id; // YENİ EKLENDİ: OrderItem'ın kendi ID'si

    @NotNull(message = "Ürün ID boş olamaz")
    private Long productId; // Frontend'den gelen veya entity'den alınan ürün ID'si

    // Yanıtta gönderilecek ek bilgiler (opsiyonel)
    private String productName; // Ürün adı (yanıtta eklenebilir)
    // private String productImageUrl; // Ürün resmi URL'si (yanıtta eklenebilir)


    @NotNull(message = "Adet boş olamaz")
    @Min(value = 1, message = "Adet en az 1 olmalıdır")
    private Integer quantity;

    /**
     * Sipariş anındaki birim fiyat.
     * Bu, ürünün o anki fiyatıdır ve sipariş oluşturulduktan sonra değişmemelidir.
     */
    @NotNull(message = "Fiyat bilgisi eksik")
    private BigDecimal price; // Sipariş anındaki birim fiyat

    // İsteğe bağlı: Kalemin toplam tutarı (price * quantity)
    // private BigDecimal totalPrice;

}
