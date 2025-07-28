package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import com.example.gitea_microservice.infrastructure.ports.VersionExtractionUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ContainerPackageUploader extends AbstractManagerPackageUploader {
    private final GiteaConfig giteaConfig;
    public ContainerPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig,VersionExtractionUseCase versionExtractor) {
            super(packageValidator);
            this.giteaConfig = giteaConfig;

    }
    @Override
    protected void unpack(Path packageFile, Path outputDir) throws IOException {
        try (InputStream fis = Files.newInputStream(packageFile);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(fis)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                Path outPath = outputDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(outputDir)) {
                    throw new IOException("Unsafe path: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream out = Files.newOutputStream(outPath)) {
                        IOUtils.copy(tarIn, out);
                    }
                }
            }
        }
    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager == PackageManager.CONTAINER;
    }
    private Path findFile(Path root, String name) throws IOException{
        try (Stream<Path> paths = Files.list(root)) {
            return paths
                .filter(p -> p.getFileName().toString().contains(name))
                .findFirst()
                .orElseThrow(() -> new IOException(name + " not found"));
        }
    }
    private String extractFullName(Path containerFile) throws IOException {
        try {
            String json = Files.readString(containerFile);
            log.info("JSON content: {}", json);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);

            JsonNode manifests = rootNode.path("manifests");
            if (manifests.isMissingNode() || !manifests.isArray() || manifests.size() == 0) {
                throw new IOException("Manifest array not found");
            }

            JsonNode firstManifest = manifests.get(0);
            JsonNode annotations = firstManifest.path("annotations");
            if (annotations.isMissingNode()) {
                throw new IOException("Annotations is empty");
            }

            JsonNode imageName = annotations.path("io.containerd.image.name");
            if (imageName.isMissingNode()) {
                throw new IOException("io.containerd.image.name is null");
            }

            return imageName.asText();
        } catch (JsonProcessingException e) {
            throw new IOException("Invalid JSON file: " + containerFile, e);
        }
    }

    private String extractShortName(String name) throws IOException{
        return List.of(name.split("/")).getLast();
    }

    @Override
    protected void runUpload(Path tempDir) throws IOException, InterruptedException {
        Path tarFile = findFile(tempDir, ".tar");
        Path metadata = tempDir.resolve("index.json");
        String name = extractFullName(metadata);
        String shortName = extractShortName(name);
        
        String registryUrl = "http://localhost:3000".replace("http://", ""); // Заменить localhost на реальный URL
        String imageName = registryUrl + "/" + giteaConfig.getOwner() + "/" + shortName;

        log.info("Container output:\n{}", 
            runCommand(tempDir, List.of(
                "docker", "login", 
                "-u", giteaConfig.getOwner(), 
                "-p", giteaConfig.getPassword(), 
                registryUrl
            ))
        );
        
        log.info("Container output:\n{}", 
            runCommand(tempDir, List.of("docker", "load", "-i", tarFile.toString())));
        
        log.info("Container output:\n{}", 
            runCommand(tempDir, List.of("docker", "tag", name, imageName)));
        
        log.info("Container output:\n{}", 
            runCommand(tempDir, List.of("docker", "push", imageName)));
    }
}