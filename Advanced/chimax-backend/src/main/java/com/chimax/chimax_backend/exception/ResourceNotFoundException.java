package com.chimax.chimax_backend.exception; // Paket adınızı kendi projenize göre güncelleyin

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Belirli bir kaynak bulunamadığında fırlatılacak özel istisna sınıfı.
 * Bu istisna yakalandığında genellikle HTTP 404 Not Found yanıtı döndürülür.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Bu anotasyon, Spring MVC'nin bu istisnayı otomatik olarak 404'e map'lemesini sağlar
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Belirtilen detay mesajıyla yeni bir ResourceNotFoundException oluşturur.
     * @param message İstisnanın detay mesajı.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Belirtilen detay mesajı ve kök neden (cause) ile yeni bir ResourceNotFoundException oluşturur.
     * @param message İstisnanın detay mesajı.
     * @param cause İstisnanın kök nedeni (başka bir istisna).
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
