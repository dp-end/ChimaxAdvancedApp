package com.chimax.chimax_backend.security; // Paket adını kontrol edin

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Gelen isteklerdeki JWT'yi doğrulayan ve kullanıcıyı Security Context'e yerleştiren filtre.
 * Her istek için bir kez çalışır (OncePerRequestFilter).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
                                    throws ServletException, IOException {

        logger.debug("JwtAuthenticationFilter: İstek işleniyor: {}", request.getRequestURI()); // Gelen isteği logla

        try {
            // 1. İstekten JWT'yi al
            String jwt = getJwtFromRequest(request);
            logger.debug("JwtAuthenticationFilter: İstekten alınan JWT: {}", (jwt != null ? "Var" : "Yok")); // Token var mı logla

            // 2. Token var mı ve geçerli mi kontrol et
            if (StringUtils.hasText(jwt)) {
                boolean isValid = tokenProvider.validateToken(jwt);
                logger.debug("JwtAuthenticationFilter: Token doğrulama sonucu: {}", isValid); // Doğrulama sonucunu logla

                if (isValid) {
                    // 3. Token'dan kullanıcı adını (email) al
                    String username = tokenProvider.getUsernameFromJWT(jwt);
                    logger.debug("JwtAuthenticationFilter: Token'dan alınan kullanıcı adı: {}", username); // Kullanıcı adını logla

                    // 4. Kullanıcı detaylarını UserDetailsService üzerinden yükle
                    // SecurityContextHolder'da zaten authentication varsa tekrar yüklemeye gerek yok
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        logger.debug("JwtAuthenticationFilter: Kullanıcı detayları yükleniyor: {}", username);
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                        logger.debug("JwtAuthenticationFilter: Yüklenen UserDetails: {}", userDetails); // Yüklenen detayları logla

                        if (userDetails != null) {
                             // 5. Authentication nesnesi oluştur
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // Credentials (token doğrulandığı için null)
                                    userDetails.getAuthorities() // Yetkiler
                            );

                            // 6. İstek detaylarını Authentication nesnesine ekle
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // 7. Authentication nesnesini SecurityContextHolder'a yerleştir
                            logger.debug("JwtAuthenticationFilter: SecurityContext'e Authentication nesnesi yerleştiriliyor: {}", authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("JwtAuthenticationFilter: Kullanıcı '{}' başarıyla doğrulandı ve Security Context'e eklendi.", username); // Başarı logu
                        } else {
                             logger.warn("JwtAuthenticationFilter: UserDetails yüklenemedi: {}", username);
                        }
                    } else {
                        logger.debug("JwtAuthenticationFilter: Kullanıcı adı token'dan alınamadı veya Security Context zaten dolu.");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("JwtAuthenticationFilter: Kullanıcı kimlik doğrulaması güvenlik bağlamına yerleştirilemedi", ex);
        }

        // 8. Filtre zincirindeki bir sonraki filtreye devam et
        filterChain.doFilter(request, response);
        logger.debug("JwtAuthenticationFilter: İstek işleme tamamlandı: {}", request.getRequestURI()); // İşlem sonunu logla
    }

    /**
     * HttpServletRequest'in Authorization başlığından JWT'yi çıkarır.
     */
    private String getJwtFromRequest(@NonNull HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        logger.trace("JwtAuthenticationFilter: Authorization header bulunamadı veya 'Bearer ' ile başlamıyor."); // Trace seviyesinde log
        return null;
    }
}

