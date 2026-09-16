package com.martinsdev.pedidos.infra.client;

import java.math.BigDecimal;

public record ProdutoDTO(Long id,
                         String nome,
                         String descricao,
                         BigDecimal preco) {
}
