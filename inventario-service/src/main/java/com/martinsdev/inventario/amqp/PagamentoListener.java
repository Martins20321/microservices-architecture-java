package com.martinsdev.inventario.amqp;

import com.martinsdev.inventario.dto.ConfirmarReservaProdutoDTO;
import com.martinsdev.inventario.event.PagamentoConcluidoEvent;
import com.martinsdev.inventario.model.MovimentacaoEstoque;
import com.martinsdev.inventario.model.enums.TipoMovimentacao;
import com.martinsdev.inventario.repository.MovimentacaoEstoqueRepository;
import com.martinsdev.inventario.service.ProdutoService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoListener {

    private final ProdutoService service;
    private final MovimentacaoEstoqueRepository estoqueRepository;

    @RabbitListener(queues = "pagamento.aprovado-inventario", containerFactory = "rabbitListenerContainerFactory")
    public void receiveAprovado(@Payload PagamentoConcluidoEvent pagamentoConcluido,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {
        // buscando todos as reservas de um pedido
        List<MovimentacaoEstoque> reservasDoPedido = estoqueRepository.findByPedidoIdAndTipoMovimentacao(pagamentoConcluido.pedidoId(), TipoMovimentacao.RESERVA);

        for (MovimentacaoEstoque reserva : reservasDoPedido) {
            // garantindo idempotencia - verifica se aquela reserva ja foi confirmada no banco
            if (estoqueRepository.findByProdutoIdAndPedidoIdAndTipoMovimentacao(reserva.getProdutoId(), reserva.getPedidoId(), TipoMovimentacao.CONFIRMACAO).isPresent()) {
                continue;
            }

            service.confirmarReservaProduto(reserva.getProdutoId(), new ConfirmarReservaProdutoDTO(reserva.getPedidoId()));
        }
        channel.basicAck(deliveryTag, false);
    }
}
