package com.chimax.chimax_backend.service;

import com.chimax.chimax_backend.dto.ProductDto; // İstekler için kullanılan DTO
import com.chimax.chimax_backend.dto.ProductResponseDto; // Yanıtlar için yeni DTO
import com.chimax.chimax_backend.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    // --- Genel Kullanıcı ve Admin için Metotlar (DTO Yanıtları ile) ---
    /**
     * Tüm aktif ürünleri listeler (genellikle tüm kullanıcılar için).
     * @return Aktif ürünlerin ProductResponseDto listesi.
     */
    List<ProductResponseDto> findAllActiveProducts();

    /**
     * Belirli bir ID'ye sahip aktif bir ürünü getirir (genellikle tüm kullanıcılar için).
     * @param id Getirilecek ürünün ID'si.
     * @return ProductResponseDto içeren Optional veya ürün bulunamazsa/aktif değilse boş Optional.
     */
    Optional<ProductResponseDto> findProductById(Long id);

    /**
     * Yeni bir ürün oluşturur (genellikle Admin tarafından kullanılır).
     * @param productDto Oluşturulacak ürün bilgilerini içeren DTO.
     * @return Oluşturulan ürünün ProductResponseDto'su.
     */
    ProductResponseDto createProduct(ProductDto productDto);

    /**
     * Mevcut bir ürünü günceller (genellikle Admin tarafından kullanılır).
     * @param id Güncellenecek ürünün ID'si.
     * @param productDto Güncel ürün bilgilerini içeren DTO.
     * @return Güncellenen ürünün ProductResponseDto'sunu içeren Optional veya ürün bulunamazsa boş Optional.
     */
    Optional<ProductResponseDto> updateProduct(Long id, ProductDto productDto);

    /**
     * Bir ürünü siler (veya pasif hale getirir - genellikle Admin tarafından kullanılır).
     * @param id Silinecek ürünün ID'si.
     */
    void deleteProduct(Long id);

    // --- Satıcıya Özel Yeni Metotlar (DTO Yanıtları ve Dosya Yükleme Entegrasyonu ile) ---

    /**
     * Belirtilen satıcı için yeni bir ürün oluşturur ve resmini kaydeder.
     * @param productDto Oluşturulacak ürün bilgilerini içeren DTO.
     * @param seller Ürünü oluşturan satıcı (User entity'si).
     * @param imageFile Yüklenecek ürün resmi (opsiyonel).
     * @return Oluşturulan ürünün ProductResponseDto'su.
     */
    ProductResponseDto createProductForSeller(ProductDto productDto, User seller, MultipartFile imageFile);

    /**
     * Belirtilen satıcıya ait tüm ürünleri listeler.
     * @param seller Ürünleri listelenecek satıcı.
     * @return Satıcıya ait ürünlerin ProductResponseDto listesi.
     */
    List<ProductResponseDto> getProductsBySeller(User seller);

    /**
     * Belirtilen satıcıya ait, verilen ID'ye sahip ürünü getirir.
     * @param productId Getirilecek ürünün ID'si.
     * @param seller Ürünün sahibi olan satıcı.
     * @return ProductResponseDto içeren Optional veya ürün bulunamazsa/satıcıya ait değilse boş Optional.
     */
    Optional<ProductResponseDto> getProductByIdAndSeller(Long productId, User seller);

    /**
     * Belirtilen satıcıya ait bir ürünü günceller ve resmini (eğer yeni resim varsa) günceller.
     * @param productId Güncellenecek ürünün ID'si.
     * @param productDto Güncel ürün bilgilerini içeren DTO.
     * @param seller Ürünün sahibi olan satıcı.
     * @param imageFile Yüklenecek yeni ürün resmi (opsiyonel).
     * @return Güncellenen ürünün ProductResponseDto'sunu içeren Optional veya ürün bulunamazsa/satıcıya ait değilse boş Optional.
     */
    Optional<ProductResponseDto> updateProductForSeller(Long productId, ProductDto productDto, User seller, MultipartFile imageFile);

    /**
     * Belirtilen satıcıya ait bir ürünü siler (veya pasif hale getirir) ve ilişkili resim dosyasını da siler.
     * @param productId Silinecek ürünün ID'si.
     * @param seller Ürünün sahibi olan satıcı.
     */
    void deleteProductForSeller(Long productId, User seller);

    // Entity'den DTO'ya dönüşüm metodu için bir imza (implementasyon sınıfında olacak)
    // Bu metodu public yapmak yerine, servis implementasyonunda private veya protected helper olarak tutmak daha yaygındır,
    // ancak eğer başka servislerden de erişilmesi gerekiyorsa arayüzde de tanımlanabilir.
    // Şimdilik implementasyon sınıfında özel bir metot olarak bırakmak daha iyi olabilir.
    // ProductResponseDto convertToProductResponseDto(Product product);
}
