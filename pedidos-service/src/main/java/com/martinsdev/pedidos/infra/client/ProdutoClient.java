package com.martinsdev.pedidos.infra.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "inventario-service")
public interface ProdutoClient {

    @GetMapping("/v1/produtos/{id}")
    ProdutoDTO buscarProdutoPorId(@PathVariable Long id);
}
