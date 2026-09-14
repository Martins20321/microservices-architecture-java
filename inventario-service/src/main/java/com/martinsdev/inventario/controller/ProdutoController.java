package com.martinsdev.inventario.controller;

import com.martinsdev.inventario.dto.*;
import com.martinsdev.inventario.service.ProdutoService;
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
@RequestMapping("/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> buscarTodos(@PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.buscarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criarProduto(@Valid @RequestBody ProdutoCriarRequestDTO produtoDTO) {
        ProdutoResponseDTO produto = service.criarProduto(produtoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(produto.id()).toUri();
        return ResponseEntity.created(uri).body(produto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(@PathVariable Long id, @Valid @RequestBody ProdutoAtualizarRequestDTO produtoDTO) {
        return ResponseEntity.ok(service.atualizarProduto(id, produtoDTO));
    }

    @PostMapping("/{id}/reposicao")
    public ResponseEntity<ProdutoDetailsReposicaoDTO> reposicaoProduto(@PathVariable Long id, @Valid @RequestBody ProdutoReposicaoRequestDTO reposicaoDTO) {
        return ResponseEntity.ok(service.reposicaoProduto(id, reposicaoDTO));
    }

    @PostMapping("/{id}/reservar")
    public ResponseEntity<ProdutoDetailsReservaDTO> reservarProduto(@PathVariable Long id, @Valid @RequestBody ReservarProdutoRequestDTO reservarProdutoDTO) {
        return ResponseEntity.ok(service.reservarProduto(id, reservarProdutoDTO));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ProdutoDetailsConfirmarReservaDTO> confirmarReservaProduto(@PathVariable Long id, @Valid @RequestBody ConfirmarReservaProdutoDTO confirmarReservaProdutoDTO) {
        return ResponseEntity.ok(service.confirmarReservaProduto(id, confirmarReservaProdutoDTO));
    }

    @PostMapping("/{id}/cancelar-reserva")
    public ResponseEntity<Void> cancelarReservaProduto(@PathVariable Long id, @Valid @RequestBody CancelarReservaProdutoDTO cancelarReservaProdutoDTO) {
        service.cancelarReserva(id, cancelarReservaProdutoDTO);
        return ResponseEntity.noContent().build();
    }
}