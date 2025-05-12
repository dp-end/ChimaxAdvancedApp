package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Admin gösterge paneli için istatistiksel verileri taşıyan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDto {

    private long totalProducts;   // Toplam ürün sayısı
    private long pendingOrders;   // İşlem bekleyen sipariş sayısı
    private long totalUsers;      // Toplam kullanıcı sayısı
    // Buraya başka istatistikler eklenebilir (örn: toplam gelir, aylık satış vb.)

}
