package com.martinsdev.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

public record LoginRequestDTO(@NotBlank String email,
                              @NotBlank String password) {

    public LoginRequestDTO {
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
