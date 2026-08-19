package com.martinsdev.pagamentos.infra.client;

import com.martinsdev.pagamentos.infra.client.dto.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "pedidos-ms", url = "${pedidos.ms.url:}")
public interface PedidoClient {

    @GetMapping("/v1/pedidos/{id}")
    PedidoDTO buscarPedido(@PathVariable Long id);
}
