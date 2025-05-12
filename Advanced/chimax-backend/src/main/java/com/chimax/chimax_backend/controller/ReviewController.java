package com.chimax.chimax_backend.controller; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.CreateReviewDto; // Yeni DTO import edildi
import com.chimax.chimax_backend.dto.ReviewDto;
import com.chimax.chimax_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Ürün yorumları ile ilgili API endpoint'lerini yöneten Controller.
 */
@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Belirli bir ürüne ait tüm yorumları getirir. (Herkese Açık)
     */
    @GetMapping
    public ResponseEntity<List<ReviewDto>> getReviewsByProductId(@PathVariable Long productId) {
        logger.info("Ürün ID {} için yorumlar getiriliyor.", productId);
        try {
            List<ReviewDto> reviews = reviewService.getReviewsForProduct(productId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Ürün ID {} için yorumlar getirilirken hata oluştu.", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Belirli bir ürüne yeni bir yorum ekler. (Sadece Giriş Yapmış Kullanıcılar)
     * @param productId Yorum yapılacak ürünün ID'si (URL'den alınır).
     * @param createReviewDto İstek gövdesinden gelen yorum bilgileri (rating, comment). // Tip Değiştirildi
     * @return Eklenen yorumun DTO'su ve HTTP 201 Created durumu veya hata mesajı.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addReview(@PathVariable Long productId,
                                       @Valid @RequestBody CreateReviewDto createReviewDto) { // Tip Değiştirildi
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        logger.info("Kullanıcı '{}', ürün ID {} için yorum ekliyor.", userEmail, productId);

        try {
            // Servis metodunu çağır (artık CreateReviewDto alıyor)
            ReviewDto savedReview = reviewService.addReview(productId, createReviewDto, userEmail);
            logger.info("Yorum başarıyla eklendi: ID {}", savedReview.getId());
            return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.warn("Yorum ekleme hatası (Kullanıcı: {}, Ürün: {}): {}", userEmail, productId, e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Yorum eklenirken beklenmedik bir hata oluştu (Kullanıcı: {}, Ürün: {})", userEmail, productId, e);
            return new ResponseEntity<>(Collections.singletonMap("error","Yorum eklenirken beklenmedik bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
