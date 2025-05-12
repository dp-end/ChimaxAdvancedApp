package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Yeni bir ürün yorumu oluşturma isteği için frontend'den gelen verileri taşıyan DTO.
 * Sadece gerekli alanları içerir (rating, comment).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDto {

    @NotNull(message = "Puan boş olamaz")
    @Min(value = 1, message = "Puan en az 1 olmalıdır")
    @Max(value = 5, message = "Puan en fazla 5 olabilir")
    private Integer rating; // Puan (1-5)

    @NotEmpty(message = "Yorum metni boş olamaz")
    @Size(min = 10, max = 1000, message = "Yorum 10 ile 1000 karakter arasında olmalıdır")
    private String comment; // Yorum metni
}




