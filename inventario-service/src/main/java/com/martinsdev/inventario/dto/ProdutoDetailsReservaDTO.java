package com.martinsdev.inventario.dto;

public record ProdutoDetailsReservaDTO(Long idMovimentacao,
                                       Long produtoId,
                                       String nomeProduto,
                                       Long pedidoId,
                                       Integer quantidadeReservada,
                                       Integer quantidadeAtualDisponivel) {
}
