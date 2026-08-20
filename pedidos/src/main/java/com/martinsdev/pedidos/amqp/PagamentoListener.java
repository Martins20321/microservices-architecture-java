package com.martinsdev.pedidos.amqp;

import com.martinsdev.pedidos.dto.PedidoResponseDTO;
import com.martinsdev.pedidos.event.PagamentoConcluidoEvent;
import com.martinsdev.pedidos.event.PagamentoCriadoEvent;
import com.martinsdev.pedidos.event.PagamentoRecusadoEvent;
import com.martinsdev.pedidos.model.Pedido;
import com.martinsdev.pedidos.model.enums.StatusPedido;
import com.martinsdev.pedidos.service.PedidoService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
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

    @RabbitListener(queues = "pagamento.aprovado-pedido")
    public void receiveAprovado(@Payload PagamentoConcluidoEvent pagamentoConcluido,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(pagamentoConcluido.pedidoId());

        if (pedido.status().equals(StatusPedido.CONFIRMADO)){
            channel.basicAck(deliveryTag, false); //Confirma que leu a mensagem, mas não faz nada
            return;
        }

        pedidoService.confirmarPagamento(pagamentoConcluido.pedidoId());
        channel.basicAck(deliveryTag, false); //Confirma a mensagem e não trabalha com múlti acks

        //Consumindo a mensagem
        String message = "Pagamento aprovado para o pedido com id: " + pagamentoConcluido.pedidoId();
        System.out.println(message);
    }

    @RabbitListener(queues = "pagamento.recusado-pedido")
    public void receiveRecusado(@Payload PagamentoRecusadoEvent pagamentoRecusado) {
        pedidoService.recusarPagamento(pagamentoRecusado.pedidoId());

        String message = "Pagamento Recusado para o pedido com id: " + pagamentoRecusado.pedidoId();
        System.out.println(message);
    }

    @RabbitListener(queues = "pagamento.aguardado-pedido")
    public void receiveAguardado(@Payload PagamentoCriadoEvent pagamentoCriado) {
        pedidoService.aguardarPagamento(pagamentoCriado.pedidoId());

        String message = "Pagamento criado para o pedido com id: " + pagamentoCriado.pedidoId()
                + " e o status atualizado para confirmar pagamento";
        System.out.println(message);
    }
}
