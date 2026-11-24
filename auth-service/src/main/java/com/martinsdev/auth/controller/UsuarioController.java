package com.martinsdev.auth.controller;

import com.martinsdev.auth.dto.UsuarioResponseDTO;
import com.martinsdev.auth.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> findMe(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }
}
