package com.example.gitea_microservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.auth.BasicAuthRequestInterceptor;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "gitea")
@Getter
@Setter
public class GiteaConfig {
    private String url;
    private String owner;
    private String token;
    @Bean
    public BasicAuthRequestInterceptor giteaAuthInterceptor() {
        return new BasicAuthRequestInterceptor("token", token);
    }
    }