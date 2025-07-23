package com.example.gitea_microservice.infrastructure.adapters.PackageValidators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageValidator;

@Component
public class AlpinePackageValidator implements PackageValidator {

    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.ALPINE;
    }

    @Override
    public void validate(MultipartFile file) throws InvalidPackageException {
        if (!file.getOriginalFilename().endsWith(".apk")) {
            throw new InvalidPackageException(
                PackageErrorType.INVALID_FORMAT,
                "Alpine-package must be .apk"
            ).withDetail("expected_extension", ".apk")
             .withDetail("actual_extension", getFileExtension(file));
        }
    }

    private String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}

