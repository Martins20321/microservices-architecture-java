package com.martinsdev.notificacoes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(Long id,
                        StatusPedido status,
                        LocalDateTime dataCriacao,
                        List<ItemPedidoDTO> itens) {
}
