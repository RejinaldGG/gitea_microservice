package com.example.gitea_microservice.infrastructure.adapters.PackageValidators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.domain.models.PublishType;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;
@Component
public class CondaPackageValidator implements PackageValidatorPort{
private final PackageManager supportedManager = PackageManager.CONDA;
    @Override
    public boolean supports(PackageManager manager) {
        return manager == supportedManager;
    }

    @Override
    public PublishType validate(MultipartFile file) throws InvalidPackageException {
        if (!file.getOriginalFilename().endsWith(".conda") && !file.getOriginalFilename().endsWith("tar.bz2")) {
            throw new InvalidPackageException(
                PackageErrorType.INVALID_FORMAT,
                "Conda-package must be .conda or .tar.bz2"
            ).withDetail("expected_extension", ".conda or .tar.bz2")
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
