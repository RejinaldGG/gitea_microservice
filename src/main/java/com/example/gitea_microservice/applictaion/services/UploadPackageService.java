package com.example.gitea_microservice.applictaion.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.GitPackage;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.GiteaClientPort;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;
import com.example.gitea_microservice.infrastructure.ports.UploadPackageUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadPackageService implements UploadPackageUseCase{
    
    private final GiteaClientPort giteaClient;
    private final PackageValidatorPort packageValidator;
    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        packageValidator.validate(manager, file);
        String version = extractVersionFromFile(file);
        try {
            GitPackage pkg = GitPackage.builder()
                .manager(manager)
                .name(file.getName())
                .version(version)
                .content(file.getBytes())
                .build();
            giteaClient.uploadPackage(pkg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   public String extractVersionFromFile(MultipartFile file) {
            String filename = file.getOriginalFilename();
        if (filename != null) {
            Pattern pattern = Pattern.compile("-(\\d+(?:\\.\\d+)*-r\\d+)\\.apk$");
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                String version = matcher.group(1); 
                return version;
            }
        }
        throw new RuntimeException("Could not extract version from filename: " + filename);

}

}
