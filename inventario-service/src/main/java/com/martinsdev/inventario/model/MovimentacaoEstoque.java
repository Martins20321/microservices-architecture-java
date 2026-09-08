package com.martinsdev.inventario.model;

import com.martinsdev.inventario.model.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_movimentacao_estoque")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "produto_id")
    private Long produtoId;
    @Column(nullable = false, name = "pedido_id")
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "tipo_movimentacao")
    private TipoMovimentacao tipoMovimentacao;
    @Column(nullable = false)
    private Integer quantidade;

    @CreationTimestamp
    private LocalDateTime dataMovimentacao;
}