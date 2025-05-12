package com.chimax.chimax_backend.controller; // Paket adınızı kendi projenize göre güncelleyin

import com.chimax.chimax_backend.dto.ProductDto; // İstek için kullanılan DTO
import com.chimax.chimax_backend.dto.ProductResponseDto; // Yanıt için yeni DTO'muz
// Product entity'sini burada doğrudan kullanmamaya çalışacağız, servis DTO döndürecek
import com.chimax.chimax_backend.entity.User;
import com.chimax.chimax_backend.service.ProductService;
import com.chimax.chimax_backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller/products") // Satıcıya özel ürün endpoint'leri için base path
@CrossOrigin(origins = "*", maxAge = 3600) // Frontend'den erişim için CORS ayarı (geliştirme için *)
public class SellerProductController {

    private static final Logger logger = LoggerFactory.getLogger(SellerProductController.class);
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public SellerProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının yeni bir ürün oluşturmasını sağlar.
     * POST /api/seller/products
     * @param productDto Ürün bilgilerini içeren DTO.
     * @param imageFile Yüklenecek ürün resmi (opsiyonel).
     * @param authentication Spring Security tarafından sağlanan kimlik doğrulama bilgisi.
     * @return Oluşturulan ürünün ProductResponseDto'su ve HTTP 201 (Created) durumu.
     */
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> createProduct( // Dönüş tipi ProductResponseDto olarak güncellendi
            @Valid @RequestPart("productDto") ProductDto productDto,
            @RequestPart(name = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) {
        
        User seller = getCurrentSeller(authentication);
        logger.info("Satıcı (ID: {}) için ürün oluşturma isteği alındı. Ürün Adı: {}, Dosya: {}",
                seller.getId(), 
                productDto.getName(), 
                (imageFile != null && !imageFile.isEmpty() ? imageFile.getOriginalFilename() : "Yok"));
        
        try {
            // ProductService.createProductForSeller artık ProductResponseDto döndürüyor
            ProductResponseDto createdProductDto = productService.createProductForSeller(productDto, seller, imageFile);
            return new ResponseEntity<>(createdProductDto, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Satıcı (ID: {}) için ürün oluşturulurken hata: {}", seller.getId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ürün oluşturulamadı: " + e.getMessage());
        }
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının kendi ürünlerini listelemesini sağlar.
     * GET /api/seller/products
     * @return Satıcının ürünlerinin ProductResponseDto listesini içeren ResponseEntity.
     */
    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ProductResponseDto>> getMyProducts(Authentication authentication) { // Dönüş tipi List<ProductResponseDto> olarak güncellendi
        User seller = getCurrentSeller(authentication);
        logger.debug("Satıcı (ID: {}) kendi ürünlerini listeliyor.", seller.getId());
        // ProductService.getProductsBySeller artık List<ProductResponseDto> döndürüyor
        List<ProductResponseDto> productResponseDtos = productService.getProductsBySeller(seller);
        return ResponseEntity.ok(productResponseDtos);
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının kendi ürünlerinden birinin detaylarını getirmesini sağlar.
     * GET /api/seller/products/{productId}
     * @return ProductResponseDto içeren ResponseEntity veya bulunamazsa 404.
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> getMyProductById(@PathVariable Long productId, Authentication authentication) { // Dönüş tipi ProductResponseDto olarak güncellendi
        User seller = getCurrentSeller(authentication);
        logger.debug("Satıcı (ID: {}) ürün (ID: {}) detayını istiyor.", seller.getId(), productId);
        // ProductService.getProductByIdAndSeller artık Optional<ProductResponseDto> döndürüyor
        Optional<ProductResponseDto> productResponseDtoOpt = productService.getProductByIdAndSeller(productId, seller);
        return productResponseDtoOpt.map(ResponseEntity::ok)
                                 .orElseThrow(() -> {
                                     logger.warn("Satıcı (ID: {}) için ürün (ID: {}) bulunamadı veya yetki yok.", seller.getId(), productId);
                                     return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ürün bulunamadı veya bu ürüne erişim yetkiniz yok.");
                                 });
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının kendi ürünlerinden birini güncellemesini sağlar.
     * PUT /api/seller/products/{productId}
     * @return Güncellenen ürünün ProductResponseDto'su ve HTTP 200 (OK) durumu veya 404.
     */
    @PutMapping(value = "/{productId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> updateMyProduct( // Dönüş tipi ProductResponseDto olarak güncellendi
            @PathVariable Long productId,
            @Valid @RequestPart("productDto") ProductDto productDto,
            @RequestPart(name = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) {
                
        User seller = getCurrentSeller(authentication);
        logger.info("Satıcı (ID: {}) ürün (ID: {}) güncelleme isteği. Dosya: {}",
                seller.getId(), 
                productId, 
                (imageFile != null && !imageFile.isEmpty() ? imageFile.getOriginalFilename() : "Değiştirilmiyor/Yok"));

        try {
            // ProductService.updateProductForSeller artık Optional<ProductResponseDto> döndürüyor
            Optional<ProductResponseDto> updatedProductResponseDtoOpt = productService.updateProductForSeller(productId, productDto, seller, imageFile);
            return updatedProductResponseDtoOpt.map(ResponseEntity::ok)
                                         .orElseThrow(() -> {
                                             logger.warn("Satıcı (ID: {}) için ürün (ID: {}) güncellenemedi. Bulunamadı veya yetki yok.", seller.getId(), productId);
                                             return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ürün güncellenemedi. Ürün bulunamadı veya bu işlem için yetkiniz yok.");
                                         });
        } catch (RuntimeException e) {
            logger.error("Satıcı (ID: {}) için ürün (ID: {}) güncellenirken hata: {}", seller.getId(), productId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ürün güncellenemedi: " + e.getMessage());
        }
    }

    /**
     * Kimliği doğrulanmış ve SELLER rolüne sahip bir satıcının kendi ürünlerinden birini silmesini sağlar.
     * DELETE /api/seller/products/{productId}
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteMyProduct(@PathVariable Long productId, Authentication authentication) {
        User seller = getCurrentSeller(authentication);
        logger.info("Satıcı (ID: {}) ürün (ID: {}) silme isteği.", seller.getId(), productId);
        
        // ProductService.getProductByIdAndSeller artık Optional<ProductResponseDto> döndürüyor.
        // Silme işlemi için varlık kontrolü ProductService.deleteProductForSeller içinde yapılmalı.
        // Eğer servis, ürün yoksa veya satıcıya ait değilse bir istisna fırlatırsa,
        // bu @ControllerAdvice ile yakalanabilir veya burada try-catch ile handle edilebilir.
        
        // Önceki kontrol:
        // Optional<Product> productOpt = productService.getProductByIdAndSeller(productId, seller);
        // if (productOpt.isEmpty()) { ... }

        // Yeni durum: deleteProductForSeller servisi varlık kontrolünü yapsın.
        try {
            // ProductService.getProductByIdAndSeller çağrısı, varlık kontrolü için kullanılabilir,
            // ancak deleteProductForSeller zaten bu kontrolü yapıyorsa gereksiz olabilir.
            // Şimdilik, deleteProductForSeller'ın kendi içinde gerekli kontrolleri yaptığını varsayıyoruz.
            productService.deleteProductForSeller(productId, seller);
            logger.info("Satıcı (ID: {}) ürün (ID: {}) başarıyla sildi.", seller.getId(), productId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException rse) { // Servis tarafından fırlatılan bilinen bir hata (örn: ürün yok)
             logger.warn("Satıcı (ID: {}) için ürün (ID: {}) silinirken hata (ResponseStatusException): {}", seller.getId(), productId, rse.getReason());
             throw rse; // Hatayı olduğu gibi tekrar fırlat, Spring Boot handle etsin
        } catch (RuntimeException e) { // Beklenmedik diğer hatalar
            logger.error("Satıcı (ID: {}) için ürün (ID: {}) silinirken beklenmedik hata: {}", seller.getId(), productId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ürün silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Authentication nesnesinden User entity'sini alır.
     */
    private User getCurrentSeller(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Kimliği doğrulanmamış veya anonim kullanıcı bu işlemi yapmaya çalıştı.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bu işlem için kimlik doğrulaması yapılmamış.");
        }
        String principalName = authentication.getName(); // Genellikle email veya username
        logger.debug("Mevcut kimliği doğrulanmış kullanıcı adı (principalName): {}", principalName);
        return userService.findUserEntityByEmail(principalName) // Bu metodun User entity'si döndürdüğünden emin olun
                .orElseThrow(() -> {
                    logger.error("Kimliği doğrulanmış kullanıcı (email: {}) sistemde bulunamadı.", principalName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Kimliği doğrulanmış kullanıcı sistemde bulunamadı: " + principalName);
                });
    }
}
