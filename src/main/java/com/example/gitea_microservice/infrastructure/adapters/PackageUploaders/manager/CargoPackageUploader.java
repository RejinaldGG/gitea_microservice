package com.example.gitea_microservice.infrastructure.adapters.PackageUploaders.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.gitea_microservice.domain.models.PackageManager;
import com.example.gitea_microservice.domain.models.PublishType;
import com.example.gitea_microservice.infrastructure.config.GiteaConfig;
import com.example.gitea_microservice.infrastructure.ports.PackageValidatorUseCase;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class CargoPackageUploader extends AbstractManagerPackageUploader {
    private final PackageValidatorUseCase packageValidator;
    private final GiteaConfig giteaConfig;
    public CargoPackageUploader(
        PackageValidatorUseCase packageValidator,GiteaConfig giteaConfig) {
            super(packageValidator);
            this.packageValidator = packageValidator;
            this.giteaConfig = giteaConfig;

    }
    @Override
    public boolean supports(PackageManager manager) {
        return manager.getPublishType() == PublishType.MANAGER && manager == PackageManager.CARGO;
    }

    private Path findCargoProjectDir(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root, 2)) {
            return paths
                .filter(p -> p.getFileName().toString().equals("Cargo.toml"))
                .findFirst()
                .orElseThrow(() -> new IOException("Cargo.toml not found"))
                .getParent();
        }
    }
    @Override
    protected void unpack(Path crateFile, Path outputDir) throws IOException {
        try (InputStream fis = Files.newInputStream(crateFile);
            GZIPInputStream gzipIn = new GZIPInputStream(fis);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

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
    protected void runUpload(Path tempDir) throws IOException, InterruptedException {
        Path packageDir = findCargoProjectDir(tempDir);
        Path cargoTomlOrig = packageDir.resolve("Cargo.toml.orig");
        Files.deleteIfExists(cargoTomlOrig);
        ProcessBuilder pb = new ProcessBuilder()
            .command("cargo", "publish", "--no-verify", "--allow-dirty")
            .directory(packageDir.toFile())
            .redirectErrorStream(true);
        Path cargoToml = packageDir.resolve("Cargo.toml");
        
        List<String> lines = Files.readAllLines(cargoToml);
        boolean hasPublish = lines.stream().anyMatch(line -> line.trim().startsWith("publish"));
        if (!hasPublish) {
            List<String> newLines = new ArrayList<>();
            for (String line : lines) {
                newLines.add(line);
                if (line.trim().startsWith("[package]")) {
                    newLines.add("publish = [\"gitea\"]"); 
                }
            }
            Files.write(cargoToml, newLines);
        }

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        log.info("cargo publish output:\n{}", output);

        if (exitCode != 0) {
            throw new RuntimeException("cargo publish failed with exit code " + exitCode);
        }
    }


    
}
