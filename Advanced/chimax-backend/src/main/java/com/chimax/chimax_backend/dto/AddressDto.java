package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Teslimat adresi bilgilerini taşıyan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotEmpty(message = "Ad Soyad boş olamaz")
    private String fullName;

    @NotEmpty(message = "Adres satırı boş olamaz")
    private String addressLine1;

    // private String addressLine2; // Opsiyonel

    @NotEmpty(message = "Şehir boş olamaz")
    private String city;

    @NotEmpty(message = "Posta kodu boş olamaz")
    @Pattern(regexp = "^[0-9]{5}$", message = "Geçerli bir posta kodu girin (5 hane)")
    private String postalCode;

    // Ülke frontend'den gelmeyebilir, varsayılan veya sabit olabilir
    private String country = "Türkiye";

    @NotEmpty(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "^[0-9\\s\\-\\+]{10,}$", message = "Geçerli bir telefon numarası girin")
    private String phone;
}
