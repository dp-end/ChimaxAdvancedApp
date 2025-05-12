package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.CreateOrderRequestDto;
import com.chimax.chimax_backend.dto.OrderDto;
import com.chimax.chimax_backend.dto.UpdateOrderStatusDto;
import com.chimax.chimax_backend.entity.User; // User entity'sini import edin
import java.util.List;
import java.util.Optional;

/**
 * Sipariş işlemleriyle ilgili iş mantığını tanımlayan arayüz.
 */
public interface OrderService {

    // --- Müşteri ve Genel Sipariş Metotları ---
    /**
     * Yeni bir sipariş oluşturur.
     * @param orderRequest Sipariş oluşturma isteğini içeren DTO.
     * @param userEmail Siparişi veren kullanıcının e-postası (SecurityContext'ten alınacak).
     * @return Oluşturulan siparişin bilgilerini içeren OrderDto.
     */
    OrderDto createOrder(CreateOrderRequestDto orderRequest, String userEmail);

    /**
     * Belirli bir kullanıcıya ait tüm siparişleri getirir.
     * @param userEmail Siparişleri istenen kullanıcının e-postası.
     * @return Kullanıcının siparişlerinin DTO listesi.
     */
    List<OrderDto> findOrdersByUser(String userEmail);

    /**
     * Belirli bir siparişin detaylarını getirir (müşteri için).
     * @param orderId Sipariş ID'si.
     * @param userEmail Siparişin sahibi olan kullanıcının e-postası (yetki kontrolü için).
     * @return Sipariş detaylarını içeren Optional<OrderDto> veya bulunamazsa/yetki yoksa boş Optional.
     */
    Optional<OrderDto> findOrderByIdAndUser(Long orderId, String userEmail);

    // --- Admin Sipariş Metotları ---
    /**
     * Bir siparişi iptal eder (Admin görevi).
     * @param orderId İptal edilecek siparişin ID'si.
     * @param reason İptal nedeni.
     * @return Güncellenmiş siparişin DTO'su.
     */
    OrderDto cancelOrder(Long orderId, String reason); // Admin işlemi

    /**
     * Tüm siparişleri getirir (Admin için).
     * @return Tüm siparişlerin DTO listesi.
     */
    List<OrderDto> findAllOrders(); // Admin işlemi

    /**
     * Belirli bir siparişin durumunu ve isteğe bağlı olarak kargo takip numarasını günceller (Admin için).
     * @param orderId Güncellenecek siparişin ID'si.
     * @param statusDto Yeni durumu ve takip numarasını içeren DTO.
     * @return Güncellenmiş siparişin DTO'su.
     */
    OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusDto statusDto); // Admin işlemi

    // --- Satıcıya Özel Yeni Sipariş Metotları ---

    /**
     * Belirli bir satıcının ürünlerini içeren siparişleri listeler.
     * İsteğe bağlı olarak sipariş durumuna göre filtreleme yapılabilir.
     * @param seller Siparişleri listelenecek satıcı.
     * @param statusFilter Sipariş durumu filtresi (örn: "PENDING", "SHIPPED", null ise tümü).
     * @return Satıcıya ait OrderDto listesi.
     */
    List<OrderDto> getOrdersForSeller(User seller, String statusFilter);

    /**
     * Satıcının, kendi ürününü içeren belirli bir siparişin detaylarını getirmesini sağlar.
     * @param orderId Getirilecek siparişin ID'si.
     * @param seller Siparişin sahibi olması beklenen (ürün bazında) satıcı.
     * @return OrderDto içeren Optional veya sipariş bulunamazsa/satıcıya ait değilse boş Optional.
     */
    Optional<OrderDto> getOrderDetailsForSeller(Long orderId, User seller);

    /**
     * Satıcının, kendi ürününe ait bir siparişin durumunu güncellemesini sağlar.
     * @param orderId Güncellenecek siparişin ID'si.
     * @param newStatus Sipariş için yeni durum (örn: "SHIPPED", "PROCESSING").
     * @param seller İşlemi yapan satıcı.
     * @return Güncellenmiş OrderDto içeren Optional.
     */
    Optional<OrderDto> updateOrderStatusForSeller(Long orderId, String newStatus, User seller);

    /**
     * Belirli bir satıcının "bekleyen" veya "işleme alınması gereken"
     * durumdaki siparişlerinin sayısını döndürür.
     * @param seller Satıcı.
     * @return Bekleyen sipariş sayısı.
     */
    long countPendingOrdersForSeller(User seller);
}
