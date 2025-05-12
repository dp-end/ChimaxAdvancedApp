package com.chimax.chimax_backend.security; // Paket adını kontrol edin

import com.chimax.chimax_backend.entity.Role;
import com.chimax.chimax_backend.entity.User;
import com.chimax.chimax_backend.repository.UserRepository; // UserRepository import
import org.springframework.security.core.GrantedAuthority; // GrantedAuthority import
import org.springframework.security.core.authority.SimpleGrantedAuthority; // SimpleGrantedAuthority import
import org.springframework.security.core.userdetails.UserDetails; // UserDetails import
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService import
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Exception import
import org.springframework.stereotype.Service; // Service anotasyonu
import org.springframework.transaction.annotation.Transactional; // Transactional (okuma için)

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security için kullanıcı detaylarını veritabanından yükleyen servis.
 * UserDetailsService arayüzünü implemente eder.
 */
@Service // Bu sınıfın bir Spring Servisi olduğunu belirtir
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // UserRepository'yi constructor injection ile alıyoruz
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Kullanıcı adı (bizim durumumuzda email) ile kullanıcıyı veritabanından yükler.
     * Spring Security kimlik doğrulama sırasında bu metodu çağırır.
     *
     * @param usernameOrEmail Kullanıcının girdiği email adresi.
     * @return Spring Security'nin anlayacağı UserDetails nesnesi.
     * @throws UsernameNotFoundException Eğer verilen email ile kullanıcı bulunamazsa.
     */
    @Override
    @Transactional(readOnly = true) // Veritabanından okuma yapıldığı için
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // UserRepository kullanarak kullanıcıyı email ile bul
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Kullanıcı bulunamadı: " + usernameOrEmail));

        // Kullanıcının rollerini Spring Security'nin anlayacağı GrantedAuthority formatına çevir
        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(user.getRoles());

        // Spring Security'nin UserDetails nesnesini oluşturup döndür
        // Bu nesne email, şifrelenmiş parola ve yetkileri içerir.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Kullanıcı adı olarak email kullanılıyor
                user.getPassword(), // Veritabanındaki şifrelenmiş parola
                user.isEnabled(), // Kullanıcı aktif mi? (User entity'sindeki alana göre)
                true, // accountNonExpired - Şimdilik true
                true, // credentialsNonExpired - Şimdilik true
                true, // accountNonLocked - Şimdilik true
                authorities // Kullanıcının rolleri/yetkileri
        );
    }

    /**
     * User entity'sindeki Role setini GrantedAuthority koleksiyonuna dönüştürür.
     * @param roles Kullanıcının sahip olduğu Roller Set'i.
     * @return Spring Security'nin anlayacağı GrantedAuthority koleksiyonu.
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                // Her bir Role nesnesini SimpleGrantedAuthority nesnesine dönüştür
                // Rol adları genellikle "ROLE_" önekiyle kullanılır (Spring Security standardı)
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList()); // Liste olarak topla
    }
}
