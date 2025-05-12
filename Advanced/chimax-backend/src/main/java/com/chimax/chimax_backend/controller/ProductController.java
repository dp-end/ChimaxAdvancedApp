package com.chimax.chimax_backend.controller;

import com.chimax.chimax_backend.dto.ProductDto; // İstekler için kullanılan DTO
import com.chimax.chimax_backend.dto.ProductResponseDto; // Yanıtlar için yeni DTO'muz
// Product entity'sini burada doğrudan kullanmamaya çalışacağız, servis DTO döndürecek
import com.chimax.chimax_backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.stream.Collectors; // Servis katmanında yapıldığı için burada gerekmeyebilir

/**
 * Ürünlerle ilgili API endpoint'lerini yöneten Controller.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Tüm aktif ürünleri listeler. (Herkese Açık)
     * GET /api/products
     * @return ProductResponseDto listesini içeren ResponseEntity.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllActiveProducts() {
        // ProductService.findAllActiveProducts() artık List<ProductResponseDto> döndürüyor.
        List<ProductResponseDto> productResponseDtos = productService.findAllActiveProducts();
        return ResponseEntity.ok(productResponseDtos);
    }

    /**
     * Belirli bir ID'ye sahip ürünü getirir. (Herkese Açık)
     * GET /api/products/{id}
     * @param id Getirilecek ürünün ID'si.
     * @return ProductResponseDto içeren ResponseEntity veya bulunamazsa 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        // ProductService.findProductById(id) artık Optional<ProductResponseDto> döndürüyor.
        return productService.findProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Yeni bir ürün ekler. (Sadece Admin Erişebilir)
     * POST /api/products
     * @param productDto İstek gövdesinden gelen ve valide edilen ürün bilgisi.
     * @return Oluşturulan ürünün ProductResponseDto'sunu ve HTTP 201 Created yanıtını döndürür.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> addProduct(@Valid @RequestBody ProductDto productDto) {
        // ProductService.createProduct(productDto) artık ProductResponseDto döndürüyor.
        ProductResponseDto responseDto = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Mevcut bir ürünü günceller. (Sadece Admin Erişebilir)
     * PUT /api/products/{id}
     * @param id Güncellenecek ürünün ID'si.
     * @param productDto İstek gövdesinden gelen ve valide edilen güncel ürün bilgileri.
     * @return Güncellenen ürünün ProductResponseDto'sunu (HTTP 200 OK) veya ürün bulunamazsa 404 Not Found.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
        // ProductService.updateProduct(id, productDto) artık Optional<ProductResponseDto> döndürüyor.
        return productService.updateProduct(id, productDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Bir ürünü siler (veya pasif hale getirir). (Sadece Admin Erişebilir)
     * DELETE /api/products/{id}
     * @param id Silinecek ürünün ID'si.
     * @return Başarılı olursa HTTP 204 No Content veya bulunamazsa 404 Not Found.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // ProductService.deleteProduct metodu, varlık kontrolünü kendi içinde yapmalı
        // ve bulunamazsa uygun bir istisna fırlatmalıdır.
        // Bu istisna bir @ControllerAdvice ile yakalanıp 404'e çevrilebilir.
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) { // Örn: ResourceNotFoundException veya benzeri özel bir exception
            // Hata loglanabilir.
            // Eğer servis özel bir exception fırlatıyorsa, ona göre catch bloğu düzenlenebilir.
            return ResponseEntity.notFound().build();
        }
    }
}
