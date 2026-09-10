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
        Map<Object, Object> produto = redisTemplate.opsForHash().entries("produto:" + id);

        if (produto.isEmpty()) { // ou nao existe ou nao esta salvo no cache
            Produto produtoSQL = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            // salva no redis e retorna ao usuario
            Map<String, Object> camposProdutoRedis = new HashMap<>();
            camposProdutoRedis.put("id", produtoSQL.getId());
            camposProdutoRedis.put("nome", produtoSQL.getNome());
            camposProdutoRedis.put("descricao", produtoSQL.getDescricao());
            camposProdutoRedis.put("preco", produtoSQL.getPreco());
            camposProdutoRedis.put("dataCriacao", produtoSQL.getDataCriacao());

            redisTemplate.opsForHash().putAll("produto:" + produtoSQL.getId(), camposProdutoRedis);

            return new ProdutoResponseDTO(produtoSQL);
        }

        return new ProdutoResponseDTO(produto);

        //return repository.findById(id).map(ProdutoResponseDTO::new).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public ProdutoResponseDTO criarProduto(ProdutoCriarRequestDTO produtoDTO) {
        //Verifica se o produto já existe pelo nome
        if (repository.existsByNome(produtoDTO.nome())) {
            throw new ProductAlreadyExistsException("Product with name '" + produtoDTO.nome() + "' already exists");
        }

        Produto produto = Produto.builder()
                .nome(produtoDTO.nome())
                .descricao(produtoDTO.descricao())
                .preco(produtoDTO.preco())
                .quantidadeDisponivel(produtoDTO.quantidadeDisponivel())
                .build();

        repository.save(produto);

        // montando a colecao de pares campo e valor
        Map<String, Object> camposProdutoRedis = new HashMap<>();
        camposProdutoRedis.put("id", produto.getId());
        camposProdutoRedis.put("nome", produto.getNome());
        camposProdutoRedis.put("descricao", produto.getDescricao());
        camposProdutoRedis.put("preco", produto.getPreco());
        camposProdutoRedis.put("dataCriacao", produto.getDataCriacao());

        redisTemplate.opsForHash().putAll("produto:" + produto.getId(), camposProdutoRedis); // envia a chave com o id do branco e os campos do produto

        return new ProdutoResponseDTO(produto);
    }

    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoAtualizarRequestDTO produtoDTO) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        // verifica se o atributo veio no corpo e evita um nulo
        if (produtoDTO.nome() != null) produto.setNome(produtoDTO.nome());
        if (produtoDTO.descricao() != null) produto.setDescricao(produtoDTO.descricao());
        if (produtoDTO.preco() != null) produto.setPreco(produtoDTO.preco());

        repository.save(produto);

        // apos atualizacao, exclui a chave do redis e forca a buscar no banco com os dados atualizados
        redisTemplate.delete("produto:" + produto.getId());

        return new ProdutoResponseDTO(produto);
    }
}
