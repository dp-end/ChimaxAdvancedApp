package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.CreateReviewDto; // Yeni DTO import
import com.chimax.chimax_backend.dto.ReviewDto;
import com.chimax.chimax_backend.entity.Product;
import com.chimax.chimax_backend.entity.Review;
import com.chimax.chimax_backend.entity.User;
import com.chimax.chimax_backend.repository.OrderRepository;
import com.chimax.chimax_backend.repository.ProductRepository;
import com.chimax.chimax_backend.repository.ReviewRepository;
import com.chimax.chimax_backend.repository.UserRepository;
import org.slf4j.Logger; // Loglama
import org.slf4j.LoggerFactory; // Loglama
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final String COMPLETED_ORDER_STATUS = "Teslim Edildi";

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProductRepository productRepository,
                             UserRepository userRepository,
                             OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByReviewDateDesc(productId);
        return reviews.stream()
                .map(this::convertToReviewDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    // Metot imzası güncellendi:
    public ReviewDto addReview(Long productId, CreateReviewDto createReviewDto, String userEmail) {
        logger.info("addReview çağrıldı: ÜrünID={}, Kullanıcı={}", productId, userEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Yorum yapılacak ürün bulunamadı: ID " + productId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Yorum yapan kullanıcı bulunamadı: " + userEmail));

        // Satın alma kontrolü
        boolean hasPurchased = orderRepository.existsByUserAndProductIdAndStatus(user, productId, COMPLETED_ORDER_STATUS);
        if (!hasPurchased) {
            throw new RuntimeException("Bu ürüne yorum yapabilmek için önce satın alıp teslim almış olmalısınız.");
        }

        // Tekrar yorum kontrolü
        boolean hasAlreadyReviewed = reviewRepository.existsByUserAndProduct(user, product);
        if (hasAlreadyReviewed) {
            throw new RuntimeException("Bu ürüne zaten daha önce yorum yapmışsınız.");
        }

        // Yeni Review Entity'si Oluştur (CreateReviewDto kullanılarak)
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(createReviewDto.getRating()); // Yeni DTO'dan al
        review.setComment(createReviewDto.getComment()); // Yeni DTO'dan al

        Review savedReview = reviewRepository.save(review);
        logger.info("Yorum başarıyla kaydedildi: ID {}", savedReview.getId());

        return convertToReviewDto(savedReview);
    }

    // === Yardımcı Dönüşüm Metodu: Review Entity -> ReviewDto ===
    private ReviewDto convertToReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName().substring(0, 1) + ".");
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        return dto;
    }
}
