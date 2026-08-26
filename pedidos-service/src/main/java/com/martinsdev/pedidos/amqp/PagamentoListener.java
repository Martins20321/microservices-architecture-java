package com.martinsdev.pedidos.amqp;

import com.martinsdev.pedidos.dto.PedidoResponseDTO;
import com.martinsdev.pedidos.event.PagamentoConcluidoEvent;
import com.martinsdev.pedidos.event.PagamentoCriadoEvent;
import com.martinsdev.pedidos.event.PagamentoRecusadoEvent;
import com.martinsdev.pedidos.model.enums.StatusPedido;
import com.martinsdev.pedidos.service.PedidoService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PagamentoListener {

    private final PedidoService pedidoService;
    private static final Logger log = LoggerFactory.getLogger(PagamentoListener.class);

    @RabbitListener(queues = "pagamento.aprovado-pedido")
    public void receiveAprovado(@Payload PagamentoConcluidoEvent pagamentoConcluido,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {

        //Acknowledge Manual
        PedidoResponseDTO pedido = pedidoService.buscarPorId(pagamentoConcluido.pedidoId());

        if (pedido.status().equals(StatusPedido.CONFIRMADO)){
            channel.basicAck(deliveryTag, false); //Confirma que leu a mensagem, mas não faz nada
            return;
        }

        pedidoService.confirmarPagamento(pagamentoConcluido.pedidoId());
        channel.basicAck(deliveryTag, false); //Confirma a mensagem e não trabalha com múlti acks

        //Consumindo a mensagem
        log.info("Pagamento aprovado para o pedido com id: {}", pagamentoConcluido.pedidoId());
    }

    @RabbitListener(queues = "pagamento.recusado-pedido")
    public void receiveRecusado(@Payload PagamentoRecusadoEvent pagamentoRecusado,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(pagamentoRecusado.pedidoId());

        if (pedido.status().equals(StatusPedido.CANCELADO)){
            channel.basicAck(deliveryTag, false);
            return;
        }

        pedidoService.recusarPagamento(pagamentoRecusado.pedidoId());
        channel.basicAck(deliveryTag, false);

        log.info("Pagamento Recusado para o pedido com id: {}", pagamentoRecusado.pedidoId());
    }

    @RabbitListener(queues = "pagamento.aguardado-pedido")
    public void receiveAguardado(@Payload PagamentoCriadoEvent pagamentoCriado,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(pagamentoCriado.pedidoId());

        if (pedido.status().equals(StatusPedido.AGUARDANDO_CONFIRMAR_PAGAMENTO)){
            channel.basicAck(deliveryTag, false);
            return;
        }

        pedidoService.aguardarPagamento(pagamentoCriado.pedidoId()); //Processa primeiro e confirma depois
        channel.basicAck(deliveryTag, false);

        log.info("Pagamento criado para o pedido com id: {} " +
                "e o status atualizado para confirmar pagamento", pagamentoCriado.pedidoId());
    }
}
