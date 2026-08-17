package com.martinsdev.pagamentos.event;

import com.martinsdev.pagamentos.model.Pagamento;

public record PagamentoConcluidoEvent(Long pedidoId) {
}
