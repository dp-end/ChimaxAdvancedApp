package com.chimax.chimax_backend.config; // Paket adınızı kontrol edin

import com.chimax.chimax_backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize gibi metot seviyesi güvenlik için gereklidir
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(withDefaults()); // CORS yapılandırmasını etkinleştir
        http.csrf(csrf -> csrf.disable()); // CSRF korumasını devre dışı bırak (stateless API'ler için yaygın)

        // HTTP istekleri için yetkilendirme kuralları
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Kayıt ve Login herkese açık
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll() // Ürün okuma herkese açık
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll() // Kategori okuma herkese açık
                
                // YENİ EKLENEN KURAL: /assets/ altında olabilecek statik kaynaklara (resimler vb.) GET izni
                .requestMatchers(HttpMethod.GET, "/assets/**").permitAll() 
                // Eğer ürün resimleriniz /product-images/ gibi farklı bir yoldaysa, onu da ekleyin:
                // .requestMatchers(HttpMethod.GET, "/product-images/**").permitAll()

                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger UI herkese açık
                
                // Satıcıya özel endpoint'ler için 'SELLER' rolü gereksinimi
                .requestMatchers("/api/seller/**").hasRole("SELLER") 
                // Not: Eğer rolleriniz veritabanında "ROLE_SELLER" gibi saklanıyorsa,
                // .hasAuthority("ROLE_SELLER") kullanmak daha doğru olabilir.
                // hasRole("SELLER") otomatik olarak "ROLE_SELLER" otoritesini arar.

                // Admin'e özel endpoint'ler için 'ADMIN' rolü gereksinimi
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Aynı şekilde, roller "ROLE_ADMIN" ise .hasAuthority("ROLE_ADMIN") kullanılabilir.

                .anyRequest().authenticated() // Yukarıdaki kurallarla eşleşmeyen diğer tüm istekler kimlik doğrulama gerektirir
            );

        // Session yönetimini STATELESS yap (JWT kullandığımız için session oluşturulmaz)
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // Oluşturduğumuz JWTAuthenticationFilter'ı Spring Security filtre zincirine ekle
        // Bu filtre, UsernamePasswordAuthenticationFilter'dan önce çalışacak
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Spring Security'nin varsayılan form login, logout ve http basic authentication'ını devre dışı bırak
        http.formLogin(form -> form.disable());
        http.logout(logout -> logout.disable());
        http.httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * CORS (Cross-Origin Resource Sharing) yapılandırması.
     * Frontend uygulamasının (örn: http://localhost:4200) backend API'sine
     * farklı bir origin'den istek yapmasına izin verir.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // İzin verilen origin (Angular uygulamanızın adresi)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // İzin verilen HTTP metotları
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        // İzin verilen HTTP başlıkları (Authorization başlığı JWT için önemlidir)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        // Tarayıcının kimlik bilgileriyle (cookie, Authorization header) istek göndermesine izin ver
        configuration.setAllowCredentials(true);
        // İsteğe bağlı: Yanıtta istemciye gönderilmesine izin verilen başlıklar
        // configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Bu CORS yapılandırmasını tüm yollar için uygula (örn: /api/**, /assets/**)
        source.registerCorsConfiguration("/**", configuration); // GÜNCELLENDİ: "/api/**" yerine "/**"
        return source;
    }
}
