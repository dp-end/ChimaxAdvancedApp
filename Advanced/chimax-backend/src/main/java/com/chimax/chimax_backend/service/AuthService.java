package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.RegisterDto;
import com.chimax.chimax_backend.dto.LoginDto; // LoginDto import et
import com.chimax.chimax_backend.dto.JwtAuthResponseDto; // JwtAuthResponseDto import et

/**
 * Kimlik doğrulama ve kullanıcı kaydı işlemlerini tanımlayan arayüz.
 */
public interface AuthService {

    String register(RegisterDto registerDto);

    /**
     * Kullanıcı girişi yapar ve JWT döndürür.
     * @param loginDto Giriş bilgilerini içeren DTO.
     * @return JWT token ve token tipini içeren DTO.
     * @throws org.springframework.security.core.AuthenticationException Giriş başarısız olursa.
     */
    JwtAuthResponseDto login(LoginDto loginDto); // Dönüş tipi JwtAuthResponseDto oldu
}
