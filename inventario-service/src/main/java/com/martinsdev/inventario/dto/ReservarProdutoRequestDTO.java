package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservarProdutoRequestDTO(@NotNull @Min(1) Integer quantidadeDesejada,
                                        @NotNull Long pedidoId) {
}
