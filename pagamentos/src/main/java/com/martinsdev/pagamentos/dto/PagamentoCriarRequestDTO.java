package com.martinsdev.pagamentos.dto;

import com.martinsdev.pagamentos.model.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record PagamentoCriarRequestDTO(@NotNull Long pedidoId,
                                       @NotNull FormaPagamento formaPagamento) {
}
