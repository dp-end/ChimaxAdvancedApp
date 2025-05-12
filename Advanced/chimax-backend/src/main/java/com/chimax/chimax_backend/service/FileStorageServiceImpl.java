// src/main/java/com/chimax/chimax_backend/service/FileStorageServiceImpl.java
package com.chimax.chimax_backend.service; // Paket adınızı kendi projenize göre güncelleyin

import com.chimax.chimax_backend.exception.FileStorageException; // Özel exception sınıfı (aşağıda tanımlanacak)
import com.chimax.chimax_backend.exception.MyFileNotFoundException; // Özel exception sınıfı (aşağıda tanımlanacak)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct; // @PostConstruct için
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID; // Benzersiz dosya adları için
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation; // Ana yükleme dizini

    // application.properties dosyasındaki 'file.upload-dir' değerini enjekte et
    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation); // Ana yükleme dizinini oluştur (eğer yoksa)
        } catch (Exception ex) {
            throw new FileStorageException("Yükleme dizini oluşturulamadı veya erişilemedi. Lütfen yolu kontrol edin: " + this.fileStorageLocation, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        // Dosya adını normalize et (güvenlik için)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Dosya adında geçersiz karakterler olup olmadığını kontrol et
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Üzgünüz! Dosya adı geçersiz yol dizisi içeriyor: " + originalFilename);
            }

            // Benzersiz bir dosya adı oluştur (UUID + orijinal uzantı)
            String fileExtension = "";
            int i = originalFilename.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFilename.substring(i);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Alt dizini oluştur (eğer yoksa)
            Path targetDirectory = this.fileStorageLocation.resolve(subDirectory).normalize();
            Files.createDirectories(targetDirectory); // Alt dizini oluştur

            // Dosyayı hedef konuma kopyala (aynı isimde dosya varsa üzerine yaz)
            Path targetLocation = targetDirectory.resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Saklanan dosyanın sadece adını veya alt dizinle birlikte göreceli yolunu döndür
            // Controller katmanı, bu adı/yolu kullanarak tam URL oluşturacaktır.
            return Paths.get(subDirectory, uniqueFileName).toString().replace("\\", "/"); // Göreceli yolu döndür

        } catch (IOException ex) {
            throw new FileStorageException(originalFilename + " dosyası saklanırken bir hata oluştu. Lütfen tekrar deneyin!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filename, String subDirectory) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subDirectory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException(filename + " dosyası bulunamadı.");
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException(filename + " dosyası bulunamadı.", ex);
        }
    }

    @Override
    public boolean deleteFile(String filename, String subDirectory) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subDirectory).resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Hata durumunda false döndür veya logla
            System.err.println(filename + " dosyası silinirken hata: " + ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Path getUploadPath() {
        return this.fileStorageLocation;
    }

    @Override
    public Path getSubdirectoryPath(String subDirectory) {
        return this.fileStorageLocation.resolve(subDirectory).normalize();
    }
}
