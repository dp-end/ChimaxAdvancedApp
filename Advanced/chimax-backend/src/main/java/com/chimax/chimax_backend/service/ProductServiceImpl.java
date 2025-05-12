package com.chimax.chimax_backend.service;

import com.chimax.chimax_backend.dto.ProductDto; // İstekler için kullanılan DTO
import com.chimax.chimax_backend.dto.ProductResponseDto; // Yanıtlar için yeni DTO
import com.chimax.chimax_backend.dto.SellerInfoResponseDto; // Satıcı bilgileri için DTO
import com.chimax.chimax_backend.entity.Category; // Category entity'si
import com.chimax.chimax_backend.entity.Product;
import com.chimax.chimax_backend.entity.User;
import com.chimax.chimax_backend.exception.FileStorageException;
import com.chimax.chimax_backend.exception.ResourceNotFoundException; // Kategori bulunamazsa fırlatılacak istisna
import com.chimax.chimax_backend.repository.CategoryRepository; // CategoryRepository
import com.chimax.chimax_backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final CategoryRepository categoryRepository; // CategoryRepository enjekte edildi
    private static final String PRODUCT_IMAGE_SUBDIRECTORY = "product-images";

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              FileStorageService fileStorageService,
                              CategoryRepository categoryRepository) { // Constructor'a CategoryRepository eklendi
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
        this.categoryRepository = categoryRepository;
    }

    // --- Entity'den DTO'ya Dönüşüm Yardımcı Metodu ---
    private ProductResponseDto convertToProductResponseDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice()); // Product entity'sinde BigDecimal olduğunu varsayıyoruz
        dto.setImageUrl(product.getImageUrl());
        
        if (product.getCategory() != null) {
            dto.setCategory(product.getCategory().getName());
        } else {
            dto.setCategory(null); // Kategori yoksa null ata
        }
        
        dto.setType(product.getType());
        dto.setActive(product.isActive()); // Aktiflik durumu DTO'ya eklendi
        dto.setStockQuantity(product.getStockQuantity()); // Stok miktarı DTO'ya eklendi

        User sellerEntity = product.getSeller(); // LAZY fetch için transaction içinde olmalı
        if (sellerEntity != null) {
            SellerInfoResponseDto sellerDto = new SellerInfoResponseDto(
                sellerEntity.getId(),
                sellerEntity.getFirstName(),
                sellerEntity.getLastName()
            );
            dto.setSeller(sellerDto);
        }
        return dto;
    }

    // --- Genel Kullanıcı ve Admin için Metotlar (DTO Yanıtları ile) ---
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAllActiveProducts() {
        logger.debug("Tüm aktif ürünler (DTO olarak) getiriliyor...");
        return productRepository.findAll().stream()
                .filter(Product::isActive) // Sadece aktif olanları filtrele
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDto> findProductById(Long id) {
        logger.debug("Ürün ID {} (DTO olarak) için arama yapılıyor...", id);
        return productRepository.findById(id)
                // Detay sayfasında pasif ürün de gösterilebilir, bu yüzden filter(Product::isActive) kaldırıldı.
                // Eğer sadece aktif ürün detayı isteniyorsa .filter(Product::isActive) eklenebilir.
                .map(this::convertToProductResponseDto);
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductDto productDto) { // Admin için
        logger.info("Admin createProduct (DTO) çağrıldı. Ürün Adı: {}, Kategori Adı (DTO'dan): {}",
                     productDto.getName(), productDto.getCategory());

        String categoryName = productDto.getCategory();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            logger.error("Admin createProduct: Kategori adı DTO'da boş veya null.");
            throw new IllegalArgumentException("Kategori adı boş olamaz.");
        }
        Category categoryEntity = categoryRepository.findByNameIgnoreCase(categoryName)
            .orElseThrow(() -> {
                logger.warn("Admin createProduct: Kategori '{}' bulunamadı.", categoryName);
                return new ResourceNotFoundException("Belirtilen kategori '" + categoryName + "' bulunamadı.");
            });
        logger.info("Admin createProduct: Kategori '{}' (ID: {}) bulundu.", categoryEntity.getName(), categoryEntity.getId());

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(categoryEntity);
        product.setType(productDto.getType());
        product.setImageUrl(productDto.getImageUrl());
        product.setStockQuantity(productDto.getStockQuantity() != null ? productDto.getStockQuantity() : 0);
        product.setActive(productDto.getActive() != null ? productDto.getActive() : true);
        // Admin ürün oluştururken satıcı null olabilir.
        // product.setSeller(null); 

        Product savedProduct = productRepository.save(product);
        logger.info("Admin tarafından yeni ürün (ID: {}) oluşturuldu. Kategori: {}, Aktif: {}",
                     savedProduct.getId(), (savedProduct.getCategory() != null ? savedProduct.getCategory().getName() : "N/A"), savedProduct.isActive());
        return convertToProductResponseDto(savedProduct);
    }

    @Override
    @Transactional
    public Optional<ProductResponseDto> updateProduct(Long id, ProductDto productDto) { // Admin için
        logger.info("Admin ürün (ID: {}) güncelleme isteği (DTO). Kategori Adı (DTO'dan): {}", id, productDto.getCategory());

        String categoryName = productDto.getCategory();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            logger.error("Admin updateProduct: Kategori adı DTO'da boş veya null. Ürün ID: {}", id);
            throw new IllegalArgumentException("Kategori adı boş olamaz.");
        }
        Category categoryEntity = categoryRepository.findByNameIgnoreCase(categoryName)
            .orElseThrow(() -> {
                logger.warn("Admin updateProduct: Kategori '{}' bulunamadı. Ürün ID: {}", categoryName, id);
                return new ResourceNotFoundException("Belirtilen kategori '" + categoryName + "' bulunamadı.");
            });
        logger.info("Admin updateProduct: Kategori '{}' (ID: {}) bulundu. Ürün ID: {}", categoryEntity.getName(), categoryEntity.getId(), id);

        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDto.getName());
                    existingProduct.setDescription(productDto.getDescription());
                    existingProduct.setPrice(productDto.getPrice());
                    existingProduct.setCategory(categoryEntity);
                    existingProduct.setType(productDto.getType());
                    existingProduct.setImageUrl(productDto.getImageUrl());
                    existingProduct.setStockQuantity(productDto.getStockQuantity() != null ? productDto.getStockQuantity() : existingProduct.getStockQuantity());
                    existingProduct.setActive(productDto.getActive() != null ? productDto.getActive() : existingProduct.isActive());
                                 
                    Product updatedProduct = productRepository.save(existingProduct);
                    logger.info("Admin tarafından ürün (ID: {}) güncellendi. Kategori: {}, Aktif: {}",
                                 updatedProduct.getId(), (updatedProduct.getCategory() != null ? updatedProduct.getCategory().getName() : "N/A"), updatedProduct.isActive());
                    return convertToProductResponseDto(updatedProduct);
                });
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id) { // Admin için - SOFT DELETE UYGULANDI
        logger.info("Admin ürün (ID: {}) için soft delete (pasif yapma) isteği.", id);
        Product productToDelete = productRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Admin soft delete: Silinecek ürün (ID: {}) bulunamadı.", id);
                return new ResourceNotFoundException("Silinecek ürün (ID: " + id + ") bulunamadı.");
            });
        
        productToDelete.setActive(false); // Ürünü pasif yap
        productRepository.save(productToDelete);
        logger.info("Admin tarafından ürün (ID: {}) başarıyla pasif yapıldı.", id);
        // Resim dosyası bu durumda silinmez.
    }

    // --- Satıcıya Özel Yeni Metotlar (DTO Yanıtları ve Dosya Yükleme Entegrasyonu ile) ---
    @Override
    @Transactional
    public ProductResponseDto createProductForSeller(ProductDto productDto, User seller, MultipartFile imageFile) {
        if (seller == null || seller.getId() == null) {
            logger.error("createProductForSeller çağrıldı ancak satıcı veya satıcı ID'si null.");
            throw new IllegalArgumentException("Ürün oluşturmak için geçerli satıcı bilgisi gereklidir.");
        }
        logger.info("createProductForSeller (DTO) çağrıldı. Satıcı ID: {}, Ürün Adı: {}, Kategori Adı (DTO'dan): {}", 
                     seller.getId(), productDto.getName(), productDto.getCategory());

        String categoryName = productDto.getCategory();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            logger.error("Satıcı createProduct: Kategori adı DTO'da boş veya null. Satıcı ID: {}", seller.getId());
            throw new IllegalArgumentException("Kategori adı boş olamaz.");
        }
        Category categoryEntity = categoryRepository.findByNameIgnoreCase(categoryName)
            .orElseThrow(() -> {
                logger.warn("Satıcı createProduct: Kategori '{}' bulunamadı. Satıcı ID: {}", categoryName, seller.getId());
                return new ResourceNotFoundException(
                    "Belirtilen kategori '" + categoryName + "' bulunamadı. " +
                    "Lütfen geçerli bir kategori seçin veya yeni bir kategori oluşturulması için yöneticiyle iletişime geçin."
                );
            });
        logger.info("Satıcı createProduct: Kategori '{}' (ID: {}) bulundu. Satıcı ID: {}", categoryEntity.getName(), categoryEntity.getId(), seller.getId());

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(categoryEntity);
        product.setType(productDto.getType());
        product.setStockQuantity(productDto.getStockQuantity() != null ? productDto.getStockQuantity() : 0);
        product.setActive(productDto.getActive() != null ? productDto.getActive() : true);
        product.setSeller(seller);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String relativeImagePath = fileStorageService.storeFile(imageFile, PRODUCT_IMAGE_SUBDIRECTORY);
                product.setImageUrl(relativeImagePath);
                logger.info("Satıcı (ID: {}) için ürün resmi '{}' başarıyla yüklendi ve kaydedildi.", seller.getId(), relativeImagePath);
            } catch (FileStorageException e) {
                logger.error("Satıcı (ID: {}) için ürün resmi yüklenirken hata oluştu: {}", seller.getId(), e.getMessage(), e);
                throw new RuntimeException("Ürün resmi yüklenemedi: " + e.getMessage(), e);
            }
        } else {
             product.setImageUrl(productDto.getImageUrl());
             logger.info("Satıcı (ID: {}) için ürün resmi yüklenmedi, DTO'daki imageUrl kullanılıyor (varsa): {}", seller.getId(), productDto.getImageUrl());
        }
        
        Product savedProduct = productRepository.save(product);
        logger.info("Satıcı (ID: {}) tarafından yeni ürün (ID: {}) oluşturuldu. Kategori: {}, Aktif: {}", 
                     seller.getId(), savedProduct.getId(), (savedProduct.getCategory() != null ? savedProduct.getCategory().getName() : "N/A"), savedProduct.isActive());
        return convertToProductResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsBySeller(User seller) {
        if (seller == null || seller.getId() == null) {
            logger.warn("getProductsBySeller (DTO) çağrıldı ancak satıcı veya satıcı ID'si null.");
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        logger.info("Satıcı (ID: {}) için ürünler (DTO olarak) getiriliyor.", seller.getId());
        // Satıcının hem aktif hem pasif ürünlerini getirmek için findAllBySeller kullanılabilir.
        // Eğer sadece aktifler isteniyorsa findBySellerAndActiveTrue repository metodu olmalı.
        // Şimdilik tüm ürünlerini getirdiğini varsayalım ve DTO'da active durumu belirtiliyor.
        List<Product> products = productRepository.findBySeller(seller); // findBySeller repository metodu olmalı
        logger.info("Satıcı (ID: {}) için {} adet ürün bulundu.", seller.getId(), products.size());
        return products.stream()
                       .map(this::convertToProductResponseDto)
                       .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDto> getProductByIdAndSeller(Long productId, User seller) {
        if (seller == null || seller.getId() == null) {
            logger.warn("getProductByIdAndSeller (DTO) çağrıldı ancak satıcı veya satıcı ID'si null. Ürün ID: {}", productId);
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        logger.debug("Ürün (ID: {}) satıcı (ID: {}) için (DTO olarak) getiriliyor.", productId, seller.getId());
        return productRepository.findById(productId)
                .filter(product -> product.getSeller() != null && product.getSeller().getId().equals(seller.getId()))
                // .filter(Product::isActive) // Satıcı kendi pasif ürünlerini de görebilmeli, bu yüzden aktiflik kontrolü kaldırıldı.
                .map(this::convertToProductResponseDto);
    }

    @Override
    @Transactional
    public Optional<ProductResponseDto> updateProductForSeller(Long productId, ProductDto productDto, User seller, MultipartFile imageFile) {
        if (seller == null || seller.getId() == null) {
            logger.warn("updateProductForSeller (DTO) çağrıldı ancak satıcı veya satıcı ID'si null. Ürün ID: {}", productId);
            throw new IllegalArgumentException("Satıcı bilgisi gereklidir.");
        }
        logger.info("Satıcı (ID: {}) ürün (ID: {}) güncelleme isteği (DTO). Kategori Adı (DTO'dan): {}, Yeni dosya: {}", 
                     seller.getId(), productId, productDto.getCategory(), (imageFile != null && !imageFile.isEmpty() ? imageFile.getOriginalFilename() : "Yok/Değiştirilmiyor"));

        Product existingProduct = productRepository.findById(productId)
            .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(seller.getId()))
            .orElseThrow(() -> {
                logger.warn("Güncellenecek ürün (ID: {}) bulunamadı veya satıcıya (ID: {}) ait değil.", productId, seller.getId());
                return new ResourceNotFoundException("Güncellenecek ürün bulunamadı veya bu kullanıcıya ait değil.");
            });

        String categoryName = productDto.getCategory();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            logger.error("Satıcı updateProduct: Kategori adı DTO'da boş veya null. Ürün ID: {}, Satıcı ID: {}", productId, seller.getId());
            throw new IllegalArgumentException("Kategori adı boş olamaz.");
        }
        Category categoryEntity = categoryRepository.findByNameIgnoreCase(categoryName)
            .orElseThrow(() -> {
                logger.warn("Satıcı updateProduct: Kategori '{}' bulunamadı. Ürün ID: {}, Satıcı ID: {}", categoryName, productId, seller.getId());
                return new ResourceNotFoundException("Belirtilen kategori '" + categoryName + "' bulunamadı.");
            });
        logger.info("Satıcı updateProduct: Kategori '{}' (ID: {}) bulundu. Ürün ID: {}, Satıcı ID: {}", categoryEntity.getName(), categoryEntity.getId(), productId, seller.getId());

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setCategory(categoryEntity);
        existingProduct.setType(productDto.getType());
        existingProduct.setStockQuantity(productDto.getStockQuantity() != null ? productDto.getStockQuantity() : existingProduct.getStockQuantity());
        existingProduct.setActive(productDto.getActive() != null ? productDto.getActive() : existingProduct.isActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty() && existingProduct.getImageUrl().startsWith(PRODUCT_IMAGE_SUBDIRECTORY + "/")) {
                String oldImageFileName = StringUtils.getFilename(existingProduct.getImageUrl());
                if (oldImageFileName != null) {
                    fileStorageService.deleteFile(oldImageFileName, PRODUCT_IMAGE_SUBDIRECTORY);
                    logger.info("Eski ürün resmi '{}' silindi (güncelleme sırasında).", existingProduct.getImageUrl());
                }
            }
            String newRelativeImagePath = fileStorageService.storeFile(imageFile, PRODUCT_IMAGE_SUBDIRECTORY);
            existingProduct.setImageUrl(newRelativeImagePath);
            logger.info("Yeni ürün resmi '{}' yüklendi ve ürün (ID: {}) için ayarlandı.", newRelativeImagePath, productId);
        } else if (productDto.getImageUrl() == null || productDto.getImageUrl().isEmpty()) {
            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty() && existingProduct.getImageUrl().startsWith(PRODUCT_IMAGE_SUBDIRECTORY + "/")) {
                String fileNameToDelete = StringUtils.getFilename(existingProduct.getImageUrl());
                 if (fileNameToDelete != null) {
                    fileStorageService.deleteFile(fileNameToDelete, PRODUCT_IMAGE_SUBDIRECTORY);
                    logger.info("Mevcut ürün resmi '{}' (ID: {}) DTO'da URL olmadığı için kaldırıldı.", existingProduct.getImageUrl(), productId);
                    existingProduct.setImageUrl(null);
                 }
            }
        }
        // DTO'dan gelen imageUrl'i doğrudan set etme mantığı (eğer bir dış URL ise ve dosya yüklenmemişse)
        // else if (productDto.getImageUrl() != null && !productDto.getImageUrl().equals(existingProduct.getImageUrl())) {
        // existingProduct.setImageUrl(productDto.getImageUrl());
        // }

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Ürün (ID: {}) satıcı (ID: {}) tarafından güncellendi. Kategori: {}, Aktif: {}", 
                     updatedProduct.getId(), seller.getId(), (updatedProduct.getCategory() != null ? updatedProduct.getCategory().getName() : "N/A"), updatedProduct.isActive());
        return Optional.of(convertToProductResponseDto(updatedProduct));
    }

    @Override
    @Transactional
    public void deleteProductForSeller(Long productId, User seller) { // SOFT DELETE UYGULANDI
        logger.info("Satıcı (ID: {}) ürün (ID: {}) için soft delete (pasif yapma) isteği.", seller.getId(), productId);
        Product productToDelete = productRepository.findById(productId)
            .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(seller.getId()))
            .orElseThrow(() -> {
                logger.warn("Silinecek/Pasif yapılacak ürün (ID: {}) bulunamadı veya satıcıya (ID: {}) ait değil.", productId, seller.getId());
                return new ResourceNotFoundException("İşlem yapılacak ürün bulunamadı veya bu kullanıcıya ait değil.");
            });
        
        productToDelete.setActive(false); // Ürünü pasif yap
        productRepository.save(productToDelete);
        logger.info("Ürün (ID: {}) satıcı (ID: {}) tarafından başarıyla pasif yapıldı.", productId, seller.getId());
    }
}
