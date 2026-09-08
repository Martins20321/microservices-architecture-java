package com.martinsdev.inventario.service;

import com.martinsdev.inventario.dto.ProdutoAtualizarRequestDTO;
import com.martinsdev.inventario.dto.ProdutoCriarRequestDTO;
import com.martinsdev.inventario.dto.ProdutoResponseDTO;
import com.martinsdev.inventario.infra.exception.ProductAlreadyExistsException;
import com.martinsdev.inventario.infra.exception.ResourceNotFoundException;
import com.martinsdev.inventario.model.Produto;
import com.martinsdev.inventario.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    public Page<ProdutoResponseDTO> buscarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(ProdutoResponseDTO::new);
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        return repository.findById(id).map(ProdutoResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public ProdutoResponseDTO criarProduto(ProdutoCriarRequestDTO produtoDTO) {
        //Verifica se o produto já existe pelo nome
        if (repository.existsByNome(produtoDTO.nome())) {
            throw new ProductAlreadyExistsException("Product with name '"  + produtoDTO.nome() + "' already exists");
        }

        Produto produto = Produto.builder()
                .nome(produtoDTO.nome())
                .descricao(produtoDTO.descricao())
                .preco(produtoDTO.preco())
                .quantidadeDisponivel(produtoDTO.quantidadeDisponivel())
                .build();

        repository.save(produto);
        return new ProdutoResponseDTO(produto);
    }

    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoAtualizarRequestDTO produtoDTO) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        produto.setNome(produtoDTO.nome());
        produto.setDescricao(produtoDTO.descricao());
        produto.setPreco(produtoDTO.preco());

        repository.save(produto);
        return new ProdutoResponseDTO(produto);
    }
}
