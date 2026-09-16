package com.martinsdev.pedidos.dto;

import com.martinsdev.pedidos.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoDTO(Integer quantidade,
                            BigDecimal valorUnitario) {
    public ItemPedidoDTO(ItemPedido itemPedido) {
        this(itemPedido.getQuantidade(), itemPedido.getValorUnitario());
    }
}
