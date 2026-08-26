package com.martinsdev.pagamentos.infra.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO (Long id,
                        StatusPedido status,
                        LocalDateTime dataCriacao,
                        LocalDateTime dataAtualizacao,
                        List<ItemPedidoDTO> itens){
}
