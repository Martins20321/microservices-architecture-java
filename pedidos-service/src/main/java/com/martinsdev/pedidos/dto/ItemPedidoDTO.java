package com.martinsdev.pedidos.dto;

import com.martinsdev.pedidos.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoDTO(String descricao,
                            Integer quantidade,
                            BigDecimal valor) {
    public ItemPedidoDTO(ItemPedido itemPedido) {
        this(itemPedido.getDescricao(), itemPedido.getQuantidade(), itemPedido.getValor());
    }
}
