package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Başarılı giriş sonrası frontend'e JWT token'ı döndürmek için kullanılan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponseDto {

    private String accessToken; // JWT token
    private String tokenType = "Bearer"; // Token tipi, genellikle Bearer
    // İsteğe bağlı: Kullanıcı rolleri veya diğer bilgiler eklenebilir
    // private List<String> roles;
}
