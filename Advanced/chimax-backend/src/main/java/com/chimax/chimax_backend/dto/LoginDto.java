package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Kullanıcı girişi için frontend'den gelen verileri taşıyan nesne (DTO).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @NotEmpty(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi girin")
    private String email; // Veya username olabilir

    @NotEmpty(message = "Şifre boş olamaz")
    private String password;
}
