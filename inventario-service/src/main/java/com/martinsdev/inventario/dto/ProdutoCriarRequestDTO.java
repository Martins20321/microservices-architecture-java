package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoCriarRequestDTO(@NotBlank String nome,
                                     @NotBlank String descricao,
                                     @NotNull @DecimalMin("0.0") BigDecimal preco,
                                     @NotNull Integer quantidadeDisponivel) {
}
