package com.martinsdev.pedidos.amqp;

import com.martinsdev.pedidos.dto.PedidoResponseDTO;
import com.martinsdev.pedidos.event.PagamentoConcluidoEvent;
import com.martinsdev.pedidos.event.PagamentoCriadoEvent;
import com.martinsdev.pedidos.event.PagamentoRecusadoEvent;
import com.martinsdev.pedidos.model.enums.StatusPedido;
import com.martinsdev.pedidos.service.PedidoService;
import com.rabbitmq.client.Channel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PagamentoListener {

    private final PedidoService pedidoService;
    private static final Logger log = LoggerFactory.getLogger(PagamentoListener.class);

    @RabbitListener(queues = "pagamento.aprovado-pedido")
    public void receiveAprovado(@Payload PagamentoConcluidoEvent pagamentoConcluido,
                                Message message,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {

        TextMapGetter<MessageProperties> getter = new TextMapGetter<>() {

            @Override
            public Iterable<String> keys(MessageProperties messageProperties) { // messageProperties - é o carrier
                Set<String> headers = messageProperties.getHeaders().keySet(); // getHeaders - devolve o map (chave, valor), keySet - devolve somente as chaves
                return headers;
            }

            @Override
            public String get(MessageProperties messageProperties, String key) {
                Object valor = messageProperties.getHeader(key);

                if (valor == null) {
                    return null;
                }
                return String.valueOf(valor);
            }
        };

        Context contextExtract = GlobalOpenTelemetry.get()
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), message.getMessageProperties(), getter);

        try (Scope scope = contextExtract.makeCurrent()) {

            //Acknowledge Manual
            PedidoResponseDTO pedido = pedidoService.buscarPorId(pagamentoConcluido.pedidoId());

            if (pedido.status().equals(StatusPedido.CONFIRMADO)) {
                log.info("Pagamento aprovado ignorado: pedido {} já está CONFIRMADO", pagamentoConcluido.pedidoId());
                channel.basicAck(deliveryTag, false); //Confirma que leu a mensagem, mas não faz nada
                return;
            }

            pedidoService.confirmarPagamento(pagamentoConcluido.pedidoId());
            channel.basicAck(deliveryTag, false); //Confirma a mensagem e não trabalha com múlti acks

            //Consumindo a mensagem
            log.info("Pagamento aprovado para o pedido com id: {}", pagamentoConcluido.pedidoId());
        }
    }

    @RabbitListener(queues = "pagamento.recusado-pedido")
    public void receiveRecusado(@Payload PagamentoRecusadoEvent pagamentoRecusado,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(pagamentoRecusado.pedidoId());

        if (pedido.status().equals(StatusPedido.CANCELADO)) {
            log.info("Pagamento recusado ignorado: pedido {} já está CANCELADO", pagamentoRecusado.pedidoId());
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

        if (pedido.status().equals(StatusPedido.AGUARDANDO_CONFIRMAR_PAGAMENTO)) {
            log.info("Pagamento criado ignorado: pedido {} já está AGUARDANDO_CONFIRMAR_PAGAMENTO", pagamentoCriado.pedidoId());
            channel.basicAck(deliveryTag, false);
            return;
        }

        pedidoService.aguardarPagamento(pagamentoCriado.pedidoId()); //Processa primeiro e confirma depois
        channel.basicAck(deliveryTag, false);

        log.info("Pagamento criado para o pedido com id: {} " +
                "e o status atualizado para confirmar pagamento", pagamentoCriado.pedidoId());
    }
}
