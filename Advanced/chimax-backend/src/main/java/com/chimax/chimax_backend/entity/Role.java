package com.chimax.chimax_backend.entity; // Paket adını kendi yapınıza göre düzeltin

import jakarta.persistence.*; // JPA (Java Persistence API) anotasyonları
import lombok.Data; // Lombok: Getter, Setter, ToString, EqualsAndHashCode, RequiredArgsConstructor
import lombok.NoArgsConstructor; // Lombok: Parametresiz constructor
import lombok.AllArgsConstructor; // Lombok: Tüm alanları içeren constructor

/**
 * Kullanıcı rollerini (ADMIN, USER, SELLER vb.) temsil eden veritabanı varlığı (entity).
 */
@Entity // Bu sınıfın bir JPA entity'si olduğunu belirtir.
@Table(name = "roles") // Veritabanında eşleşeceği tablonun adı.
@Data // Lombok: Otomatik olarak getter, setter, toString, equals, hashCode oluşturur.
@NoArgsConstructor // Lombok: JPA için gerekli olan boş constructor'ı oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren constructor'ı oluşturur.
public class Role {

    /**
     * Rolün benzersiz kimliği (Primary Key).
     * GenerationType.IDENTITY: ID'nin veritabanı tarafından otomatik artırılacağını belirtir (MySQL için AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Rol ID'si için Integer veya Long kullanılabilir.

    /**
     * Rolün adı (Örn: "ROLE_USER", "ROLE_ADMIN", "ROLE_SELLER").
     * nullable = false: Bu alan boş olamaz.
     * unique = true: Bu alandaki değerler benzersiz olmalıdır.
     * length = 50: Veritabanındaki sütunun maksimum karakter uzunluğu.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // Not: User entity'si ile olan @ManyToMany ilişkisi genellikle User entity'si
    // tarafında `mappedBy` veya `@JoinTable` ile yönetilir. Bu Role entity'si
    // genellikle sadece rolün kendisini tanımlar.
}
