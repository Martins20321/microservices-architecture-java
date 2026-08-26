package com.martinsdev.pagamentos.controller;

import com.martinsdev.pagamentos.dto.PagamentoCriarRequestDTO;
import com.martinsdev.pagamentos.dto.PagamentoResponseDTO;
import com.martinsdev.pagamentos.service.PagamentoService;
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
@RequestMapping("/v1/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService service;

    @GetMapping
    public ResponseEntity<Page<PagamentoResponseDTO>> buscarTodos(@PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.buscarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> criarPagamento(@RequestBody @Valid PagamentoCriarRequestDTO pagamentoDTO) {
        PagamentoResponseDTO pagamento = service.criarPagamento(pagamentoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(pagamento.id()).toUri();
        return ResponseEntity.created(uri).body(pagamento);
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<PagamentoResponseDTO> aprovarPagamento(@PathVariable String id) {
        return ResponseEntity.ok(service.aprovarPagamento(id));
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<PagamentoResponseDTO> recusarPagamento(@PathVariable String id) {
        return ResponseEntity.ok(service.recusarPagamento(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPagamento(@PathVariable String id) {
        service.cancelarPagamento(id);
        return ResponseEntity.noContent().build();
    }
}
