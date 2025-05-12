    package com.chimax.chimax_backend.entity; // Paket adınızı kontrol edin

    import jakarta.persistence.*;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;

    @Entity
    @Table(name = "categories")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Category {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true, length = 100)
        private String name;

        // İsteğe bağlı: Açıklama, üst kategori gibi alanlar eklenebilir
        // private String description;
        // @ManyToOne
        // @JoinColumn(name = "parent_category_id")
        // private Category parentCategory;
    }
    