package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.AdminDashboardStatsDto;

/**
 * Admin gösterge paneli verilerini hesaplayan servis arayüzü.
 */
public interface DashboardService {

    /**
     * Gösterge paneli için temel istatistikleri hesaplar ve döndürür.
     * @return AdminDashboardStatsDto nesnesi.
     */
    AdminDashboardStatsDto getDashboardStats();

}
