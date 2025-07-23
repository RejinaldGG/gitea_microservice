package com.example.gitea_microservice.infrastructure.adapters.PackageValidators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageValidator;

@Component
public class ArchPackageValidator implements PackageValidator {
 @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.ARCH;
    }

    @Override
    public void validate(MultipartFile file) throws InvalidPackageException {
        if (!file.getOriginalFilename().endsWith(".pkg.tar.zst")) {
            throw new InvalidPackageException(
                PackageErrorType.INVALID_FORMAT,
                "Arch-package must be .pkg.tar.zst"
            ).withDetail("expected_extension", ".pkg.tar.zst")
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
