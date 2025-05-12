// src/main/java/com/chimax/chimax_backend/exception/FileStorageException.java
package com.chimax.chimax_backend.exception; // Paket adınızı kendi projenize göre güncelleyin

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
