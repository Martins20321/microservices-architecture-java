package com.martinsdev.inventario.dto;

import com.martinsdev.inventario.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record ProdutoResponseDTO(Long id,
                                 String nome,
                                 String descricao,
                                 BigDecimal preco,
                                 Integer quantidadeDisponivel,
                                 LocalDateTime dataCriacao) {

    public ProdutoResponseDTO(Produto produto) {
        this(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getPreco(), produto.getQuantidadeDisponivel(), produto.getDataCriacao());
    }

    //Utilizado pelo redis
    public ProdutoResponseDTO(Map<Object, Object> produto) {
        this(((Number) produto.get("id")).longValue(),
                (String) produto.get("nome"),
                (String) produto.get("descricao"),
                BigDecimal.valueOf(((Number) produto.get("preco")).doubleValue()),
                (Integer) produto.get("quantidadeDisponivel"),
                LocalDateTime.parse((String) produto.get("dataCriacao")));
    }
}