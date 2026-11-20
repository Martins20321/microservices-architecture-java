package com.martinsdev.auth.service;

import com.martinsdev.auth.dto.LoginJWTResponseDTO;
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
    private final TokenService tokenService;

    public LoginJWTResponseDTO register(UsuarioCriarRequestDTO usuarioDTO) {

        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new EmailAlreadyExistsException(usuarioDTO.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(usuarioDTO.nome())
                .email(usuarioDTO.email())
                .passwordHash(passwordEncoder.encode(usuarioDTO.password()))
                .ativo(true)
                .build();

        Usuario savedUser = usuarioRepository.save(usuario);

        String token = tokenService.geracaoToken(savedUser);

        return new LoginJWTResponseDTO(token);
    }
}
