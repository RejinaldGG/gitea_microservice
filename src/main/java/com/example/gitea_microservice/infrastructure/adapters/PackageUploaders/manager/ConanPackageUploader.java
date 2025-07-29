package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
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
public class ConanPackageUploader extends AbstractManagerPackageUploader {
    private final GiteaConfig giteaConfig;
    private final VersionExtractionUseCase versionExtractor;
    private String pkgName;
    public ConanPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig,VersionExtractionUseCase versionExtractor) {
            super(packageValidator);
            this.giteaConfig = giteaConfig;
            this.versionExtractor = versionExtractor;

    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.CONAN;
    }


    @Override
    public void uploadPackage(PackageManager manager, MultipartFile file) {
        validate(manager, file);
    try {

        Path tempDir = createTempDir(manager);
        Path pkgFile = writePackageFile(tempDir, file);
        pkgName = file.getOriginalFilename();
        unpack(pkgFile, tempDir);
        runUpload(tempDir);
        deleteTempDir(tempDir);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException("Failed to publish "+ manager.getDisplayName() +" package", e);
    }
    }


    private Path findProjectDir(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root, 2)) {
            return paths
                .filter(p -> p.getFileName().toString().equals("conanfile.py"))
                .findFirst()
                .orElseThrow(() -> new IOException("conanfile.py not found"))
                .getParent();
        }
    }
    
    private String extractConanAttribute(Path conanfile, String attribute) throws IOException {
        return Files.readAllLines(conanfile).stream()
            .map(String::trim)
            .filter(line -> line.startsWith(attribute))
            .map(line -> line.split("="))
            .filter(parts -> parts.length == 2)
            .map(parts -> parts[1].trim().replaceAll("['\"]", ""))
            .findFirst()
            .orElseThrow(() -> new InvalidPackageException(PackageErrorType.INVALID_FORMAT, HttpStatus.UNPROCESSABLE_ENTITY, "Could not find '" + attribute + "' in conanfile.py"));
    }

    private String extractName(Path conanfile) throws IOException {
        return extractConanAttribute(conanfile, "name");
    }

    @Override
    protected void runUpload(Path tempDir) throws IOException, InterruptedException {
        Path conanDir = findProjectDir(tempDir);
        Path metadata = conanDir.resolve("conanfile.py");
        String name = extractName(metadata);
        String version = versionExtractor.extractVersion(PackageManager.CONAN, pkgName);
        
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "remote", "add", "--force", giteaConfig.getOwner(), giteaConfig.getUrl()+"/api/packages/"+giteaConfig.getOwner()+"/conan")));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "remote", "login", giteaConfig.getOwner(), giteaConfig.getOwner(), "-p", giteaConfig.getPassword())));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "profile", "detect", "--force")));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "create", ".", "--name=" + name, "--version=" + version)));
        log.info("Conan output:\n{}", runCommand(conanDir, List.of("conan", "upload", name+"/"+version,"--remote="+giteaConfig.getOwner())));
        
}
}