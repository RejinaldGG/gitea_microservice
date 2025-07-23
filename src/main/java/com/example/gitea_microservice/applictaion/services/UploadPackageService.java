package com.example.gitea_microservice.applictaion.services;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.GitPackage;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.GiteaClientPort;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;
import com.example.gitea_microservice.infrastructure.ports.UploadPackageUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadPackageService implements UploadPackageUseCase{
    
    private final GiteaClientPort giteaClient;
    private final PackageValidatorPort packageValidator;
    private final VersionExtractionUseCase versionExtractor;
    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        try {
            packageValidator.validate(manager, file);
            byte[] fileContent = file.getBytes();
            String fileName = file.getOriginalFilename();
            String version = versionExtractor.extractVersion(manager, fileName);
            
                GitPackage pkg = GitPackage.builder()
                    .manager(manager)
                    .name(fileName)
                    .version(version)
                    .content(fileContent)
                    .build();
                giteaClient.uploadPackage(pkg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   public String extractVersionFromFile(MultipartFile file) {
            String filename = file.getOriginalFilename();
        if (filename != null) {
            Pattern pattern = Pattern.compile("-((\\d+(?:\\.\\d+)*)(?:-r\\d+|-\\d+))(?:-[\\w]+)?\\.(apk|pkg\\.tar\\.zst)$");

            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                String version = matcher.group(1); 
                return version;
            }
        }
        throw new InvalidPackageException(PackageErrorType.INVALID_FORMAT, "Could not extract version from filename: " + filename);

}

}
