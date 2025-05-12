package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Kullanıcı bilgilerini frontend'e taşımak için kullanılan DTO.
 * Şifre gibi hassas bilgiler içermez.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles; // Rol isimlerini liste olarak gönderebiliriz
    private boolean enabled; // Kullanıcının aktif/pasif durumu
    // İsteğe bağlı: createdAt, updatedAt gibi ek bilgiler
}

