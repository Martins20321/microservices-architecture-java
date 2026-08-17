package com.martinsdev.pedidos.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;

@Configuration
public class PedidoAMQPConfiguration {

    @Bean
    public Queue pagamentoAprovadoPedidoQueue() {
        return QueueBuilder.durable("pagamento.aprovado-pedido")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .build();
    }

    @Bean
    public DirectExchange directExchangePagamento() {
        return ExchangeBuilder.directExchange("pagamentos.ex").build();
    }

    @Bean
    public Binding bindingPagamentoAprovado(Queue pagamentoAprovadoPedidoQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(pagamentoAprovadoPedidoQueue).to(directExchangePagamento).with("pagamento.aprovado-pedido");
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationListener(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }
}
