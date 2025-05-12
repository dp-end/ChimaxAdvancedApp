package com.chimax.chimax_backend.service; // Paket adını kontrol et

import com.chimax.chimax_backend.dto.UserDto;
import com.chimax.chimax_backend.entity.Role;
import com.chimax.chimax_backend.entity.User;
import com.chimax.chimax_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto) // Her User'ı DTO'ya çevir
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::convertToUserDto); // Bulunursa DTO'ya çevir
    }


    @Override
    @Transactional
    public Optional<UserDto> updateUserStatus(Long userId, boolean enabled) {
        // Kullanıcıyı bul
        return userRepository.findById(userId)
                .map(user -> {
                    // Durumu güncelle
                    user.setEnabled(enabled);
                    // Kaydet ve güncellenmiş DTO'yu döndür
                    User updatedUser = userRepository.save(user);
                    return convertToUserDto(updatedUser);
                }); // Kullanıcı bulunamazsa boş Optional döner
    }

    /**
     * E-posta adresine göre bir User entity'si bulur.
     * Bu metot, DTO dönüşümü yapmadan doğrudan User entity'sini döndürür.
     * Bu, genellikle kimlik doğrulama veya controller katmanında User entity'sine
     * doğrudan ihtiyaç duyulduğunda kullanılır.
     * @param email Aranacak kullanıcının e-posta adresi.
     * @return User entity'sini içeren Optional veya kullanıcı bulunamazsa boş Optional.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserEntityByEmail(String email) {
        // UserRepository'deki findByEmail metodu çağrılır.
        // Bu metodun Optional<User> döndürdüğü varsayılır.
        return userRepository.findByEmail(email);
    }

    // Eğer kullanıcı adı (username) ile de User entity'sine erişmeniz gerekiyorsa,
    // benzer bir metot eklenebilir:
    /*
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserEntityByUsername(String username) {
        return userRepository.findByUsername(username); // UserRepository'de findByUsername olmalı
    }
    */

    // === Yardımcı Dönüşüm Metodu: User Entity -> UserDto ===
    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setEnabled(user.isEnabled());
        // Rol isimlerini alıp listeye çevir
        if (user.getRoles() != null) {
            userDto.setRoles(user.getRoles().stream()
                    .map(Role::getName) // Her Role nesnesinden sadece ismi al
                    .collect(Collectors.toList()));
        }
        return userDto;
    }
}
