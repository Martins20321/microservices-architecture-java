package com.martinsdev.inventario.service;

import com.martinsdev.inventario.dto.*;
import com.martinsdev.inventario.infra.exception.InsufficientStockException;
import com.martinsdev.inventario.infra.exception.ProductAlreadyExistsException;
import com.martinsdev.inventario.infra.exception.ResourceNotFoundException;
import com.martinsdev.inventario.model.MovimentacaoEstoque;
import com.martinsdev.inventario.model.Produto;
import com.martinsdev.inventario.model.enums.TipoMovimentacao;
import com.martinsdev.inventario.repository.MovimentacaoEstoqueRepository;
import com.martinsdev.inventario.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final MovimentacaoEstoqueRepository estoqueRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Page<ProdutoResponseDTO> buscarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(ProdutoResponseDTO::new);
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Map<Object, Object> produto = redisTemplate.opsForHash().entries("produto:" + id);
        Object quantidadeDisponivel = redisTemplate.opsForValue().get("estoque:" + id);

        if (produto.isEmpty()) { // ou nao existe ou nao esta salvo no cache
            Produto produtoSQL = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found by id: " + id));

            // salva no redis e retorna ao usuario
            Map<String, Object> camposProdutoRedis = new HashMap<>();
            camposProdutoRedis.put("id", produtoSQL.getId());
            camposProdutoRedis.put("nome", produtoSQL.getNome());
            camposProdutoRedis.put("descricao", produtoSQL.getDescricao());
            camposProdutoRedis.put("preco", produtoSQL.getPreco());
            camposProdutoRedis.put("dataCriacao", produtoSQL.getDataCriacao());

            redisTemplate.opsForHash().putAll("produto:" + produtoSQL.getId(), camposProdutoRedis);
            redisTemplate.opsForValue().set("estoque:" + produtoSQL.getId(), produtoSQL.getQuantidadeDisponivel());

            return new ProdutoResponseDTO(produtoSQL);
        }

        return new ProdutoResponseDTO(produto, quantidadeDisponivel);

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
        redisTemplate.opsForValue().set("estoque:" + produto.getId(), produto.getQuantidadeDisponivel()); // chave utilizada para armazenar a quantidade disponivel de um produto usuando operacoes atomicas (INCR/DECR)

        return new ProdutoResponseDTO(produto);
    }

    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoAtualizarRequestDTO produtoDTO) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found by id: " + id));

        // verifica se o atributo veio no corpo e evita um nulo
        if (produtoDTO.nome() != null) produto.setNome(produtoDTO.nome());
        if (produtoDTO.descricao() != null) produto.setDescricao(produtoDTO.descricao());
        if (produtoDTO.preco() != null) produto.setPreco(produtoDTO.preco());

        repository.save(produto);

        // apos atualizacao, exclui a chave do redis e forca a buscar no banco com os dados atualizados
        redisTemplate.delete("produto:" + produto.getId());

        return new ProdutoResponseDTO(produto);
    }

    @Transactional
    public ProdutoDetailsReposicaoDTO reposicaoProduto(Long id, ProdutoReposicaoRequestDTO reposicaoDTO) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found by id: " + id));

        // redis garantindo atomicidade e isolamento
        Long novaQuantidade = redisTemplate.opsForValue().increment("estoque:" + produto.getId(), reposicaoDTO.quantidadeReposicao());
        produto.setQuantidadeDisponivel(novaQuantidade.intValue()); // atualizando no banco SQL

        // registrando a movimentacao no estoque
        MovimentacaoEstoque movimentacaoEstoque = MovimentacaoEstoque.builder()
                .produtoId(produto.getId())
                .tipoMovimentacao(TipoMovimentacao.REPOSICAO)
                .quantidade(reposicaoDTO.quantidadeReposicao()) // reposicao = sempre adiciona uma quantidade
                .build();

        repository.save(produto);
        estoqueRepository.save(movimentacaoEstoque);

        return new ProdutoDetailsReposicaoDTO(movimentacaoEstoque.getId(),
                produto.getId(),
                produto.getNome(),
                reposicaoDTO.quantidadeReposicao(),
                produto.getQuantidadeDisponivel());
    }

    @Transactional
    public ProdutoDetailsReservaDTO reservarProduto(Long id, ReservarProdutoRequestDTO reservarProdutoDTO) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found by id: " + id));

        // redis garantindo atomicidade e isolamento - decrementa a quantidade disponivel pela quantidade a ser reservada e retorna a quantidade atual disponivel
        Long quantidadeAtualDisponivel = redisTemplate.opsForValue().decrement("estoque:" + produto.getId(), reservarProdutoDTO.quantidadeDesejada()); //DECRBY

        // redis pode retornar um valor negativo em caso de estoque insufiente
        if (quantidadeAtualDisponivel < 0) {
            // incrementa denovo o valor que foi decrementado
            redisTemplate.opsForValue().increment("estoque:" + produto.getId(), reservarProdutoDTO.quantidadeDesejada());
            throw new InsufficientStockException("Insufficient stock for product: " + produto.getNome() +
                    ": requested " + reservarProdutoDTO.quantidadeDesejada() +
                    ", available " + quantidadeAtualDisponivel + reservarProdutoDTO.quantidadeDesejada());
        }

        produto.setQuantidadeDisponivel(quantidadeAtualDisponivel.intValue());

        MovimentacaoEstoque movimentacaoEstoque = MovimentacaoEstoque.builder()
                .produtoId(produto.getId())
                .pedidoId(reservarProdutoDTO.pedidoId())
                .tipoMovimentacao(TipoMovimentacao.RESERVA)
                .quantidade(reservarProdutoDTO.quantidadeDesejada()) // reserva = sempre diminui uma quantidade
                .build();

        repository.save(produto);
        estoqueRepository.save(movimentacaoEstoque);

        return new ProdutoDetailsReservaDTO(movimentacaoEstoque.getId(),
                produto.getId(),
                produto.getNome(),
                movimentacaoEstoque.getPedidoId(),
                reservarProdutoDTO.quantidadeDesejada(),
                produto.getQuantidadeDisponivel());
    }

    public ProdutoDetailsConfirmarReservaDTO confirmarReservaProduto(Long id, ConfirmarReservaProdutoDTO confirmarReservaProduto) {
        // buscando a reserva original com um metodo do Spring Data Jpa
        MovimentacaoEstoque reserva = estoqueRepository.findByProdutoIdAndPedidoIdAndTipoMovimentacao(id, confirmarReservaProduto.pedidoId(), TipoMovimentacao.RESERVA)
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found related to this order: " + confirmarReservaProduto.pedidoId()));

        // registrando a confirmacao
        MovimentacaoEstoque movimentacaoEstoque = MovimentacaoEstoque.builder()
                .produtoId(reserva.getProdutoId())
                .pedidoId(reserva.getPedidoId())
                .tipoMovimentacao(TipoMovimentacao.CONFIRMACAO)
                .quantidade(reserva.getQuantidade())
                .build();

        estoqueRepository.save(movimentacaoEstoque);

        return new ProdutoDetailsConfirmarReservaDTO(movimentacaoEstoque.getId(),
                movimentacaoEstoque.getProdutoId(),
                movimentacaoEstoque.getPedidoId(),
                movimentacaoEstoque.getQuantidade());
    }
}
