package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProdutoReposicaoRequestDTO(@NotNull @Min(1) Integer quantidadeReposicao) {
}
