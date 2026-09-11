package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProdutoReposicaoRequestDTO(@NotBlank @Min(1) Integer quantidadeReposicao) {
}
