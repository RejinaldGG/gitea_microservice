package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders;

import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.UploadPackagePort;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;



public abstract class AbstractPackageUploader implements UploadPackagePort {
    private final VersionExtractionUseCase versionExtractor;
    private final PackageValidatorUseCase packageValidator;
    public AbstractPackageUploader(
                               PackageValidatorUseCase packageValidator,
                               VersionExtractionUseCase versionExtractor) {
        this.packageValidator = packageValidator;
        this.versionExtractor = versionExtractor;
    }

    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        packageValidator.validate(manager, file);
        String version = versionExtractor.extractVersion(manager, file.getOriginalFilename());
        doUpload(manager, file, version);
           }

    protected abstract void doUpload(PackageManager manager, MultipartFile file, String version);
}