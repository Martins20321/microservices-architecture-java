package com.martinsdev.notificacoes.dto;

import java.math.BigDecimal;

public record ItemPedidoDTO(String descricao,
                            Integer quantidade,
                            BigDecimal valor) {
}
