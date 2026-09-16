package com.martinsdev.pedidos.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_item_pedido")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "produto_id")
    private Long produtoId;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, name = "valor_unitario")
    private BigDecimal valorUnitario;

    @ManyToOne
    @JoinColumn(name = "pedido_Id")
    private Pedido pedido;
}
