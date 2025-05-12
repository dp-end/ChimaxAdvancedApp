package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.JwtAuthResponseDto; // DTO import
import com.chimax.chimax_backend.dto.LoginDto; // DTO import
import com.chimax.chimax_backend.dto.RegisterDto;
import com.chimax.chimax_backend.entity.Role;
import com.chimax.chimax_backend.entity.User;
import com.chimax.chimax_backend.repository.RoleRepository;
import com.chimax.chimax_backend.repository.UserRepository;
import com.chimax.chimax_backend.security.JwtTokenProvider; // JwtTokenProvider import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; // AuthenticationManager import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Token import
import org.springframework.security.core.Authentication; // Authentication import
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContextHolder import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Kayıt için

import java.util.HashSet;
import java.util.Set;

/**
 * AuthService arayüzünün implementasyonu.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // AuthenticationManager eklendi
    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider eklendi

    @Autowired // Tek constructor olduğu için opsiyonel ama okunabilirlik için kalabilir
    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, // Constructor'a eklendi
                           JwtTokenProvider jwtTokenProvider) { // Constructor'a eklendi
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional // Kayıt işlemi transactional olmalı
    public String register(RegisterDto registerDto) {
        // E-posta adresinin zaten kullanılıp kullanılmadığını kontrol et
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Hata: Bu e-posta adresi zaten kullanılıyor!");
            // Daha iyi bir hata yönetimi için özel bir exception sınıfı (örn: EmailAlreadyExistsException)
            // ve global bir exception handler kullanılabilir.
        }

        // Yeni User nesnesi oluştur
        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEnabled(true); // Kullanıcıyı varsayılan olarak aktif yap (e-posta onayı isteniyorsa false olabilir)

        // Kullanıcı rollerini ayarla
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Hata: ROLE_USER rolü bulunamadı! Lütfen DataInitializer'ı kontrol edin."));
        roles.add(userRole);

        // YENİ: Satıcı olarak kaydolma isteğini kontrol et
        if (Boolean.TRUE.equals(registerDto.getRegisterAsSeller())) {
            Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                    .orElseThrow(() -> new RuntimeException("Hata: ROLE_SELLER rolü bulunamadı! Lütfen DataInitializer'ı kontrol edin."));
            roles.add(sellerRole);
            System.out.println("Kullanıcı " + registerDto.getEmail() + " için ROLE_SELLER atandı.");
        }

        user.setRoles(roles);
        userRepository.save(user);

        return "Kullanıcı başarıyla kaydedildi!";
    }

    /**
     * Kullanıcı girişi yapar, kimlik doğrular ve JWT oluşturur.
     * Bu metot, AuthService arayüzündeki tanımı implemente eder.
     */
    @Override // Arayüzdeki metodu implemente ettiğimizi belirtir
    public JwtAuthResponseDto login(LoginDto loginDto) {
        // 1. AuthenticationManager ile kimlik doğrulama yap
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(), // Kullanıcı adı olarak email
                        loginDto.getPassword() // Şifre
                )
        );

        // 2. Kimlik doğrulama başarılıysa, SecurityContext'e Authentication nesnesini yerleştir
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. JwtTokenProvider ile JWT oluştur
        String token = jwtTokenProvider.generateToken(authentication);

        // 4. Yanıt DTO'sunu oluştur ve döndür
        JwtAuthResponseDto response = new JwtAuthResponseDto();
        response.setAccessToken(token);
        // response.setTokenType("Bearer"); // JwtAuthResponseDto'da varsayılan olarak ayarlı olabilir

        return response;
    }
}
