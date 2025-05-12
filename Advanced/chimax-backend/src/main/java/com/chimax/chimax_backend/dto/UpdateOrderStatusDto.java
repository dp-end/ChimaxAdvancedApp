package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Admin tarafından sipariş durumunu güncelleme isteği için DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDto {

    @NotEmpty(message = "Yeni durum boş olamaz")
    private String newStatus; // Yeni sipariş durumu (örn: "SHIPPED", "DELIVERED")

    // İsteğe bağlı: Kargo takip numarası da eklenebilir
    private String trackingNumber;
}

