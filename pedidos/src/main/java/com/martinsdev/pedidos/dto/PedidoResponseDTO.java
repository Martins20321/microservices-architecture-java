package com.martinsdev.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.martinsdev.pedidos.model.Pedido;
import com.martinsdev.pedidos.model.enums.StatusPedido;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PedidoResponseDTO(Long id,
                                StatusPedido status,
                                LocalDateTime dataCriacao,
                                LocalDateTime dataAtualizacao,
                                List<ItemPedidoDTO> itens) {
    public PedidoResponseDTO(Pedido pedido) {
        this(pedido.getId(), pedido.getStatus(), pedido.getDataCriacao(), pedido.getDataAtualizacao(),
                pedido.getItens().stream().map(ItemPedidoDTO::new).toList());
    }
}
