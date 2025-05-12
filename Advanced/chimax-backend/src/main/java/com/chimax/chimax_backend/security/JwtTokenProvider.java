package com.chimax.chimax_backend.security; // Paket adını kontrol edin

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // GrantedAuthority import edildi
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List; // List import edildi
import java.util.stream.Collectors; // Collectors import edildi

/**
 * JWT (JSON Web Token) oluşturma, doğrulama ve ayrıştırma işlemlerini yöneten yardımcı sınıf.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        // ... (init metodu aynı) ...
         if (jwtSecretString == null || jwtSecretString.length() < 32) {
             logger.warn("JWT secret key is not configured properly or too short in application.properties. Using a default insecure key.");
             jwtSecretString = "VmVyeVNlY3JldEtleUZvckpXVEF1dGhlbnRpY2F0aW9uQW5kQXV0aG9yaXphdGlvblNhbXBsZTEyMw=="; // MUTLAKA DEĞİŞTİRİLMELİ
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("JWT Secret Key initialized successfully.");
        } catch (IllegalArgumentException e) {
             logger.error("Invalid Base64 encoded JWT secret key in application.properties!", e);
             byte[] keyBytes = Decoders.BASE64.decode("DefaultInsecureSecretKeyForDevelopmentPurposeOnlyMustBeChanged");
             this.key = Keys.hmacShaKeyFor(keyBytes);
             logger.warn("Using default insecure JWT key due to configuration error.");
        }
    }

    /**
     * Verilen Authentication nesnesinden bir JWT oluşturur.
     * Artık rolleri de token'a ekler.
     * @param authentication Kullanıcının kimlik doğrulama bilgileri.
     * @return Oluşturulan JWT String'i.
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // === ROLLERİ ALMA VE EKLEME ===
        // Authentication nesnesinden yetkileri (rolleri) al
        List<String> roles = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority) // Her GrantedAuthority'den rol adını (String) al
                                .collect(Collectors.toList()); // Bunları bir liste yap
        // ==============================

        // Token'ı oluştururken rolleri "roles" adında bir claim olarak ekle
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("roles", roles) // <-- ROLLER EKLENDİ
                .signWith(key) // Algoritma anahtardan anlaşılır
                .compact();
    }

    /**
     * Verilen JWT'den kullanıcı adını (subject) çıkarır.
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

     /**
     * Verilen JWT'den rolleri çıkarır.
     * Bu metot artık JwtAuthenticationFilter tarafından kullanılabilir.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // "roles" claim'ini List<String> olarak al
            // Eğer claim yoksa veya tipi yanlışsa null dönebilir veya hata verebilir.
            Object rolesClaim = claims.get("roles");
            if (rolesClaim instanceof List) {
                 // Güvenli cast işlemi
                 return (List<String>) rolesClaim;
            }
             logger.warn("JWT does not contain 'roles' claim or it's not a List.");
             return null; // Veya Collections.emptyList();
        } catch (Exception e) {
            logger.error("Could not get roles from JWT: {}", e.getMessage());
            return null;
        }
    }


    /**
     * Verilen JWT'nin geçerli olup olmadığını kontrol eder.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException ex) { logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) { logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) { logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) { logger.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (SignatureException ex) { logger.error("JWT signature does not match: {}", ex.getMessage());
        }
        return false;
    }
}
