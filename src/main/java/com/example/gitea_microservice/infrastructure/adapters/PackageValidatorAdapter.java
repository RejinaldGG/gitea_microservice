package com.example.gitea_microservice.infrastructure.adapters;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PackageValidatorAdapter implements PackageValidatorPort {

    @Override
    public void validate(PackageManager manager, MultipartFile file) throws InvalidPackageException {
        if (file.isEmpty()) {
            throw new RuntimeException("Empty package file");
        }

        switch (manager) {
            case ALPINE:
                validateAlpine(file);
                break;
            case ARCH:
                validateArch(file);
                break;
            default: 
            break;
        }
    }


    
    private void validateArch(MultipartFile file) {
            if (!file.getOriginalFilename().endsWith(".pkg.tar.zst")) {
        throw new InvalidPackageException(
            PackageErrorType.INVALID_FORMAT,
            "Arch-package must be .pkg.tar.zst"
        ).withDetail("expected_extension", ".pkg.tar.zst")
         .withDetail("actual_extension", getFileExtension(file));
    }
}
    private void validateAlpine(MultipartFile file) throws InvalidPackageException {
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
