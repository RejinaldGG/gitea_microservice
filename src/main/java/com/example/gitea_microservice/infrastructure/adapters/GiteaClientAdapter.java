package com.example.gitea_microservice.infrastructure.adapters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.GitPackage;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.GitClientPort;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GiteaClientAdapter implements GitClientPort{
    
 private final GiteaFeignClient giteaFeignClient;
    private final GiteaConfig giteaConfig;
    
    private MultipartFile convertToMultipart(GitPackage pkg) {
    return new MultipartFile() {
        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() {
            return pkg.getName() + "-" + pkg.getVersion();
        }
        @Override public String getContentType() { return "application/octet-stream"; }
        @Override public boolean isEmpty() { return pkg.getContent() == null || pkg.getContent().length == 0; }
        @Override public long getSize() { return pkg.getContent().length; }
        @Override public byte[] getBytes() { return pkg.getContent(); }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(pkg.getContent()); }
        @Override public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream out = new FileOutputStream(dest)) {
                out.write(pkg.getContent());
            }
        }
    };
}
    @Override
    public void uploadAlpinePackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadAlpinePackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            pkg.getVersion(),
            pkg.getManager().name().toLowerCase(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }

    @Override
    public void uploadComposerPackage(GitPackage pkg) {
       MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadComposerPackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            pkg.getVersion(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }

    @Override
    public void uploadCargoPackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadAlpinePackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            pkg.getVersion(),
            pkg.getManager().name().toLowerCase(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }

    @Override
    public void uploadArchPackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadArchPackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }

    @Override
    public void uploadCondaPackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadCondaPackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            pkg.getName(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }
    @Override
    public void uploadCranPackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadCranPackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }
    @Override
    public void uploadDebianPackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadDebianPackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            pkg.getVersion(),
            pkg.getManager().name().toLowerCase(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }@Override
    public void uploadGoPackage(GitPackage pkg) {
        MultipartFile file = convertToMultipart(pkg);
        
        giteaFeignClient.uploadGoPackage(
            giteaConfig.getOwner(),
            pkg.getManager().name().toLowerCase(),
            "token " + giteaConfig.getToken(),
            file
            
        );
    }
   }
