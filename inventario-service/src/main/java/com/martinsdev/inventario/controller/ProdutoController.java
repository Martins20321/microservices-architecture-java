package com.martinsdev.inventario.controller;

import com.martinsdev.inventario.dto.ProdutoAtualizarRequestDTO;
import com.martinsdev.inventario.dto.ProdutoCriarRequestDTO;
import com.martinsdev.inventario.dto.ProdutoResponseDTO;
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
}