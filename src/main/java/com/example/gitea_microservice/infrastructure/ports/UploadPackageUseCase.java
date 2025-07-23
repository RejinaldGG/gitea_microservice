package com.example.gitea_microservice.infrastructure.ports;

import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;

public interface UploadPackageUseCase {
    void uploadPackage(PackageManager manager, MultipartFile file);
}
