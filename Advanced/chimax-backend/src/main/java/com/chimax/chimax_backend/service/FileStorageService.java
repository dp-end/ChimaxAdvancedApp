// src/main/java/com/chimax/chimax_backend/service/FileStorageService.java
package com.chimax.chimax_backend.service; // Paket adınızı kendi projenize göre güncelleyin

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {

    /**
     * Verilen dosyayı belirtilen alt dizine kaydeder.
     * Dosya adı çakışmalarını önlemek için benzersiz bir ad oluşturabilir.
     * @param file Yüklenecek dosya.
     * @param subDirectory Dosyanın kaydedileceği ana yükleme dizini içindeki alt klasör (örn: "products").
     * @return Kaydedilen dosyanın adı (veya tam yolu, implementasyona bağlı).
     */
    String storeFile(MultipartFile file, String subDirectory);

    /**
     * Belirtilen alt dizindeki dosyayı Resource olarak yükler.
     * @param filename Yüklenecek dosyanın adı.
     * @param subDirectory Dosyanın bulunduğu alt klasör.
     * @return Dosyayı temsil eden Resource nesnesi.
     */
    Resource loadFileAsResource(String filename, String subDirectory);

    /**
     * Belirtilen alt dizindeki dosyayı siler.
     * @param filename Silinecek dosyanın adı.
     * @param subDirectory Dosyanın bulunduğu alt klasör.
     * @return Silme işlemi başarılıysa true, aksi halde false.
     */
    boolean deleteFile(String filename, String subDirectory);

    /**
     * Yükleme için kullanılacak ana dizinin Path nesnesini döndürür.
     * @return Ana yükleme dizininin yolu.
     */
    Path getUploadPath();

    /**
     * Belirtilen alt dizinin Path nesnesini döndürür.
     * @param subDirectory Alt dizin adı.
     * @return Alt dizinin yolu.
     */
    Path getSubdirectoryPath(String subDirectory);
}
