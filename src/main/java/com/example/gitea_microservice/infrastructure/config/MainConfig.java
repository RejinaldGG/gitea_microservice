package com.example.gitea_microservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.gitea_microservice.infrastructure.adapters.GiteaClientAdapter;
import com.example.gitea_microservice.infrastructure.adapters.GiteaFeignClient;
import com.example.gitea_microservice.infrastructure.ports.GiteaClientPort;

// @Configuration
// public class MainConfig {
    
//     @Bean
//     public GiteaClientPort giteaClientPort(GiteaFeignClient feignClient, GiteaConfig config) {
//         return new GiteaClientAdapter(feignClient, config);
//     }
// }
