package com.martinsdev.pagamentos.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PagamentoAMQPConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PagamentoAMQPConfiguration.class);

    @Bean
    public DirectExchange directExchangePagamento() {
        return ExchangeBuilder.directExchange("pagamentos.ex").build();
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter jacksonJsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonJsonMessageConverter);

        //Publish Confirms
        //Verifica se a mensagem chegou com sucesso na Exchange
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("[CONFIRM] Mensagem entregue com sucesso à Exchange! ID {}", correlationData.getId());
            } else {
                log.warn("[CONFIRM] Falha ao entregar a mensagem à Exchange! ID {} - Motivo: {}", correlationData.getId(), cause);
            }
            });

        //Informa explicitamente para avisar uma falha de roteamento
        rabbitTemplate.setMandatory(true);
        //Dispara quando uma mensagem chega na exchange e não consegue ser roteada para uma queue
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("A mensagem da Exchange({}) não conseguiu ser roteada para uma Queue! " +
                            "Erro informado: {} - Routing Key utilizada: {}",
                    returned.getExchange(), returned.getReplyText(), returned.getRoutingKey());
        });
            return rabbitTemplate;
        }

        @Bean
        public RabbitAdmin rabbitAdmin (ConnectionFactory connectionFactory){
            return new RabbitAdmin(connectionFactory);
        }

        @Bean
        public ApplicationListener<ApplicationReadyEvent> applicationListener (RabbitAdmin rabbitAdmin){
            return event -> rabbitAdmin.initialize();
        }
    }
