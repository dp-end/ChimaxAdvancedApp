package com.chimax.chimax_backend.dto; // Paket adınızı kendi projenize göre güncelleyin

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Kategori verilerini taşımak için kullanılan Veri Taşıma Nesnesi (DTO).
 * Genellikle API yanıtlarında Category entity'si yerine kullanılır.
 */
@Data // Otomatik olarak getter, setter, toString, equals ve hashCode metotlarını oluşturur
@NoArgsConstructor // Argümansız constructor oluşturur
@AllArgsConstructor // Tüm alanları içeren constructor oluşturur
public class CategoryDto {

    /**
     * Kategorinin benzersiz kimliği (ID).
     */
    private Long id;

    /**
     * Kategorinin adı.
     */
    private String name;

    /**
     * İsteğe bağlı: Kategori için bir açıklama.
     * Eğer Category entity'nizde description alanı varsa ve bunu DTO'da da göstermek isterseniz
     * bu alanı ve ilgili getter/setter'ı (Lombok @Data ile otomatik gelir) ekleyebilirsiniz.
     */
    // private String description;

    // İsteğe bağlı: Eğer alt kategoriler gibi ilişkiler varsa,
    // bunlar da DTO'da uygun bir şekilde temsil edilebilir (örneğin List<CategoryDto> children).
    // Ancak basit bir kategori listelemesi için genellikle id ve name yeterlidir.
}
