package com.example.gitea_microservice.applictaion.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.domain.models.PublishType;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PackageValidationService implements PackageValidatorUseCase {
    private final List<PackageValidatorPort> validators;
    
    @Override
    public PublishType validate(PackageManager manager, MultipartFile file) throws InvalidPackageException {
        if (file.isEmpty()) {
            throw new InvalidPackageException(PackageErrorType.EMPTY_PACKAGE, "Empty package file");
        }
        return validators.stream()
            .filter(v -> v.supports(manager))
            .findFirst()
            .orElseThrow(() -> new InvalidPackageException(
                PackageErrorType.UNSUPPORTED_PACKAGE_MANAGER,
                "Unsupported package manager: " + manager
            ))
            .validate(file);
        
    }
}
