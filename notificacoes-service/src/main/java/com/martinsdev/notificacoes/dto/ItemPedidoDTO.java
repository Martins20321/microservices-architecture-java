package com.martinsdev.notificacoes.dto;

import java.math.BigDecimal;

public record ItemPedidoDTO(Long produtoId,
                            String nome,
                            Integer quantidade,
                            BigDecimal valorUnitario) {
}
