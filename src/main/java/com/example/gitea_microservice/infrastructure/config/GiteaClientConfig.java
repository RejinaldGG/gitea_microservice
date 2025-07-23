package com.example.gitea_microservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.auth.BasicAuthRequestInterceptor;

@Configuration
public class GiteaClientConfig {
    
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
        @Value("${gitea.token}") String token
    ) {
        return new BasicAuthRequestInterceptor("token", token);
    }
}
