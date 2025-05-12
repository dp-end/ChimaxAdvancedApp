package com.chimax.chimax_backend.entity; // Paket adınızı kendi yapınıza göre düzeltin

import jakarta.persistence.*; // JPA (Java Persistence API) anotasyonları
import lombok.Data; // Lombok: Getter, Setter, ToString, EqualsAndHashCode
import lombok.NoArgsConstructor; // Lombok: Parametresiz constructor
import lombok.AllArgsConstructor; // Lombok: Tüm alanları içeren constructor

import java.util.HashSet;
import java.util.Set;

/**
 * Uygulama kullanıcılarını temsil eden veritabanı varlığı (entity).
 * Kullanıcılar birden fazla role sahip olabilir (örn: USER, ADMIN, SELLER).
 */
@Entity
@Table(name = "users", // Veritabanında 'users' tablosu
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email") // Email alanı benzersiz olmalı
       })
@Data // Lombok: Otomatik olarak getter, setter, toString, equals, hashCode vb. oluşturur.
@NoArgsConstructor // Lombok: JPA için gerekli olan boş constructor'ı oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren constructor'ı oluşturur.
public class User {

    /**
     * Kullanıcının benzersiz kimliği (Primary Key).
     * Otomatik artan bir değerdir.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Kullanıcının adı.
     * Boş olamaz.
     */
    @Column(nullable = false, length = 100)
    private String firstName;

    /**
     * Kullanıcının soyadı.
     * Boş olamaz.
     */
    @Column(nullable = false, length = 100)
    private String lastName;

    /**
     * Kullanıcının e-posta adresi. Genellikle kullanıcı adı olarak da kullanılır.
     * Benzersiz ve boş olamaz.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Kullanıcının şifrelenmiş parolası.
     * Asla düz metin olarak saklanmamalıdır. Spring Security BCryptPasswordEncoder kullanılmalıdır.
     * BCrypt genellikle 60 karakterlik bir hash üretir, bu yüzden length=60 veya biraz daha fazla olabilir.
     */
    @Column(nullable = false, length = 60)
    private String password;

    /**
     * Kullanıcının sahip olduğu roller. Role entity'si ile @ManyToMany ilişkisi kurulur.
     * FetchType.EAGER: User yüklendiğinde ilişkili roller de hemen veritabanından çekilir.
     * Rol sayısı azsa ve sık kullanılıyorsa EAGER performanslı olabilir.
     * CascadeType.PERSIST ve CascadeType.MERGE: User kaydedilirken/güncellenirken,
     * eğer User'a yeni bir Role eklenmişse ve bu Role veritabanında yoksa,
     * Role de kaydedilir/güncellenir. (Dikkatli kullanılmalıdır, genellikle roller önceden tanımlanır)
     * @JoinTable: ManyToMany ilişkisini yöneten ara tabloyu (user_roles) tanımlar.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles", // Ara tablonun adı
               joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), // Bu entity'nin (User) ara tablodaki FK sütunu
               inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")) // Diğer entity'nin (Role) ara tablodaki FK sütunu
    private Set<Role> roles = new HashSet<>(); // Set kullanmak aynı rolün tekrar eklenmesini engeller.

    /**
     * Kullanıcının hesabının aktif olup olmadığını belirtir.
     * Örneğin, bir kullanıcı banlandığında false yapılabilir.
     * columnDefinition = "BOOLEAN DEFAULT true": Veritabanında varsayılan değeri true olan bir boolean sütun oluşturur.
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean enabled = true;

    // İsteğe bağlı: Zaman damgaları eklenebilir (createdAt, updatedAt)
    // @CreationTimestamp
    // @Column(nullable = false, updatable = false)
    // private java.time.LocalDateTime createdAt;

    // @UpdateTimestamp
    // @Column(nullable = false)
    // private java.time.LocalDateTime updatedAt;
}
