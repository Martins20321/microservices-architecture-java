package com.martinsdev.pagamentos.service;

import com.martinsdev.pagamentos.dto.PagamentoCriarRequestDTO;
import com.martinsdev.pagamentos.dto.PagamentoResponseDTO;
import com.martinsdev.pagamentos.infra.client.PedidoClient;
import com.martinsdev.pagamentos.infra.client.dto.ItemPedidoDTO;
import com.martinsdev.pagamentos.infra.client.dto.PedidoDTO;
import com.martinsdev.pagamentos.infra.client.dto.StatusPedido;
import com.martinsdev.pagamentos.infra.exception.InvalidOperationException;
import com.martinsdev.pagamentos.infra.exception.ResourceNotFoundException;
import com.martinsdev.pagamentos.model.Pagamento;
import com.martinsdev.pagamentos.model.enums.StatusPagamento;
import com.martinsdev.pagamentos.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository repository;
    private final PedidoClient pedidoClient;

    public Page<PagamentoResponseDTO> buscarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(PagamentoResponseDTO::new);
    }

    public PagamentoResponseDTO buscarPorId(String id) {
        return repository.findById(id).map(PagamentoResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public PagamentoResponseDTO criarPagamento(PagamentoCriarRequestDTO pagamentoDTO) {
        //código para buscar um pedido pelo OpenFeing
        PedidoDTO pedido = pedidoClient.buscarPedido(pagamentoDTO.pedidoId());

        //Existe um pagamento relacionado a um pedidoId onde não seja com o Status CANCELADO
        if (repository.existsByPedidoIdAndStatusNot(pedido.id(), StatusPagamento.CANCELADO)) {
            throw new InvalidOperationException("There is already an active payment for this order");
        }

        if (pedido.status() == StatusPedido.CANCELADO) {
            throw new InvalidOperationException("Cannot create payment for a cancelled order");
        }

        List<ItemPedidoDTO> itens = pedido.itens();
        BigDecimal valor = itens.stream().map(ItemPedidoDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);

        Pagamento pagamento = Pagamento.builder()
                .pedidoId(pagamentoDTO.pedidoId())
                .valor(valor)
                .status(StatusPagamento.PENDENTE)
                .formaPagamento(pagamentoDTO.formaPagamento())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        repository.save(pagamento);
        return new PagamentoResponseDTO(pagamento);
    }

    public PagamentoResponseDTO aprovarPagamento(String id) {
        Pagamento pagamento = buscarPagamentoPendente(id);

        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataAtualizacao(LocalDateTime.now());

        //Confirmando pagamento e alterando o Status para CONFIRMADO do lado de Pedidos
        pedidoClient.confirmarPagamento(pagamento.getPedidoId());

        repository.save(pagamento);
        return new PagamentoResponseDTO(pagamento);
    }

    public PagamentoResponseDTO recusarPagamento(String id) {
        Pagamento pagamento = buscarPagamentoPendente(id);

        pagamento.setStatus(StatusPagamento.RECUSADO);
        pagamento.setDataAtualizacao(LocalDateTime.now());

        repository.save(pagamento);
        return new PagamentoResponseDTO(pagamento);
    }

    public void cancelarPagamento(String id) {
        Pagamento pagamento = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        pagamento.setStatus(StatusPagamento.CANCELADO);
        pagamento.setDataAtualizacao(LocalDateTime.now());
        repository.save(pagamento);
    }

    private Pagamento buscarPagamentoPendente(String id) {
        Pagamento pagamento = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new InvalidOperationException("This operation is not valid! Changes can only be made when the status is PENDING.");
        }

        return pagamento;
    }
}
