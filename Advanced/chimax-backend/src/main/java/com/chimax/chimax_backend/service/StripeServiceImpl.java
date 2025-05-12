package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeServiceImpl implements StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeServiceImpl.class);

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || !secretKey.startsWith("sk_")) {
             logger.error("Stripe Secret Key is missing or invalid in application.properties! Payment processing will fail.");
             // Uygulamanın başlamasını engellemek yerine sadece loglamak daha iyi olabilir
             // veya burada bir IllegalStateException fırlatılabilir.
             // throw new IllegalStateException("Stripe Secret Key is not configured properly.");
        } else {
             Stripe.apiKey = secretKey;
             logger.info("Stripe API Key initialized successfully.");
        }
    }

    @Override
    public PaymentIntent createPaymentIntent(Long amount, String currency) throws StripeException {
        // API anahtarının init() sırasında ayarlandığını varsayıyoruz.
        // Eğer init() başarısız olduysa, Stripe.apiKey null olabilir ve Stripe.create çağrısı hata verecektir.
        // Veya burada ek bir kontrol yapabiliriz:
        if (Stripe.apiKey == null) {
             logger.error("Stripe API Key is not configured. Cannot create Payment Intent.");
             // StripeException yerine standart bir exception fırlat
             throw new IllegalStateException("Stripe API Anahtarı yapılandırılmamış. Ödeme işlemi başlatılamıyor.");
             // Veya null döndürülüp controller'da ele alınabilir, ancak exception daha iyi.
        }

        PaymentIntentCreateParams params =
            PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency.toLowerCase())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();

        logger.info("Stripe Payment Intent oluşturuluyor: Tutar={}, Para Birimi={}", amount, currency);
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        logger.info("Stripe Payment Intent başarıyla oluşturuldu. ID: {}", paymentIntent.getId());
        return paymentIntent;
    }

    @Override
    public boolean verifyPayment(String paymentIntentId, Long expectedAmount) throws StripeException {
         if (Stripe.apiKey == null) {
             logger.error("Stripe API Key not configured. Cannot verify payment.");
             return false;
         }
         try {
            logger.info("Payment Intent doğrulanıyor: {}", paymentIntentId);
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            boolean succeeded = "succeeded".equals(paymentIntent.getStatus());
            boolean amountMatches = expectedAmount.equals(paymentIntent.getAmount());

            if (succeeded && amountMatches) {
                logger.info("Ödeme başarıyla doğrulandı: Payment Intent ID {}", paymentIntentId);
                return true;
            } else {
                logger.warn("Ödeme doğrulaması başarısız: Payment Intent ID {}. Durum: {}, Tutar Eşleşiyor: {}",
                            paymentIntentId, paymentIntent.getStatus(), amountMatches);
                return false;
            }
        } catch (StripeException e) {
            logger.error("Doğrulama için Payment Intent {} alınırken hata: {}", paymentIntentId, e.getMessage());
            throw e;
        }
    }


    @Override
    public boolean refundPayment(String paymentIntentId) throws StripeException {
         if (Stripe.apiKey == null) {
             logger.error("Stripe API Key not configured. Cannot process refund.");
             return false;
         }
         try {
             logger.info("Payment Intent için iade başlatılıyor: {}", paymentIntentId);
             RefundCreateParams params = RefundCreateParams.builder()
                 .setPaymentIntent(paymentIntentId)
                 .build();

             Refund refund = Refund.create(params);
             logger.info("Payment Intent için iade işlendi: {}. İade ID: {}, Durum: {}",
                         paymentIntentId, refund.getId(), refund.getStatus());
             return "succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus());
         } catch (StripeException e) {
             logger.error("Payment Intent {} için iade işlenirken hata: {}", paymentIntentId, e.getMessage());
             throw e;
         }
    }
}
