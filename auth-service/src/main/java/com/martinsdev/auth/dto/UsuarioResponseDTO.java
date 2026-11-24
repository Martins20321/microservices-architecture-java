package com.martinsdev.auth.dto;

import com.martinsdev.auth.model.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponseDTO(UUID id,
                                 String nome,
                                 String email,
                                 boolean ativo,
                                 LocalDateTime dataCriacao,
                                 LocalDateTime dataAtualizacao) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.isAtivo(), usuario.getDataCriacao(), usuario.getDataAtualizacao());
    }
}
