package com.example.gitea_microservice.infrastructure.ports;

import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.models.PackageManager;

public interface PackageValidator {
    boolean supports(PackageManager manager);
    void validate(MultipartFile file) throws InvalidPackageException;
}
