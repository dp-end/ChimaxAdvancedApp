package com.chimax.chimax_backend.entity; // Paket adını kontrol et

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * İletişim formundan gelen mesajları temsil eden veritabanı varlığı (entity).
 */
@Entity
@Table(name = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Lob // Uzun mesajlar için
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Mesajın alındığı tarih ve saat.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    /**
     * Mesajın okunup okunmadığı veya yanıtlanıp yanıtlanmadığı gibi
     * durumları takip etmek için bir alan eklenebilir (opsiyonel).
     * Örn: "NEW", "READ", "REPLIED"
     */
    @Column(length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'NEW'")
    private String status = "NEW";

    // Admin tarafından eklenen notlar için bir alan eklenebilir (opsiyonel)
    // @Lob
    // @Column(columnDefinition = "TEXT")
    // private String adminNotes;
}
