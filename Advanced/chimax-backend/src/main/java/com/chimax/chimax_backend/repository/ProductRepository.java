package com.chimax.chimax_backend.repository; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.Product; // Product entity'sini import et
import com.chimax.chimax_backend.entity.User; // User entity'sini import et (seller alanı için gerekli)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // @Repository anotasyonu eklendi (isteğe bağlı ama iyi pratik)

// import org.springframework.data.domain.Page; // Sayfalama için (ileride gerekirse)
// import org.springframework.data.domain.Pageable; // Sayfalama için (ileride gerekirse)
import java.util.List; // Liste döndürmek için
import java.util.Optional; // Optional döndürmek için (ileride gerekirse)

/**
 * Product entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 * JpaRepository<EntityType, PrimaryKeyType>
 */
@Repository // Spring Data JPA repository'lerini belirtmek için iyi bir pratiktir.
public interface ProductRepository extends JpaRepository<Product, Long> { // Entity: Product, Primary Key Tipi: Long

    // --- Satıcıya Özel Sorgu Metotları ---

    /**
     * Belirtilen satıcıya ait tüm ürünleri (aktif veya pasif) bulur.
     * Spring Data JPA, metot adına göre sorguyu otomatik oluşturur: "findBy" + "Seller" (Product entity'sindeki alan adı)
     * @param seller Ürünleri listelenecek satıcı (User entity'si).
     * @return Belirtilen satıcıya ait ürünlerin listesi.
     */
    List<Product> findBySeller(User seller);

    /**
     * Belirtilen satıcıya ait ve aktif olan tüm ürünleri bulur.
     * Spring Data JPA, metot adına göre sorguyu otomatik oluşturur: "findBy" + "Seller" + "And" + "ActiveTrue"
     * @param seller Ürünleri listelenecek satıcı.
     * @return Belirtilen satıcıya ait aktif ürünlerin listesi.
     */
    List<Product> findBySellerAndActiveTrue(User seller);

    /**
     * Belirtilen ID'ye ve satıcıya ait ürünü bulur.
     * Bu, bir ürünün hem varlığını hem de belirli bir satıcıya ait olup olmadığını kontrol etmek için kullanışlıdır.
     * @param id Ürünün ID'si.
     * @param seller Ürünün sahibi olması beklenen satıcı.
     * @return Eşleşen ürünü içeren Optional, bulunamazsa boş Optional.
     */
    Optional<Product> findByIdAndSeller(Long id, User seller);


    // --- İleride Eklenebilecek Diğer Örnek Metotlar ---
    // List<Product> findByCategoryAndActiveTrue(String category);
    // Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    // List<Product> findByActiveTrue(); // Sadece aktif ürünleri getirme (findAllActiveProducts için alternatif)

}
