package com.martinsdev.pagamentos.model;

import com.martinsdev.pagamentos.model.enums.FormaPagamento;
import com.martinsdev.pagamentos.model.enums.StatusPagamento;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "pagamentos")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class Pagamento {

    @Id
    private String id;
    private Long pedidoId;
    private BigDecimal valor;
    private StatusPagamento status;
    private FormaPagamento formaPagamento;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
