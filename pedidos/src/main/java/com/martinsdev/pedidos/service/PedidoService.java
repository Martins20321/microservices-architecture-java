package com.martinsdev.pedidos.service;

import com.martinsdev.pedidos.dto.PedidoAtualizarRequestDTO;
import com.martinsdev.pedidos.dto.PedidoCriarRequestDTO;
import com.martinsdev.pedidos.dto.PedidoResponseDTO;
import com.martinsdev.pedidos.infra.exception.InvalidOperationException;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;

    public Page<PedidoResponseDTO> buscarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(PedidoResponseDTO::new);
    }

    public PedidoResponseDTO buscarPorId(Long id) {
        return repository.findById(id).map(PedidoResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoCriarRequestDTO pedidoDTO) {
        List<ItemPedido> itens = pedidoDTO.itens().stream()
                .map(item -> ItemPedido.builder()
                        .descricao(item.descricao())
                        .quantidade(item.quantidade())
                        .valor(item.valor())
                        .build()).toList();

        Pedido pedido = Pedido.builder()
                .status(StatusPedido.REALIZADO)
                .itens(itens)
                .build();

        //setando para itemPedido
        itens.forEach(itemPedido -> itemPedido.setPedido(pedido));
        repository.save(pedido);
        return new PedidoResponseDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO confirmarPagamento(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        pedido.setStatus(StatusPedido.CONFIRMADO);

        repository.save(pedido);
        return new PedidoResponseDTO(pedido);
    }

    @Transactional
    //O pedido será cancelado ao recusar o pagamento
    public PedidoResponseDTO recusarPagamento(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        pedido.setStatus(StatusPedido.CANCELADO);

        repository.save(pedido);
        return new PedidoResponseDTO(pedido);
    }

    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
    }
}
