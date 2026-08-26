package com.martinsdev.pagamentos.dto;

import com.martinsdev.pagamentos.model.enums.StatusPagamento;
import jakarta.validation.constraints.NotNull;

public record PagamentoAtualizarRequestDTO(@NotNull StatusPagamento novoStatus) {
}
