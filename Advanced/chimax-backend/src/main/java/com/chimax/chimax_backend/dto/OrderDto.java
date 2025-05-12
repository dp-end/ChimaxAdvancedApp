package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Frontend'e sipariş bilgilerini döndürmek için kullanılan DTO.
 * Hassas bilgiler (örn: User entity'nin tamamı) yerine sadece gerekli alanları içerir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private AddressDto shippingAddress; // Adres bilgilerini içerir
    private String paymentMethod;
    private String trackingNumber;
    private List<OrderItemDto> items; // Sipariş kalemlerini içerir

    /**
     * Siparişi veren kullanıcı bilgilerini içeren DTO.
     * Bu alan, OrderServiceImpl'deki convertToOrderDto ve convertToOrderDtoForSellerView
     * metotlarında set edilecektir.
     */
    private UserDto user; // YENİ EKLENDİ: Siparişi veren kullanıcı

    // İptal ve iade bilgileri (opsiyonel)
    private String cancellationReason;
    private String refundStatus;
}
