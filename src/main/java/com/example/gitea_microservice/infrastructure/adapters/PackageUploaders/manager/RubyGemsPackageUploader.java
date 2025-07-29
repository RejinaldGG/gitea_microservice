package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.exception.InvalidPackageException;
import com.example.gitea_microservice.domain.exception.PackageErrorType;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;

import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class RubyGemsPackageUploader extends AbstractManagerPackageUploader {
    private final GiteaConfig giteaConfig;
    public RubyGemsPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig,VersionExtractionUseCase versionExtractor) {
            super(packageValidator);
            this.giteaConfig = giteaConfig;

    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.RUBYGEMS;
    }


@Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
    try {
        Path tempDir = createTempDir(manager);
        Path pkgFile = writePackageFile(tempDir, file);
        createConfig();
        runUpload(pkgFile);
        deleteTempDir(tempDir);
    } catch (IOException e) {
        throw new RuntimeException("Failed to publish "+ manager.getDisplayName() +" package", e);
    }
    }


private void createConfig() throws IOException {
    Path gemDir = Paths.get("/root/.gem");
    Path file = gemDir.resolve("credentials"); 

    if (!Files.exists(gemDir)) {
        Files.createDirectories(gemDir); 
    }
    String content = String.format(
        "%s/api/packages/%s/rubygems: Bearer %s",
        giteaConfig.getUrl(), giteaConfig.getOwner(), giteaConfig.getToken()
    );
    Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
}

    @Override
    protected void runUpload(Path pkgFile) throws IOException {        
        log.info("RubyGems output:\n{}", runCommand(pkgFile.getParent(), 
        List.of("gem", 
                "push", 
                "--host",
                String.format(
                    "%s/api/packages/%s/rubygems",
                    giteaConfig.getUrl(), giteaConfig.getOwner(), giteaConfig.getToken()),
                pkgFile.toString()
                )));

        
}
}