package com.martinsdev.inventario.service;

import com.martinsdev.inventario.dto.ProdutoAtualizarRequestDTO;
import com.martinsdev.inventario.dto.ProdutoCriarRequestDTO;
import com.martinsdev.inventario.dto.ProdutoResponseDTO;
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
                .orElseThrow(() -> new RuntimeException("")); //Exceçao personaliza a ser criada
    }

    public ProdutoResponseDTO criarProduto(ProdutoCriarRequestDTO produtoDTO) {
        //Verifica se o produto já existe pelo nome
        if (repository.existsByNome(produtoDTO.nome())) {
            throw new RuntimeException(""); //Exceçao personaliza a ser criada
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
                .orElseThrow(() -> new RuntimeException("")); //Exceçao personaliza a ser criada

        produto.setNome(produtoDTO.nome());
        produto.setDescricao(produtoDTO.descricao());
        produto.setPreco(produtoDTO.preco());

        repository.save(produto);
        return new ProdutoResponseDTO(produto);
    }
}
