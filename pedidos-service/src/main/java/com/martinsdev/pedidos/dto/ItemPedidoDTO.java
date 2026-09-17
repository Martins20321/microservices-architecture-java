package com.martinsdev.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.martinsdev.pedidos.infra.client.ProdutoDTO;
import com.martinsdev.pedidos.model.ItemPedido;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemPedidoDTO(Long produtoId,
                            String nome,
                            Integer quantidade,
                            BigDecimal valorUnitario) {
    public ItemPedidoDTO(ItemPedido itemPedido, ProdutoDTO produto) {
        this(produto.id(), produto.nome(), itemPedido.getQuantidade(), itemPedido.getValorUnitario());
    }

    public ItemPedidoDTO(ItemPedido itemPedido) {
        this(itemPedido.getProdutoId(), null, itemPedido.getQuantidade(), itemPedido.getValorUnitario());
    }
}
