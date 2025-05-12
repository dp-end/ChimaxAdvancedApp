package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.ContactMessageDto;
import com.chimax.chimax_backend.entity.ContactMessage; // ContactMessage import et
import org.springframework.web.multipart.MultipartFile;
import java.util.List; // List import et

/**
 * İletişim formu işlemleriyle ilgili iş mantığını tanımlayan arayüz.
 */
public interface ContactService {

    /**
     * Gelen iletişim mesajını işler (veritabanına kaydeder).
     * @param messageDto Mesaj bilgilerini içeren DTO.
     * @return Kaydedilen mesaj nesnesi.
     */
    ContactMessage processContactMessage(ContactMessageDto messageDto); // Dönüş tipi eklendi

    /**
     * Gelen tasarım dosyasını ve ilgili bilgileri işler (örn: kaydeder, loglar).
     * @param fullName Gönderenin adı soyadı.
     * @param email Gönderenin e-postası.
     * @param phoneNumber Gönderenin telefonu.
     * @param file Yüklenen tasarım dosyası.
     * @return İşlem sonucuyla ilgili bir mesaj veya nesne döndürebilir.
     */
    String processDesignSubmission(String fullName, String email, String phoneNumber, MultipartFile file); // Dönüş tipi String olarak değiştirildi (örnek)

    /**
     * Tüm iletişim mesajlarını getirir (Admin için).
     * @return ContactMessage listesi.
     */
    List<ContactMessage> getAllMessages(); // Yeni metot eklendi

}
