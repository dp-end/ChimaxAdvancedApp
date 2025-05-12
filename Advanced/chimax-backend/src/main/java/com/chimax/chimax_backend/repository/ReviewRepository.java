package com.chimax.chimax_backend.repository; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.Review;
import com.chimax.chimax_backend.entity.User; // User import et
import com.chimax.chimax_backend.entity.Product; // Product import et
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Review entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Belirli bir ürüne ait tüm yorumları bulur.
     * Yorum tarihine göre tersten sıralamak yaygındır.
     * @param productId Yorumları aranacak ürünün ID'si.
     * @return Ürüne ait yorumların listesi.
     */
    List<Review> findByProductIdOrderByReviewDateDesc(Long productId);

    /**
     * Belirli bir kullanıcının belirli bir ürüne daha önce yorum yapıp yapmadığını kontrol eder.
     * @param user Yorumu yapan kullanıcı.
     * @param product Yorum yapılan ürün.
     * @return Eğer kullanıcı bu ürüne yorum yapmışsa true, aksi halde false döner.
     */
    boolean existsByUserAndProduct(User user, Product product); // Veya existsByUserIdAndProductId(Long userId, Long productId);

}
