package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmarReservaProdutoDTO(@NotNull Long pedidoId) {
}
