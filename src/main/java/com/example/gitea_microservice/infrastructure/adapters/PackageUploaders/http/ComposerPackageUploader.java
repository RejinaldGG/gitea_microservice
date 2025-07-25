package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.http;


import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.GitPackage;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.adapters.GiteaClientAdapter;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;
@Component
public class ComposerPackageUploader extends AbstractHttpPackageUploader{
    protected final GiteaClientAdapter giteaClient;
    protected final PackageValidatorUseCase packageValidator;
    protected final VersionExtractionUseCase versionExtractor;
    
    public ComposerPackageUploader(GiteaClientAdapter giteaClient,
                               PackageValidatorUseCase packageValidator,
                               VersionExtractionUseCase versionExtractor) {
        super(packageValidator, versionExtractor);
        this.giteaClient = giteaClient;
        this.packageValidator = packageValidator;
        this.versionExtractor = versionExtractor;
    }

    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.COMPOSER;
    }
    @Override
    protected void doUpload(PackageManager manager, MultipartFile file, String version) {
        GitPackage pkg = buildGitPackage(manager, file, version);
        giteaClient.uploadComposerPackage(pkg);
    }
}
