package com.martinsdev.pagamentos.infra.client;

import com.martinsdev.pagamentos.infra.client.dto.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "pedidos-ms")
public interface PedidoClient {

    @GetMapping("/v1/pedidos/{id}")
    PedidoDTO buscarPedido(@PathVariable Long id);

    @PutMapping("/v1/pedidos/{id}/confirmar-pagamento")
    void confirmarPagamento(@PathVariable Long id);
}
