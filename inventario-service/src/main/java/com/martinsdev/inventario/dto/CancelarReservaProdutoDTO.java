package com.martinsdev.inventario.dto;

import jakarta.validation.constraints.NotNull;

public record CancelarReservaProdutoDTO(@NotNull Long pedidoId) {
}
