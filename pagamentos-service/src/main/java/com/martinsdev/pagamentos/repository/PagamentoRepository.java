package com.martinsdev.pagamentos.repository;

import com.martinsdev.pagamentos.model.Pagamento;
import com.martinsdev.pagamentos.model.enums.StatusPagamento;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PagamentoRepository extends MongoRepository<Pagamento, String> {

    boolean existsByPedidoIdAndStatusNot(Long pedidoId, StatusPagamento status);
}
