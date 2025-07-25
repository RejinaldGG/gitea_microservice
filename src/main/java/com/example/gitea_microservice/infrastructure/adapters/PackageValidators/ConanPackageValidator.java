package com.example.gitea_microservice.infrastructure.adapters.PackageValidators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.domain.models.PublishType;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;
@Component
public class ConanPackageValidator implements PackageValidatorPort{
 
    private final PackageManager supportedManager = PackageManager.CONAN;
    @Override
    public boolean supports(PackageManager manager) {
        return manager == supportedManager;
    }

    @Override
    public PublishType validate(MultipartFile file) throws InvalidPackageException {
        if (!file.getOriginalFilename().endsWith(".tgz")) {
            throw new InvalidPackageException(
                PackageErrorType.INVALID_FORMAT,
                "Conan-package must be .tgz"
            ).withDetail("expected_extension", ".tgz")
             .withDetail("actual_extension", getFileExtension(file));
        }
        return supportedManager.getPublishType();
    }

    private String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
