package com.martinsdev.inventario.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventarioAMQPConfiguration {

    @Bean // referencia para a exchange de pagamentos
    public DirectExchange directExchangePagamento() {
        return ExchangeBuilder.directExchange("pagamentos.ex").build();
    }

    @Bean
    public Queue pagamentoAprovadoInventarioQueue() {
        return QueueBuilder.durable("pagamento.aprovado-inventario")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .build();
    }

    @Bean
    public Queue pagamentoRecusadoInventarioQueue() {
        return QueueBuilder.durable("pagamento.recusado-inventario")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .build();
    }

    // Bindings - Queues e Exchange
    @Bean
    public Binding bindingPagamentoAprovado(Queue pagamentoAprovadoInventarioQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(pagamentoAprovadoInventarioQueue).to(directExchangePagamento).with("pagamento.aprovado");
    }

    @Bean
    public Binding bindingRecusadoAprovado(Queue pagamentoRecusadoInventarioQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(pagamentoRecusadoInventarioQueue).to(directExchangePagamento).with("pagamento.recusado");
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               JacksonJsonMessageConverter jacksonJsonMessageConverter,
                                                                               SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // concurrent consumers
        factory.setConcurrentConsumers(1); //Minimo de consumers ativos
        factory.setMaxConcurrentConsumers(3); //escala dinamicamente com o maximo de 3
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> initializeRabbitAdmin(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }
}
