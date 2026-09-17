package com.martinsdev.inventario.dto;

import java.math.BigDecimal;

public record ProdutoDetailsReservaDTO(Long idMovimentacao,
                                       Long produtoId,
                                       String nomeProduto,
                                       BigDecimal valorUnitario,
                                       Long pedidoId,
                                       Integer quantidadeReservada,
                                       Integer quantidadeAtualDisponivel) {
}
