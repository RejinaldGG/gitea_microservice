package com.example.gitea_microservice.infrastructure.adapters;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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
        value = "/api/packages/{owner}/{type}/{branch}/{repository}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadAlpinePackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @PathVariable("branch") String branch,
        @PathVariable("repository") String repository,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );

        @PutMapping(
        value = "/api/packages/{owner}/{type}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadArchPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );

    @PutMapping(
        value = "/api/packages/{owner}/{type}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadComposerPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @RequestParam("version") String version,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );

     @PutMapping(
        value = "/api/packages/{owner}/{type}/{filename}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadCondaPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @PathVariable("filename") String filename,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );

    @PutMapping(
        value = "/api/packages/{owner}/cargo/api/v1/crates/new",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    String uploadPackageCargo(
        @RequestHeader("Authorization") String token,
        @RequestPart("metadata") String metadata,
        @RequestPart("package") MultipartFile file,
        @PathVariable("owner") String owner
    );

     @PutMapping(
        value = "/api/packages/{owner}/{type}/src",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadCranPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );
    @PutMapping(
        value = "/api/packages/{owner}/{type}/pool/{distribution}/{component}/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadDebianPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @PathVariable("distribution") String distribution,
        @PathVariable("component") String component,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );
    @PutMapping(
        value = "/api/packages/{owner}/{type}/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadGoPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );
     @PutMapping(
        value = "/api/packages/{owner}/{type}/{group}/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadRpmPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @PathVariable("group") String group,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );
    @PutMapping(
        value = "/api/packages/{owner}/{type}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<Void> uploadRubyGemsPackage(
        @PathVariable("owner") String owner,
        @PathVariable("type") String packageType,
        @PathVariable("group") String group,
        @RequestHeader("Authorization") String token,
        @RequestPart("data") MultipartFile file
    );
}
