package com.martinsdev.pedidos.infra.client;

public record ReservarProdutoRequestDTO(Integer quantidadeDesejada,
                                        Long pedidoId) {
}
