package com.martinsdev.auth.service;

import com.martinsdev.auth.dto.UsuarioCriarRequestDTO;
import com.martinsdev.auth.infra.exception.EmailAlreadyExistsException;
import com.martinsdev.auth.model.Usuario;
import com.martinsdev.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario register(UsuarioCriarRequestDTO usuarioDTO) {

        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new EmailAlreadyExistsException(usuarioDTO.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(usuarioDTO.nome())
                .email(usuarioDTO.email())
                .passwordHash(passwordEncoder.encode(usuarioDTO.password()))
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);
        return usuario;
    }
}
