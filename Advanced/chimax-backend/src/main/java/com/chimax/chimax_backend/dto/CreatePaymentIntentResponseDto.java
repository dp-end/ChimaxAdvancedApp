package com.chimax.chimax_backend.dto; // Paket adını kontrol et

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Backend'den frontend'e Stripe Payment Intent'in clientSecret'ını döndürmek için kullanılan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentResponseDto {

    /**
     * Frontend'in Stripe.js ile ödemeyi onaylamak için kullanacağı gizli anahtar.
     */
    private String clientSecret;

}
