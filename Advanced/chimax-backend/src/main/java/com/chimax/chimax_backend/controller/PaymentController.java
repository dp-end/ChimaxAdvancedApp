package com.chimax.chimax_backend.controller; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.CreatePaymentIntentRequestDto;
import com.chimax.chimax_backend.dto.CreatePaymentIntentResponseDto;
import com.chimax.chimax_backend.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Authentication import
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContextHolder import
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * Ödeme işlemleriyle ilgili API endpoint'lerini yöneten Controller.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    /**
     * Yeni bir Stripe Payment Intent oluşturur. (Sadece Giriş Yapmış Kullanıcılar)
     */
    @PostMapping("/create-intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequestDto paymentIntentRequest) {

        // === DEBUG LOGLARI EKLENDİ ===
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("PaymentController: /create-intent isteği alındı.");
        if (authentication != null) {
             logger.info("PaymentController: Mevcut Authentication: Principal={}, Authenticated={}, Authorities={}",
                 authentication.getPrincipal(), authentication.isAuthenticated(), authentication.getAuthorities());
        } else {
             logger.warn("PaymentController: SecurityContextHolder'da Authentication nesnesi bulunamadı!");
        }
        // ============================

        try {
            logger.debug("PaymentController: İstek gövdesi: Tutar={}, Para Birimi={}",
                        paymentIntentRequest.getAmount(), paymentIntentRequest.getCurrency());

            if (paymentIntentRequest.getAmount() == null || paymentIntentRequest.getAmount() <= 0) {
                 logger.warn("PaymentController: Geçersiz tutar: {}", paymentIntentRequest.getAmount());
                 return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Geçersiz tutar."));
            }
            if (paymentIntentRequest.getCurrency() == null || paymentIntentRequest.getCurrency().isBlank()) {
                 logger.warn("PaymentController: Para birimi belirtilmedi.");
                 return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Para birimi belirtilmelidir."));
            }

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                paymentIntentRequest.getAmount(),
                paymentIntentRequest.getCurrency()
            );

            CreatePaymentIntentResponseDto response = new CreatePaymentIntentResponseDto(paymentIntent.getClientSecret());
            logger.info("PaymentController: Payment Intent başarıyla oluşturuldu. ID: {}", paymentIntent.getId());
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("PaymentController: Stripe Payment Intent oluşturulurken hata: {}", e.getMessage(), e); // Hatanın tamamını logla
            Map<String, String> errorBody = Collections.singletonMap("error", "Ödeme başlatılamadı: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        } catch (IllegalStateException e) {
             logger.error("PaymentController: Stripe yapılandırma hatası: {}", e.getMessage(), e);
             Map<String, String> errorBody = Collections.singletonMap("error", e.getMessage());
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
         catch (Exception e) {
            logger.error("PaymentController: Payment Intent oluşturulurken beklenmedik hata", e);
             Map<String, String> errorBody = Collections.singletonMap("error", "Ödeme başlatılırken beklenmedik bir hata oluştu.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}
