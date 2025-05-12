package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// import java.util.List; // İsteğe bağlı: Sepet ürünleri

/**
 * Frontend'den Stripe Payment Intent oluşturma isteği için gelen verileri taşıyan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentRequestDto {

    /**
     * Ödenecek tutar (kuruş cinsinden veya en küçük para birimi).
     * Frontend'den gönderilirken çarpma işlemi yapılmalı (örn: 1500.50 TL -> 150050).
     */
    @NotNull(message = "Tutar boş olamaz")
    @Min(value = 50, message = "Tutar en az 50 kuruş olmalıdır") // Stripe minimum limitleri olabilir
    private Long amount; // Long kullanmak kuruş işlemleri için daha uygun olabilir

    /**
     * Para birimi kodu (ISO 4217 formatında, örn: "try", "usd").
     */
    @NotEmpty(message = "Para birimi boş olamaz")
    private String currency = "try"; // Varsayılan olarak TRY

    // İsteğe bağlı: Ödeme açıklaması veya sipariş özeti için ürün ID'leri eklenebilir
    // private List<Long> itemIds;
    // private String description;

}
