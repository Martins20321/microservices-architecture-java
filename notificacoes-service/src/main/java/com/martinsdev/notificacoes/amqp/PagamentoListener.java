package com.martinsdev.notificacoes.amqp;

import com.martinsdev.notificacoes.dto.PedidoDTO;
import com.martinsdev.notificacoes.event.PagamentoConcluidoEvent;
import com.martinsdev.notificacoes.event.PagamentoRecusadoEvent;
import com.martinsdev.notificacoes.infra.client.PedidoClient;
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
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PagamentoListener {

    private static final Logger log = LoggerFactory.getLogger(PagamentoListener.class);
    private final PedidoClient pedidoClient;

    @RabbitListener(queues = "pagamento.aprovado-notificacao", containerFactory = "simpleRabbitListenerContainerFactory")
    public void receivePagamentoAprovado(@Payload PagamentoConcluidoEvent pagamentoConcluidoEvent,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {

        // Se comunicando para obter as informações do pedido - Via rede
        PedidoDTO pedido = pedidoClient.buscarPedido(pagamentoConcluidoEvent.pedidoId());

        //iterando e já formatando os pedidos
        String itensFormatados = pedido.itens().stream().map(item -> " - "
                + item.descricao() + "\n"
                + " - Quantidade: " + item.quantidade() + "\n"
                + " - Valor: R$ " + item.valor())
                .collect(Collectors.joining("\n"));

        log.info("""
                Assunto: Pagamento confirmado - Pedido #{}
                
                        Prezado(a) cliente,
                
                        Informamos que o pagamento referente ao seu pedido #{}, realizado em {}, foi aprovado com sucesso.
                
                        Itens do pedido:
                        {}
                
                        Seu pedido está confirmado e seguirá para as próximas etapas de processamento.
                
                        Agradecemos a confiança em nossos serviços.
                
                        Atenciosamente,
                        Equipe de Atendimento
                """, pedido.id(), pedido.id(), pedido.dataCriacao(), itensFormatados);
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(queues = "pagamento.recusado-notificacao", containerFactory = "simpleRabbitListenerContainerFactory")
    public void receivePagamentoRecusado(@Payload PagamentoRecusadoEvent pagamentoRecusado,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) throws IOException {
        PedidoDTO pedido = pedidoClient.buscarPedido(pagamentoRecusado.pedidoId());

        String itensFormatados = pedido.itens().stream().map(item -> " - "
                + item.descricao() + "\n"
                + " - Quantidade: " + item.quantidade() + "\n"
                + " - Valor: R$ " + item.valor())
                .collect(Collectors.joining("\n"));

        log.info("""
                Assunto: Pagamento não aprovado - Pedido #{}
                
                Prezado(a) cliente,
                
                Informamos que não foi possível confirmar o pagamento referente ao seu pedido #{}, realizado em {}.
                
                Itens do pedido:
                {}
                
                
                O pedido foi cancelado em decorrência da recusa do pagamento. 
                Caso deseje refazer a compra, entre em contato com nossa equipe ou tente novamente através de nossos canais oficiais.
                
                Atenciosamente,
                Equipe de Atendimento
                """, pedido.id(), pedido.id(), pedido.dataCriacao(), itensFormatados);
        channel.basicAck(deliveryTag, false);
    }
}
