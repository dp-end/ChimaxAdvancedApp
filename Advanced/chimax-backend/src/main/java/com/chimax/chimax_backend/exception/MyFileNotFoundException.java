// src/main/java/com/chimax/chimax_backend/exception/MyFileNotFoundException.java
package com.chimax.chimax_backend.exception; // Paket adınızı kendi projenize göre güncelleyin

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Bu exception fırlatıldığında HTTP 404 döner
public class MyFileNotFoundException extends RuntimeException {
    public MyFileNotFoundException(String message) {
        super(message);
    }

    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
