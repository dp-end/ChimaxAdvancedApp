package com.chimax.chimax_backend.repository; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.Order;
import com.chimax.chimax_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // @Query import et
import org.springframework.data.repository.query.Param; // @Param import et
import org.springframework.stereotype.Repository; // @Repository anotasyonu eklendi

import java.util.List;

/**
 * Order entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 */
@Repository // Spring Data JPA repository'lerini belirtmek için iyi bir pratiktir.
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Belirli bir kullanıcıya ait tüm siparişleri bulur.
     * Sipariş tarihine göre tersten sıralamak yaygındır.
     * @param user Siparişleri aranacak kullanıcı.
     * @return Kullanıcının sipariş listesi.
     */
    List<Order> findByUserOrderByOrderDateDesc(User user);

    /**
     * Belirli bir kullanıcının, belirli bir ürünü içeren ve durumu 'Teslim Edildi'
     * olan bir siparişi olup olmadığını kontrol eder.
     * JPQL (Java Persistence Query Language) ile özel bir sorgu kullanıyoruz.
     * @param user Kullanıcı nesnesi.
     * @param productId Ürün ID'si.
     * @param status Kontrol edilecek sipariş durumu (örn: "Teslim Edildi").
     * @return Eğer eşleşen sipariş varsa true, yoksa false döner.
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM Order o JOIN o.items i " +
           "WHERE o.user = :user AND i.product.id = :productId AND o.status = :status")
    boolean existsByUserAndProductIdAndStatus(@Param("user") User user,
                                              @Param("productId") Long productId,
                                              @Param("status") String status);

    // Alternatif (Spring Data JPA metot isimlendirme ile - daha uzun olabilir):
    // boolean existsByUserAndItems_Product_IdAndStatus(User user, Long productId, String status);


    // --- SATICIYA ÖZEL YENİ SORGULAMA METOTLARI ---

    /**
     * Belirli bir satıcıya ait ürünleri içeren tüm siparişleri (distinct) bulur ve sipariş tarihine göre tersten sıralar.
     * Bu sorgu, OrderItem -> Product -> Seller ilişkisine dayanır.
     * Siparişin en az bir kalemi belirtilen satıcıya aitse siparişi getirir.
     * @param seller Siparişleri listelenecek satıcı (User entity'si).
     * @return Belirtilen satıcıya ait ürünleri içeren siparişlerin listesi.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi JOIN oi.product p WHERE p.seller = :seller ORDER BY o.orderDate DESC")
    List<Order> findOrdersBySeller(@Param("seller") User seller);

    /**
     * Belirli bir satıcıya ait ürünleri içeren ve belirtilen durumda olan tüm siparişleri (distinct) bulur
     * ve sipariş tarihine göre tersten sıralar.
     * @param seller Siparişleri listelenecek satıcı.
     * @param status Filtrelenecek sipariş durumu (String olarak, büyük/küçük harf duyarlı olabilir, serviste handle edilmeli).
     * @return Belirtilen satıcıya ait ve belirtilen durumda olan siparişlerin listesi.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi JOIN oi.product p WHERE p.seller = :seller AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findOrdersBySellerAndStatus(@Param("seller") User seller, @Param("status") String status);

    /**
     * Belirli bir satıcıya ait ürünleri içeren ve belirtilen durumda olan siparişlerin sayısını döndürür.
     * @param seller Satıcı.
     * @param status Sayılacak siparişlerin durumu.
     * @return Eşleşen siparişlerin sayısı.
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi JOIN oi.product p WHERE p.seller = :seller AND o.status = :status")
    long countOrdersBySellerAndStatus(@Param("seller") User seller, @Param("status") String status);

    // İleride birden fazla durumu saymak için (örneğin PENDING veya PROCESSING):
    // @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi JOIN oi.product p WHERE p.seller = :seller AND o.status IN :statuses")
    // long countOrdersBySellerAndStatusIn(@Param("seller") User seller, @Param("statuses") List<String> statuses);

}
