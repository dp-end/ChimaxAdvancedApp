package com.chimax.chimax_backend.controller; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.CreateOrderRequestDto;
import com.chimax.chimax_backend.dto.OrderDto;
import com.chimax.chimax_backend.dto.UpdateOrderStatusDto; // Admin için durum güncelleme DTO'su
import com.chimax.chimax_backend.entity.User; // User entity importu
import com.chimax.chimax_backend.service.OrderService;
import com.chimax.chimax_backend.service.UserService; // UserService importu
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // @Autowired için
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // YENİ IMPORT
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Kendi exception'ınızla değiştirebilirsiniz
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // HTTP durum kodları için

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;

/**
 * Siparişlerle ilgili API endpoint'lerini yöneten Controller.
 * Müşteri, Admin ve Satıcı sipariş işlemlerini içerir.
 */
@RestController
@RequestMapping("/api/orders") // Ana sipariş yolu
@CrossOrigin(origins = "*", maxAge = 3600) // CORS ayarı
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final UserService userService; // UserService enjekte edildi

    @Autowired // Constructor injection
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // === MÜŞTERİ SİPARİŞ ENDPOINT'LERİ ===

    /**
     * Yeni bir sipariş oluşturur. (Sadece Giriş Yapmış Kullanıcılar)
     * POST /api/orders
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequestDto orderRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Yetkisiz sipariş oluşturma denemesi.");
            return new ResponseEntity<>(Collections.singletonMap("error","Sipariş vermek için giriş yapmalısınız."), HttpStatus.UNAUTHORIZED);
        }
        String userEmail = authentication.getName();
        logger.info("Kullanıcı '{}' için sipariş oluşturma isteği alındı.", userEmail);
        try {
            OrderDto createdOrder = orderService.createOrder(orderRequest, userEmail);
            logger.info("Sipariş başarıyla oluşturuldu: ID {}", createdOrder.getId());
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.warn("Sipariş oluşturma hatası ({}): {}", userEmail, e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Sipariş oluşturulurken beklenmedik bir hata oluştu ({})", userEmail, e);
            return new ResponseEntity<>(Collections.singletonMap("error","Sipariş oluşturulurken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Giriş yapmış kullanıcının kendi sipariş geçmişini listeler.
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        logger.info("Kullanıcı '{}' için sipariş geçmişi isteniyor.", userEmail);
        try {
            List<OrderDto> orders = orderService.findOrdersByUser(userEmail);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            logger.warn("Siparişler getirilirken hata ({}): {}", userEmail, e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Siparişler getirilirken beklenmedik hata ({})", userEmail, e);
            return new ResponseEntity<>(Collections.singletonMap("error","Siparişler getirilirken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Giriş yapmış kullanıcının belirli bir siparişinin detayını getirir.
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderByIdForCurrentUser(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        logger.info("Kullanıcı '{}', sipariş ID {} detayını istiyor.", userEmail, orderId);
        try {
            Optional<OrderDto> orderDtoOptional = orderService.findOrderByIdAndUser(orderId, userEmail);
            // DÜZELTME: Optional.map().orElseGet() yerine if-else yapısı
            if (orderDtoOptional.isPresent()) {
                logger.info("Sipariş ID {} detayı bulundu.", orderId);
                return ResponseEntity.ok(orderDtoOptional.get());
            } else {
                logger.warn("Sipariş ID {} bulunamadı veya kullanıcı '{}' için yetki yok.", orderId, userEmail);
                Map<String, String> errorBody = Collections.singletonMap("error", "Sipariş bulunamadı veya bu siparişe erişim yetkiniz yok.");
                return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) { // Örneğin OrderService içinde kullanıcı bulunamazsa fırlatılan hata
            logger.warn("Sipariş detayı getirilirken hata (Kullanıcı: {}, SiparişID: {}): {}", userEmail, orderId, e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Sipariş detayı getirilirken beklenmedik hata (Kullanıcı: {}, SiparişID: {})", userEmail, orderId, e);
            return new ResponseEntity<>(Collections.singletonMap("error","Sipariş detayı getirilirken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === ADMIN SİPARİŞ ENDPOINT'LERİ ===

    /**
     * Tüm siparişleri listeler (Sadece Admin).
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrdersForAdmin() {
        logger.info("Admin tüm siparişleri listeleme isteği aldı.");
        List<OrderDto> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Bir siparişin durumunu günceller (Sadece Admin).
     * PUT /api/orders/admin/{orderId}/status
     */
    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatusByAdmin(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusDto statusDto) {
        logger.info("Admin, sipariş ID {} için durum güncelleme isteği gönderdi: {}", orderId, statusDto);
        try {
            OrderDto updatedOrder = orderService.updateOrderStatus(orderId, statusDto);
            logger.info("Sipariş ID {} durumu admin tarafından başarıyla güncellendi.", orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            logger.warn("Admin sipariş durum güncelleme hatası ({}): {}", orderId, e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Admin sipariş durum güncelleme sırasında beklenmedik hata ({})", orderId, e);
            return new ResponseEntity<>(Collections.singletonMap("error","Sipariş durumu güncellenirken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Bir siparişi iptal eder (Sadece Admin).
     * PUT /api/orders/admin/{orderId}/cancel
     */
    @PutMapping("/admin/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrderByAdmin(@PathVariable Long orderId, @RequestBody(required = false) Map<String, String> payload) {
        String cancelReason = payload != null ? payload.getOrDefault("reason", "Admin tarafından iptal edildi.") : "Admin tarafından iptal edildi.";
        logger.info("Admin, sipariş ID {} için iptal isteği gönderdi. Neden: {}", orderId, cancelReason);
        try {
            OrderDto cancelledOrder = orderService.cancelOrder(orderId, cancelReason);
            logger.info("Sipariş ID {} admin tarafından başarıyla iptal edildi.", orderId);
            return ResponseEntity.ok(cancelledOrder);
        } catch (RuntimeException e) {
            logger.warn("Admin sipariş iptal hatası ({}): {}", orderId, e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Admin sipariş iptal edilirken beklenmedik bir hata oluştu ({})", orderId, e);
            return new ResponseEntity<>(Collections.singletonMap("error","Sipariş iptal edilirken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // === SATICI SİPARİŞ ENDPOINT'LERİ ===

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının kendi ürünlerini içeren siparişleri listelemesini sağlar.
     * GET /api/orders/seller/my-orders
     */
    @GetMapping("/seller/my-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderDto>> getMyOrdersAsSeller(
            @RequestParam(required = false) String statusFilter,
            Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        logger.info("Satıcı '{}' (ID: {}) kendi siparişlerini istiyor. Durum filtresi: {}", seller.getEmail(), seller.getId(), statusFilter);
        List<OrderDto> orders = orderService.getOrdersForSeller(seller, statusFilter);
        return ResponseEntity.ok(orders);
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının, kendi ürününü içeren
     * belirli bir siparişin detaylarını getirmesini sağlar.
     * GET /api/orders/seller/{orderId}
     */
    @GetMapping("/seller/{orderId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<OrderDto> getOrderDetailsForMyProductAsSeller(@PathVariable Long orderId, Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        logger.info("Satıcı '{}' (ID: {}), sipariş ID {} detayını istiyor.", seller.getEmail(), seller.getId(), orderId);
        // DÜZELTME: Optional.map().orElseThrow() yerine if-else veya orElseGet ile ResponseStatusException fırlatma
        Optional<OrderDto> orderDtoOpt = orderService.getOrderDetailsForSeller(orderId, seller);
        if (orderDtoOpt.isPresent()) {
            return ResponseEntity.ok(orderDtoOpt.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sipariş bulunamadı veya bu siparişe erişim yetkiniz yok.");
        }
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının, kendi ürününü içeren
     * bir siparişin durumunu güncellemesini sağlar.
     * PUT /api/orders/seller/{orderId}/status
     */
    @PutMapping("/seller/{orderId}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateOrderStatusAsSeller(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        String newStatus = payload.get("newStatus");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return new ResponseEntity<>(Collections.singletonMap("error", "Yeni sipariş durumu belirtilmelidir."), HttpStatus.BAD_REQUEST);
        }
        logger.info("Satıcı '{}' (ID: {}), sipariş ID {} durumunu '{}' olarak güncellemek istiyor.", seller.getEmail(), seller.getId(), orderId, newStatus);
        try {
            Optional<OrderDto> updatedOrderOpt = orderService.updateOrderStatusForSeller(orderId, newStatus, seller);
            // DÜZELTME: Optional.map().orElseThrow() yerine if-else veya orElseGet ile ResponseStatusException fırlatma
            if (updatedOrderOpt.isPresent()) {
                return ResponseEntity.ok(updatedOrderOpt.get());
            } else {
                 // Servis katmanı zaten AccessDeniedException veya uygun bir RuntimeException fırlatabilir.
                 // Eğer servis Optional.empty() döndürüyorsa, bu ürünün satıcıya ait olmadığını veya bulunamadığını gösterir.
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sipariş güncellenemedi. Sipariş bulunamadı veya bu işlem için yetkiniz yok.");
            }
        } catch (IllegalArgumentException | AccessDeniedException e) { // DÜZELTME: AccessDeniedException import edildi
            logger.warn("Satıcı sipariş durum güncelleme hatası (SiparişID: {}, SatıcıID: {}): {}", orderId, seller.getId(), e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) { // Diğer beklenmedik hatalar için
             logger.error("Satıcı sipariş durum güncelleme sırasında beklenmedik hata (SiparişID: {}, SatıcıID: {}): {}", orderId, seller.getId(), e.getMessage(), e);
             return new ResponseEntity<>(Collections.singletonMap("error", "Sipariş durumu güncellenirken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının bekleyen sipariş sayısını getirir.
     * GET /api/orders/seller/count/pending
     */
    @GetMapping("/seller/count/pending")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Long> getPendingOrderCountAsSeller(Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        logger.info("Satıcı '{}' (ID: {}) bekleyen sipariş sayısını istiyor.", seller.getEmail(), seller.getId());
        long count = orderService.countPendingOrdersForSeller(seller);
        return ResponseEntity.ok(count);
    }

    /**
     * Authentication nesnesinden User entity'sini alır.
     */
    private User getCurrentSeller(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bu işlem için kimlik doğrulaması yapılmamış.");
        }
        String principalName = authentication.getName();
        return userService.findUserEntityByEmail(principalName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kimliği doğrulanmış kullanıcı sistemde bulunamadı: " + principalName));
    }
}
