    package com.chimax.chimax_backend.repository; // Paket adınızı kontrol edin

    import com.chimax.chimax_backend.entity.Category;
    import org.springframework.data.jpa.repository.JpaRepository;
    import java.util.Optional;

    public interface CategoryRepository extends JpaRepository<Category, Long> {
        Optional<Category> findByNameIgnoreCase(String name);
        boolean existsByNameIgnoreCase(String name);
    }
    