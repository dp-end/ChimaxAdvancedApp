package com.chimax.chimax_backend.controller; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.JwtAuthResponseDto;
import com.chimax.chimax_backend.dto.LoginDto;
import com.chimax.chimax_backend.dto.RegisterDto; // Bu DTO artık registerAsSeller alanını içermeli
import com.chimax.chimax_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin; // CrossOrigin import edildi
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Collections;

/**
 * Kimlik doğrulama ve kayıt ile ilgili API endpoint'lerini yöneten Controller.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600) // Frontend'den erişim için CORS ayarı (geliştirme için *)
                                         // Production'da origins = "http://localhost:4200" gibi spesifik olmalı
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Yeni kullanıcı kaydı oluşturur.
     * RegisterDto artık 'registerAsSeller' alanını da içerebilir.
     * AuthService bu alanı işleyecektir.
     * POST /api/auth/register
     * @param registerDto İstek gövdesinden gelen ve valide edilen kayıt bilgileri.
     * @return Başarılı kayıt mesajını içeren bir JSON nesnesi (HTTP 201 Created) veya hata mesajı.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        try {
            logger.info("Register isteği alındı: {} (Satıcı olarak kaydol: {})",
                        registerDto.getEmail(),
                        registerDto.getRegisterAsSeller()); // Loglamaya eklendi
            String responseMessage = authService.register(registerDto);
            logger.info("Kullanıcı başarıyla kaydedildi: {}", registerDto.getEmail());

            Map<String, String> responseBody = Collections.singletonMap("message", responseMessage);
            return new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            logger.error("Kayıt hatası ({}): {}", registerDto.getEmail(), e.getMessage());
            Map<String, String> errorBody = Collections.singletonMap("error", e.getMessage());
            return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Beklenmedik kayıt hatası ({}): ", registerDto.getEmail(), e);
            Map<String, String> errorBody = Collections.singletonMap("error", "Kayıt sırasında beklenmedik bir hata oluştu.");
            return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Kullanıcı girişi yapar ve JWT döndürür.
     * POST /api/auth/login
     * @param loginDto Giriş bilgilerini içeren DTO.
     * @return JWT ve HTTP 200 (OK) veya hata mesajı.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        try {
            logger.info("Login isteği alındı: {}", loginDto.getEmail());
            JwtAuthResponseDto jwtAuthResponse = authService.login(loginDto);
            logger.info("Kullanıcı başarıyla giriş yaptı: {}", loginDto.getEmail());
            return ResponseEntity.ok(jwtAuthResponse);
        } catch (AuthenticationException e) {
            logger.warn("Login başarısız ({}): {}", loginDto.getEmail(), e.getMessage());
            Map<String, String> errorBody = Collections.singletonMap("error", "Giriş başarısız: Geçersiz e-posta veya şifre.");
            return new ResponseEntity<>(errorBody, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Beklenmedik login hatası ({}): ", loginDto.getEmail(), e);
            Map<String, String> errorBody = Collections.singletonMap("error", "Giriş sırasında beklenmedik bir hata oluştu.");
            return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
