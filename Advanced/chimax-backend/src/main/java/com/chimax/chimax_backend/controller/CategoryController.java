    package com.chimax.chimax_backend.controller; // Paket adınızı kontrol edin

    import com.chimax.chimax_backend.entity.Category;
    // Veya doğrudan CategoryRepository kullanabilirsiniz basit listeleme için
    import com.chimax.chimax_backend.repository.CategoryRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import java.util.List;

    @RestController
    @RequestMapping("/api/categories") // Kategoriler için base path
    public class CategoryController {

        // Basitlik adına doğrudan Repository kullanıyoruz, idealde bir CategoryService olurdu.
        private final CategoryRepository categoryRepository;

        @Autowired
        public CategoryController(CategoryRepository categoryRepository) {
            this.categoryRepository = categoryRepository;
        }

        @GetMapping
        public ResponseEntity<List<Category>> getAllCategories() {
            // İdealde CategoryResponseDto gibi bir DTO döndürmek daha iyi olabilir
            // ama şimdilik direkt Category entity'lerini döndürelim.
            // Jackson'ın Category entity'sini serileştirebildiğinden emin olun
            // (eğer içinde lazy-loaded ilişkiler yoksa sorun olmaz).
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.ok(categories);
        }
    }
    