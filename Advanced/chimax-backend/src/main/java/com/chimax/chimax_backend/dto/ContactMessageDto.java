package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * İletişim formundan gelen standart mesaj bilgilerini taşıyan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDto {

    @NotEmpty(message = "İsim boş olamaz")
    @Size(max = 100)
    private String name;

    @NotEmpty(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi girin")
    @Size(max = 100)
    private String email;

    @NotEmpty(message = "Mesaj boş olamaz")
    @Size(min = 10, max = 2000, message = "Mesaj 10 ile 2000 karakter arasında olmalıdır")
    private String message;
}
