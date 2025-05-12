package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.stripe.exception.StripeException; // StripeException import
import com.stripe.model.PaymentIntent; // PaymentIntent import

/**
 * Stripe ödeme işlemleriyle ilgili iş mantığını tanımlayan arayüz.
 */
public interface StripeService {

    /**
     * Verilen tutar ve para birimi için bir Stripe Payment Intent oluşturur.
     * @param amount Tutar (kuruş veya en küçük para birimi cinsinden).
     * @param currency Para birimi kodu (örn: "try").
     * @return Oluşturulan PaymentIntent nesnesi.
     * @throws StripeException Stripe API hatası olursa.
     */
    PaymentIntent createPaymentIntent(Long amount, String currency) throws StripeException;

    /**
     * Bir Payment Intent'in başarılı olup olmadığını ve tutarının doğru olup olmadığını kontrol eder.
     * @param paymentIntentId Kontrol edilecek Payment Intent ID'si.
     * @param expectedAmount Beklenen tutar (kuruş cinsinden).
     * @return Ödeme başarılı ve tutar doğruysa true, aksi halde false.
     * @throws StripeException Stripe API hatası olursa.
     */
    boolean verifyPayment(String paymentIntentId, Long expectedAmount) throws StripeException; // İade için de kullanılabilir

    /**
     * Belirli bir ödemeyi iade eder.
     * @param paymentIntentId İade edilecek ödemenin Payment Intent ID'si.
     * @return İade başarılıysa true, aksi halde false.
     * @throws StripeException Stripe API hatası olursa.
     */
    boolean refundPayment(String paymentIntentId) throws StripeException; // Sipariş iptali için

}
