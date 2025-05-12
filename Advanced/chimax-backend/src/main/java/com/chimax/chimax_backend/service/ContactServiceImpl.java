package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.ContactMessageDto;
import com.chimax.chimax_backend.entity.ContactMessage; // Entity import
import com.chimax.chimax_backend.repository.ContactMessageRepository; // Repository import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // Sıralama için import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Transactional import
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime; // Tarih için
import java.util.List; // List için

// Dosya kaydetme işlemleri için importlar (ileride eklenecek)
// ...

@Service
public class ContactServiceImpl implements ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactMessageRepository contactMessageRepository; // Repository enjekte edildi

    // @Value("${upload.path:/path/to/your/uploads}")
    // private String uploadPath;

    @Autowired
    public ContactServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Override
    @Transactional // Veritabanına yazma işlemi
    public ContactMessage processContactMessage(ContactMessageDto messageDto) {
        logger.info("Yeni İletişim Mesajı Alındı ve Kaydediliyor:");
        logger.info("İsim: {}, E-posta: {}", messageDto.getName(), messageDto.getEmail());

        // DTO'dan Entity'ye dönüşüm
        ContactMessage message = new ContactMessage();
        message.setName(messageDto.getName());
        message.setEmail(messageDto.getEmail());
        message.setMessage(messageDto.getMessage());
        message.setReceivedAt(LocalDateTime.now()); // Zamanı şimdi ayarla (@CreationTimestamp da yapar)
        message.setStatus("NEW"); // Varsayılan durum

        // Veritabanına kaydet
        ContactMessage savedMessage = contactMessageRepository.save(message);
        logger.info("Mesaj başarıyla kaydedildi: ID {}", savedMessage.getId());

        // TODO: Yöneticiye e-posta bildirimi gönderilebilir

        return savedMessage; // Kaydedilen entity'yi döndür
    }

    @Override
    @Transactional // Dosya kaydetme ve potansiyel DB işlemi için
    public String processDesignSubmission(String fullName, String email, String phoneNumber, MultipartFile file) {
        logger.info("Yeni Tasarım Gönderimi Alındı:");
        logger.info("İsim Soyisim: {}, E-posta: {}, Telefon: {}", fullName, email, phoneNumber);

        if (file == null || file.isEmpty()) {
             logger.warn("Tasarım gönderimi alındı ancak dosya boş veya yok.");
             throw new RuntimeException("Tasarım dosyası yüklenmemiş."); // Hata fırlatmak daha iyi
        }

        logger.info("Dosya Adı: {}, Boyutu: {}, Tipi: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

        // TODO: Dosyayı güvenli bir yere kaydetme mantığı
        // String savedFilePath = saveFile(file); // Dosyayı kaydet ve yolunu al

        // TODO: İlgili bilgileri (belki dosya yolu ile birlikte) ayrı bir tabloya veya
        // ContactMessage tablosuna (tip alanı ekleyerek) kaydet.
        // DesignSubmission submission = new DesignSubmission(...);
        // designSubmissionRepository.save(submission);

        // TODO: Yöneticiye bildirim gönder

        // Şimdilik sadece başarı mesajı döndürelim
        return "Tasarımınız başarıyla alındı ve kaydedildi (placeholder).";
    }

    @Override
    @Transactional(readOnly = true) // Sadece okuma
    public List<ContactMessage> getAllMessages() {
        logger.debug("getAllMessages (Admin) çağrıldı.");
        // Tüm mesajları tarihe göre tersten sıralı getir
        return contactMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "receivedAt"));
    }

    // TODO: Dosya kaydetme için yardımcı metot
    // private String saveFile(MultipartFile file) throws IOException { ... }
}
