package com.example.gitea_microservice.infrastructure.adapters;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageValidator;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PackageValidatorAdapter implements PackageValidatorPort {

    
   private final List<PackageValidator> validators;

    @Override
    public void validate(PackageManager manager, MultipartFile file) throws InvalidPackageException {
        if (file.isEmpty()) {
            throw new InvalidPackageException(PackageErrorType.EMPTY_PACKAGE, "Empty package file");
        }
        validators.forEach(v -> System.out.println("Found validator: " + v.getClass().getSimpleName()));
        validators.stream()
            .filter(v -> v.supports(manager))
            .findFirst()
            .orElseThrow(() -> new InvalidPackageException(
                PackageErrorType.UNSUPPORTED_PACKAGE_MANAGER,
                "Unsupported package manager: " + manager
            ))
            .validate(file);
    }
}
