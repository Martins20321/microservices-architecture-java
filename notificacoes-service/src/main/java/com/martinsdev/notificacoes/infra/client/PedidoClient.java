package com.martinsdev.notificacoes.infra.client;

import com.martinsdev.notificacoes.dto.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "pedidos-service")
public interface PedidoClient {

    @GetMapping("/v1/pedidos/{id}")
    PedidoDTO buscarPedido(@PathVariable Long id);
}
