package com.example.gitea_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GiteaMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiteaMicroserviceApplication.class, args);
	}

}
