package com.martinsdev.pedidos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ItemPedidoCriarRequestDTO(@NotBlank String descricao,
                                        @Min(value = 1) Integer quantidade,
                                        @DecimalMin(value = "0.0", inclusive = false) BigDecimal valor) {
}
