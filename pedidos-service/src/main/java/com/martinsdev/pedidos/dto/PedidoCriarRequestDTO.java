package com.martinsdev.pedidos.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoCriarRequestDTO(@Size(min = 1) List<ItemPedidoCriarRequestDTO> itens) {
}
