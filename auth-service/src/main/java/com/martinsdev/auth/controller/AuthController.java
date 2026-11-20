package com.martinsdev.auth.controller;

import com.martinsdev.auth.dto.LoginJWTResponseDTO;
import com.martinsdev.auth.dto.UsuarioCriarRequestDTO;
import com.martinsdev.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginJWTResponseDTO> register(@RequestBody @Valid UsuarioCriarRequestDTO usuarioDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(usuarioDTO));
    }
}
