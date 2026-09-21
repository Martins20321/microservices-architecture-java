package com.martinsdev.inventario.repository;

import com.martinsdev.inventario.model.MovimentacaoEstoque;
import com.martinsdev.inventario.model.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    // consulta jpa para buscar a combinacao de 3 campos para ter certeza na hora de confirmar reserva
    Optional<MovimentacaoEstoque> findByProdutoIdAndPedidoIdAndTipoMovimentacao(Long produtoId, Long pedidoId, TipoMovimentacao tipoMovimentacao);

    // consulta jpa para buscar os produtos reservados de um pedido
    List<MovimentacaoEstoque> findByPedidoIdAndTipoMovimentacao(Long pedidoId, TipoMovimentacao tipoMovimentacao);
}
