package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Kullanıcı kaydı için frontend'den gelen verileri taşıyan nesne (Data Transfer Object).
 * Artık satıcı olarak kaydolma seçeneğini de içerir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    @NotEmpty(message = "İsim boş olamaz")
    @Size(min = 2, max = 100, message = "İsim en az 2, en fazla 100 karakter olmalıdır") // max eklendi
    private String firstName;

    @NotEmpty(message = "Soyisim boş olamaz")
    @Size(min = 2, max = 100, message = "Soyisim en az 2, en fazla 100 karakter olmalıdır") // max eklendi
    private String lastName;

    @NotEmpty(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi girin")
    @Size(max = 100, message = "E-posta en fazla 100 karakter olabilir") // max eklendi
    private String email;

    @NotEmpty(message = "Şifre boş olamaz")
    @Size(min = 6, max = 100, message = "Şifre en az 6, en fazla 100 karakter olmalıdır") // max eklendi (şifre hash'lenmeden önceki uzunluk)
    private String password;

    /**
     * Kullanıcının satıcı olarak da kaydolmak isteyip istemediğini belirtir.
     * Frontend'den bu alan true olarak gelirse, kullanıcıya hem ROLE_USER hem de ROLE_SELLER atanır.
     * Eğer frontend'den bu alan gönderilmezse (null gelirse) veya false gelirse,
     * sadece ROLE_USER atanır.
     * Bu alan için özel bir validasyon gerekmez, null veya false olması varsayılan davranıştır.
     */
    private Boolean registerAsSeller; // Yeni alan eklendi

    // İsteğe bağlı: Frontend'den rol seçimi gelmeyecekse bu alana gerek yok.
    // private Set<String> roles;
}
