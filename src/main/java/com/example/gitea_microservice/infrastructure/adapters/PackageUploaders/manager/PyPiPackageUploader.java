package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PyPiPackageUploader extends AbstractManagerPackageUploader {
    private final GiteaConfig giteaConfig;
    public PyPiPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig,VersionExtractionUseCase versionExtractor) {
            super(packageValidator);
            this.giteaConfig = giteaConfig;

    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.PYPI;
    }

    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        super.validate(manager, file);
    try {
        Path tempDir = createTempDir(manager);
        Path pkgFile = writePackageFile(tempDir, file);
        createConfig();
        runUpload(tempDir, pkgFile, file);
        deleteTempDir(tempDir);
    } catch (IOException e) {
        throw new RuntimeException("Failed to publish "+ manager.getDisplayName() +" package", e);
    }
    }

    private void createConfig() throws IOException
    {   
        Path file = Paths.get("/root/.pypirc");
        if (!Files.exists(file)) {
        String content = String.format(
            "[distutils]\nindex-servers = gitea\n\n[gitea]\n" +
            "repository = %s/api/packages/%s/pypi\n" +
            "username = %s\npassword = %s",
            giteaConfig.getUrl(), giteaConfig.getOwner(), giteaConfig.getOwner(), giteaConfig.getToken()
        );
 
        Files.writeString(file, content);
        }
    }

    private void runUpload(Path tempDir, Path pkgFile, MultipartFile file) throws IOException
    {   

        log.info("PyPi output:\n{}", runCommand(tempDir, List.of("python3", "-m", "twine", "upload", "--repository", "gitea", pkgFile.toString())));
    }
    @Override
    protected void runUpload(Path packageDir) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'runUpload'");
    }
    
}
