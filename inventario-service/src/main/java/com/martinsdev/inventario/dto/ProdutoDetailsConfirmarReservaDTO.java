package com.martinsdev.inventario.dto;

public record ProdutoDetailsConfirmarReservaDTO(Long idMovimentacao,
                                                Long produtoId,
                                                Long pedidoId,
                                                Integer quantidadeConfirmada) {
}
