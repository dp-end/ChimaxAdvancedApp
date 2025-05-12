package com.chimax.chimax_backend.entity; // Paket adını kontrol et

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ürün yorumlarını temsil eden veritabanı varlığı (entity).
 */
@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Yorumun ait olduğu ürün.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Yorumu yapan kullanıcı.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Yorumu yapan kullanıcı

    /**
     * Kullanıcının verdiği puan (1-5).
     */
    @Column(nullable = false)
    private int rating;

    /**
     * Yorum metni.
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    /**
     * Yorumun yapıldığı tarih ve saat.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime reviewDate;

}
