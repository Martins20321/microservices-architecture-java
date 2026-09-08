package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ProdutoAtualizarRequestDTO(String nome,
                                         String descricao,
                                         @DecimalMin("0.0") BigDecimal preco) {
}
