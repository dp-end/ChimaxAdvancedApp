package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.AdminDashboardStatsDto;
import com.chimax.chimax_backend.repository.OrderRepository;
import com.chimax.chimax_backend.repository.ProductRepository;
import com.chimax.chimax_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // Sipariş durumları için

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // Bekleyen sipariş olarak kabul edilecek durumlar
    private static final List<String> PENDING_ORDER_STATUSES = List.of("PROCESSING", "PENDING", "HAZIRLANIYOR");

    @Autowired
    public DashboardServiceImpl(ProductRepository productRepository,
                                OrderRepository orderRepository,
                                UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public AdminDashboardStatsDto getDashboardStats() {
        // Repository'lerin count() metotları toplam kayıt sayısını verir.
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();

        // Bekleyen siparişleri saymak için özel bir sorgu veya filtreleme gerekir.
        // Şimdilik basit bir filtreleme yapalım (performans için repository'de yapmak daha iyi olabilir).
        long pendingOrders = orderRepository.findAll().stream()
                .filter(order -> PENDING_ORDER_STATUSES.contains(order.getStatus().toUpperCase()))
                .count();
        // Veya OrderRepository'ye countByStatusIn(List<String> statuses) metodu eklenebilir.

        // DTO'yu oluştur ve döndür
        return new AdminDashboardStatsDto(totalProducts, pendingOrders, totalUsers);
    }
}
