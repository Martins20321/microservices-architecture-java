package com.martinsdev.inventario.redis;

import com.martinsdev.inventario.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservaExpirationListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(ReservaExpirationListener.class);
    private final ProdutoService service;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String messageBody = new String(message.getBody());
        String[] split = messageBody.split(":");
        Long produtoId = Long.valueOf(split[1]);
        Long pedidoId = Long.valueOf(split[2]);

        // delega a regra para o service
        service.expirarReserva(produtoId, pedidoId);

        log.info("Método de expirarReserva foi chamado");
    }
}
