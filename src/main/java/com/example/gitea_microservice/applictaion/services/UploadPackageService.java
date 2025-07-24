package com.example.gitea_microservice.applictaion.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.UploadPackagePort;
import com.example.gitea_microservice.infrastructure.ports.UploadPackageUseCase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadPackageService implements UploadPackageUseCase{
    
    private final List<UploadPackagePort> uploaders;
    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
       uploaders.stream()
            .filter(e -> e.supports(manager))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No uploader defined for: " + manager))
            .uploadPackage(manager, file);
    }

}
