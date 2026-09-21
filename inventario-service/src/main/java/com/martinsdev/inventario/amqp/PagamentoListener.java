package com.martinsdev.inventario.amqp;

import com.martinsdev.inventario.dto.CancelarReservaProdutoDTO;
import com.martinsdev.inventario.dto.ConfirmarReservaProdutoDTO;
import com.martinsdev.inventario.event.PagamentoConcluidoEvent;
import com.martinsdev.inventario.event.PagamentoRecusadoEvent;
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
        //executado depois que todas as mensagens forem confirmadas
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(queues = "pagamento.recusado-inventario", containerFactory = "rabbitListenerContainerFactory")
    public void receiveRecusado(@Payload PagamentoRecusadoEvent pagamentoRecusado,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {
        // buscando todos as reservas de um pedido
        List<MovimentacaoEstoque> reservasDoPedido = estoqueRepository.findByPedidoIdAndTipoMovimentacao(pagamentoRecusado.pedidoId(), TipoMovimentacao.RESERVA);

        for (MovimentacaoEstoque reserva : reservasDoPedido) {
            // idempotencia - verifica se ja existe um cancelamento de reserva para aquele produto para aquele pedido
            if (estoqueRepository.findByProdutoIdAndPedidoIdAndTipoMovimentacao(reserva.getProdutoId(), reserva.getPedidoId(), TipoMovimentacao.CANCELAMENTO_RESERVA).isPresent()) {
                continue;
            }

            // para cada reserva chama o service para cancelar
            service.cancelarReserva(reserva.getProdutoId(), new CancelarReservaProdutoDTO(reserva.getPedidoId()));
        }
        // confirma ao RabbitMQ uma vez, depois que todas as reservas do pedido já foram tratadas
        channel.basicAck(deliveryTag, false);
    }
}
