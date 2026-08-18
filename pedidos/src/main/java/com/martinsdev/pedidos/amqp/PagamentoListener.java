package com.martinsdev.pedidos.amqp;

import com.martinsdev.pedidos.event.PagamentoConcluidoEvent;
import com.martinsdev.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagamentoListener {

    private final PedidoService pedidoService;

    @RabbitListener(queues = "pagamento.aprovado-pedido")
    public void receive(@Payload PagamentoConcluidoEvent pagamentoConcluido) {
        pedidoService.confirmarPagamento(pagamentoConcluido.pedidoId());

        //Consumindo a mensagem
        String message = "Pagamento aprovado para o pedido com id: " + pagamentoConcluido.pedidoId();
        System.out.println(message);
    }
}
