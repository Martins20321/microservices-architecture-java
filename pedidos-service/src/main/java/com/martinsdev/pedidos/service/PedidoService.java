package com.martinsdev.pedidos.service;

import com.martinsdev.pedidos.dto.ItemPedidoCriarRequestDTO;
import com.martinsdev.pedidos.dto.ItemPedidoDTO;
import com.martinsdev.pedidos.dto.PedidoCriarRequestDTO;
import com.martinsdev.pedidos.dto.PedidoResponseDTO;
import com.martinsdev.pedidos.infra.client.ProdutoClient;
import com.martinsdev.pedidos.infra.client.ProdutoDTO;
import com.martinsdev.pedidos.infra.exception.ResourceNotFoundException;
import com.martinsdev.pedidos.model.ItemPedido;
import com.martinsdev.pedidos.model.Pedido;
import com.martinsdev.pedidos.model.enums.StatusPedido;
import com.martinsdev.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final ProdutoClient produtoClient;

    public Page<PedidoResponseDTO> buscarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(PedidoResponseDTO::new);
    }

    public PedidoResponseDTO buscarPorId(Long id) {
        return repository.findById(id).map(PedidoResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public PedidoResponseDTO criarPedido(PedidoCriarRequestDTO pedidoDTO) {
        List<ItemPedido> itens = new ArrayList<>();
        List<ItemPedidoDTO> itensDTO = new ArrayList<>(); // utilizado somente para melhor formatacao de resposta, incluindo o nome do produto

        for (ItemPedidoCriarRequestDTO item : pedidoDTO.itens()){
            ProdutoDTO produto = produtoClient.buscarProdutoPorId(item.produtoId());

            ItemPedido itemPedido = ItemPedido.builder()
                    .produtoId(produto.id()) // usando da fonte da verdade (inventario)
                    .quantidade(item.quantidade())
                    .valorUnitario(produto.preco())
                    .build();

            ItemPedidoDTO itemPedidoDTO = new ItemPedidoDTO(itemPedido, produto);

            itens.add(itemPedido);
            itensDTO.add(itemPedidoDTO);
        }

        Pedido pedido = Pedido.builder()
                .status(StatusPedido.REALIZADO)
                .itens(itens)
                .build();

        //setando para itemPedido
        itens.forEach(itemPedido -> itemPedido.setPedido(pedido));
        repository.save(pedido);

        return new PedidoResponseDTO(pedido, itensDTO);
    }

    //Quando pagamento for criado relacionado a esse pedido, informa o status AGUARDANDO_CONFIRMAR_PAGAMENTO
    public PedidoResponseDTO aguardarPagamento(Long id){
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        pedido.setStatus(StatusPedido.AGUARDANDO_CONFIRMAR_PAGAMENTO);
        repository.save(pedido);

        return new PedidoResponseDTO(pedido);
    }

    public PedidoResponseDTO confirmarPagamento(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        pedido.setStatus(StatusPedido.CONFIRMADO);

        repository.save(pedido);
        return new PedidoResponseDTO(pedido);
    }

    //O pedido será cancelado ao recusar o pagamento
    public PedidoResponseDTO recusarPagamento(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        pedido.setStatus(StatusPedido.CANCELADO);

        repository.save(pedido);
        return new PedidoResponseDTO(pedido);
    }

    public void cancelarPedido(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
    }
}
