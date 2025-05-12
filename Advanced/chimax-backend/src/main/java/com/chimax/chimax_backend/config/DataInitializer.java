package com.chimax.chimax_backend.config; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.Role;
import com.chimax.chimax_backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Uygulama başlangıcında gerekli verileri (örn: roller) oluşturan sınıf.
 */
@Component // Spring bu sınıfı otomatik olarak bulup çalıştıracak
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer çalışıyor..."); // Loglama

        // Kullanılacak rol isimleri
        String userRoleName = "ROLE_USER";
        String adminRoleName = "ROLE_ADMIN";
        String sellerRoleName = "ROLE_SELLER"; // Yeni satıcı rolü

        // Rolleri kontrol et ve eksikse ekle
        createRoleIfNotFound(userRoleName);
        createRoleIfNotFound(adminRoleName);
        createRoleIfNotFound(sellerRoleName); // Yeni satıcı rolünü ekle

        // İsteğe bağlı: Başlangıçta bir admin veya satıcı kullanıcısı oluşturulabilir
        // (UserRepository ve PasswordEncoder da enjekte edilmeli)
        // if (userRepository.findByEmail("admin@chimax.com").isEmpty()) { ... }
        // if (userRepository.findByEmail("seller@chimax.com").isEmpty()) { ... }
    }

    /**
     * Belirtilen isimde bir rol yoksa oluşturur.
     * @param roleName Oluşturulacak veya kontrol edilecek rolün adı.
     */
    private void createRoleIfNotFound(String roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            Role newRole = new Role(); // Lombok @NoArgsConstructor kullanılıyor
            newRole.setName(roleName); // Lombok @Data (setter) kullanılıyor
            roleRepository.save(newRole);
            System.out.println(roleName + " oluşturuldu.");
        } else {
            System.out.println(roleName + " zaten mevcut.");
        }
    }
}
