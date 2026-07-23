package com.martinsdev.pedidos.dto;

import com.martinsdev.pedidos.model.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record PedidoAtualizarRequestDTO(@NotNull StatusPedido status) {
}
