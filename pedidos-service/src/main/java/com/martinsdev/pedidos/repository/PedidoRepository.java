package com.martinsdev.pedidos.repository;

import com.martinsdev.pedidos.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p JOIN FETCH p.itens")
    Page<Pedido> findAllWithItens(Pageable pageable); // Evita o N+1 Query Problem
}
