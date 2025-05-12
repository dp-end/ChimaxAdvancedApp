package com.chimax.chimax_backend.controller; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.ContactMessageDto;
import com.chimax.chimax_backend.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Web anotasyonları
import org.springframework.web.multipart.MultipartFile; // Dosya yükleme için

import java.util.Collections;
import java.util.Map;

/**
 * İletişim formu ile ilgili API endpoint'lerini yöneten Controller.
 */
@RestController
@RequestMapping("/api/contact") // Temel yol /api/contact
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Standart iletişim formundan gelen mesajı alır ve işler.
     * POST /api/contact/message
     * @param messageDto İstek gövdesinden gelen mesaj bilgileri.
     * @return Başarı mesajı veya hata.
     */
    @PostMapping("/message")
    public ResponseEntity<?> receiveContactMessage(@Valid @RequestBody ContactMessageDto messageDto) {
        try {
            logger.info("/contact/message isteği alındı: {}", messageDto.getEmail());
            contactService.processContactMessage(messageDto);
            // Başarılı yanıt döndür
            Map<String, String> response = Collections.singletonMap("message", "Mesajınız başarıyla alındı. En kısa sürede geri dönüş yapılacaktır.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("/contact/message işlenirken hata oluştu", e);
            Map<String, String> errorResponse = Collections.singletonMap("error", "Mesajınız gönderilirken bir hata oluştu. Lütfen tekrar deneyin.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Tasarım yükleme formundan gelen bilgileri ve dosyayı alır ve işler.
     * Bu endpoint multipart/form-data tipinde istek bekler.
     * POST /api/contact/design
     * @param fullName Form verisi olarak gelen ad soyad.
     * @param email Form verisi olarak gelen e-posta.
     * @param phoneNumber Form verisi olarak gelen telefon.
     * @param file Form verisi olarak gelen tasarım dosyası.
     * @return Başarı mesajı veya hata.
     */
    @PostMapping("/design")
    public ResponseEntity<?> receiveDesignSubmission(
            @RequestParam("fullName") String fullName, // Form verilerini @RequestParam ile al
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("file") MultipartFile file) { // Dosyayı @RequestParam ile al
        try {
            logger.info("/contact/design isteği alındı: Email={}, Dosya={}", email, file.getOriginalFilename());
            // Validasyonlar burada veya serviste yapılabilir
            if (file.isEmpty()) {
                 return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Lütfen bir tasarım dosyası seçin."));
            }
            // TODO: Dosya boyutu, tipi kontrolü eklenebilir

            contactService.processDesignSubmission(fullName, email, phoneNumber, file);
            Map<String, String> response = Collections.singletonMap("message", "Tasarımınız başarıyla alındı. İnceleyip sizinle iletişime geçeceğiz.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) { // Servisten gelen bilinen hatalar (örn: dosya kaydedilemedi)
             logger.error("/contact/design işlenirken hata: {}", e.getMessage());
             return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
         catch (Exception e) {
            logger.error("/contact/design işlenirken beklenmedik hata oluştu", e);
            Map<String, String> errorResponse = Collections.singletonMap("error", "Tasarımınız gönderilirken bir hata oluştu. Lütfen tekrar deneyin.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
