package com.martinsdev.pagamentos.dto;

import com.martinsdev.pagamentos.model.enums.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagamentoCriarRequestDTO(@NotNull Long pedidoId,
                                       @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal valor,
                                       @NotNull FormaPagamento formaPagamento) {
}
