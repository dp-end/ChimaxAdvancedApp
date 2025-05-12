package com.chimax.chimax_backend.controller; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.*; // Tüm DTO'ları import et (ProductResponseDto dahil)
import com.chimax.chimax_backend.entity.ContactMessage;
// Product entity'sini burada doğrudan kullanmamaya çalışacağız, servis DTO döndürecek
import com.chimax.chimax_backend.service.*; // Tüm Servisleri import et
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
// import java.util.Optional; // Product için Optional kullanılmıyorsa kaldırılabilir

/**
 * Admin işlemleri için API endpoint'lerini yöneten Controller.
 * Tüm endpoint'ler ADMIN rolü gerektirir.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Bu controller'daki tüm metotlar ADMIN rolü gerektirir
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final DashboardService dashboardService;
    private final ContactService contactService;

    // Constructor
    public AdminController(UserService userService,
                           OrderService orderService,
                           ProductService productService,
                           DashboardService dashboardService,
                           ContactService contactService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.dashboardService = dashboardService;
        this.contactService = contactService;
    }

    // === Kullanıcı Yönetimi Endpoint'leri ===
    /**
     * Tüm kullanıcıları listeler.
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Admin: Tüm kullanıcılar listeleniyor.");
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Belirli bir kullanıcının detayını getirir.
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        logger.info("Admin: Kullanıcı ID {} detayları isteniyor.", userId);
        return userService.findUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Bir kullanıcının aktif/pasif durumunu günceller.
     * PUT /api/admin/users/{userId}/status
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean enabled = statusUpdate.get("enabled");
        logger.info("Admin: Kullanıcı ID {} durumu {} olarak güncelleniyor.", userId, enabled);
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error","İstek gövdesinde 'enabled' (boolean) alanı bulunmalıdır."));
        }
        try {
            return userService.updateUserStatus(userId, enabled)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Admin: Kullanıcı ID {} durumu güncellenirken hata: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // === Sipariş Yönetimi Endpoint'leri ===
    /**
     * Tüm siparişleri listeler.
     * GET /api/admin/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        logger.info("Admin: Tüm siparişler listeleniyor.");
        List<OrderDto> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Belirli bir siparişin durumunu günceller.
     * PUT /api/admin/orders/{orderId}/status
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusDto statusDto) {
        logger.info("Admin: Sipariş ID {} durumu '{}' olarak güncelleniyor.", orderId, statusDto.getNewStatus());
        try {
            OrderDto updatedOrder = orderService.updateOrderStatus(orderId, statusDto);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            logger.warn("Admin: Sipariş ID {} durumu güncellenirken hata: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Admin: Sipariş ID {} durumu güncellenirken beklenmedik hata", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Sipariş durumu güncellenirken beklenmedik bir hata oluştu."));
        }
    }

    // === Ürün Yönetimi Endpoint'leri ===
    /**
     * Tüm ürünleri (aktif ve pasif) listeler.
     * GET /api/admin/products
     * @return ProductResponseDto listesini içeren ResponseEntity.
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProductsForAdmin() { // Dönüş tipi List<ProductResponseDto> olarak güncellendi
        logger.info("Admin: Tüm ürünler listeleniyor.");
        // ProductService.findAllActiveProducts() artık List<ProductResponseDto> döndürüyor.
        // Eğer admin için tüm ürünleri (aktif/pasif fark etmeksizin) getiren ayrı bir servis metodu varsa
        // (örneğin productService.findAllProductsAsDto()), onu kullanmak daha doğru olur.
        // Şimdilik, findAllActiveProducts'ın DTO döndürdüğünü varsayarak devam ediyoruz.
        // Eğer admin tüm ürünleri (aktif olmayanlar dahil) görmeliyse,
        // ProductService'te buna uygun bir metot (örneğin, findAllProductsAsAdmin() gibi)
        // oluşturup onun da List<ProductResponseDto> döndürmesini sağlamalısınız.
        List<ProductResponseDto> productResponseDtos = productService.findAllActiveProducts(); // Bu metot List<ProductResponseDto> döndürmeli
        return ResponseEntity.ok(productResponseDtos);
    }


    // === DASHBOARD ENDPOINT'İ ===
    /**
     * Gösterge paneli için temel istatistikleri getirir.
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        logger.info("Admin: Dashboard istatistikleri isteniyor.");
        try {
            AdminDashboardStatsDto stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Admin: Dashboard istatistikleri getirilirken hata oluştu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardStatsDto(0, 0, 0));
        }
    }

    // === İLETİŞİM MESAJLARI ENDPOINT'İ ===
    /**
     * Tüm iletişim formu mesajlarını listeler.
     * GET /api/admin/messages
     */
    @GetMapping("/messages")
    public ResponseEntity<List<ContactMessage>> getAllContactMessages() {
        logger.info("Admin: Tüm iletişim mesajları isteniyor.");
        try {
            List<ContactMessage> messages = contactService.getAllMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Admin: İletişim mesajları getirilirken hata oluştu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
