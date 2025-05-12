package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.UserDto;
import com.chimax.chimax_backend.entity.User; // User entity'sini import edin
import java.util.List;
import java.util.Optional;

/**
 * Kullanıcı yönetimi ile ilgili iş mantığını tanımlayan arayüz.
 */
public interface UserService {

    /**
     * Tüm kullanıcıları getirir (Admin için).
     * DTO olarak döndürülür, hassas bilgiler (şifre vb.) dışarıda bırakılır.
     * @return UserDto listesi.
     */
    List<UserDto> findAllUsers();

    /**
     * Belirli bir kullanıcının durumunu günceller (aktif/pasif).
     * @param userId Güncellenecek kullanıcının ID'si.
     * @param enabled Yeni durum (true=aktif, false=pasif).
     * @return Güncellenmiş kullanıcı bilgilerini içeren Optional<UserDto>.
     */
    Optional<UserDto> updateUserStatus(Long userId, boolean enabled);

    /**
     * Belirli bir kullanıcıyı ID ile DTO olarak getirir (Admin için).
     * @param userId Aranacak kullanıcının ID'si.
     * @return UserDto içeren Optional veya boş Optional.
     */
    Optional<UserDto> findUserById(Long userId);

    /**
     * E-posta adresine göre bir User entity'si bulur.
     * Bu metot genellikle kimlik doğrulama ve yetkilendirme süreçlerinde,
     * veya bir controller'ın kimliği doğrulanmış kullanıcıya ait User entity'sine
     * doğrudan erişmesi gerektiğinde kullanılır.
     * @param email Aranacak kullanıcının e-posta adresi.
     * @return User entity'sini içeren Optional veya kullanıcı bulunamazsa boş Optional.
     */
    Optional<User> findUserEntityByEmail(String email); // Yeni metot User entity'si döndürür

    // Eğer kullanıcı adı (username) gibi farklı bir alanla da User entity'sine
    // erişmeniz gerekiyorsa, benzer bir metot eklenebilir:
    // Optional<User> findUserEntityByUsername(String username);
}
