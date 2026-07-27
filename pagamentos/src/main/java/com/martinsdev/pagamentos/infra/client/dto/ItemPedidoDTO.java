package com.martinsdev.pagamentos.infra.client.dto;

import java.math.BigDecimal;

public record ItemPedidoDTO(String descricao,
                            Integer quantidade,
                            BigDecimal valor) {
}
