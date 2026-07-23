package com.martinsdev.pagamentos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.martinsdev.pagamentos.model.Pagamento;
import com.martinsdev.pagamentos.model.enums.FormaPagamento;
import com.martinsdev.pagamentos.model.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagamentoResponseDTO(String id,
                                   Long pedidoId,
                                   BigDecimal valor,
                                   StatusPagamento status,
                                   FormaPagamento formaPagamento,
                                   LocalDateTime dataCriacao,
                                   LocalDateTime dataAtualizacao) {
    public PagamentoResponseDTO(Pagamento pagamento) {
        this(pagamento.getId(), pagamento.getPedidoId(), pagamento.getValor(), pagamento.getStatus(),
                pagamento.getFormaPagamento(), pagamento.getDataCriacao(), pagamento.getDataAtualizacao());
    }
}
