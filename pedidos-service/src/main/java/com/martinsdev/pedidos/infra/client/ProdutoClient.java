package com.martinsdev.pedidos.infra.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "inventario-service")
public interface ProdutoClient {

    @PostMapping("/v1/produtos/{id}/reservar")
    ProdutoDetailsReservaDTO solicitarReservaProduto(@PathVariable Long id, ReservarProdutoRequestDTO reservaProdutoDTO);
}
