package com.chimax.chimax_backend.repository; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort; // Sıralama için
import java.util.List;

/**
 * ContactMessage entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> { // Entity: ContactMessage, PK Tipi: Long

    /**
     * Tüm mesajları belirli bir sıralama ile getirir.
     * @param sort Sıralama kriterleri (örn: tarihe göre tersten).
     * @return Sıralanmış mesaj listesi.
     */
    List<ContactMessage> findAll(Sort sort);

    // İleride duruma göre filtreleme metotları eklenebilir
    // List<ContactMessage> findByStatus(String status, Sort sort);
}
