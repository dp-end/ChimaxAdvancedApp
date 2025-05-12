package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.*; // Tüm DTO'ları import et
import com.chimax.chimax_backend.entity.*; // Tüm Entity'leri import et
import com.chimax.chimax_backend.repository.*; // Tüm Repository'leri import et
// import com.chimax.chimax_backend.model.OrderStatus; // Eğer bir OrderStatus enum'unuz varsa
import org.slf4j.Logger; // Loglama
import org.slf4j.LoggerFactory; // Loglama
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // Sıralama için import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException; // Yetkilendirme için


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    // private final StripeService stripeService; // Ödeme onayı/iadesi için gerekebilir

    private static final List<String> COMPLETED_STATUSES = List.of("DELIVERED", "TESLİM EDİLDİ");
    private static final List<String> CANCELLED_STATUSES = List.of("CANCELLED", "İPTAL EDİLDİ");
    private static final List<String> CANCELLABLE_STATUSES = List.of("PROCESSING", "PENDING", "HAZIRLANIYOR");


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository
                           /*, StripeService stripeService */) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        // this.stripeService = stripeService;
    }

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto orderRequest, String userEmail) {
        logger.info("createOrder çağrıldı: Kullanıcı={}, Ödeme Yöntemi={}", userEmail, orderRequest.getPaymentMethod());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("createOrder - Kullanıcı bulunamadı: {}", userEmail);
                    return new RuntimeException("Kullanıcı bulunamadı: " + userEmail);
                });

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PROCESSING"); // Varsayılan durum
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setPaymentIntentId(orderRequest.getPaymentIntentId());

        AddressDto shippingAddressDto = orderRequest.getShippingAddress();
        order.setShippingFullName(shippingAddressDto.getFullName());
        order.setShippingAddressLine1(shippingAddressDto.getAddressLine1());
        order.setShippingCity(shippingAddressDto.getCity());
        order.setShippingPostalCode(shippingAddressDto.getPostalCode());
        order.setShippingCountry(shippingAddressDto.getCountry());
        order.setShippingPhone(shippingAddressDto.getPhone());

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderItemDto itemDto : orderRequest.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> {
                        logger.error("createOrder - Ürün bulunamadı: ID {}", itemDto.getProductId());
                        return new RuntimeException("Siparişinizdeki bir ürün bulunamadı: ID " + itemDto.getProductId());
                    });

            if (!product.isActive() || product.getStockQuantity() < itemDto.getQuantity()) {
                logger.error("createOrder - Stok yetersiz veya ürün aktif değil: Ürün ID {}, İstenen {}, Kalan {}, Aktif {}",
                             itemDto.getProductId(), itemDto.getQuantity(), product.getStockQuantity(), product.isActive());
                throw new RuntimeException("Stok yetersiz veya ürün aktif değil: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice());
            order.addOrderItem(orderItem);

            calculatedTotal = calculatedTotal.add(
                    product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()))
            );
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
        }
        order.setTotalAmount(calculatedTotal);
        Order savedOrder = orderRepository.save(order);
        logger.info("Sipariş başarıyla kaydedildi: ID {}", savedOrder.getId());
        return convertToOrderDto(savedOrder, user); // Müşteri için DTO dönüşümü
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersByUser(String userEmail) {
        logger.debug("findOrdersByUser çağrıldı: Kullanıcı={}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userEmail));
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream().map(order -> convertToOrderDto(order, user)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDto> findOrderByIdAndUser(Long orderId, String userEmail) {
        logger.debug("findOrderByIdAndUser çağrıldı: SiparişID={}, Kullanıcı={}", orderId, userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userEmail));
        return orderRepository.findById(orderId)
                .filter(order -> order.getUser().getId().equals(user.getId()))
                .map(order -> convertToOrderDto(order, user));
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId, String reason) {
        logger.info("cancelOrder çağrıldı: SiparişID={}, Neden={}", orderId, reason);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: ID " + orderId));

        String currentStatusUpper = order.getStatus().toUpperCase();
        if (!CANCELLABLE_STATUSES.contains(currentStatusUpper)) {
            logger.warn("İptal edilemez sipariş durumu: SiparişID={}, Durum={}", orderId, order.getStatus());
            throw new RuntimeException("Bu sipariş durumu ("+ order.getStatus() +") iptal edilemez.");
        }
        order.setStatus("CANCELLED");
        logger.info("Sipariş ID {} için stoklar geri yükleniyor.", orderId);
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }
        Order cancelledOrder = orderRepository.save(order);
        logger.info("Sipariş ID {} başarıyla iptal edildi.", orderId);
        return convertToOrderDto(cancelledOrder, cancelledOrder.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAllOrders() {
        logger.debug("findAllOrders (Admin) çağrıldı.");
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
        return orders.stream()
                .map(order -> convertToOrderDto(order, order.getUser())) // Admin tüm detayları görebilir
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusDto statusDto) {
        logger.info("updateOrderStatus (Admin) çağrıldı: SiparişID={}, YeniDurum={}", orderId, statusDto.getNewStatus());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: ID " + orderId));

        String currentStatusUpper = order.getStatus().toUpperCase();
        String newStatusUpper = statusDto.getNewStatus().toUpperCase();

        if (CANCELLED_STATUSES.contains(currentStatusUpper) || COMPLETED_STATUSES.contains(currentStatusUpper)) {
            logger.warn("İptal edilmiş/Tamamlanmış siparişin (ID: {}) durumu değiştirilemez.", orderId);
            throw new RuntimeException("İptal edilmiş veya tamamlanmış siparişin durumu değiştirilemez.");
        }
        order.setStatus(newStatusUpper);
        if (statusDto.getTrackingNumber() != null && !statusDto.getTrackingNumber().isBlank()) {
            order.setTrackingNumber(statusDto.getTrackingNumber());
        }
        Order updatedOrder = orderRepository.save(order);
        logger.info("Sipariş ID {} durumu başarıyla '{}' olarak güncellendi.", orderId, newStatusUpper);
        return convertToOrderDto(updatedOrder, updatedOrder.getUser());
    }

    // --- Satıcıya Özel Yeni Sipariş Metotlarının Implementasyonları ---

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersForSeller(User seller, String statusFilter) {
        if (seller == null) {
            logger.warn("getOrdersForSeller çağrıldı ancak satıcı bilgisi null.");
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        logger.debug("getOrdersForSeller çağrıldı: SatıcıID={}, DurumFiltresi={}", seller.getId(), statusFilter);
        List<Order> orders;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            orders = orderRepository.findOrdersBySellerAndStatus(seller, statusFilter.toUpperCase());
        } else {
            orders = orderRepository.findOrdersBySeller(seller);
        }
        // Satıcıya özel DTO dönüşümü kullanılacak
        return orders.stream().map(order -> convertToOrderDtoForSellerView(order, seller)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDto> getOrderDetailsForSeller(Long orderId, User seller) {
        if (seller == null) {
            logger.warn("getOrderDetailsForSeller çağrıldı ancak satıcı bilgisi null.");
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        logger.debug("getOrderDetailsForSeller çağrıldı: SiparişID={}, SatıcıID={}", orderId, seller.getId());
        return orderRepository.findById(orderId)
                .filter(order -> order.getItems().stream() // Siparişin en az bir kalemi bu satıcıya ait olmalı
                        .anyMatch(item -> item.getProduct() != null &&
                                         item.getProduct().getSeller() != null &&
                                         item.getProduct().getSeller().getId().equals(seller.getId())))
                .map(order -> convertToOrderDtoForSellerView(order, seller));
    }

    @Override
    @Transactional
    public Optional<OrderDto> updateOrderStatusForSeller(Long orderId, String newStatus, User seller) {
        if (seller == null) {
            logger.warn("updateOrderStatusForSeller çağrıldı ancak satıcı bilgisi null.");
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Yeni sipariş durumu boş olamaz.");
        }
        logger.info("updateOrderStatusForSeller çağrıldı: SiparişID={}, YeniDurum={}, SatıcıID={}", orderId, newStatus, seller.getId());

        Optional<Order> orderOpt = orderRepository.findById(orderId)
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct() != null &&
                                         item.getProduct().getSeller() != null &&
                                         item.getProduct().getSeller().getId().equals(seller.getId())));

        if (orderOpt.isEmpty()) {
            logger.warn("updateOrderStatusForSeller - Sipariş bulunamadı veya satıcıya ait değil: SiparişID={}", orderId);
            return Optional.empty(); // Ya sipariş yok ya da bu satıcıya ait değil.
        }

        Order order = orderOpt.get();
        String currentStatusUpper = order.getStatus().toUpperCase();
        String newStatusUpper = newStatus.toUpperCase();

        // Satıcının değiştirebileceği durumları ve geçişleri burada kontrol edin
        // Örneğin, satıcı 'DELIVERED' veya 'CANCELLED' yapamasın, sadece 'PROCESSING', 'SHIPPED' gibi.
        if (CANCELLED_STATUSES.contains(currentStatusUpper) || COMPLETED_STATUSES.contains(currentStatusUpper)) {
            logger.warn("Satıcı, iptal edilmiş/tamamlanmış siparişin (ID: {}) durumunu değiştiremez.", orderId);
            throw new AccessDeniedException("İptal edilmiş veya tamamlanmış siparişin durumu satıcı tarafından değiştirilemez.");
        }
        // TODO: Satıcı için geçerli durum geçişlerini daha detaylı kontrol et.

        order.setStatus(newStatusUpper);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Sipariş ID {} durumu satıcı tarafından başarıyla '{}' olarak güncellendi.", orderId, newStatusUpper);
        return Optional.of(convertToOrderDtoForSellerView(updatedOrder, seller));
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingOrdersForSeller(User seller) {
        if (seller == null) {
            logger.warn("countPendingOrdersForSeller çağrıldı ancak satıcı bilgisi null.");
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        logger.debug("countPendingOrdersForSeller çağrıldı: SatıcıID={}", seller.getId());
        // "PENDING" veya "PROCESSING" gibi satıcının ilgilenmesi gereken durumlar
        String pendingStatus = "PENDING"; // Veya OrderStatus.PENDING.name() veya listenizdeki bir durum
        // Alternatif olarak birden fazla durumu sayabilirsiniz
        // Örneğin: return orderRepository.countOrdersBySellerAndStatusIn(seller, List.of("PENDING", "PROCESSING"));
        return orderRepository.countOrdersBySellerAndStatus(seller, pendingStatus);
    }

    // === Yardımcı Dönüşüm Metotları ===

    // Genel Order -> OrderDto dönüşümü (Müşteri veya Admin için)
    private OrderDto convertToOrderDto(Order order, User perspectiveUser) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTrackingNumber(order.getTrackingNumber());

        if (order.getUser() != null) { // Siparişi veren kullanıcı bilgisi
            UserDto customerDto = new UserDto();
            customerDto.setId(order.getUser().getId());
            customerDto.setFirstName(order.getUser().getFirstName());
            customerDto.setLastName(order.getUser().getLastName());
            customerDto.setEmail(order.getUser().getEmail());
            dto.setUser(customerDto); // OrderDto'da UserDto alanı olduğunu varsayıyoruz
        }
        
        AddressDto addressDto = new AddressDto();
        addressDto.setFullName(order.getShippingFullName());
        addressDto.setAddressLine1(order.getShippingAddressLine1());
        addressDto.setCity(order.getShippingCity());
        addressDto.setPostalCode(order.getShippingPostalCode());
        addressDto.setCountry(order.getShippingCountry());
        addressDto.setPhone(order.getShippingPhone());
        dto.setShippingAddress(addressDto);

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList()));
        }
        return dto;
    }
    
    // Satıcı perspektifinden Order -> OrderDto dönüşümü
    // Bu metot, satıcının sadece kendi ürünlerini ve görmesi gereken bilgileri içerecek şekilde özelleştirilebilir.
    private OrderDto convertToOrderDtoForSellerView(Order order, User seller) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod()); // Satıcı ödeme yöntemini görebilir
        dto.setTrackingNumber(order.getTrackingNumber()); // Satıcı kargo takibini görebilir

        // Müşteri bilgileri (satıcı için önemli olabilir)
        if (order.getUser() != null) {
            UserDto customerDto = new UserDto();
            // Müşterinin tüm bilgilerini vermek yerine sadece gerekli olanları verebilirsiniz.
            // Örneğin, sadece isim veya bir müşteri ID'si.
            // Şimdilik tam UserDto'yu verelim, ancak bu güvenlik açısından gözden geçirilmeli.
            customerDto.setId(order.getUser().getId());
            customerDto.setFirstName(order.getUser().getFirstName());
            customerDto.setLastName(order.getUser().getLastName());
            customerDto.setEmail(order.getUser().getEmail()); // Satıcı müşteriyle iletişime geçebilir
            dto.setUser(customerDto);
        }

        // Kargo Adresi (satıcı için önemli)
        AddressDto addressDto = new AddressDto();
        addressDto.setFullName(order.getShippingFullName());
        addressDto.setAddressLine1(order.getShippingAddressLine1());
        addressDto.setCity(order.getShippingCity());
        addressDto.setPostalCode(order.getShippingPostalCode());
        addressDto.setCountry(order.getShippingCountry());
        addressDto.setPhone(order.getShippingPhone());
        dto.setShippingAddress(addressDto);

        // Sipariş kalemleri: SADECE BU SATICIYA AİT OLANLARI filtrele ve DTO'ya çevir
        // ve bu kalemlere göre siparişin satıcıya düşen toplam tutarını hesapla.
        BigDecimal sellerSpecificTotalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            List<OrderItemDto> sellerItemDtos = order.getItems().stream()
                    .filter(item -> item.getProduct() != null &&
                                   item.getProduct().getSeller() != null &&
                                   item.getProduct().getSeller().getId().equals(seller.getId()))
                    .map(item -> {
                        OrderItemDto itemDto = convertOrderItemToDto(item);
                        // Bu satıcıya ait kalemlerin tutarını topla
                        // sellerSpecificTotalAmount = sellerSpecificTotalAmount.add(
                        //    item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity()))
                        // );
                        return itemDto;
                    })
                    .collect(Collectors.toList());
            dto.setItems(sellerItemDtos);

            // Siparişin genel toplam tutarı yerine, bu satıcıya ait ürünlerin toplam tutarını hesapla
            // Bu, OrderDto'nuzda `sellerPortionTotalAmount` gibi ayrı bir alan gerektirebilir
            // veya `totalAmount` alanını bu satıcıya özel olarak set edebilirsiniz.
            // Şimdilik siparişin genel toplamını gösteriyoruz, bu daha sonra özelleştirilebilir.
            // Eğer her bir OrderItemDto'da satıcıya ait ürünlerin fiyatı varsa, dto.items üzerinden toplanabilir.
            // Veya siparişin genel toplamını gösterip, satıcının kendi ürünlerinin detayını göstermek yeterli olabilir.
            // Şimdilik siparişin genel toplamını kullanıyoruz.
            dto.setTotalAmount(order.getTotalAmount()); 
            // Alternatif: Satıcıya özel toplamı hesapla
            // dto.setTotalAmount(sellerSpecificTotalAmount);
        }
        return dto;
    }

    private OrderItemDto convertOrderItemToDto(OrderItem item) {
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setId(item.getId()); // OrderItem ID'si
        if (item.getProduct() != null) {
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            // itemDto.setProductImageUrl(item.getProduct().getImageUrl()); // İsteğe bağlı
        } else {
            itemDto.setProductName("Bilinmeyen Ürün");
        }
        itemDto.setQuantity(item.getQuantity());
        itemDto.setPrice(item.getPriceAtOrder()); // Sipariş anındaki birim fiyat
        // itemDto.setTotalPrice(item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity()))); // Kalem toplamı
        return itemDto;
    }
}
