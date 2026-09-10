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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

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

        // montando a colecao de pares campo e valor
        Map<String, Object> camposProdutoRedis = new HashMap<>();
        camposProdutoRedis.put("nome", produto.getNome());
        camposProdutoRedis.put("descricao", produto.getDescricao());
        camposProdutoRedis.put("preco", produto.getPreco());
        camposProdutoRedis.put( "quantidadeDisponivel", produto.getQuantidadeDisponivel());

        repository.save(produto);
        redisTemplate.opsForHash().putAll("produto:" + produto.getId(), camposProdutoRedis); // envia a chave com o id do branco e os campos do produto

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
