package com.martinsdev.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record UsuarioCriarRequestDTO(@NotBlank @Size(max = 100) String nome,
                                     @NotBlank @Email @Size(max = 80) String email,
                                     @NotBlank @Size(min = 6, max = 80) String password) {
    public UsuarioCriarRequestDTO {
        if (nome != null) {
            nome = nome.strip();
        }
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
