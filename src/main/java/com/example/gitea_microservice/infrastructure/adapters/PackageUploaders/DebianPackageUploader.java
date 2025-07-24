package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.GitPackage;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.adapters.GiteaClientAdapter;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

@Component
public class DebianPackageUploader extends AbstractPackageUploader{
    protected final GiteaClientAdapter giteaClient;
    protected final PackageValidatorUseCase packageValidator;
    protected final VersionExtractionUseCase versionExtractor;
    
    public DebianPackageUploader(GiteaClientAdapter giteaClient,
                               PackageValidatorUseCase packageValidator,
                               VersionExtractionUseCase versionExtractor) {
        super(packageValidator, versionExtractor);
        this.giteaClient = giteaClient;
        this.packageValidator = packageValidator;
        this.versionExtractor = versionExtractor;
    }

    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.DEBIAN;
    }
    @Override
    protected void doUpload(PackageManager manager, MultipartFile file, String version) {
        byte[] fileContent;
       try {
        fileContent = file.getBytes();
        String fileName = file.getOriginalFilename();
        GitPackage pkg = GitPackage.builder()
            .manager(manager)
            .name(fileName)
            .version(version)
            .content(fileContent)
            .build();
        giteaClient.uploadDebianPackage(pkg);
       } catch (IOException e) {
        e.printStackTrace();
       }   }

}
