package com.martinsdev.pedidos.controller;

import com.martinsdev.pedidos.dto.PedidoAtualizarRequestDTO;
import com.martinsdev.pedidos.dto.PedidoCriarRequestDTO;
import com.martinsdev.pedidos.dto.PedidoResponseDTO;
import com.martinsdev.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> buscarTodos(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.buscarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody @Valid PedidoCriarRequestDTO pedidoDTO) {
        PedidoResponseDTO pedido = service.criarPedido(pedidoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(pedido.id()).toUri();
        return ResponseEntity.created(uri).body(pedido);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> atualizarPedido(@PathVariable Long id, @RequestBody @Valid PedidoAtualizarRequestDTO pedidoDTO) {
        return ResponseEntity.ok(service.atualizarPedido(id, pedidoDTO));
    }

    @PutMapping("/{id}/confirmar-pagamento")
    public ResponseEntity<PedidoResponseDTO> confirmarPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmarPagamento(id));
    }

    @PutMapping("/{id}/recusar-pagamento")
    public ResponseEntity<PedidoResponseDTO> recusarPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(service.recusarPagamento(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        service.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }
}
