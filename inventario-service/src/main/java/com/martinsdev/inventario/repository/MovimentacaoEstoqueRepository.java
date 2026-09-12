package com.martinsdev.inventario.repository;

import com.martinsdev.inventario.model.MovimentacaoEstoque;
import com.martinsdev.inventario.model.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    // consulta jpa para buscar a combinacao de 3 campos para ter certeza na hora de confirmar reserva
    Optional<MovimentacaoEstoque> findByProdutoIdAndPedidoIdAndTipoMovimentacao(Long produtoId, Long pedidoId, TipoMovimentacao tipoMovimentacao);
}
