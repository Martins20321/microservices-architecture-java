package com.martinsdev.inventario.dto;

import com.martinsdev.inventario.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponseDTO(Long id,
                                 String nome,
                                 String descricao,
                                 BigDecimal preco,
                                 Integer quantidadeDisponivel,
                                 LocalDateTime dataCriacao) {

    public ProdutoResponseDTO(Produto produto) {
        this(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getPreco(), produto.getQuantidadeDisponivel(), produto.getDataCriacao());
    }
}
