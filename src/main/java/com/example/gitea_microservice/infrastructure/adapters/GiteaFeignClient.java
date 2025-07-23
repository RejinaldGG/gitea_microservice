package com.example.gitea_microservice.infrastructure.adapters;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.infrastructure.config.GiteaClientConfig;

@FeignClient(
    name = "gitea-client",
    url = "${gitea.url}",
    configuration = GiteaClientConfig.class
)
public interface GiteaFeignClient {
    
    
    @PutMapping(
        value = "/api/packages/{owner}/{type}/dev/rep",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );

}
