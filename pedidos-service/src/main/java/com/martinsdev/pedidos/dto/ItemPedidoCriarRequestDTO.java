package com.martinsdev.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoCriarRequestDTO(@NotNull Long produtoId,
                                        @Min(value = 1) Integer quantidade) {
}
