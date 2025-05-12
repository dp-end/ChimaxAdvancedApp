package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.Valid; // İç içe DTO validasyonu için
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Frontend'den yeni sipariş oluşturma isteği geldiğinde kullanılacak DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDto {

    @NotNull(message = "Teslimat adresi bilgileri eksik")
    @Valid // AddressDto içindeki validasyonları da tetikler
    private AddressDto shippingAddress;

    @NotEmpty(message = "Ödeme yöntemi belirtilmelidir")
    private String paymentMethod; // Örn: "stripe"

    // Stripe ile ödeme yapıldıysa, ödeme referansı
    private String paymentIntentId;

    @NotEmpty(message = "Sipariş kalemleri boş olamaz")
    @Size(min = 1, message = "Sepette en az 1 ürün olmalıdır")
    @Valid // Liste içindeki OrderItemDto'ların validasyonunu tetikler
    private List<OrderItemDto> items;

    // Not: Toplam tutar genellikle backend'de hesaplanır ve teyit edilir,
    // frontend'den gelen değere güvenilmez.
    // private BigDecimal totalAmount;
}

