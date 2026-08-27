package com.martinsdev.notificacoes.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificacoesAQMPConfiguration {

    @Bean
    public Queue pagamentoAprovadoNotificacaoQueue() {
        return QueueBuilder.durable("pagamento.aprovado-notificacao")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .build();
    }

    @Bean
    public Queue pagamentoRecusadoNotificacaoQueue() {
        return QueueBuilder.durable("pagamento.recusado-notificacao")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .build();
    }

    @Bean
    public DirectExchange directExchangePagamento() {
        return ExchangeBuilder.directExchange("pagamentos.ex").build();
    }

    @Bean
    public Binding bindingPagamentoAprovadoNotificacao(Queue pagamentoAprovadoNotificacaoQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(pagamentoAprovadoNotificacaoQueue).to(directExchangePagamento).with("pagamento.aprovado");
    }

    @Bean
    public Binding bindingPagamentoRecusadoNotificacao(Queue pagamentoRecusadoNotificacaoQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(pagamentoRecusadoNotificacaoQueue).to(directExchangePagamento).with("pagamento.recusado");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                                     JacksonJsonMessageConverter jacksonJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return factory;
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
