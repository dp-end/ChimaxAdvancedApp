package com.chimax.chimax_backend.repository; // Paket adını kontrol et
import com.chimax.chimax_backend.entity.Role; // Role entity'sini import et
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Optional import et

/**
 * Role entity'si için veritabanı işlemlerini yöneten repository arayüzü.
 * JpaRepository<EntityType, PrimaryKeyType>
 */
public interface RoleRepository extends JpaRepository<Role, Integer> { // Entity: Role, Primary Key Tipi: Integer

    /**
     * Rolü ismine göre bulan metot. Spring Data JPA, metot isminden sorguyu otomatik türetir.
     * Optional<Role>: Rol bulunamayabilir, bu yüzden Optional kullanmak iyi bir pratiktir.
     * @param name Aranacak rol adı (örn: "ROLE_ADMIN")
     * @return Bulunan rolü içeren Optional nesnesi veya boş Optional.
     */
    Optional<Role> findByName(String name);

    // JpaRepository sayesinde save(), findById(), findAll(), deleteById() gibi
    // temel CRUD metotları otomatik olarak kullanılabilir.
    // Özel sorgular gerekirse buraya eklenebilir (@Query anotasyonu ile veya metot isimlendirme kurallarıyla).
}
