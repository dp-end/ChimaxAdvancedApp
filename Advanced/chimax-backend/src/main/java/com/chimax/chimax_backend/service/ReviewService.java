package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.CreateReviewDto; // Yeni DTO import edildi
import com.chimax.chimax_backend.dto.ReviewDto;
import java.util.List;

public interface ReviewService {

    List<ReviewDto> getReviewsForProduct(Long productId);

    /**
     * Bir ürüne yeni bir yorum ekler.
     * @param productId Yorum yapılacak ürünün ID'si.
     * @param createReviewDto Eklenecek yorumun bilgilerini içeren DTO (rating, comment). // Tip değiştirildi
     * @param userEmail Yorumu yapan kullanıcının e-postası.
     * @return Eklenen yorumun DTO'su.
     */
    ReviewDto addReview(Long productId, CreateReviewDto createReviewDto, String userEmail); // Parametre tipi değiştirildi

}
