package com.martinsdev.gateway.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class Configurations {

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
