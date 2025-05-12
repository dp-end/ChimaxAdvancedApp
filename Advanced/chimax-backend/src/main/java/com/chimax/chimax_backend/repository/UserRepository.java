package com.chimax.chimax_backend.repository; // Paket adını kontrol et

import com.chimax.chimax_backend.entity.User; // User entity'sini import et
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // @Repository anotasyonu eklendi
import java.util.Optional;

/**
 * User entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 * JpaRepository<EntityType, PrimaryKeyType>
 */
@Repository // Spring Data JPA repository'lerini belirtmek için iyi bir pratiktir.
public interface UserRepository extends JpaRepository<User, Long> { // Entity: User, Primary Key Tipi: Long

    /**
     * Kullanıcıyı e-posta adresine göre bulan metot.
     * Spring Security genellikle kullanıcıyı username (bizim durumumuzda email) ile bulur.
     * @param email Aranacak e-posta adresi.
     * @return Bulunan kullanıcıyı içeren Optional nesnesi veya boş Optional.
     */
    Optional<User> findByEmail(String email);

    /**
     * Belirli bir e-posta adresinin veritabanında zaten var olup olmadığını kontrol eden metot.
     * @param email Kontrol edilecek e-posta adresi.
     * @return E-posta varsa true, yoksa false döner.
     */
    Boolean existsByEmail(String email);

    // save(), findById(), findAll(), deleteById() vb. metotlar JpaRepository'den gelir.
    // Eğer kullanıcı adı (username) ile de arama yapmanız gerekiyorsa, benzer bir metot ekleyebilirsiniz:
    // Optional<User> findByUsername(String username);
    // Boolean existsByUsername(String username);
}
