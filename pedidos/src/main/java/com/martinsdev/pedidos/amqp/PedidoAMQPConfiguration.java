package com.martinsdev.pedidos.amqp;

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
public class PedidoAMQPConfiguration {

    @Bean
    public Queue pagamentoAprovadoPedidoQueue() {
        return QueueBuilder.durable("pagamento.aprovado-pedido")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .deadLetterExchange("pagamentos.dlx")
                .build();
    }

    @Bean
    public Queue pagamentoRecusadoPedidoQueue() {
        return QueueBuilder
                .durable("pagamento.recusado-pedido")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .deadLetterExchange("pagamentos.dlx")
                .build();
    }

    @Bean
    public Queue aguardandoPagamentoPedidoQueue() {
        return QueueBuilder
                .durable("pagamento.aguardado-pedido")
                .quorum()
                .withArgument("x-quorum-initial-group-size", 3)
                .deadLetterExchange("pagamentos.dlx")
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
    public Binding bidingPagamentoRecusado(Queue pagamentoRecusadoPedidoQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(pagamentoRecusadoPedidoQueue).to(directExchangePagamento).with("pagamento.recusado-pedido");
    }

    @Bean
    public Binding bidingAguardandoPagamento(Queue aguardandoPagamentoPedidoQueue, DirectExchange directExchangePagamento) {
        return BindingBuilder.bind(aguardandoPagamentoPedidoQueue).to(directExchangePagamento).with("pagamento.aguardado-pedido");
    }

    //DLQs
    @Bean
    public Queue pagamentoAprovadoPedidoDLQ() {
        return QueueBuilder
                .durable("pagamento-aprovado-pedido.dlq")
                .build();
    }

    @Bean
    public Queue pagamentoAguardadoPedidoDLQ() {
        return QueueBuilder
                .durable("pagamento-aguardado-pedido.dlq")
                .build();
    }

    @Bean
    public Queue pagamentoRecusadoPedidoDLQ() {
        return QueueBuilder
                .durable("pagamento-recusado-pedido.dlq")
                .build();
    }

    //DLX
    @Bean
    public DirectExchange dlxPagamento() {
        return ExchangeBuilder.directExchange("pagamentos.dlx").build();
    }

    //Bindings - DLX e DLQs
    @Bean
    public Binding bindingPagamentoAprovadoDLQ(Queue pagamentoAprovadoPedidoDLQ, DirectExchange dlxPagamento) {
        return BindingBuilder.bind(pagamentoAprovadoPedidoDLQ).to(dlxPagamento).with("pagamento.aprovado-pedido");
    }

    @Bean
    public Binding bindingPagamentoAguardadoDLQ(Queue pagamentoAguardadoPedidoDLQ, DirectExchange dlxPagamento) {
        return BindingBuilder.bind(pagamentoAguardadoPedidoDLQ).to(dlxPagamento).with("pagamento.aguardado-pedido");
    }

    @Bean
    public Binding bindingPagamentoRecusadoDLQ(Queue pagamentoRecusadoPedidoDLQ, DirectExchange dlxPagamento) {
        return BindingBuilder.bind(pagamentoRecusadoPedidoDLQ).to(dlxPagamento).with("pagamento.recusado-pedido");
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    //Injetando as configurações do retry manualmente
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               JacksonJsonMessageConverter jacksonJsonMessageConverter,
                                                                               SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        //concurrent consumers
        factory.setConcurrentConsumers(1); //Minimo de consumers ativos
        factory.setMaxConcurrentConsumers(3); //escala dinamicamente
        return factory;
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
